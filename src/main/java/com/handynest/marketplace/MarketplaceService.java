package com.handynest.marketplace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.catalog.category.Category;
import com.handynest.catalog.category.CategoryAccessPolicy;
import com.handynest.catalog.category.CategoryQueryRepository;
import com.handynest.catalog.category.CategoryServiceMode;
import com.handynest.common.api.PageResponse;
import com.handynest.common.audit.AuditLogService;
import com.handynest.common.error.AccessDeniedBusinessException;
import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.DuplicateResourceException;
import com.handynest.common.error.FeatureDisabledException;
import com.handynest.common.error.InvalidStatusTransitionException;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.common.idempotency.IdempotencyDecision;
import com.handynest.common.idempotency.IdempotencyKey;
import com.handynest.common.idempotency.IdempotencyService;
import com.handynest.common.publicid.PublicIdGenerator;
import com.handynest.files.FileStorageProperties;
import com.handynest.files.FileStorageService;
import com.handynest.files.PresignedStorageUrl;
import com.handynest.files.StoredObjectMetadata;
import com.handynest.geo.City;
import com.handynest.geo.CityRepository;
import com.handynest.geo.District;
import com.handynest.geo.DistrictRepository;
import com.handynest.identity.ConsentType;
import com.handynest.identity.CustomerProfile;
import com.handynest.identity.CustomerProfileRepository;
import com.handynest.identity.RoleName;
import com.handynest.identity.User;
import com.handynest.identity.UserConsentService;
import com.handynest.identity.UserProfileService;
import com.handynest.identity.UserRepository;
import com.handynest.market.MarketConfigService;
import com.handynest.notification.DomainEventPublisher;
import com.handynest.notification.DomainEventType;
import com.handynest.notification.NotificationService;
import com.handynest.notification.NotificationType;
import com.handynest.performer.PerformerCategory;
import com.handynest.performer.PerformerProfile;
import com.handynest.performer.PerformerProfileRepository;
import com.handynest.platform.PlatformSettingKey;
import com.handynest.platform.PlatformSettingService;
import com.handynest.risk.CustomerRiskPolicy;
import com.handynest.risk.RiskEventService;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MarketplaceService {

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;
  private static final long MAX_CHAT_ATTACHMENT_SIZE_BYTES = 26_214_400L;
  private static final long MAX_TASK_IMAGE_SIZE_BYTES = 10_485_760L;
  private static final Set<String> TASK_IMAGE_CONTENT_TYPES =
      Set.of("image/jpeg", "image/png", "image/webp");
  private static final Set<String> BLOCKED_ATTACHMENT_EXTENSIONS =
      Set.of("bat", "cmd", "com", "dll", "exe", "js", "jar", "msi", "ps1", "scr", "sh");
  private static final Set<String> BLOCKED_CONTENT_TYPES =
      Set.of(
          "application/x-msdownload",
          "application/x-sh",
          "application/x-bat",
          "application/x-msdos-program");
  private static final Pattern SAFE_FILENAME_CHARS = Pattern.compile("[^A-Za-z0-9._-]");
  private static final String RESOURCE_TASK = "MARKETPLACE_TASK";
  private static final String RESOURCE_OFFER = "TASK_OFFER";
  private static final String RESOURCE_DEAL = "DEAL";
  private static final String RESOURCE_MILESTONE = "MILESTONE";
  private static final String RESOURCE_DISPUTE = "DISPUTE_CASE";
  private static final String RESOURCE_FEEDBACK = "MARKETPLACE_FEEDBACK";
  private static final String RESOURCE_ATTACHMENT = "MARKETPLACE_ATTACHMENT";
  private static final String RESOURCE_CONTACT_REVEAL = "CONTACT_REVEAL";

  private final MarketplaceTaskRepository taskRepository;
  private final TaskOfferRepository offerRepository;
  private final DealRepository dealRepository;
  private final MilestoneRepository milestoneRepository;
  private final DealChatRepository chatRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final DisputeCaseRepository disputeCaseRepository;
  private final MarketplaceFeedbackRepository feedbackRepository;
  private final MarketplaceAttachmentRepository attachmentRepository;
  private final FavoritePerformerRepository favoritePerformerRepository;
  private final FileStorageService fileStorageService;
  private final FileStorageProperties fileStorageProperties;
  private final RiskEventService riskEventService;
  private final NotificationService notificationService;
  private final IdempotencyService idempotencyService;
  private final UserProfileService userProfileService;
  private final ObjectMapper objectMapper;
  private final UserRepository userRepository;
  private final CustomerProfileRepository customerProfileRepository;
  private final CategoryQueryRepository categoryRepository;
  private final CategoryAccessPolicy categoryAccessPolicy;
  private final CustomerRiskPolicy customerRiskPolicy;
  private final CityRepository cityRepository;
  private final DistrictRepository districtRepository;
  private final PerformerProfileRepository performerProfileRepository;
  private final PlatformSettingService platformSettingService;
  private final UserConsentService userConsentService;
  private final DomainEventPublisher domainEventPublisher;
  private final MarketConfigService marketConfigService;
  private final ContactRevealRepository contactRevealRepository;
  private final AuditLogService auditLogService;

  @Transactional
  public MarketplaceTaskResponse createTask(
      UserDetails userDetails, String idempotencyKey, TaskCreateRequest request) {
    User customer = userProfileService.currentUser(userDetails);
    userConsentService.requireCurrent(customer, ConsentType.CUSTOMER_RULES);
    IdempotencyDecision decision =
        beginIdempotency(customer, idempotencyKey, "/api/v1/tasks", request);
    if (decision.replay()) {
      return toTaskResponse(findTaskByPublicId(replayResourceId(decision.key(), RESOURCE_TASK)));
    }

    customerRiskPolicy.assertTaskCreationAllowed(customer, taskAmount(request));

    Category category = findCategory(request.categoryId());
    categoryAccessPolicy.assertAvailableForPublicTask(category);
    City city = findCity(request.cityId());
    assertSupportedCity(city);
    if (request.countryCode() != null
        && !request.countryCode().isBlank()
        && !city.getCountry().getCode().equalsIgnoreCase(request.countryCode().trim())) {
      throw new BadRequestBusinessException("cityId does not belong to countryCode");
    }
    riskEventService.assertPreDealContentAllowed(request.description());

    MarketplaceTask task = new MarketplaceTask(customer, category, city);
    task.setTitle(request.title().trim());
    task.setDescription(request.description().trim());
    task.setServiceMode(request.serviceMode());
    task.setPriceType(request.priceType());
    task.setBudgetMin(request.budgetMin());
    task.setBudgetMax(request.budgetMax());
    task.setFixedPrice(request.fixedPrice());
    task.setCurrency(city.getCountry().getCurrencyCode());
    task.setAddressText(blankToNull(request.addressText()));
    task.setLatitude(request.latitude());
    task.setLongitude(request.longitude());
    task.setExpiresAt(
        request.expiresAt() == null
            ? Instant.now()
                .plus(
                    platformSettingService.integerValue(PlatformSettingKey.TASK_EXPIRATION_DAYS),
                    ChronoUnit.DAYS)
            : request.expiresAt());
    if (request.districtId() != null && !request.districtId().isBlank()) {
      task.setDistrict(findDistrict(request.districtId()));
    }
    validateTaskPricing(task);
    task.publish();

    MarketplaceTask savedTask = taskRepository.save(task);
    domainEventPublisher.publish(DomainEventType.TASK_CREATED, "Task", savedTask.getPublicId());
    domainEventPublisher.publish(DomainEventType.TASK_PUBLISHED, "Task", savedTask.getPublicId());
    MarketplaceTaskResponse response = toTaskResponse(savedTask);
    completeIdempotency(decision, 201, response, RESOURCE_TASK, savedTask.getPublicId());
    return response;
  }

  @Transactional(readOnly = true)
  public PageResponse<MarketplaceTaskResponse> searchTasks(
      TaskStatus status,
      String categoryId,
      String cityId,
      String countryCode,
      CategoryServiceMode serviceMode,
      int page,
      int size) {
    if (!platformSettingService.booleanValue(PlatformSettingKey.PUBLIC_TASKS_ENABLED)) {
      throw new FeatureDisabledException("Public tasks");
    }
    int safeSize = Math.min(Math.max(size, 1), 100);
    Pageable pageable =
        PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

    return PageResponse.from(
        taskRepository
            .findAll(taskSearchSpec(status, categoryId, cityId, countryCode, serviceMode), pageable)
            .map(this::toTaskResponse));
  }

  @Transactional(readOnly = true)
  public MarketplaceTaskResponse findTask(String taskId) {
    if (!platformSettingService.booleanValue(PlatformSettingKey.PUBLIC_TASKS_ENABLED)) {
      throw new FeatureDisabledException("Public tasks");
    }
    MarketplaceTask task = findTaskByPublicId(taskId);
    if (!task.getCountry().isSupported() || !task.getCity().isSupported()) {
      throw new ResourceNotFoundException("Task", taskId);
    }
    return toTaskResponse(task);
  }

  @Transactional(readOnly = true)
  public PageResponse<MarketplaceTaskResponse> myTasks(
      UserDetails userDetails, TaskStatus status, int page, int size) {
    User customer = userProfileService.currentUser(userDetails);
    int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    Pageable pageable =
        PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    return PageResponse.from(
        taskRepository
            .findAll(myTaskSpec(customer.getId(), status), pageable)
            .map(this::toTaskResponse));
  }

  @Transactional
  public MarketplaceTaskResponse repeatTask(
      UserDetails userDetails, String idempotencyKey, String taskId) {
    User customer = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(customer, idempotencyKey, "/api/v1/tasks/" + taskId + "/repeat", null);
    if (decision.replay()) {
      return toTaskResponse(findTaskByPublicId(replayResourceId(decision.key(), RESOURCE_TASK)));
    }

    MarketplaceTask sourceTask = findTaskByPublicId(taskId);
    assertTaskOwner(sourceTask, customer);
    customerRiskPolicy.assertTaskCreationAllowed(customer, taskAmount(sourceTask));

    MarketplaceTask repeatedTask =
        new MarketplaceTask(customer, sourceTask.getCategory(), sourceTask.getCity());
    repeatedTask.markAsRepeatOf(sourceTask);
    repeatedTask.setTitle(sourceTask.getTitle());
    repeatedTask.setDescription(sourceTask.getDescription());
    repeatedTask.setServiceMode(sourceTask.getServiceMode());
    repeatedTask.setPriceType(sourceTask.getPriceType());
    repeatedTask.setBudgetMin(sourceTask.getBudgetMin());
    repeatedTask.setBudgetMax(sourceTask.getBudgetMax());
    repeatedTask.setFixedPrice(sourceTask.getFixedPrice());
    repeatedTask.setCurrency(sourceTask.getCurrency());
    repeatedTask.setAddressText(sourceTask.getAddressText());
    repeatedTask.setLatitude(sourceTask.getLatitude());
    repeatedTask.setLongitude(sourceTask.getLongitude());
    repeatedTask.setExpiresAt(
        Instant.now()
            .plus(
                platformSettingService.integerValue(PlatformSettingKey.TASK_EXPIRATION_DAYS),
                ChronoUnit.DAYS));
    if (sourceTask.getDistrict() != null) {
      repeatedTask.setDistrict(sourceTask.getDistrict());
    }
    repeatedTask.setPreferredPerformer(sourceTask.getSelectedPerformer());
    validateTaskPricing(repeatedTask);
    repeatedTask.publish();

    MarketplaceTask savedTask = taskRepository.save(repeatedTask);
    domainEventPublisher.publish(DomainEventType.TASK_CREATED, "Task", savedTask.getPublicId());
    domainEventPublisher.publish(DomainEventType.TASK_PUBLISHED, "Task", savedTask.getPublicId());
    MarketplaceTaskResponse response = toTaskResponse(savedTask);
    completeIdempotency(decision, 201, response, RESOURCE_TASK, savedTask.getPublicId());
    return response;
  }

  @Transactional
  public MarketplaceTaskResponse cancelTask(UserDetails userDetails, String taskId) {
    User customer = userProfileService.currentUser(userDetails);
    MarketplaceTask task =
        taskRepository
            .findByPublicIdForUpdate(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    assertTaskOwner(task, customer);
    Instant now = Instant.now();
    task.cancelOpen(now);
    offerRepository
        .findAllByTaskIdAndStatus(task.getId(), TaskOfferStatus.PENDING)
        .forEach(
            offer -> {
              offer.reject(now);
              notifyOfferTransition(
                  offer,
                  NotificationType.OFFER_REJECTED,
                  "Отклик отклонён",
                  "Заказ отменён заказчиком");
            });
    domainEventPublisher.publish(DomainEventType.TASK_CANCELED, "Task", task.getPublicId());
    riskEventService.recordCancellationPatternIfNeeded(task);
    return toTaskResponse(task);
  }

  @Transactional
  public MarketplaceTaskResponse adminPublishTask(UserDetails userDetails, String taskId) {
    assertAdmin(userDetails);
    MarketplaceTask task =
        taskRepository
            .findByPublicIdForUpdate(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    task.publish();
    notifyUser(
        task.getCustomer(),
        NotificationType.TASK_PUBLISHED,
        "Заказ опубликован",
        "Заказ прошёл модерацию и опубликован",
        "Task",
        task.getPublicId(),
        Map.of());
    return toTaskResponse(task);
  }

  @Transactional
  public MarketplaceTaskResponse adminRejectTask(
      UserDetails userDetails, String taskId, TaskModerationRejectRequest request) {
    assertAdmin(userDetails);
    MarketplaceTask task =
        taskRepository
            .findByPublicIdForUpdate(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    String reason = request == null ? null : blankToNull(request.reason());
    task.rejectByModeration();
    notifyUser(
        task.getCustomer(),
        NotificationType.TASK_REJECTED,
        "Заказ отклонён",
        reason == null ? "Заказ не прошёл модерацию" : reason,
        "Task",
        task.getPublicId(),
        reason == null ? Map.of() : Map.of("reason", reason));
    return toTaskResponse(task);
  }

  @Scheduled(fixedDelayString = "${app.marketplace.expiry.cleanup-interval:PT5M}")
  @Transactional
  public void expireOpenTasksAndOffers() {
    expireOpenTasksAndOffers(Instant.now());
  }

  @Transactional
  public long expireOpenTasksAndOffers(Instant now) {
    List<MarketplaceTask> expiredTasks =
        taskRepository.findAllByStatusAndExpiresAtLessThanEqual(
            TaskStatus.OPEN, now, PageRequest.of(0, 100));
    expiredTasks.forEach(
        task -> {
          task.expire(now);
          offerRepository
              .findAllByTaskIdAndStatus(task.getId(), TaskOfferStatus.PENDING)
              .forEach(
                  offer -> {
                    offer.expire(now);
                    notifyOfferTransition(
                        offer,
                        NotificationType.OFFER_EXPIRED,
                        "Срок отклика истёк",
                        "Заказ закрыт по истечении срока");
                  });
          notifyUser(
              task.getCustomer(),
              NotificationType.TASK_EXPIRED,
              "Срок заказа истёк",
              "Заказ закрыт по истечении срока публикации",
              "Task",
              task.getPublicId(),
              Map.of());
        });

    List<TaskOffer> expiredOffers =
        offerRepository.findAllByStatusAndExpiresAtLessThanEqual(
            TaskOfferStatus.PENDING, now, PageRequest.of(0, 200));
    expiredOffers.forEach(
        offer -> {
          offer.expire(now);
          notifyOfferTransition(
              offer,
              NotificationType.OFFER_EXPIRED,
              "Срок отклика истёк",
              "Отклик закрыт по истечении срока");
        });
    return expiredTasks.size() + expiredOffers.size();
  }

  @Transactional
  public TaskOfferResponse createOffer(
      UserDetails userDetails,
      String idempotencyKey,
      String taskId,
      TaskOfferCreateRequest request) {
    User user = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(user, idempotencyKey, "/api/v1/tasks/" + taskId + "/offers", request);
    if (decision.replay()) {
      return toOfferResponse(findOfferByPublicId(replayResourceId(decision.key(), RESOURCE_OFFER)));
    }

    PerformerProfile performer =
        performerProfileRepository
            .findByUserId(user.getId())
            .orElseThrow(
                () -> new ResourceNotFoundException("PerformerProfile", user.getPublicId()));
    Instant monthStart =
        Instant.now()
            .atZone(ZoneOffset.UTC)
            .withDayOfMonth(1)
            .toLocalDate()
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant();
    long monthlyOffers =
        offerRepository.countByPerformerIdAndCreatedAtGreaterThanEqual(
            performer.getId(), monthStart);
    if (monthlyOffers
        >= platformSettingService.integerValue(PlatformSettingKey.FREE_OFFER_LIMIT_MONTHLY)) {
      throw new AccessDeniedBusinessException("Monthly free offer limit reached");
    }
    MarketplaceTask task = findTaskByPublicId(taskId);

    if (task.getCustomer().getId().equals(user.getId())) {
      throw new BadRequestBusinessException("Customer cannot create an offer for own task");
    }
    if (task.getStatus() != TaskStatus.OPEN) {
      throw new InvalidStatusTransitionException("Task", task.getStatus().name(), "OFFER_PENDING");
    }
    if (isExpired(task.getExpiresAt())) {
      task.expire(Instant.now());
      throw new InvalidStatusTransitionException(
          "Task", TaskStatus.EXPIRED.name(), "OFFER_PENDING");
    }
    if (!performer.isAvailable()) {
      throw new BadRequestBusinessException("Performer is not available");
    }
    assertPerformerCanServeTask(performer, task);
    if (offerRepository.existsByTaskIdAndPerformerIdAndStatus(
        task.getId(), performer.getId(), TaskOfferStatus.PENDING)) {
      throw new DuplicateResourceException("Active offer already exists for this task");
    }

    TaskOffer offer = new TaskOffer(task, performer);
    riskEventService.assertPreDealContentAllowed(request.message());
    offer.setMessage(request.message().trim());
    offer.setProposedPrice(request.proposedPrice());
    offer.setCurrency(task.getCurrency());
    offer.setEstimatedDuration(blankToNull(request.estimatedDuration()));
    offer.setIncludesMaterials(Boolean.TRUE.equals(request.includesMaterials()));
    offer.setExpiresAt(defaultOfferExpiresAt(task, request.expiresAt()));

    TaskOffer savedOffer = offerRepository.save(offer);
    notifyUser(
        task.getCustomer(),
        NotificationType.NEW_OFFER,
        "Новый отклик на заказ",
        performer.getDisplayName() + " отправил отклик на заказ \"" + task.getTitle() + "\"",
        "Task",
        task.getPublicId(),
        Map.of(
            "offerId", savedOffer.getPublicId(),
            "performerId", performer.getPublicId()));
    TaskOfferResponse response = toOfferResponse(savedOffer);
    completeIdempotency(decision, 201, response, RESOURCE_OFFER, savedOffer.getPublicId());
    return response;
  }

  @Transactional(readOnly = true)
  public List<TaskOfferResponse> listTaskOffers(UserDetails userDetails, String taskId) {
    User user = userProfileService.currentUser(userDetails);
    MarketplaceTask task = findTaskByPublicId(taskId);
    assertTaskOwner(task, user);
    return offerRepository.findAllByTaskPublicIdOrderByCreatedAtDesc(taskId).stream()
        .map(this::toOfferResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<TaskOfferResponse> myOffers(UserDetails userDetails) {
    User user = userProfileService.currentUser(userDetails);
    return offerRepository.findAllByPerformerUserIdOrderByCreatedAtDesc(user.getId()).stream()
        .map(this::toOfferResponse)
        .toList();
  }

  @Transactional
  public TaskOfferResponse cancelOffer(
      UserDetails userDetails, String idempotencyKey, String taskId, String offerId) {
    User user = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            user,
            idempotencyKey,
            "/api/v1/tasks/" + taskId + "/offers/" + offerId + "/cancel",
            null);
    if (decision.replay()) {
      TaskOffer replayed = findOfferByPublicId(replayResourceId(decision.key(), RESOURCE_OFFER));
      assertOfferOwner(replayed, user);
      return toOfferResponse(replayed);
    }

    TaskOffer offer =
        offerRepository
            .findByTaskPublicIdAndPublicIdForUpdate(taskId, offerId)
            .orElseThrow(() -> new ResourceNotFoundException("TaskOffer", offerId));
    assertOfferOwner(offer, user);
    offer.cancel(Instant.now());
    TaskOfferResponse response = toOfferResponse(offer);
    completeIdempotency(decision, 200, response, RESOURCE_OFFER, offer.getPublicId());
    return response;
  }

  @Transactional
  public DealResponse acceptOffer(
      UserDetails userDetails, String idempotencyKey, String taskId, String offerId) {
    User customer = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            customer,
            idempotencyKey,
            "/api/v1/tasks/" + taskId + "/offers/" + offerId + "/accept",
            null);
    if (decision.replay()) {
      Deal deal = findDealByPublicId(replayResourceId(decision.key(), RESOURCE_DEAL));
      assertDealCustomer(deal, customer);
      return toDealResponse(deal, findChatByDeal(deal));
    }

    MarketplaceTask task =
        taskRepository
            .findByPublicIdForUpdate(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    assertTaskOwner(task, customer);
    if (task.getStatus() != TaskStatus.OPEN) {
      throw new InvalidStatusTransitionException(
          "Task", task.getStatus().name(), TaskStatus.IN_PROGRESS.name());
    }
    if (isExpired(task.getExpiresAt())) {
      task.expire(Instant.now());
      throw new InvalidStatusTransitionException(
          "Task", TaskStatus.EXPIRED.name(), TaskStatus.IN_PROGRESS.name());
    }
    if (dealRepository.existsByTaskId(task.getId())) {
      throw new DuplicateResourceException("Deal already exists for this task");
    }

    TaskOffer offer =
        offerRepository
            .findByTaskPublicIdAndPublicIdForUpdate(taskId, offerId)
            .orElseThrow(() -> new ResourceNotFoundException("TaskOffer", offerId));
    if (offer.getStatus() != TaskOfferStatus.PENDING) {
      throw new InvalidStatusTransitionException(
          "TaskOffer", offer.getStatus().name(), TaskOfferStatus.ACCEPTED.name());
    }
    if (isExpired(offer.getExpiresAt())) {
      offer.expire(Instant.now());
      throw new InvalidStatusTransitionException(
          "TaskOffer", TaskOfferStatus.EXPIRED.name(), TaskOfferStatus.ACCEPTED.name());
    }

    Instant now = Instant.now();
    offer.accept(now);
    task.acceptOffer(offer, now);
    offerRepository.findAllByTaskIdAndStatus(task.getId(), TaskOfferStatus.PENDING).stream()
        .filter(otherOffer -> !otherOffer.getId().equals(offer.getId()))
        .forEach(
            otherOffer -> {
              otherOffer.reject(now);
              notifyOfferTransition(
                  otherOffer,
                  NotificationType.OFFER_REJECTED,
                  "Выбран другой исполнитель",
                  "Заказчик принял другой отклик");
            });

    Deal deal = dealRepository.save(new Deal(task, offer));
    DealChat chat = chatRepository.save(new DealChat(deal));
    domainEventPublisher.publish(DomainEventType.DEAL_CREATED, "Deal", deal.getPublicId());
    domainEventPublisher.publish(DomainEventType.CHAT_CREATED, "DealChat", chat.getPublicId());
    notifyUser(
        offer.getPerformer().getUser(),
        NotificationType.OFFER_ACCEPTED,
        "Отклик принят",
        "Ваш отклик на заказ \"" + task.getTitle() + "\" принят",
        "Deal",
        deal.getPublicId(),
        Map.of(
            "taskId", task.getPublicId(),
            "chatId", chat.getPublicId()));
    DealResponse response = toDealResponse(deal, chat);
    completeIdempotency(decision, 200, response, RESOURCE_DEAL, deal.getPublicId());
    return response;
  }

  @Transactional(readOnly = true)
  public DealResponse findDeal(UserDetails userDetails, String dealId) {
    User user = userProfileService.currentUser(userDetails);
    Deal deal = findDealByPublicId(dealId);
    assertDealParticipant(deal, user);
    return toDealResponse(deal, findChatByDeal(deal));
  }

  @Transactional(readOnly = true)
  public List<DealResponse> myDeals(UserDetails userDetails) {
    User user = userProfileService.currentUser(userDetails);
    return dealRepository
        .findAllByCustomerIdOrPerformerUserIdOrderByCreatedAtDesc(user.getId(), user.getId())
        .stream()
        .map(deal -> toDealResponse(deal, findChatByDeal(deal)))
        .toList();
  }

  @Transactional
  public ContactRevealResponse revealContact(
      UserDetails userDetails,
      String idempotencyKey,
      String dealId,
      ContactRevealRequest request,
      String ipAddress,
      String userAgent) {
    User actor = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            actor, idempotencyKey, "/api/v1/deals/" + dealId + "/contact-reveals", request);
    if (decision.replay()) {
      ContactReveal reveal =
          contactRevealRepository
              .findByPublicId(replayResourceId(decision.key(), RESOURCE_CONTACT_REVEAL))
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "ContactReveal", decision.key().getResponseResourceId()));
      assertContactRevealVisible(reveal, actor);
      return toContactRevealResponse(reveal);
    }

    Deal deal =
        dealRepository
            .findByPublicIdForUpdate(dealId)
            .orElseThrow(() -> new ResourceNotFoundException("Deal", dealId));
    User subject = contactSubject(deal, actor, request.participantId());
    assertContactRevealAllowed(deal, request.contactType());
    assertVerifiedContact(subject, request.contactType());

    ContactReveal existing =
        contactRevealRepository
            .findByDealIdAndRequestedByIdAndSubjectUserIdAndContactType(
                deal.getId(), actor.getId(), subject.getId(), request.contactType())
            .orElse(null);
    ContactReveal reveal =
        existing != null
            ? existing
            : contactRevealRepository.save(
                new ContactReveal(
                    deal,
                    actor,
                    subject,
                    request.contactType(),
                    requireIpAddress(ipAddress),
                    blankToNull(userAgent)));
    deal.markContactShared();
    if (existing == null) {
      auditLogService.append(
          actor,
          "CONTACT_REVEALED",
          "Deal",
          deal.getPublicId(),
          Map.of(
              "contactRevealId", reveal.getPublicId(),
              "contactType", reveal.getContactType().name(),
              "subjectUserId", subject.getPublicId()),
          requireIpAddress(ipAddress),
          blankToNull(userAgent));
      domainEventPublisher.publishToUsers(
          DomainEventType.CONTACT_REVEALED,
          "Deal",
          deal.getPublicId(),
          List.of(deal.getCustomer(), deal.getPerformer().getUser()),
          NotificationType.CONTACT_REVEALED,
          "Контакт открыт",
          "Участник сделки открыл подтверждённый контакт для координации работ",
          "Deal",
          deal.getPublicId(),
          Map.of("contactType", reveal.getContactType().name()));
    }

    ContactRevealResponse response = toContactRevealResponse(reveal);
    completeIdempotency(decision, 200, response, RESOURCE_CONTACT_REVEAL, reveal.getPublicId());
    return response;
  }

  @Transactional(readOnly = true)
  public List<ContactRevealHistoryResponse> contactRevealHistory(
      UserDetails userDetails, String dealId) {
    User actor = userProfileService.currentUser(userDetails);
    Deal deal = findDealByPublicId(dealId);
    assertDealParticipantOrSupport(deal, actor);
    return contactRevealRepository.findAllByDealPublicIdOrderByCreatedAtAsc(dealId).stream()
        .map(this::toContactRevealHistoryResponse)
        .toList();
  }

  @Transactional
  public DealResponse cancelDeal(
      UserDetails userDetails,
      String idempotencyKey,
      String dealId,
      DealCancelRequest request,
      String ipAddress,
      String userAgent) {
    User actor = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(actor, idempotencyKey, "/api/v1/deals/" + dealId + "/cancel", request);
    if (decision.replay()) {
      Deal replayed = findDealByPublicId(replayResourceId(decision.key(), RESOURCE_DEAL));
      assertDealParticipant(replayed, actor);
      return toDealResponse(replayed, findChatByDeal(replayed));
    }

    Deal deal =
        dealRepository
            .findByPublicIdForUpdate(dealId)
            .orElseThrow(() -> new ResourceNotFoundException("Deal", dealId));
    assertDealParticipant(deal, actor);
    DealChat chat = findChatByDeal(deal);
    Instant canceledAt = Instant.now();
    ContactReveal latestReveal =
        contactRevealRepository.findTopByDealIdOrderByCreatedAtDesc(deal.getId()).orElse(null);
    boolean afterContactReveal = latestReveal != null;

    deal.cancelByParticipant(
        actor, request.reason(), blankToNull(request.comment()), afterContactReveal, canceledAt);
    deal.getTask().cancelActiveDeal(canceledAt);
    chat.makeReadOnly(canceledAt);
    auditLogService.append(
        actor,
        "DEAL_CANCELED",
        "Deal",
        deal.getPublicId(),
        Map.of("reason", request.reason().name(), "afterContactReveal", afterContactReveal),
        requireIpAddress(ipAddress),
        blankToNull(userAgent));
    riskEventService.recordDealCancellationAfterReveal(
        deal, actor, request.reason(), latestReveal, canceledAt);
    domainEventPublisher.publish(
        DomainEventType.DEAL_CANCELED,
        "Deal",
        deal.getPublicId(),
        otherDealParticipant(deal, actor),
        NotificationType.DEAL_CANCELED,
        "Сделка отменена",
        "Второй участник отменил сделку: " + request.reason().name(),
        "Deal",
        deal.getPublicId(),
        Map.of("reason", request.reason().name()));

    DealResponse response = toDealResponse(deal, chat);
    completeIdempotency(decision, 200, response, RESOURCE_DEAL, deal.getPublicId());
    return response;
  }

  @Transactional
  public MilestoneResponse createMilestone(
      UserDetails userDetails,
      String idempotencyKey,
      String dealId,
      MilestoneCreateRequest request) {
    User customer = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            customer, idempotencyKey, "/api/v1/deals/" + dealId + "/milestones", request);
    if (decision.replay()) {
      Milestone milestone =
          findMilestoneByPublicId(replayResourceId(decision.key(), RESOURCE_MILESTONE));
      assertDealCustomer(milestone.getDeal(), customer);
      return toMilestoneResponse(milestone);
    }

    Deal deal = findDealByPublicId(dealId);
    assertDealCustomer(deal, customer);
    BigDecimal dealAmount = deal.getAcceptedOffer().getProposedPrice();
    if (dealAmount == null
        || dealAmount.compareTo(
                platformSettingService.decimalValue(PlatformSettingKey.MILESTONE_THRESHOLD))
            < 0) {
      throw new BadRequestBusinessException("Deal amount is below the milestone threshold");
    }
    if (deal.getStatus() != DealStatus.ACTIVE
        && deal.getStatus() != DealStatus.REVISION_REQUESTED) {
      throw new InvalidStatusTransitionException(
          "Deal", deal.getStatus().name(), DealStatus.ACTIVE.name());
    }

    Milestone milestone =
        milestoneRepository.save(
            new Milestone(
                deal,
                request.title().trim(),
                blankToNull(request.description()),
                request.amount(),
                deal.getTask().getCurrency(),
                request.dueDate()));
    deal.enableMilestones();
    MilestoneResponse response = toMilestoneResponse(milestone);
    completeIdempotency(decision, 201, response, RESOURCE_MILESTONE, milestone.getPublicId());
    return response;
  }

  @Transactional(readOnly = true)
  public List<MilestoneResponse> listDealMilestones(UserDetails userDetails, String dealId) {
    User user = userProfileService.currentUser(userDetails);
    Deal deal = findDealByPublicId(dealId);
    assertDealParticipant(deal, user);
    return milestoneRepository.findAllByDealPublicIdOrderByCreatedAtAsc(dealId).stream()
        .map(this::toMilestoneResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public MilestoneResponse findMilestone(UserDetails userDetails, String milestoneId) {
    User user = userProfileService.currentUser(userDetails);
    Milestone milestone = findMilestoneByPublicId(milestoneId);
    assertDealParticipant(milestone.getDeal(), user);
    return toMilestoneResponse(milestone);
  }

  @Transactional
  public MilestoneResponse startMilestone(
      UserDetails userDetails, String idempotencyKey, String milestoneId) {
    User performerUser = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            performerUser, idempotencyKey, "/api/v1/milestones/" + milestoneId + "/start", null);
    if (decision.replay()) {
      Milestone milestone =
          findMilestoneByPublicId(replayResourceId(decision.key(), RESOURCE_MILESTONE));
      assertDealPerformer(milestone.getDeal(), performerUser);
      return toMilestoneResponse(milestone);
    }

    Milestone milestone = findMilestoneByPublicIdForUpdate(milestoneId);
    assertDealPerformer(milestone.getDeal(), performerUser);
    assertMilestoneTransition(milestone, MilestoneStatus.IN_PROGRESS);
    milestone.start();
    MilestoneResponse response = toMilestoneResponse(milestone);
    completeIdempotency(decision, 200, response, RESOURCE_MILESTONE, milestone.getPublicId());
    return response;
  }

  @Transactional
  public MilestoneResponse submitMilestone(
      UserDetails userDetails,
      String idempotencyKey,
      String milestoneId,
      MilestoneDecisionRequest request) {
    User performerUser = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            performerUser,
            idempotencyKey,
            "/api/v1/milestones/" + milestoneId + "/submit",
            request);
    if (decision.replay()) {
      Milestone milestone =
          findMilestoneByPublicId(replayResourceId(decision.key(), RESOURCE_MILESTONE));
      assertDealPerformer(milestone.getDeal(), performerUser);
      return toMilestoneResponse(milestone);
    }

    Milestone milestone = findMilestoneByPublicIdForUpdate(milestoneId);
    assertDealPerformer(milestone.getDeal(), performerUser);
    assertMilestoneTransition(milestone, MilestoneStatus.SUBMITTED);
    milestone.submit(Instant.now());
    MilestoneResponse response = toMilestoneResponse(milestone);
    completeIdempotency(decision, 200, response, RESOURCE_MILESTONE, milestone.getPublicId());
    return response;
  }

  @Transactional
  public MilestoneResponse acceptMilestone(
      UserDetails userDetails,
      String idempotencyKey,
      String milestoneId,
      MilestoneDecisionRequest request) {
    User customer = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            customer, idempotencyKey, "/api/v1/milestones/" + milestoneId + "/accept", request);
    if (decision.replay()) {
      Milestone milestone =
          findMilestoneByPublicId(replayResourceId(decision.key(), RESOURCE_MILESTONE));
      assertDealCustomer(milestone.getDeal(), customer);
      return toMilestoneResponse(milestone);
    }

    Milestone milestone = findMilestoneByPublicIdForUpdate(milestoneId);
    assertDealCustomer(milestone.getDeal(), customer);
    assertMilestoneTransition(milestone, MilestoneStatus.ACCEPTED);
    milestone.accept(Instant.now());
    MilestoneResponse response = toMilestoneResponse(milestone);
    completeIdempotency(decision, 200, response, RESOURCE_MILESTONE, milestone.getPublicId());
    return response;
  }

  @Transactional
  public MilestoneResponse rejectMilestone(
      UserDetails userDetails,
      String idempotencyKey,
      String milestoneId,
      MilestoneDecisionRequest request) {
    User customer = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            customer, idempotencyKey, "/api/v1/milestones/" + milestoneId + "/reject", request);
    if (decision.replay()) {
      Milestone milestone =
          findMilestoneByPublicId(replayResourceId(decision.key(), RESOURCE_MILESTONE));
      assertDealCustomer(milestone.getDeal(), customer);
      return toMilestoneResponse(milestone);
    }

    Milestone milestone = findMilestoneByPublicIdForUpdate(milestoneId);
    assertDealCustomer(milestone.getDeal(), customer);
    String reason = blankToNull(request.reason());
    if (reason == null) {
      throw new BadRequestBusinessException("Rejection reason is required");
    }
    assertMilestoneTransition(milestone, MilestoneStatus.REJECTED);
    milestone.reject(Instant.now());
    MilestoneResponse response = toMilestoneResponse(milestone);
    completeIdempotency(decision, 200, response, RESOURCE_MILESTONE, milestone.getPublicId());
    return response;
  }

  @Transactional
  public MilestoneResponse disputeMilestone(
      UserDetails userDetails,
      String idempotencyKey,
      String milestoneId,
      MilestoneDecisionRequest request) {
    User participant = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            participant, idempotencyKey, "/api/v1/milestones/" + milestoneId + "/dispute", request);
    if (decision.replay()) {
      Milestone milestone =
          findMilestoneByPublicId(replayResourceId(decision.key(), RESOURCE_MILESTONE));
      assertDealParticipant(milestone.getDeal(), participant);
      return toMilestoneResponse(milestone);
    }

    Milestone milestone = findMilestoneByPublicIdForUpdate(milestoneId);
    assertDealParticipant(milestone.getDeal(), participant);
    assertMilestoneTransition(milestone, MilestoneStatus.DISPUTED);
    milestone.dispute();
    MilestoneResponse response = toMilestoneResponse(milestone);
    completeIdempotency(decision, 200, response, RESOURCE_MILESTONE, milestone.getPublicId());
    return response;
  }

  @Transactional
  public MilestoneResponse cancelMilestone(
      UserDetails userDetails,
      String idempotencyKey,
      String milestoneId,
      MilestoneDecisionRequest request) {
    User customer = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            customer, idempotencyKey, "/api/v1/milestones/" + milestoneId + "/cancel", request);
    if (decision.replay()) {
      Milestone milestone =
          findMilestoneByPublicId(replayResourceId(decision.key(), RESOURCE_MILESTONE));
      assertDealCustomer(milestone.getDeal(), customer);
      return toMilestoneResponse(milestone);
    }

    Milestone milestone = findMilestoneByPublicIdForUpdate(milestoneId);
    assertDealCustomer(milestone.getDeal(), customer);
    assertMilestoneTransition(milestone, MilestoneStatus.CANCELED);
    milestone.cancel();
    MilestoneResponse response = toMilestoneResponse(milestone);
    completeIdempotency(decision, 200, response, RESOURCE_MILESTONE, milestone.getPublicId());
    return response;
  }

  @Transactional(readOnly = true)
  public List<DealChatResponse> myChats(UserDetails userDetails) {
    User user = userProfileService.currentUser(userDetails);
    return chatRepository.findSummariesForParticipant(user.getId()).stream()
        .map(this::toChatResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public DealChatResponse findChat(UserDetails userDetails, String chatId) {
    User user = userProfileService.currentUser(userDetails);
    DealChat chat = findChatByPublicId(chatId);
    assertChatParticipant(chat, user);
    return chatRepository
        .findSummaryForParticipant(chatId, user.getId())
        .map(this::toChatResponse)
        .orElseThrow(() -> new ResourceNotFoundException("Chat", chatId));
  }

  @Transactional(readOnly = true)
  public PageResponse<ChatMessageResponse> listChatMessages(
      UserDetails userDetails, String chatId, int page, int size) {
    User user = userProfileService.currentUser(userDetails);
    DealChat chat = findChatByPublicId(chatId);
    assertChatParticipant(chat, user);

    Pageable pageable =
        PageRequest.of(
            Math.max(page, 0), safePageSize(size), Sort.by(Sort.Direction.ASC, "createdAt"));
    return PageResponse.from(
        chatMessageRepository
            .findAllByChatPublicIdAndDeletedAtIsNull(chat.getPublicId(), pageable)
            .map(this::toChatMessageResponse));
  }

  @Transactional(readOnly = true)
  public ChatMessageTimelineResponse chatMessageTimeline(
      UserDetails userDetails, String chatId, String before, String after, int requestedLimit) {
    User user = userProfileService.currentUser(userDetails);
    DealChat chat = findChatByPublicId(chatId);
    assertChatParticipant(chat, user);
    if (before != null && after != null) {
      throw new BadRequestBusinessException("before and after cannot be combined");
    }

    int limit = Math.min(Math.max(requestedLimit, 1), MAX_PAGE_SIZE);
    Pageable page = PageRequest.of(0, limit + 1);
    List<ChatMessage> fetched;
    boolean hasMoreOlder = false;
    boolean hasMoreNewer = false;

    if (before != null) {
      ChatMessage cursor = findChatMessageCursor(chat, before);
      fetched =
          new ArrayList<>(
              chatMessageRepository.findByChatPublicIdAndDeletedAtIsNullAndIdLessThanOrderByIdDesc(
                  chatId, cursor.getId(), page));
      hasMoreOlder = fetched.size() > limit;
      trimExtra(fetched, limit);
      Collections.reverse(fetched);
    } else if (after != null) {
      ChatMessage cursor = findChatMessageCursor(chat, after);
      fetched =
          new ArrayList<>(
              chatMessageRepository
                  .findByChatPublicIdAndDeletedAtIsNullAndIdGreaterThanOrderByIdAsc(
                      chatId, cursor.getId(), page));
      hasMoreNewer = fetched.size() > limit;
      trimExtra(fetched, limit);
    } else {
      fetched =
          new ArrayList<>(
              chatMessageRepository.findByChatPublicIdAndDeletedAtIsNullOrderByIdDesc(
                  chatId, page));
      hasMoreOlder = fetched.size() > limit;
      trimExtra(fetched, limit);
      Collections.reverse(fetched);
    }

    List<ChatMessageResponse> messages = fetched.stream().map(this::toChatMessageResponse).toList();
    return new ChatMessageTimelineResponse(
        messages,
        messages.isEmpty() ? null : messages.getFirst().publicId(),
        messages.isEmpty() ? null : messages.getLast().publicId(),
        hasMoreOlder,
        hasMoreNewer);
  }

  @Transactional
  public ChatMessageResponse sendChatMessage(
      UserDetails userDetails, String chatId, ChatMessageRequest request) {
    User sender = userProfileService.currentUser(userDetails);
    DealChat chat = findChatByPublicId(chatId);
    assertChatParticipant(chat, sender);
    if (chat.getStatus() != ChatStatus.ACTIVE) {
      throw new InvalidStatusTransitionException(
          "Chat", chat.getStatus().name(), ChatStatus.ACTIVE.name());
    }

    String text = blankToNull(request.text());
    if (text == null) {
      throw new BadRequestBusinessException("Message text is required");
    }
    boolean riskFlag =
        riskEventService.chatMessageRiskFlag(
            text, contactRevealRepository.existsByDealId(chat.getDeal().getId()));
    ChatMessage message =
        chatMessageRepository.save(
            new ChatMessage(chat, sender, ChatMessageType.TEXT, text, null, null, riskFlag));
    recordRiskIfNeeded(message);
    notifyChatRecipient(
        chat, sender, NotificationType.NEW_CHAT_MESSAGE, "Новое сообщение", text, message);
    return toChatMessageResponse(message);
  }

  @Transactional
  public void markChatRead(UserDetails userDetails, String chatId) {
    User reader = userProfileService.currentUser(userDetails);
    DealChat chat = findChatByPublicId(chatId);
    assertChatParticipant(chat, reader);
    chatMessageRepository.markUnreadMessagesAsRead(
        chat.getPublicId(), reader.getId(), Instant.now());
  }

  @Transactional
  public DisputeCaseResponse openDispute(
      UserDetails userDetails, String idempotencyKey, String chatId, OpenDisputeRequest request) {
    User opener = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            opener, idempotencyKey, "/api/v1/chats/" + chatId + "/open-dispute", request);
    if (decision.replay()) {
      DisputeCase disputeCase =
          findDisputeByPublicId(replayResourceId(decision.key(), RESOURCE_DISPUTE));
      assertDealParticipant(disputeCase.getDeal(), opener);
      return toDisputeCaseResponse(disputeCase, findChatByDeal(disputeCase.getDeal()));
    }

    DealChat chat = findChatByPublicIdForUpdate(chatId);
    assertChatParticipant(chat, opener);

    Deal deal = chat.getDeal();
    MarketplaceTask task = chat.getTask();
    if (deal.getStatus() == DealStatus.COMPLETED || deal.getStatus() == DealStatus.CANCELED) {
      throw new InvalidStatusTransitionException(
          "Deal", deal.getStatus().name(), DealStatus.DISPUTED.name());
    }
    if (deal.getStatus() == DealStatus.DISPUTED
        || disputeCaseRepository.existsByDealId(deal.getId())) {
      throw new DuplicateResourceException("Dispute already exists for this deal");
    }

    String reason = blankToNull(request.reason());
    if (reason == null) {
      throw new BadRequestBusinessException("Dispute reason is required");
    }
    String description = blankToNull(request.description());

    deal.openDispute();
    task.openDispute();
    DisputeCase disputeCase =
        disputeCaseRepository.save(new DisputeCase(deal, opener, reason, description));
    saveSystemMessage(chat, opener, ChatMessageType.DISPUTE_OPENED, "DISPUTE_OPENED", reason);
    notifyUser(
        otherDealParticipant(deal, opener),
        NotificationType.DISPUTE_OPENED,
        "Открыт спор",
        "По заказу \"" + task.getTitle() + "\" открыт спор",
        "DisputeCase",
        disputeCase.getPublicId(),
        Map.of(
            "dealId", deal.getPublicId(),
            "taskId", task.getPublicId()));
    DisputeCaseResponse response = toDisputeCaseResponse(disputeCase, chat);
    completeIdempotency(decision, 200, response, RESOURCE_DISPUTE, disputeCase.getPublicId());
    return response;
  }

  @Transactional(readOnly = true)
  public DisputeCaseResponse findDispute(UserDetails userDetails, String disputeId) {
    User user = userProfileService.currentUser(userDetails);
    DisputeCase disputeCase = findDisputeByPublicId(disputeId);
    assertDealParticipantOrAdmin(disputeCase.getDeal(), user);
    return toDisputeCaseResponse(disputeCase, findChatByDeal(disputeCase.getDeal()));
  }

  @Transactional(readOnly = true)
  public PageResponse<DisputeCaseResponse> myDisputes(UserDetails userDetails, int page, int size) {
    User user = userProfileService.currentUser(userDetails);
    Pageable pageable =
        PageRequest.of(
            Math.max(page, 0),
            Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
            Sort.by(Sort.Direction.DESC, "createdAt"));
    return PageResponse.from(
        disputeCaseRepository
            .findAllByDealCustomerIdOrDealPerformerUserIdOrderByCreatedAtDesc(
                user.getId(), user.getId(), pageable)
            .map(
                disputeCase ->
                    toDisputeCaseResponse(disputeCase, findChatByDeal(disputeCase.getDeal()))));
  }

  @Transactional(readOnly = true)
  public PageResponse<DisputeCaseResponse> adminDisputes(
      UserDetails userDetails, DisputeCaseStatus status, int page, int size) {
    assertSupport(userDetails);
    DisputeCaseStatus effectiveStatus = status == null ? DisputeCaseStatus.OPEN : status;
    Pageable pageable =
        PageRequest.of(
            Math.max(page, 0),
            Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
            Sort.by(Sort.Direction.ASC, "createdAt"));
    return PageResponse.from(
        disputeCaseRepository
            .findAllByStatusOrderByCreatedAtAsc(effectiveStatus, pageable)
            .map(
                disputeCase ->
                    toDisputeCaseResponse(disputeCase, findChatByDeal(disputeCase.getDeal()))));
  }

  @Transactional
  public DisputeCaseResponse adminStartDisputeReview(
      UserDetails userDetails, String disputeId, DisputeReviewRequest request) {
    User admin = assertAdmin(userDetails);
    DisputeCase disputeCase = findDisputeForUpdate(disputeId);
    disputeCase.markUnderReview();
    DealChat chat = findChatByDeal(disputeCase.getDeal());
    saveSystemMessage(
        chat,
        admin,
        ChatMessageType.SYSTEM,
        "DISPUTE_UNDER_REVIEW",
        reviewComment(request, "Dispute review started"));
    return toDisputeCaseResponse(disputeCase, chat);
  }

  @Transactional
  public DisputeCaseResponse adminRequestDisputeEvidence(
      UserDetails userDetails, String disputeId, boolean customer, DisputeReviewRequest request) {
    User admin = assertAdmin(userDetails);
    DisputeCase disputeCase = findDisputeForUpdate(disputeId);
    if (customer) {
      disputeCase.requestCustomerEvidence();
    } else {
      disputeCase.requestPerformerEvidence();
    }
    User recipient =
        customer
            ? disputeCase.getDeal().getCustomer()
            : disputeCase.getDeal().getPerformer().getUser();
    String message = reviewComment(request, "Additional dispute evidence is required");
    DealChat chat = findChatByDeal(disputeCase.getDeal());
    saveSystemMessage(
        chat,
        admin,
        ChatMessageType.SYSTEM,
        customer ? "CUSTOMER_EVIDENCE_REQUESTED" : "PERFORMER_EVIDENCE_REQUESTED",
        message);
    notifyUser(
        recipient,
        NotificationType.DISPUTE_OPENED,
        "Запрошены материалы по спору",
        message,
        "DisputeCase",
        disputeCase.getPublicId(),
        Map.of("status", disputeCase.getStatus().name()));
    return toDisputeCaseResponse(disputeCase, chat);
  }

  @Transactional
  public DisputeCaseResponse adminResolveDispute(
      UserDetails userDetails,
      String idempotencyKey,
      String disputeId,
      DisputeResolutionRequest request) {
    User admin = assertAdmin(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            admin, idempotencyKey, "/api/v1/admin/disputes/" + disputeId + "/resolve", request);
    if (decision.replay()) {
      DisputeCase disputeCase =
          findDisputeByPublicId(replayResourceId(decision.key(), RESOURCE_DISPUTE));
      return toDisputeCaseResponse(disputeCase, findChatByDeal(disputeCase.getDeal()));
    }

    DisputeCase disputeCase =
        disputeCaseRepository
            .findByPublicIdForUpdate(disputeId)
            .orElseThrow(() -> new ResourceNotFoundException("DisputeCase", disputeId));
    if (disputeCase.getResolvedAt() != null || isResolvedDisputeStatus(disputeCase.getStatus())) {
      throw new InvalidStatusTransitionException(
          "DisputeCase", disputeCase.getStatus().name(), request.status().name());
    }
    if (!isAdminResolutionStatus(request.status())) {
      throw new InvalidStatusTransitionException(
          "DisputeCase", disputeCase.getStatus().name(), request.status().name());
    }
    String adminDecision = blankToNull(request.adminDecision());
    if (adminDecision == null) {
      throw new BadRequestBusinessException("adminDecision is required");
    }

    Instant now = Instant.now();
    disputeCase.resolve(request.status(), adminDecision, now);
    Deal deal = disputeCase.getDeal();
    MarketplaceTask task = disputeCase.getTask();
    DealChat chat = findChatByDeal(deal);
    applyDisputeResolution(deal, task, chat, request.status(), now);
    saveSystemMessage(
        chat, admin, ChatMessageType.DISPUTE_RESOLVED, "DISPUTE_RESOLVED", adminDecision);
    notifyDisputeResolved(disputeCase, adminDecision);
    DisputeCaseResponse response = toDisputeCaseResponse(disputeCase, chat);
    completeIdempotency(decision, 200, response, RESOURCE_DISPUTE, disputeCase.getPublicId());
    return response;
  }

  @Transactional
  public DealResponse submitWork(
      UserDetails userDetails,
      String idempotencyKey,
      String chatId,
      WorkSubmissionRequest request) {
    User performerUser = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            performerUser, idempotencyKey, "/api/v1/chats/" + chatId + "/submit-work", request);
    if (decision.replay()) {
      Deal deal = findDealByPublicId(replayResourceId(decision.key(), RESOURCE_DEAL));
      assertDealPerformer(deal, performerUser);
      return toDealResponse(deal, findChatByDeal(deal));
    }

    DealChat chat = findChatByPublicIdForUpdate(chatId);
    assertChatPerformer(chat, performerUser);
    String message = blankToNull(request.message());

    Deal deal = chat.getDeal();
    MarketplaceTask task = chat.getTask();
    if (deal.getStatus() != DealStatus.ACTIVE
        && deal.getStatus() != DealStatus.REVISION_REQUESTED) {
      throw new InvalidStatusTransitionException(
          "Deal", deal.getStatus().name(), DealStatus.WORK_SUBMITTED.name());
    }

    Instant now = Instant.now();
    if (deal.getStatus() == DealStatus.REVISION_REQUESTED) {
      deal.resumeAfterRevision();
      task.resumeAfterRevision();
    }
    deal.submitWork();
    task.submitWork(now);
    saveSystemMessage(
        chat,
        performerUser,
        ChatMessageType.WORK_SUBMITTED,
        "WORK_SUBMITTED",
        message == null ? "Work submitted for review" : message);
    notifyUser(
        deal.getCustomer(),
        NotificationType.WORK_SUBMITTED,
        "Работа отправлена на проверку",
        "Исполнитель отправил результат по заказу \"" + task.getTitle() + "\"",
        "Deal",
        deal.getPublicId(),
        Map.of(
            "taskId", task.getPublicId(),
            "chatId", chat.getPublicId()));
    DealResponse response = toDealResponse(deal, chat);
    completeIdempotency(decision, 200, response, RESOURCE_DEAL, deal.getPublicId());
    return response;
  }

  @Transactional
  public DealResponse acceptWork(
      UserDetails userDetails,
      String idempotencyKey,
      String chatId,
      WorkAcceptanceRequest request) {
    User customer = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            customer, idempotencyKey, "/api/v1/chats/" + chatId + "/accept-work", request);
    if (decision.replay()) {
      Deal deal = findDealByPublicId(replayResourceId(decision.key(), RESOURCE_DEAL));
      assertDealCustomer(deal, customer);
      return toDealResponse(deal, findChatByDeal(deal));
    }

    DealChat chat = findChatByPublicIdForUpdate(chatId);
    assertChatCustomer(chat, customer);
    String message = blankToNull(request.message());

    Deal deal = chat.getDeal();
    MarketplaceTask task = chat.getTask();
    if (deal.getStatus() != DealStatus.WORK_SUBMITTED) {
      throw new InvalidStatusTransitionException(
          "Deal", deal.getStatus().name(), DealStatus.COMPLETED.name());
    }
    if (deal.isMilestoneEnabled()
        && milestoneRepository.existsByDealIdAndStatusNot(deal.getId(), MilestoneStatus.ACCEPTED)) {
      throw new InvalidStatusTransitionException(
          "Milestone", "NOT_ALL_ACCEPTED", DealStatus.COMPLETED.name());
    }

    Instant now = Instant.now();
    deal.complete(now);
    task.completeWork(now);
    chat.makeReadOnly(now);
    domainEventPublisher.publish(DomainEventType.DEAL_COMPLETED, "Deal", deal.getPublicId());
    saveSystemMessage(
        chat,
        customer,
        ChatMessageType.WORK_ACCEPTED,
        "WORK_ACCEPTED",
        message == null ? "Work accepted" : message);
    notifyUser(
        deal.getPerformer().getUser(),
        NotificationType.WORK_ACCEPTED,
        "Работа принята",
        "Заказчик принял работу по заказу \"" + task.getTitle() + "\"",
        "Deal",
        deal.getPublicId(),
        Map.of(
            "taskId", task.getPublicId(),
            "chatId", chat.getPublicId()));
    DealResponse response = toDealResponse(deal, chat);
    completeIdempotency(decision, 200, response, RESOURCE_DEAL, deal.getPublicId());
    return response;
  }

  @Transactional
  public DealResponse requestRevision(
      UserDetails userDetails, String idempotencyKey, String chatId, RevisionRequest request) {
    User customer = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            customer, idempotencyKey, "/api/v1/chats/" + chatId + "/request-revision", request);
    if (decision.replay()) {
      Deal deal = findDealByPublicId(replayResourceId(decision.key(), RESOURCE_DEAL));
      assertDealCustomer(deal, customer);
      return toDealResponse(deal, findChatByDeal(deal));
    }

    DealChat chat = findChatByPublicIdForUpdate(chatId);
    assertChatCustomer(chat, customer);
    String reason = blankToNull(request.reason());
    if (reason == null) {
      throw new BadRequestBusinessException("Revision reason is required");
    }

    Deal deal = chat.getDeal();
    MarketplaceTask task = chat.getTask();
    if (deal.getStatus() != DealStatus.WORK_SUBMITTED) {
      throw new InvalidStatusTransitionException(
          "Deal", deal.getStatus().name(), DealStatus.REVISION_REQUESTED.name());
    }
    if (deal.getRevisionCount() >= 2) {
      throw new InvalidStatusTransitionException(
          "Deal", "REVISION_LIMIT_REACHED", DealStatus.DISPUTED.name());
    }

    deal.requestRevision();
    task.requestRevision();
    saveSystemMessage(
        chat, customer, ChatMessageType.REVISION_REQUESTED, "REVISION_REQUESTED", reason);
    notifyUser(
        deal.getPerformer().getUser(),
        NotificationType.REVISION_REQUESTED,
        "Запрошена доработка",
        "Заказчик запросил доработку по заказу \"" + task.getTitle() + "\"",
        "Deal",
        deal.getPublicId(),
        Map.of(
            "taskId", task.getPublicId(),
            "chatId", chat.getPublicId()));
    DealResponse response = toDealResponse(deal, chat);
    completeIdempotency(decision, 200, response, RESOURCE_DEAL, deal.getPublicId());
    return response;
  }

  @Transactional
  public FeedbackResponse createFeedback(
      UserDetails userDetails,
      String idempotencyKey,
      String taskId,
      FeedbackCreateRequest request) {
    User sender = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(sender, idempotencyKey, "/api/v1/tasks/" + taskId + "/feedbacks", request);
    if (decision.replay()) {
      MarketplaceFeedback feedback =
          findFeedbackByPublicId(replayResourceId(decision.key(), RESOURCE_FEEDBACK));
      if (!feedback.getSender().getId().equals(sender.getId())) {
        throw new AccessDeniedBusinessException("Only feedback sender can replay this request");
      }
      return toFeedbackResponse(feedback);
    }

    Deal deal =
        dealRepository
            .findByTaskPublicIdForUpdate(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Deal", taskId));
    assertDealParticipant(deal, sender);
    if (deal.getStatus() != DealStatus.COMPLETED) {
      throw new InvalidStatusTransitionException(
          "Deal", deal.getStatus().name(), DealStatus.COMPLETED.name());
    }
    if (feedbackRepository.existsByDealIdAndSenderId(deal.getId(), sender.getId())) {
      throw new DuplicateResourceException("Feedback already exists for this deal and sender");
    }

    User receiver = feedbackReceiver(deal, sender);
    if (receiver.getId().equals(deal.getCustomer().getId())
        && !platformSettingService.booleanValue(PlatformSettingKey.CUSTOMER_RATING_ENABLED)) {
      throw new FeatureDisabledException("Customer rating");
    }
    MarketplaceFeedback feedback =
        feedbackRepository.save(
            new MarketplaceFeedback(
                deal, sender, receiver, request.grade(), blankToNull(request.text())));
    notifyUser(
        receiver,
        NotificationType.FEEDBACK_CREATED,
        "Получен новый отзыв",
        "По завершённому заказу оставлен новый отзыв",
        "Feedback",
        feedback.getPublicId(),
        Map.of("taskId", deal.getTask().getPublicId()));
    applyReceiverRating(deal, receiver, request.grade());
    FeedbackResponse response = toFeedbackResponse(feedback);
    completeIdempotency(decision, 200, response, RESOURCE_FEEDBACK, feedback.getPublicId());
    return response;
  }

  @Transactional(readOnly = true)
  public List<FeedbackResponse> taskFeedbacks(String taskId) {
    findTaskByPublicId(taskId);
    return feedbackRepository
        .findAllByTaskPublicIdAndHiddenByAdminFalseOrderByCreatedAtDesc(taskId)
        .stream()
        .map(this::toFeedbackResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<FeedbackResponse> userFeedbacks(String userId) {
    userRepository
        .findByPublicId(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    return feedbackRepository
        .findAllByReceiverPublicIdAndHiddenByAdminFalseOrderByCreatedAtDesc(userId)
        .stream()
        .map(this::toFeedbackResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<FeedbackResponse> performerFeedbacks(String performerId) {
    performerProfileRepository
        .findByPublicId(performerId)
        .orElseThrow(() -> new ResourceNotFoundException("PerformerProfile", performerId));
    return feedbackRepository
        .findAllByDealPerformerPublicIdAndHiddenByAdminFalseOrderByCreatedAtDesc(performerId)
        .stream()
        .map(this::toFeedbackResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<FavoritePerformerResponse> myFavoritePerformers(UserDetails userDetails) {
    User customer = userProfileService.currentUser(userDetails);
    return favoritePerformerRepository
        .findAllByCustomerIdOrderByCreatedAtDesc(customer.getId())
        .stream()
        .map(this::toFavoritePerformerResponse)
        .toList();
  }

  @Transactional
  public FavoritePerformerResponse addFavoritePerformer(
      UserDetails userDetails, String performerId, FavoritePerformerRequest request) {
    User customer = userProfileService.currentUser(userDetails);
    PerformerProfile performer =
        performerProfileRepository
            .findByPublicId(performerId)
            .orElseThrow(() -> new ResourceNotFoundException("PerformerProfile", performerId));
    if (performer.getUser().getId().equals(customer.getId())) {
      throw new BadRequestBusinessException(
          "Customer cannot add own performer profile to favorites");
    }
    if (favoritePerformerRepository.existsByCustomerIdAndPerformerProfileId(
        customer.getId(), performer.getId())) {
      throw new DuplicateResourceException("Performer already exists in favorites");
    }

    FavoritePerformer favorite =
        favoritePerformerRepository.save(
            new FavoritePerformer(customer, performer, blankToNull(request.note())));
    return toFavoritePerformerResponse(favorite);
  }

  @Transactional
  public void removeFavoritePerformer(UserDetails userDetails, String performerId) {
    User customer = userProfileService.currentUser(userDetails);
    FavoritePerformer favorite =
        favoritePerformerRepository
            .findByCustomerIdAndPerformerProfilePublicId(customer.getId(), performerId)
            .orElseThrow(() -> new ResourceNotFoundException("FavoritePerformer", performerId));
    favoritePerformerRepository.delete(favorite);
  }

  @Transactional
  public AttachmentUploadResponse addChatAttachment(
      UserDetails userDetails, String chatId, AttachmentMetadataRequest request) {
    User owner = userProfileService.currentUser(userDetails);
    DealChat chat = findChatByPublicId(chatId);
    assertChatParticipant(chat, owner);
    if (chat.getStatus() != ChatStatus.ACTIVE) {
      throw new InvalidStatusTransitionException(
          "Chat", chat.getStatus().name(), ChatStatus.ACTIVE.name());
    }
    validateAttachmentMetadata(request);

    MarketplaceAttachment attachment =
        attachmentRepository.save(
            newAttachment(
                owner,
                chat.getTask(),
                null,
                AttachmentType.CHAT_FILE,
                AttachmentVisibility.PARTICIPANTS_ONLY,
                request));
    return toAttachmentUploadResponse(attachment);
  }

  @Transactional
  public ChatAttachmentCompleteResponse completeChatAttachment(
      UserDetails userDetails, String chatId, String attachmentId) {
    User owner = userProfileService.currentUser(userDetails);
    DealChat chat = findChatByPublicIdForUpdate(chatId);
    assertChatParticipant(chat, owner);
    MarketplaceAttachment attachment =
        attachmentRepository
            .findByPublicIdForUpdate(attachmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));

    if (attachment.getAttachmentType() != AttachmentType.CHAT_FILE
        || attachment.getTask() == null
        || !attachment.getTask().getId().equals(chat.getTask().getId())
        || !attachment.getOwner().getId().equals(owner.getId())) {
      throw new AccessDeniedBusinessException("Attachment does not belong to this chat upload");
    }
    if (attachment.getChatMessage() != null) {
      if (!attachment.getChatMessage().getChat().getId().equals(chat.getId())) {
        throw new AccessDeniedBusinessException("Attachment belongs to another chat");
      }
      return new ChatAttachmentCompleteResponse(
          toAttachmentResponse(attachment), toChatMessageResponse(attachment.getChatMessage()));
    }
    if (chat.getStatus() != ChatStatus.ACTIVE) {
      throw new InvalidStatusTransitionException(
          "Chat", chat.getStatus().name(), ChatStatus.ACTIVE.name());
    }

    verifyUploadedObject(attachment);
    String messageText = attachment.getOriginalFilename();
    ChatMessage message =
        chatMessageRepository.save(
            new ChatMessage(
                chat,
                owner,
                ChatMessageType.ATTACHMENT,
                messageText,
                null,
                attachment.getId(),
                false));
    attachment.attachToMessage(message);
    notifyChatRecipient(
        chat,
        owner,
        NotificationType.NEW_CHAT_MESSAGE,
        "Новое вложение в чате",
        messageText,
        message);
    return new ChatAttachmentCompleteResponse(
        toAttachmentResponse(attachment), toChatMessageResponse(message));
  }

  @Transactional
  public TaskAttachmentUploadResponse addTaskAttachment(
      UserDetails userDetails, String taskId, AttachmentMetadataRequest request) {
    User owner = userProfileService.currentUser(userDetails);
    MarketplaceTask task = findTaskByPublicId(taskId);
    assertTaskOwner(task, owner);
    if (task.getStatus() != TaskStatus.OPEN) {
      throw new InvalidStatusTransitionException(
          "Task", task.getStatus().name(), TaskStatus.OPEN.name());
    }
    validateTaskImage(request);

    MarketplaceAttachment attachment =
        attachmentRepository.save(
            newAttachment(
                owner,
                task,
                null,
                AttachmentType.TASK_IMAGE,
                AttachmentVisibility.PUBLIC,
                request));
    PresignedStorageUrl uploadUrl = fileStorageService.createUploadUrl(attachment);
    return new TaskAttachmentUploadResponse(
        toTaskAttachmentResponse(attachment),
        uploadUrl.url(),
        uploadUrl.method(),
        uploadUrl.headers(),
        uploadUrl.expiresAt());
  }

  @Transactional(readOnly = true)
  public List<TaskAttachmentResponse> taskAttachments(String taskId) {
    MarketplaceTask task = findTaskByPublicId(taskId);
    assertTaskAttachmentsPublic(task);
    return attachmentRepository
        .findAllByTaskPublicIdAndAttachmentTypeAndDeletedAtIsNullOrderByCreatedAtAsc(
            taskId, AttachmentType.TASK_IMAGE)
        .stream()
        .map(this::toTaskAttachmentResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public AttachmentDownloadUrlResponse taskAttachmentDownloadUrl(
      String taskId, String attachmentId) {
    MarketplaceTask task = findTaskByPublicId(taskId);
    assertTaskAttachmentsPublic(task);
    MarketplaceAttachment attachment = findAttachmentByPublicId(attachmentId);
    if (attachment.getAttachmentType() != AttachmentType.TASK_IMAGE
        || attachment.getTask() == null
        || !attachment.getTask().getId().equals(task.getId())) {
      throw new ResourceNotFoundException("TaskAttachment", attachmentId);
    }
    PresignedStorageUrl downloadUrl = fileStorageService.createDownloadUrl(attachment);
    return new AttachmentDownloadUrlResponse(
        attachment.getPublicId(),
        downloadUrl.url(),
        downloadUrl.method(),
        downloadUrl.headers(),
        downloadUrl.expiresAt());
  }

  @Transactional(readOnly = true)
  public List<AttachmentResponse> chatAttachments(UserDetails userDetails, String chatId) {
    User user = userProfileService.currentUser(userDetails);
    DealChat chat = findChatByPublicId(chatId);
    assertChatParticipant(chat, user);
    return attachmentRepository
        .findAllByChatMessageChatPublicIdAndDeletedAtIsNullOrderByCreatedAtDesc(chatId)
        .stream()
        .map(this::toAttachmentResponse)
        .toList();
  }

  @Transactional
  public AttachmentUploadResponse addDisputeAttachment(
      UserDetails userDetails,
      String idempotencyKey,
      String disputeId,
      AttachmentMetadataRequest request) {
    User owner = userProfileService.currentUser(userDetails);
    IdempotencyDecision decision =
        beginIdempotency(
            owner, idempotencyKey, "/api/v1/disputes/" + disputeId + "/attachments", request);
    if (decision.replay()) {
      MarketplaceAttachment attachment =
          findAttachmentByPublicId(replayResourceId(decision.key(), RESOURCE_ATTACHMENT));
      assertCanDownloadAttachment(attachment, owner);
      return toAttachmentUploadResponse(attachment);
    }
    DisputeCase disputeCase = findDisputeByPublicId(disputeId);
    assertDealParticipant(disputeCase.getDeal(), owner);
    if (!disputeCase.acceptsEvidence()) {
      throw new InvalidStatusTransitionException(
          "DisputeCase", disputeCase.getStatus().name(), "EVIDENCE_UPLOAD");
    }
    validateAttachmentMetadata(request);

    MarketplaceAttachment attachment =
        attachmentRepository.save(
            newAttachment(
                owner,
                disputeCase.getTask(),
                disputeCase,
                AttachmentType.DISPUTE_EVIDENCE,
                AttachmentVisibility.PARTICIPANTS_ONLY,
                request));
    AttachmentUploadResponse response = toAttachmentUploadResponse(attachment);
    completeIdempotency(decision, 201, response, RESOURCE_ATTACHMENT, attachment.getPublicId());
    return response;
  }

  @Transactional(readOnly = true)
  public List<AttachmentResponse> disputeAttachments(UserDetails userDetails, String disputeId) {
    User user = userProfileService.currentUser(userDetails);
    DisputeCase disputeCase = findDisputeByPublicId(disputeId);
    assertDealParticipantOrAdmin(disputeCase.getDeal(), user);
    return attachmentRepository
        .findAllByDisputeCasePublicIdAndDeletedAtIsNullOrderByCreatedAtDesc(disputeId)
        .stream()
        .map(this::toAttachmentResponse)
        .toList();
  }

  @Transactional(readOnly = true)
  public AttachmentDownloadUrlResponse attachmentDownloadUrl(
      UserDetails userDetails, String attachmentId) {
    User user = userProfileService.currentUser(userDetails);
    MarketplaceAttachment attachment = findAttachmentByPublicId(attachmentId);
    assertCanDownloadAttachment(attachment, user);
    PresignedStorageUrl downloadUrl = fileStorageService.createDownloadUrl(attachment);
    return new AttachmentDownloadUrlResponse(
        attachment.getPublicId(),
        downloadUrl.url(),
        downloadUrl.method(),
        downloadUrl.headers(),
        downloadUrl.expiresAt());
  }

  private Specification<MarketplaceTask> taskSearchSpec(
      TaskStatus status,
      String categoryId,
      String cityId,
      String countryCode,
      CategoryServiceMode serviceMode) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(
          criteriaBuilder.equal(root.get("publicationStatus"), PublicationStatus.PUBLISHED));
      predicates.add(
          criteriaBuilder.equal(root.get("status"), status == null ? TaskStatus.OPEN : status));
      String effectiveCountryCode =
          countryCode == null || countryCode.isBlank()
              ? marketConfigService.defaultCountry().getCode()
              : countryCode.trim().toUpperCase(Locale.ROOT);
      predicates.add(criteriaBuilder.equal(root.get("country").get("code"), effectiveCountryCode));
      predicates.add(criteriaBuilder.isTrue(root.get("country").get("supported")));
      predicates.add(criteriaBuilder.isTrue(root.get("city").get("supported")));
      if (categoryId != null) {
        predicates.add(criteriaBuilder.equal(root.get("category").get("publicId"), categoryId));
      }
      if (cityId != null) {
        predicates.add(criteriaBuilder.equal(root.get("city").get("publicId"), cityId));
      }
      if (serviceMode != null) {
        predicates.add(criteriaBuilder.equal(root.get("serviceMode"), serviceMode));
      }
      return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    };
  }

  private Specification<MarketplaceTask> myTaskSpec(Long customerId, TaskStatus status) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(criteriaBuilder.equal(root.get("customer").get("id"), customerId));
      if (status != null) {
        predicates.add(criteriaBuilder.equal(root.get("status"), status));
      }
      return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
    };
  }

  private void validateTaskPricing(MarketplaceTask task) {
    if (task.getExpiresAt().isBefore(Instant.now())) {
      throw new BadRequestBusinessException("expiresAt must be in the future");
    }
    if (task.getBudgetMin() != null
        && task.getBudgetMax() != null
        && task.getBudgetMin().compareTo(task.getBudgetMax()) > 0) {
      throw new BadRequestBusinessException("budgetMin must be less than or equal to budgetMax");
    }
    if (task.getPriceType() == PriceType.FIXED && task.getFixedPrice() == null) {
      throw new BadRequestBusinessException("fixedPrice is required for FIXED tasks");
    }
  }

  private void validateAttachmentMetadata(AttachmentMetadataRequest request) {
    String filename = blankToNull(request.originalFilename());
    String contentType = blankToNull(request.contentType());
    if (filename == null) {
      throw new BadRequestBusinessException("originalFilename is required");
    }
    if (contentType == null) {
      throw new BadRequestBusinessException("contentType is required");
    }
    if (request.sizeBytes() <= 0 || request.sizeBytes() > MAX_CHAT_ATTACHMENT_SIZE_BYTES) {
      throw new BadRequestBusinessException("Attachment size exceeds allowed limit");
    }
    if (isBlockedAttachment(filename, contentType)) {
      throw new BadRequestBusinessException("Attachment file type is not allowed");
    }
  }

  private void validateTaskImage(AttachmentMetadataRequest request) {
    validateAttachmentMetadata(request);
    String contentType = request.contentType().trim().toLowerCase(Locale.ROOT);
    if (!TASK_IMAGE_CONTENT_TYPES.contains(contentType)) {
      throw new BadRequestBusinessException("Task image content type is not allowed");
    }
    if (request.sizeBytes() > MAX_TASK_IMAGE_SIZE_BYTES) {
      throw new BadRequestBusinessException("Task image size exceeds allowed limit");
    }
  }

  private void verifyUploadedObject(MarketplaceAttachment attachment) {
    StoredObjectMetadata stored = fileStorageService.inspectObject(attachment);
    if (!stored.exists()) {
      throw new BadRequestBusinessException("Uploaded object was not found in storage");
    }
    if (stored.sizeBytes() != attachment.getSizeBytes()) {
      throw new BadRequestBusinessException("Uploaded object size does not match metadata");
    }
    String storedContentType = blankToNull(stored.contentType());
    if (storedContentType == null
        || !storedContentType.equalsIgnoreCase(attachment.getContentType())) {
      throw new BadRequestBusinessException("Uploaded object content type does not match metadata");
    }
  }

  private ChatMessage findChatMessageCursor(DealChat chat, String cursor) {
    ChatMessage message =
        chatMessageRepository
            .findByPublicIdAndDeletedAtIsNull(cursor)
            .orElseThrow(() -> new ResourceNotFoundException("ChatMessage", cursor));
    if (!message.getChat().getId().equals(chat.getId())) {
      throw new BadRequestBusinessException("Message cursor does not belong to this chat");
    }
    return message;
  }

  private <T> void trimExtra(List<T> values, int limit) {
    if (values.size() > limit) {
      values.removeLast();
    }
  }

  private boolean isBlockedAttachment(String filename, String contentType) {
    String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
    if (BLOCKED_CONTENT_TYPES.contains(normalizedContentType)) {
      return true;
    }
    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex < 0 || dotIndex == filename.length() - 1) {
      return false;
    }
    String extension = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    return BLOCKED_ATTACHMENT_EXTENSIONS.contains(extension);
  }

  private void assertPerformerCanServeTask(PerformerProfile performer, MarketplaceTask task) {
    PerformerCategory performerCategory =
        performer.getCategories().stream()
            .filter(category -> category.getCategory().getId().equals(task.getCategory().getId()))
            .findFirst()
            .orElseThrow(
                () -> new BadRequestBusinessException("Performer does not serve this category"));
    categoryAccessPolicy.assertPerformerCanServeCategory(performer, performerCategory);
    if (task.getServiceMode() == CategoryServiceMode.REMOTE && !performer.isWorksRemotely()) {
      throw new BadRequestBusinessException("Performer does not work remotely");
    }
    if (task.getServiceMode() == CategoryServiceMode.ONSITE && !performer.isWorksOnsite()) {
      throw new BadRequestBusinessException("Performer does not work onsite");
    }
  }

  private Instant defaultOfferExpiresAt(MarketplaceTask task, Instant requestedExpiresAt) {
    Instant expiresAt =
        requestedExpiresAt == null ? Instant.now().plus(7, ChronoUnit.DAYS) : requestedExpiresAt;
    if (expiresAt.isAfter(task.getExpiresAt())) {
      return task.getExpiresAt();
    }
    if (expiresAt.isBefore(Instant.now())) {
      throw new BadRequestBusinessException("expiresAt must be in the future");
    }
    return expiresAt;
  }

  private boolean isExpired(Instant expiresAt) {
    return expiresAt != null && !expiresAt.isAfter(Instant.now());
  }

  private MarketplaceTask findTaskByPublicId(String taskId) {
    return taskRepository
        .findByPublicId(taskId)
        .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
  }

  private Deal findDealByPublicId(String dealId) {
    return dealRepository
        .findByPublicId(dealId)
        .orElseThrow(() -> new ResourceNotFoundException("Deal", dealId));
  }

  private Milestone findMilestoneByPublicId(String milestoneId) {
    return milestoneRepository
        .findByPublicId(milestoneId)
        .orElseThrow(() -> new ResourceNotFoundException("Milestone", milestoneId));
  }

  private Milestone findMilestoneByPublicIdForUpdate(String milestoneId) {
    return milestoneRepository
        .findByPublicIdForUpdate(milestoneId)
        .orElseThrow(() -> new ResourceNotFoundException("Milestone", milestoneId));
  }

  private DisputeCase findDisputeByPublicId(String disputeId) {
    return disputeCaseRepository
        .findByPublicId(disputeId)
        .orElseThrow(() -> new ResourceNotFoundException("DisputeCase", disputeId));
  }

  private DealChat findChatByDeal(Deal deal) {
    return chatRepository
        .findByDealId(deal.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Chat", deal.getPublicId()));
  }

  private DealChat findChatByPublicId(String chatId) {
    return chatRepository
        .findByPublicId(chatId)
        .orElseThrow(() -> new ResourceNotFoundException("Chat", chatId));
  }

  private DealChat findChatByPublicIdForUpdate(String chatId) {
    return chatRepository
        .findByPublicIdForUpdate(chatId)
        .orElseThrow(() -> new ResourceNotFoundException("Chat", chatId));
  }

  private MarketplaceAttachment findAttachmentByPublicId(String attachmentId) {
    return attachmentRepository
        .findByPublicIdAndDeletedAtIsNull(attachmentId)
        .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));
  }

  private TaskOffer findOfferByPublicId(String offerId) {
    return offerRepository
        .findByPublicId(offerId)
        .orElseThrow(() -> new ResourceNotFoundException("TaskOffer", offerId));
  }

  private MarketplaceFeedback findFeedbackByPublicId(String feedbackId) {
    return feedbackRepository
        .findByPublicId(feedbackId)
        .orElseThrow(() -> new ResourceNotFoundException("Feedback", feedbackId));
  }

  private IdempotencyDecision beginIdempotency(
      User user, String idempotencyKey, String endpoint, Object request) {
    return idempotencyService.begin(user, idempotencyKey, endpoint, toJson(request));
  }

  private void completeIdempotency(
      IdempotencyDecision decision,
      int responseStatus,
      Object response,
      String resourceType,
      String resourceId) {
    idempotencyService.complete(
        decision.key(), responseStatus, toJson(response), resourceType, resourceId);
  }

  private String replayResourceId(IdempotencyKey key, String expectedResourceType) {
    if (!expectedResourceType.equals(key.getResponseResourceType())
        || key.getResponseResourceId() == null) {
      throw new BadRequestBusinessException("Idempotency replay resource is not available");
    }
    return key.getResponseResourceId();
  }

  private String toJson(Object value) {
    if (value == null) {
      return "";
    }
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize idempotency payload", exception);
    }
  }

  private Category findCategory(String categoryId) {
    return categoryRepository
        .findByPublicId(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
  }

  private City findCity(String cityId) {
    return cityRepository
        .findByPublicId(cityId)
        .orElseThrow(() -> new ResourceNotFoundException("City", cityId));
  }

  private void assertSupportedCity(City city) {
    if (!city.isSupported() || !city.getCountry().isSupported()) {
      throw new BadRequestBusinessException("Selected city is not supported by the active market");
    }
  }

  private District findDistrict(String districtId) {
    return districtRepository
        .findByPublicId(districtId)
        .orElseThrow(() -> new ResourceNotFoundException("District", districtId));
  }

  private void assertTaskOwner(MarketplaceTask task, User user) {
    if (!task.getCustomer().getId().equals(user.getId())) {
      throw new AccessDeniedBusinessException("Only task customer can perform this action");
    }
  }

  private User assertAdmin(UserDetails userDetails) {
    User user = userProfileService.currentUser(userDetails);
    if (!user.getRoles().contains(RoleName.ADMIN)) {
      throw new AccessDeniedBusinessException("Only admins can perform this action");
    }
    return user;
  }

  private User assertSupport(UserDetails userDetails) {
    User user = userProfileService.currentUser(userDetails);
    if (!user.getRoles().contains(RoleName.ADMIN)
        && !user.getRoles().contains(RoleName.MODERATOR)) {
      throw new AccessDeniedBusinessException("Only support roles can access this resource");
    }
    return user;
  }

  private void assertDealCustomer(Deal deal, User user) {
    if (!deal.getCustomer().getId().equals(user.getId())) {
      throw new AccessDeniedBusinessException("Only deal customer can perform this action");
    }
  }

  private void assertDealPerformer(Deal deal, User user) {
    if (!deal.getPerformer().getUser().getId().equals(user.getId())) {
      throw new AccessDeniedBusinessException("Only deal performer can perform this action");
    }
  }

  private void assertDealParticipant(Deal deal, User user) {
    boolean customer = deal.getCustomer().getId().equals(user.getId());
    boolean performer = deal.getPerformer().getUser().getId().equals(user.getId());
    if (!customer && !performer) {
      throw new AccessDeniedBusinessException("Only deal participants can access this deal");
    }
  }

  private void assertDealParticipantOrAdmin(Deal deal, User user) {
    assertDealParticipantOrSupport(deal, user);
  }

  private void assertDealParticipantOrSupport(Deal deal, User user) {
    if (user.getRoles().contains(RoleName.ADMIN) || user.getRoles().contains(RoleName.MODERATOR)) {
      return;
    }
    assertDealParticipant(deal, user);
  }

  private User contactSubject(Deal deal, User actor, String requestedParticipantId) {
    boolean support =
        actor.getRoles().contains(RoleName.ADMIN) || actor.getRoles().contains(RoleName.MODERATOR);
    if (!support) {
      assertDealParticipant(deal, actor);
      User subject = otherDealParticipant(deal, actor);
      if (requestedParticipantId != null
          && !requestedParticipantId.isBlank()
          && !subject.getPublicId().equals(requestedParticipantId.trim())) {
        throw new AccessDeniedBusinessException(
            "Participants may reveal only the other deal participant's contact");
      }
      return subject;
    }
    String participantId = blankToNull(requestedParticipantId);
    if (participantId == null) {
      throw new BadRequestBusinessException("participantId is required for support contact reveal");
    }
    if (deal.getCustomer().getPublicId().equals(participantId)) {
      return deal.getCustomer();
    }
    if (deal.getPerformer().getUser().getPublicId().equals(participantId)) {
      return deal.getPerformer().getUser();
    }
    throw new BadRequestBusinessException("participantId is not a participant of this deal");
  }

  private void assertContactRevealAllowed(Deal deal, ContactType contactType) {
    if (contactType != ContactType.PHONE) {
      throw new BadRequestBusinessException("Only PHONE contact reveal is supported");
    }
    if (deal.getStatus() != DealStatus.ACTIVE
        && deal.getStatus() != DealStatus.WORK_SUBMITTED
        && deal.getStatus() != DealStatus.REVISION_REQUESTED) {
      throw new InvalidStatusTransitionException("Deal", deal.getStatus().name(), "CONTACT_REVEAL");
    }
    Category category = deal.getTask().getCategory();
    if (!category.isAllowsContactReveal()
        || !category.isRequiresOnsiteCoordination()
        || !category.isAllowsOnsite()
        || deal.getTask().getServiceMode() == CategoryServiceMode.REMOTE
        || category.getContactRevealStage()
            == com.handynest.catalog.category.ContactRevealStage.NEVER) {
      throw new AccessDeniedBusinessException("Contact reveal is not allowed for this service");
    }
    if (category.getContactRevealStage()
            == com.handynest.catalog.category.ContactRevealStage.AFTER_WORK_STARTED
        && deal.getStatus() == DealStatus.ACTIVE) {
      throw new AccessDeniedBusinessException("Contact reveal is not available at this deal stage");
    }
  }

  private void assertVerifiedContact(User subject, ContactType contactType) {
    if (contactType == ContactType.PHONE
        && (subject.getPhone() == null
            || subject.getPhone().isBlank()
            || !subject.isPhoneVerified())) {
      throw new BadRequestBusinessException("Participant phone must be present and verified");
    }
  }

  private void assertContactRevealVisible(ContactReveal reveal, User user) {
    assertDealParticipantOrSupport(reveal.getDeal(), user);
    if (!reveal.getRequestedBy().getId().equals(user.getId())
        && !user.getRoles().contains(RoleName.ADMIN)
        && !user.getRoles().contains(RoleName.MODERATOR)) {
      throw new AccessDeniedBusinessException("Only the reveal requester may replay this result");
    }
  }

  private DisputeCase findDisputeForUpdate(String disputeId) {
    return disputeCaseRepository
        .findByPublicIdForUpdate(disputeId)
        .orElseThrow(() -> new ResourceNotFoundException("DisputeCase", disputeId));
  }

  private String reviewComment(DisputeReviewRequest request, String fallback) {
    String comment = request == null ? null : blankToNull(request.comment());
    return comment == null ? fallback : comment;
  }

  private void assertMilestoneTransition(Milestone milestone, MilestoneStatus targetStatus) {
    MilestoneStatus currentStatus = milestone.getStatus();
    boolean allowed =
        switch (targetStatus) {
          case IN_PROGRESS ->
              currentStatus == MilestoneStatus.PENDING || currentStatus == MilestoneStatus.REJECTED;
          case SUBMITTED -> currentStatus == MilestoneStatus.IN_PROGRESS;
          case ACCEPTED ->
              currentStatus == MilestoneStatus.SUBMITTED
                  || currentStatus == MilestoneStatus.DISPUTED;
          case REJECTED -> currentStatus == MilestoneStatus.SUBMITTED;
          case DISPUTED -> currentStatus == MilestoneStatus.SUBMITTED;
          case CANCELED ->
              currentStatus == MilestoneStatus.PENDING
                  || currentStatus == MilestoneStatus.IN_PROGRESS
                  || currentStatus == MilestoneStatus.DISPUTED;
          case PENDING -> false;
        };
    if (!allowed) {
      throw new InvalidStatusTransitionException(
          "Milestone", currentStatus.name(), targetStatus.name());
    }
  }

  private void assertChatParticipant(DealChat chat, User user) {
    boolean customer = chat.getCustomer().getId().equals(user.getId());
    boolean performer = chat.getPerformer().getUser().getId().equals(user.getId());
    if (!customer && !performer) {
      throw new AccessDeniedBusinessException("Only chat participants can access this chat");
    }
  }

  private void assertOfferOwner(TaskOffer offer, User user) {
    if (!offer.getPerformer().getUser().getId().equals(user.getId())) {
      throw new AccessDeniedBusinessException("Only offer owner can cancel this offer");
    }
  }

  private void assertTaskAttachmentsPublic(MarketplaceTask task) {
    if (task.getPublicationStatus() != PublicationStatus.PUBLISHED) {
      throw new ResourceNotFoundException("Task", task.getPublicId());
    }
  }

  private void assertChatCustomer(DealChat chat, User user) {
    if (!chat.getCustomer().getId().equals(user.getId())) {
      throw new AccessDeniedBusinessException("Only customer can perform this action");
    }
  }

  private void assertChatPerformer(DealChat chat, User user) {
    if (!chat.getPerformer().getUser().getId().equals(user.getId())) {
      throw new AccessDeniedBusinessException("Only performer can perform this action");
    }
  }

  private void assertCanDownloadAttachment(MarketplaceAttachment attachment, User user) {
    if (attachment.getAttachmentType() == AttachmentType.VERIFICATION_DOCUMENT) {
      throw new AccessDeniedBusinessException(
          "Verification documents require the audited admin endpoint");
    }
    if (attachment.getVisibility() == AttachmentVisibility.PUBLIC) {
      return;
    }
    if (attachment.getVisibility() == AttachmentVisibility.ADMIN_ONLY) {
      if (!user.getRoles().contains(RoleName.ADMIN)) {
        throw new AccessDeniedBusinessException("Only admins can access this attachment");
      }
      return;
    }
    if (attachment.getChatMessage() != null) {
      assertChatParticipant(attachment.getChatMessage().getChat(), user);
      return;
    }
    if (attachment.getDisputeCase() != null) {
      assertDealParticipantOrAdmin(attachment.getDisputeCase().getDeal(), user);
      return;
    }
    if (!attachment.getOwner().getId().equals(user.getId())) {
      throw new AccessDeniedBusinessException("Only attachment owner can access this attachment");
    }
  }

  private User feedbackReceiver(Deal deal, User sender) {
    if (deal.getCustomer().getId().equals(sender.getId())) {
      return deal.getPerformer().getUser();
    }
    if (deal.getPerformer().getUser().getId().equals(sender.getId())) {
      return deal.getCustomer();
    }
    throw new AccessDeniedBusinessException("Only deal participants can leave feedback");
  }

  private void applyDisputeResolution(
      Deal deal,
      MarketplaceTask task,
      DealChat chat,
      DisputeCaseStatus status,
      Instant resolvedAt) {
    switch (status) {
      case RESOLVED_RELEASE, RESOLVED_PARTIAL -> {
        deal.resolveDisputeCompleted(resolvedAt);
        task.resolveDisputeCompleted(resolvedAt);
        chat.makeReadOnly(resolvedAt);
      }
      case RESOLVED_REFUND, CANCELED -> {
        deal.resolveDisputeCanceled(resolvedAt);
        task.resolveDisputeCanceled(resolvedAt);
        chat.makeReadOnly(resolvedAt);
      }
      default ->
          throw new InvalidStatusTransitionException(
              "DisputeCase", status.name(), "ADMIN_RESOLUTION");
    }
  }

  private boolean isAdminResolutionStatus(DisputeCaseStatus status) {
    return status == DisputeCaseStatus.RESOLVED_REFUND
        || status == DisputeCaseStatus.RESOLVED_RELEASE
        || status == DisputeCaseStatus.RESOLVED_PARTIAL
        || status == DisputeCaseStatus.CANCELED;
  }

  private boolean isResolvedDisputeStatus(DisputeCaseStatus status) {
    return isAdminResolutionStatus(status);
  }

  private void applyReceiverRating(Deal deal, User receiver, int grade) {
    if (deal.getPerformer().getUser().getId().equals(receiver.getId())) {
      PerformerProfile performer = deal.getPerformer();
      performer.applyRating(grade, performer.getRatingCount());
      return;
    }
    CustomerProfile customerProfile =
        customerProfileRepository
            .findByUserId(receiver.getId())
            .orElseThrow(
                () -> new ResourceNotFoundException("CustomerProfile", receiver.getPublicId()));
    customerProfile.applyRating(grade, customerProfile.getRatingCount());
  }

  private void notifyChatRecipient(
      DealChat chat,
      User sender,
      NotificationType type,
      String title,
      String body,
      ChatMessage message) {
    notifyUser(
        otherDealParticipant(chat.getDeal(), sender),
        type,
        title,
        body,
        "Chat",
        chat.getPublicId(),
        Map.of(
            "dealId", chat.getDeal().getPublicId(),
            "taskId", chat.getTask().getPublicId(),
            "messageId", message.getPublicId()));
  }

  private void notifyDisputeResolved(DisputeCase disputeCase, String decision) {
    Deal deal = disputeCase.getDeal();
    MarketplaceTask task = disputeCase.getTask();
    Map<String, String> metadata =
        Map.of(
            "dealId", deal.getPublicId(),
            "taskId", task.getPublicId(),
            "status", disputeCase.getStatus().name());
    domainEventPublisher.publishToUsers(
        DomainEventType.DISPUTE_RESOLVED,
        "DisputeCase",
        disputeCase.getPublicId(),
        List.of(deal.getCustomer(), deal.getPerformer().getUser()),
        NotificationType.DISPUTE_RESOLVED,
        "Спор решён",
        decision,
        "DisputeCase",
        disputeCase.getPublicId(),
        metadata);
  }

  private void notifyUser(
      User user,
      NotificationType type,
      String title,
      String body,
      String targetType,
      String targetId,
      Map<String, ?> metadata) {
    notificationService.notifyUser(user, type, title, body, targetType, targetId, metadata);
  }

  private void notifyOfferTransition(
      TaskOffer offer, NotificationType type, String title, String body) {
    notifyUser(
        offer.getPerformer().getUser(),
        type,
        title,
        body,
        "TaskOffer",
        offer.getPublicId(),
        Map.of("taskId", offer.getTask().getPublicId()));
  }

  private User otherDealParticipant(Deal deal, User actor) {
    if (deal.getCustomer().getId().equals(actor.getId())) {
      return deal.getPerformer().getUser();
    }
    return deal.getCustomer();
  }

  private void saveSystemMessage(
      DealChat chat, User sender, ChatMessageType messageType, String systemCode, String text) {
    ChatMessage message =
        chatMessageRepository.save(
            new ChatMessage(
                chat, sender, messageType, text, systemCode, null, hasContactRisk(text)));
    recordRiskIfNeeded(message);
  }

  private MarketplaceAttachment newAttachment(
      User owner,
      MarketplaceTask task,
      DisputeCase disputeCase,
      AttachmentType attachmentType,
      AttachmentVisibility visibility,
      AttachmentMetadataRequest request) {
    String publicId = PublicIdGenerator.defaultGenerator().newUlid();
    String storageKey = storageKey(attachmentType, publicId, request.originalFilename());
    return new MarketplaceAttachment(
        publicId,
        owner,
        task,
        disputeCase,
        attachmentType,
        fileStorageService.provider(),
        fileStorageProperties.bucketFor(attachmentType, visibility),
        storageKey,
        request.originalFilename().trim(),
        request.contentType().trim().toLowerCase(Locale.ROOT),
        request.sizeBytes(),
        blankToNull(request.checksum()),
        visibility);
  }

  private String storageKey(
      AttachmentType attachmentType, String publicId, String originalFilename) {
    return attachmentType.name().toLowerCase(Locale.ROOT).replace('_', '-')
        + "/"
        + publicId
        + "/"
        + safeFilename(originalFilename);
  }

  private String safeFilename(String originalFilename) {
    String trimmed = originalFilename == null ? "file" : originalFilename.trim();
    String safe = SAFE_FILENAME_CHARS.matcher(trimmed).replaceAll("_");
    return safe.isBlank() ? "file" : safe;
  }

  private MarketplaceTaskResponse toTaskResponse(MarketplaceTask task) {
    return new MarketplaceTaskResponse(
        task.getPublicId(),
        task.getCustomer().getPublicId(),
        task.getCategory().getPublicId(),
        task.getCategory().getTitle(),
        task.getTitle(),
        task.getDescription(),
        task.getServiceMode().name(),
        task.getPriceType().name(),
        task.getBudgetMin(),
        task.getBudgetMax(),
        task.getFixedPrice(),
        task.getCurrency(),
        task.getCountry().getCode(),
        task.getCountry().getNameRu(),
        task.getRegion() == null ? null : task.getRegion().getPublicId(),
        task.getRegion() == null ? null : task.getRegion().getNameRu(),
        task.getCity().getPublicId(),
        task.getCity().getNameRu(),
        task.getDistrict() == null ? null : task.getDistrict().getPublicId(),
        task.getDistrict() == null ? null : task.getDistrict().getNameRu(),
        task.getAddressText(),
        task.getLatitude(),
        task.getLongitude(),
        task.getStatus().name(),
        task.getPublicationStatus().name(),
        task.getSelectedOffer() == null ? null : task.getSelectedOffer().getPublicId(),
        task.getSelectedPerformer() == null ? null : task.getSelectedPerformer().getPublicId(),
        task.getRepeatOfTask() == null ? null : task.getRepeatOfTask().getPublicId(),
        task.getPreferredPerformer() == null ? null : task.getPreferredPerformer().getPublicId(),
        task.getRevisionCount(),
        task.getExpiresAt(),
        task.getAcceptedAt(),
        task.getCreatedAt(),
        task.getUpdatedAt());
  }

  private TaskOfferResponse toOfferResponse(TaskOffer offer) {
    return new TaskOfferResponse(
        offer.getPublicId(),
        offer.getTask().getPublicId(),
        offer.getTask().getTitle(),
        offer.getTask().getCategory().getPublicId(),
        offer.getTask().getCategory().getTitle(),
        offer.getTask().getCity().getPublicId(),
        offer.getTask().getCity().getNameRu(),
        offer.getPerformer().getPublicId(),
        offer.getPerformer().getDisplayName(),
        offer.getPerformer().getRatingAverage(),
        offer.getPerformer().getRatingCount(),
        offer.getPerformer().getEffectiveVerificationLevel().name(),
        offer.getMessage(),
        offer.getProposedPrice(),
        offer.getCurrency(),
        offer.getEstimatedDuration(),
        offer.isIncludesMaterials(),
        offer.getStatus().name(),
        offer.getExpiresAt(),
        offer.getAcceptedAt(),
        offer.getRejectedAt(),
        offer.getCanceledAt(),
        offer.getCreatedAt(),
        offer.getUpdatedAt());
  }

  private DealResponse toDealResponse(Deal deal, DealChat chat) {
    return new DealResponse(
        deal.getPublicId(),
        deal.getTask().getPublicId(),
        deal.getTask().getTitle(),
        deal.getTask().getCategory().getPublicId(),
        deal.getTask().getCategory().getTitle(),
        deal.getTask().getCity().getPublicId(),
        deal.getTask().getCity().getNameRu(),
        deal.getCustomer().getPublicId(),
        displayName(deal.getCustomer()),
        deal.getPerformer().getPublicId(),
        deal.getPerformer().getDisplayName(),
        deal.getAcceptedOffer().getPublicId(),
        chat.getPublicId(),
        deal.getStatus().name(),
        deal.getPaymentMode().name(),
        deal.getPaymentStatus().name(),
        deal.getContactVisibilityStatus().name(),
        deal.isMilestoneEnabled(),
        deal.getRevisionCount(),
        deal.getCompletedAt(),
        deal.getCanceledAt(),
        deal.getCanceledBy() == null ? null : deal.getCanceledBy().getPublicId(),
        deal.getCancelReason(),
        deal.getCancelComment(),
        deal.isCanceledAfterContactReveal(),
        deal.getCreatedAt(),
        deal.getUpdatedAt());
  }

  private ContactRevealResponse toContactRevealResponse(ContactReveal reveal) {
    return new ContactRevealResponse(
        reveal.getPublicId(),
        reveal.getDeal().getPublicId(),
        reveal.getRequestedBy().getPublicId(),
        reveal.getSubjectUser().getPublicId(),
        reveal.getContactType(),
        reveal.getSubjectUser().getPhone(),
        reveal.getCreatedAt());
  }

  private ContactRevealHistoryResponse toContactRevealHistoryResponse(ContactReveal reveal) {
    return new ContactRevealHistoryResponse(
        reveal.getPublicId(),
        reveal.getDeal().getPublicId(),
        reveal.getRequestedBy().getPublicId(),
        reveal.getSubjectUser().getPublicId(),
        reveal.getContactType(),
        reveal.getCreatedAt());
  }

  private MilestoneResponse toMilestoneResponse(Milestone milestone) {
    return new MilestoneResponse(
        milestone.getPublicId(),
        milestone.getDeal().getPublicId(),
        milestone.getTask().getPublicId(),
        milestone.getTitle(),
        milestone.getDescription(),
        milestone.getAmount(),
        milestone.getCurrency(),
        milestone.getDueDate(),
        milestone.getStatus().name(),
        milestone.getSubmittedAt(),
        milestone.getAcceptedAt(),
        milestone.getRejectedAt(),
        milestone.getCreatedAt(),
        milestone.getUpdatedAt());
  }

  private DealChatResponse toChatResponse(DealChatSummaryProjection chat) {
    return new DealChatResponse(
        chat.getPublicId(),
        chat.getDealId(),
        chat.getTaskId(),
        chat.getTaskTitle(),
        chat.getCustomerId(),
        chat.getCustomerDisplayName(),
        chat.getPerformerId(),
        chat.getPerformerDisplayName(),
        chat.getParticipantRole(),
        chat.getStatus(),
        chat.getLastMessageText(),
        chat.getLastMessageType(),
        chat.getLastMessageAt(),
        chat.getUnreadCount(),
        chat.getClosedAt(),
        chat.getCreatedAt(),
        chat.getUpdatedAt());
  }

  private DisputeCaseResponse toDisputeCaseResponse(DisputeCase disputeCase, DealChat chat) {
    return new DisputeCaseResponse(
        disputeCase.getPublicId(),
        disputeCase.getDeal().getPublicId(),
        disputeCase.getTask().getPublicId(),
        chat.getPublicId(),
        disputeCase.getOpenedByUser().getPublicId(),
        disputeCase.getReason(),
        disputeCase.getDescription(),
        disputeCase.getStatus().name(),
        disputeCase.getAdminDecision(),
        disputeCase.getResolvedAt(),
        disputeCase.getCreatedAt(),
        disputeCase.getUpdatedAt());
  }

  private ChatMessageResponse toChatMessageResponse(ChatMessage message) {
    return new ChatMessageResponse(
        message.getPublicId(),
        message.getChat().getPublicId(),
        message.getSender().getPublicId(),
        displayName(message.getSender()),
        message.getMessageType().name(),
        message.getText(),
        message.getSystemCode(),
        attachmentPublicId(message.getAttachmentId()),
        message.getCreatedAt(),
        message.getReadAt(),
        message.isRiskFlag(),
        message.isRiskFlag() ? "Keep communication and payments inside HandyNest." : null);
  }

  private FeedbackResponse toFeedbackResponse(MarketplaceFeedback feedback) {
    return new FeedbackResponse(
        feedback.getPublicId(),
        feedback.getTask().getPublicId(),
        feedback.getDeal().getPublicId(),
        feedback.getSender().getPublicId(),
        displayName(feedback.getSender()),
        feedback.getReceiver().getPublicId(),
        displayName(feedback.getReceiver()),
        feedback.getDeal().getPerformer().getPublicId(),
        feedback.getGrade(),
        feedback.getText(),
        feedback.isHiddenByAdmin(),
        feedback.getModerationStatus().name(),
        feedback.getCreatedAt());
  }

  private FavoritePerformerResponse toFavoritePerformerResponse(FavoritePerformer favorite) {
    PerformerProfile performer = favorite.getPerformerProfile();
    return new FavoritePerformerResponse(
        performer.getPublicId(),
        performer.getDisplayName(),
        performer.getBaseCity().getPublicId(),
        performer.getBaseCity().getNameRu(),
        performer.getRatingAverage(),
        performer.getRatingCount(),
        performer.isAvailable(),
        favorite.getNote(),
        favorite.getCreatedAt());
  }

  private AttachmentResponse toAttachmentResponse(MarketplaceAttachment attachment) {
    return new AttachmentResponse(
        attachment.getPublicId(),
        attachment.getOwner().getPublicId(),
        attachment.getTask() == null ? null : attachment.getTask().getPublicId(),
        attachment.getChatMessage() == null ? null : attachment.getChatMessage().getPublicId(),
        attachment.getDisputeCase() == null ? null : attachment.getDisputeCase().getPublicId(),
        attachment.getAttachmentType().name(),
        attachment.getOriginalFilename(),
        attachment.getContentType(),
        attachment.getSizeBytes(),
        attachment.getVerificationDocumentType() == null
            ? null
            : attachment.getVerificationDocumentType().name(),
        attachment.getCreatedAt());
  }

  private TaskAttachmentResponse toTaskAttachmentResponse(MarketplaceAttachment attachment) {
    return new TaskAttachmentResponse(
        attachment.getPublicId(),
        attachment.getTask().getPublicId(),
        attachment.getOriginalFilename(),
        attachment.getContentType(),
        attachment.getSizeBytes(),
        attachment.getCreatedAt());
  }

  private AttachmentUploadResponse toAttachmentUploadResponse(MarketplaceAttachment attachment) {
    PresignedStorageUrl uploadUrl = fileStorageService.createUploadUrl(attachment);
    return new AttachmentUploadResponse(
        toAttachmentResponse(attachment),
        uploadUrl.url(),
        uploadUrl.method(),
        uploadUrl.headers(),
        uploadUrl.expiresAt());
  }

  private String attachmentPublicId(Long attachmentId) {
    if (attachmentId == null) {
      return null;
    }
    return attachmentRepository
        .findById(attachmentId)
        .map(MarketplaceAttachment::getPublicId)
        .orElse(null);
  }

  private BigDecimal taskAmount(TaskCreateRequest request) {
    return maxAmount(request.fixedPrice(), request.budgetMax(), request.budgetMin());
  }

  private BigDecimal taskAmount(MarketplaceTask task) {
    return maxAmount(task.getFixedPrice(), task.getBudgetMax(), task.getBudgetMin());
  }

  private BigDecimal maxAmount(BigDecimal... amounts) {
    BigDecimal result = null;
    for (BigDecimal amount : amounts) {
      if (amount != null && (result == null || amount.compareTo(result) > 0)) {
        result = amount;
      }
    }
    return result;
  }

  private String blankToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private String requireIpAddress(String ipAddress) {
    String value = blankToNull(ipAddress);
    return value == null ? "unknown" : value;
  }

  private int safePageSize(int requestedSize) {
    if (requestedSize <= 0) {
      return DEFAULT_PAGE_SIZE;
    }
    return Math.min(requestedSize, MAX_PAGE_SIZE);
  }

  private boolean hasContactRisk(String text) {
    return riskEventService.hasRisk(text);
  }

  private void recordRiskIfNeeded(ChatMessage message) {
    if (message.isRiskFlag()) {
      riskEventService.recordChatMessageRisk(message);
    }
  }

  private String displayName(User user) {
    String fullName =
        (nullToBlank(user.getFirstName()) + " " + nullToBlank(user.getLastName())).trim();
    return fullName.isBlank() ? user.getEmail() : fullName;
  }

  private String nullToBlank(String value) {
    return value == null ? "" : value;
  }
}
