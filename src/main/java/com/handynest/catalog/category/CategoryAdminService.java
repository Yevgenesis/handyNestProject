package com.handynest.catalog.category;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.common.error.AccessDeniedBusinessException;
import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.ConflictBusinessException;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.common.idempotency.IdempotencyDecision;
import com.handynest.common.idempotency.IdempotencyService;
import com.handynest.common.publicid.PublicIdGenerator;
import com.handynest.identity.RoleName;
import com.handynest.identity.User;
import com.handynest.identity.UserProfileService;
import com.handynest.performer.PerformerCategory;
import com.handynest.performer.PerformerCategoryRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryAdminService {

  private static final String CREATE_ENDPOINT = "/api/v1/admin/categories";
  private static final String RESOURCE_CATEGORY = "CATEGORY";

  private final CategoryQueryRepository categoryRepository;
  private final CategoryTranslationRepository translationRepository;
  private final CategorySynonymRepository synonymRepository;
  private final CategoryAuditEventRepository auditEventRepository;
  private final PerformerCategoryRepository performerCategoryRepository;
  private final UserProfileService userProfileService;
  private final IdempotencyService idempotencyService;
  private final ObjectMapper objectMapper;

  @Transactional(readOnly = true)
  public List<CategoryAdminResponse> list(
      UserDetails userDetails, CategoryRiskLevel riskLevel, Boolean active) {
    assertAdmin(userProfileService.currentUser(userDetails));
    List<Category> categories =
        categoryRepository.findAllByOrderBySortOrderAscIdAsc().stream()
            .filter(category -> riskLevel == null || category.getRiskLevel() == riskLevel)
            .filter(category -> active == null || category.isActive() == active)
            .toList();
    Map<Long, String> publicIds = publicIdsByInternalId();
    return categories.stream()
        .map(category -> toResponse(category, publicIds.get(category.getParentId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public CategoryAdminResponse find(UserDetails userDetails, String categoryId) {
    assertAdmin(userProfileService.currentUser(userDetails));
    Category category = findCategory(categoryId);
    return toResponse(category, parentPublicId(category));
  }

  @Transactional
  public CategoryAdminResponse create(
      UserDetails userDetails, String idempotencyKey, CategoryAdminUpsertRequest request) {
    User admin = userProfileService.currentUser(userDetails);
    assertAdmin(admin);
    IdempotencyDecision decision =
        idempotencyService.begin(admin, idempotencyKey, CREATE_ENDPOINT, toJson(request));
    if (decision.replay()) {
      String resourceId = decision.key().getResponseResourceId();
      if (!RESOURCE_CATEGORY.equals(decision.key().getResponseResourceType())
          || resourceId == null) {
        throw new ConflictBusinessException("Idempotent category response is unavailable");
      }
      Category existing = findCategory(resourceId);
      return toResponse(existing, parentPublicId(existing));
    }

    validateRequest(request, null);
    if (categoryRepository.existsBySlugIgnoreCase(normalizeSlug(request.slug()))) {
      throw new ConflictBusinessException("Category slug already exists");
    }
    Category parent = findParent(request.parentId());
    String ruTitle = translationName(request.translations(), "ru");
    Category category = categoryRepository.save(newCategory(request, parent, ruTitle));
    replaceTranslations(category, request.translations());
    replaceSynonyms(category, request.synonyms());
    auditEventRepository.save(
        new CategoryAuditEvent(
            category, admin, CategoryAuditAction.CREATED, "category", Instant.now()));

    CategoryAdminResponse response =
        toResponse(category, parent == null ? null : parent.getPublicId());
    idempotencyService.complete(
        decision.key(), 201, toJson(response), RESOURCE_CATEGORY, category.getPublicId());
    return response;
  }

  @Transactional
  public CategoryAdminResponse update(
      UserDetails userDetails, String categoryId, CategoryAdminUpsertRequest request) {
    User admin = userProfileService.currentUser(userDetails);
    assertAdmin(admin);
    Category category =
        categoryRepository
            .findByPublicIdForUpdate(categoryId)
            .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
    validateRequest(request, category);
    if (request.version() == null) {
      throw new BadRequestBusinessException("version is required for category updates");
    }
    if (category.getVersion() != request.version()) {
      throw new ConflictBusinessException("Category was modified by another administrator");
    }
    if (categoryRepository.existsBySlugIgnoreCaseAndPublicIdNot(
        normalizeSlug(request.slug()), categoryId)) {
      throw new ConflictBusinessException("Category slug already exists");
    }

    Category parent = findParent(request.parentId());
    assertNoParentCycle(category, parent);
    CategorySnapshot before = CategorySnapshot.from(category);
    boolean translationsChanged = translationsChanged(category, request.translations());
    boolean synonymsChanged = synonymsChanged(category, request.synonyms());
    boolean requiredReviewBefore = requiresManualReview(category);
    applyConfiguration(category, request, parent, translationName(request.translations(), "ru"));
    replaceTranslations(category, request.translations());
    replaceSynonyms(category, request.synonyms());
    reconcilePerformerAssignments(category, requiredReviewBefore);

    List<String> changedFields = before.changedFields(category);
    if (translationsChanged && !changedFields.contains("translations")) {
      changedFields.add("translations");
    }
    if (synonymsChanged) {
      changedFields.add("synonyms");
    }
    if (!changedFields.isEmpty()) {
      auditEventRepository.save(
          new CategoryAuditEvent(
              category,
              admin,
              CategoryAuditAction.UPDATED,
              String.join(",", changedFields),
              Instant.now()));
    }
    categoryRepository.flush();
    return toResponse(category, parent == null ? null : parent.getPublicId());
  }

  @Transactional(readOnly = true)
  public List<CategoryAuditEventResponse> auditEvents(UserDetails userDetails, String categoryId) {
    assertAdmin(userProfileService.currentUser(userDetails));
    findCategory(categoryId);
    return auditEventRepository.findAllByCategoryPublicIdOrderByCreatedAtDesc(categoryId).stream()
        .map(
            event ->
                new CategoryAuditEventResponse(
                    event.getAction(),
                    event.getActor().getPublicId(),
                    event.getChangedFields(),
                    event.getCreatedAt()))
        .toList();
  }

  private Category newCategory(
      CategoryAdminUpsertRequest request, Category parent, String ruTitle) {
    Category category = new Category();
    category.setPublicId(PublicIdGenerator.defaultGenerator().newUlid());
    applyConfiguration(category, request, parent, ruTitle);
    return category;
  }

  private void applyConfiguration(
      Category category, CategoryAdminUpsertRequest request, Category parent, String ruTitle) {
    category.setTitle(ruTitle);
    category.setParentId(parent == null ? null : parent.getId());
    category.setSlug(normalizeSlug(request.slug()));
    category.setIcon(blankToNull(request.icon()));
    category.setRiskLevel(request.riskLevel());
    category.setServiceMode(request.serviceMode());
    category.setLaunchPhase(request.launchPhase());
    category.setActive(request.active());
    category.setPublicVisible(request.publicVisible());
    category.setSortOrder(request.sortOrder());
    category.setWeight(request.sortOrder());
    category.setRequiresVerificationLevel(request.requiresVerificationLevel());
    category.setRequiresManualApproval(request.requiresManualApproval());
    category.setRequiresLicense(request.requiresLicense());
    category.setAllowsRemote(request.allowsRemote());
    category.setAllowsOnsite(request.allowsOnsite());
    category.setAllowsEscrow(request.allowsEscrow());
    category.setAllowsCash(request.allowsCash());
    category.setAllowsMilestones(request.allowsMilestones());
    category.setAllowsAttachments(request.allowsAttachments());
    category.setAllowsContactReveal(request.allowsContactReveal());
    category.setRequiresOnsiteCoordination(request.requiresOnsiteCoordination());
    category.setContactRevealStage(request.contactRevealStage());
  }

  private void replaceTranslations(Category category, List<CategoryTranslationRequest> requests) {
    translationRepository.deleteAllByCategoryId(category.getId());
    translationRepository.flush();
    translationRepository.saveAll(
        requests.stream()
            .map(
                request ->
                    new CategoryTranslation(
                        category,
                        normalizeLocale(request.locale()),
                        request.name().trim(),
                        blankToNull(request.description())))
            .toList());
  }

  private void replaceSynonyms(Category category, List<CategorySynonymRequest> requests) {
    synonymRepository.deleteAllByCategoryId(category.getId());
    synonymRepository.flush();
    synonymRepository.saveAll(
        requests.stream()
            .map(
                request -> {
                  String value = request.value().trim();
                  return new CategorySynonym(
                      category,
                      normalizeLocale(request.locale()),
                      value,
                      normalizeSearchText(value));
                })
            .toList());
  }

  private void reconcilePerformerAssignments(Category category, boolean requiredReviewBefore) {
    boolean requiredReviewNow = requiresManualReview(category);
    if (requiredReviewBefore == requiredReviewNow) {
      return;
    }
    List<PerformerCategory> assignments =
        performerCategoryRepository.findAllByCategoryId(category.getId());
    if (requiredReviewNow) {
      assignments.forEach(PerformerCategory::requireFreshApproval);
    } else {
      assignments.forEach(PerformerCategory::removeApprovalRequirement);
    }
  }

  private boolean translationsChanged(
      Category category, List<CategoryTranslationRequest> requests) {
    List<String> existing =
        translationRepository.findAllByCategoryIdOrderByLocale(category.getId()).stream()
            .map(
                translation ->
                    translation.getLocale()
                        + "|"
                        + translation.getName()
                        + "|"
                        + Objects.toString(translation.getDescription(), ""))
            .sorted()
            .toList();
    List<String> requested =
        requests.stream()
            .map(
                request ->
                    normalizeLocale(request.locale())
                        + "|"
                        + request.name().trim()
                        + "|"
                        + Objects.toString(blankToNull(request.description()), ""))
            .sorted()
            .toList();
    return !existing.equals(requested);
  }

  private boolean synonymsChanged(Category category, List<CategorySynonymRequest> requests) {
    List<String> existing =
        synonymRepository.findAllByCategoryIdOrderByLocaleAscValueAsc(category.getId()).stream()
            .map(synonym -> synonym.getLocale() + "|" + synonym.getNormalizedValue())
            .sorted()
            .toList();
    List<String> requested =
        requests.stream()
            .map(
                request ->
                    normalizeLocale(request.locale()) + "|" + normalizeSearchText(request.value()))
            .sorted()
            .toList();
    return !existing.equals(requested);
  }

  private void validateRequest(CategoryAdminUpsertRequest request, Category existing) {
    validateTranslations(request.translations());
    validateSynonyms(request.synonyms());
    validateServiceMode(request);
    validateRiskPolicy(request);
    if (request.parentId() != null && request.parentId().isBlank()) {
      throw new BadRequestBusinessException("parentId must be null or a category publicId");
    }
    if (existing != null && existing.getPublicId().equals(request.parentId())) {
      throw new BadRequestBusinessException("Category cannot be its own parent");
    }
  }

  private void validateTranslations(List<CategoryTranslationRequest> translations) {
    Set<String> locales = new HashSet<>();
    for (CategoryTranslationRequest translation : translations) {
      String locale = normalizeLocale(translation.locale());
      if (!locales.add(locale)) {
        throw new BadRequestBusinessException("Duplicate category translation locale: " + locale);
      }
    }
    if (!locales.contains("ru") || !locales.contains("kk")) {
      throw new BadRequestBusinessException(
          "Russian and Kazakh category translations are required");
    }
  }

  private void validateSynonyms(List<CategorySynonymRequest> synonyms) {
    Set<String> unique = new HashSet<>();
    for (CategorySynonymRequest synonym : synonyms) {
      String key = normalizeLocale(synonym.locale()) + ":" + normalizeSearchText(synonym.value());
      if (!unique.add(key)) {
        throw new BadRequestBusinessException("Duplicate category synonym: " + synonym.value());
      }
    }
  }

  private void validateServiceMode(CategoryAdminUpsertRequest request) {
    if (!request.allowsRemote() && !request.allowsOnsite()) {
      throw new BadRequestBusinessException("Category must allow remote or onsite service");
    }
    if (request.serviceMode() == CategoryServiceMode.REMOTE
        && (!request.allowsRemote() || request.allowsOnsite())) {
      throw new BadRequestBusinessException("REMOTE category must allow only remote service");
    }
    if (request.serviceMode() == CategoryServiceMode.ONSITE
        && (!request.allowsOnsite() || request.allowsRemote())) {
      throw new BadRequestBusinessException("ONSITE category must allow only onsite service");
    }
    if (request.serviceMode() == CategoryServiceMode.HYBRID
        && (!request.allowsRemote() || !request.allowsOnsite())) {
      throw new BadRequestBusinessException("HYBRID category must allow remote and onsite service");
    }
  }

  private void validateRiskPolicy(CategoryAdminUpsertRequest request) {
    int verificationRank = verificationRank(request.requiresVerificationLevel());
    if (request.riskLevel() == CategoryRiskLevel.MEDIUM && verificationRank < 1) {
      throw new BadRequestBusinessException(
          "MEDIUM risk category requires phone verification or higher");
    }
    if (request.riskLevel() == CategoryRiskLevel.HIGH && verificationRank < 2) {
      throw new BadRequestBusinessException(
          "HIGH risk category requires ID verification or higher");
    }
    if (request.requiresLicense() && verificationRank < 2) {
      throw new BadRequestBusinessException("Licensed category requires ID verification or higher");
    }
    boolean prohibited =
        request.riskLevel() == CategoryRiskLevel.PROHIBITED
            || request.launchPhase() == CategoryLaunchPhase.PROHIBITED_FOR_MVP
            || request.launchPhase() == CategoryLaunchPhase.PROHIBITED_ALWAYS;
    if (prohibited && request.publicVisible()) {
      throw new BadRequestBusinessException("Prohibited category cannot be publicly visible");
    }
  }

  private int verificationRank(RequiredVerificationLevel level) {
    return switch (level) {
      case NONE -> 0;
      case PHONE_VERIFIED -> 1;
      case ID_VERIFIED -> 2;
      case BUSINESS_VERIFIED -> 3;
    };
  }

  private void assertNoParentCycle(Category category, Category parent) {
    Category current = parent;
    Set<Long> visited = new HashSet<>();
    while (current != null) {
      if (current.getId().equals(category.getId())) {
        throw new BadRequestBusinessException("Category parent would create a cycle");
      }
      if (!visited.add(current.getId()) || current.getParentId() == null) {
        return;
      }
      Long parentId = current.getParentId();
      current =
          categoryRepository
              .findById(parentId)
              .orElseThrow(() -> new ResourceNotFoundException("Category", parentId.toString()));
    }
  }

  private Category findParent(String parentId) {
    if (parentId == null) {
      return null;
    }
    return findCategory(parentId);
  }

  private Category findCategory(String categoryId) {
    return categoryRepository
        .findByPublicId(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
  }

  private String parentPublicId(Category category) {
    return category.getParentId() == null
        ? null
        : categoryRepository
            .findById(category.getParentId())
            .map(Category::getPublicId)
            .orElse(null);
  }

  private Map<Long, String> publicIdsByInternalId() {
    Map<Long, String> result = new HashMap<>();
    categoryRepository
        .findAll()
        .forEach(category -> result.put(category.getId(), category.getPublicId()));
    return result;
  }

  private CategoryAdminResponse toResponse(Category category, String parentId) {
    List<CategoryTranslationResponse> translations =
        translationRepository.findAllByCategoryIdOrderByLocale(category.getId()).stream()
            .map(
                translation ->
                    new CategoryTranslationResponse(
                        translation.getLocale(),
                        translation.getName(),
                        translation.getDescription()))
            .toList();
    List<CategorySynonymResponse> synonyms =
        synonymRepository.findAllByCategoryIdOrderByLocaleAscValueAsc(category.getId()).stream()
            .map(synonym -> new CategorySynonymResponse(synonym.getLocale(), synonym.getValue()))
            .toList();
    return new CategoryAdminResponse(
        category.getPublicId(),
        parentId,
        category.getSlug(),
        category.getIcon(),
        category.getRiskLevel(),
        category.getServiceMode(),
        category.getLaunchPhase(),
        category.isActive(),
        category.isPublicVisible(),
        category.getSortOrder(),
        category.getRequiresVerificationLevel(),
        category.isRequiresManualApproval(),
        category.isRequiresLicense(),
        category.isAllowsRemote(),
        category.isAllowsOnsite(),
        category.isAllowsEscrow(),
        category.isAllowsCash(),
        category.isAllowsMilestones(),
        category.isAllowsAttachments(),
        category.isAllowsContactReveal(),
        category.isRequiresOnsiteCoordination(),
        category.getContactRevealStage(),
        translations,
        synonyms,
        category.getVersion(),
        category.getCreatedAt(),
        category.getUpdatedAt());
  }

  private String translationName(List<CategoryTranslationRequest> translations, String locale) {
    return translations.stream()
        .filter(translation -> locale.equals(normalizeLocale(translation.locale())))
        .findFirst()
        .map(CategoryTranslationRequest::name)
        .map(String::trim)
        .orElseThrow(() -> new BadRequestBusinessException(locale + " translation is required"));
  }

  private boolean requiresManualReview(Category category) {
    return category.isRequiresManualApproval() || category.isRequiresLicense();
  }

  private void assertAdmin(User user) {
    if (!user.getRoles().contains(RoleName.ADMIN)) {
      throw new AccessDeniedBusinessException("Admin role is required");
    }
  }

  private String normalizeSlug(String slug) {
    return slug.trim().toLowerCase(Locale.ROOT);
  }

  static String normalizeSearchText(String value) {
    return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
  }

  private String normalizeLocale(String locale) {
    return locale.trim().toLowerCase(Locale.ROOT);
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Cannot serialize category request", exception);
    }
  }

  private record CategorySnapshot(
      Long parentId,
      String slug,
      String icon,
      CategoryRiskLevel riskLevel,
      CategoryServiceMode serviceMode,
      CategoryLaunchPhase launchPhase,
      boolean active,
      boolean publicVisible,
      int sortOrder,
      RequiredVerificationLevel verificationLevel,
      boolean manualApproval,
      boolean license,
      boolean remote,
      boolean onsite,
      boolean escrow,
      boolean cash,
      boolean milestones,
      boolean attachments,
      boolean contactReveal,
      boolean onsiteCoordination,
      ContactRevealStage contactRevealStage,
      String title) {
    static CategorySnapshot from(Category category) {
      return new CategorySnapshot(
          category.getParentId(),
          category.getSlug(),
          category.getIcon(),
          category.getRiskLevel(),
          category.getServiceMode(),
          category.getLaunchPhase(),
          category.isActive(),
          category.isPublicVisible(),
          category.getSortOrder(),
          category.getRequiresVerificationLevel(),
          category.isRequiresManualApproval(),
          category.isRequiresLicense(),
          category.isAllowsRemote(),
          category.isAllowsOnsite(),
          category.isAllowsEscrow(),
          category.isAllowsCash(),
          category.isAllowsMilestones(),
          category.isAllowsAttachments(),
          category.isAllowsContactReveal(),
          category.isRequiresOnsiteCoordination(),
          category.getContactRevealStage(),
          category.getTitle());
    }

    List<String> changedFields(Category category) {
      List<String> changed = new ArrayList<>();
      add(changed, "parentId", !Objects.equals(parentId, category.getParentId()));
      add(changed, "slug", !Objects.equals(slug, category.getSlug()));
      add(changed, "icon", !Objects.equals(icon, category.getIcon()));
      add(changed, "riskLevel", riskLevel != category.getRiskLevel());
      add(changed, "serviceMode", serviceMode != category.getServiceMode());
      add(changed, "launchPhase", launchPhase != category.getLaunchPhase());
      add(changed, "active", active != category.isActive());
      add(changed, "publicVisible", publicVisible != category.isPublicVisible());
      add(changed, "sortOrder", sortOrder != category.getSortOrder());
      add(
          changed,
          "requiresVerificationLevel",
          verificationLevel != category.getRequiresVerificationLevel());
      add(changed, "requiresManualApproval", manualApproval != category.isRequiresManualApproval());
      add(changed, "requiresLicense", license != category.isRequiresLicense());
      add(changed, "allowsRemote", remote != category.isAllowsRemote());
      add(changed, "allowsOnsite", onsite != category.isAllowsOnsite());
      add(changed, "allowsEscrow", escrow != category.isAllowsEscrow());
      add(changed, "allowsCash", cash != category.isAllowsCash());
      add(changed, "allowsMilestones", milestones != category.isAllowsMilestones());
      add(changed, "allowsAttachments", attachments != category.isAllowsAttachments());
      add(changed, "allowsContactReveal", contactReveal != category.isAllowsContactReveal());
      add(
          changed,
          "requiresOnsiteCoordination",
          onsiteCoordination != category.isRequiresOnsiteCoordination());
      add(changed, "contactRevealStage", contactRevealStage != category.getContactRevealStage());
      add(changed, "translations", !Objects.equals(title, category.getTitle()));
      return changed;
    }

    private static void add(List<String> changed, String field, boolean condition) {
      if (condition) {
        changed.add(field);
      }
    }
  }
}
