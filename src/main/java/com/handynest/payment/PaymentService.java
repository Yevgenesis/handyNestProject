package com.handynest.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.common.error.AccessDeniedBusinessException;
import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.FeatureDisabledException;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.common.idempotency.IdempotencyDecision;
import com.handynest.common.idempotency.IdempotencyService;
import com.handynest.identity.ConsentType;
import com.handynest.identity.RoleName;
import com.handynest.identity.User;
import com.handynest.identity.UserConsentService;
import com.handynest.identity.UserProfileService;
import com.handynest.marketplace.Deal;
import com.handynest.marketplace.DealRepository;
import com.handynest.marketplace.PaymentMode;
import com.handynest.marketplace.PaymentStatus;
import com.handynest.notification.DomainEventPublisher;
import com.handynest.notification.DomainEventType;
import com.handynest.notification.NotificationType;
import com.handynest.platform.PlatformSettingKey;
import com.handynest.platform.PlatformSettingService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {

  private static final String RESOURCE_PAYMENT_TRANSACTION = "PAYMENT_TRANSACTION";

  private final PaymentTransactionRepository paymentTransactionRepository;
  private final DealRepository dealRepository;
  private final PaymentGateway paymentGateway;
  private final PaymentProperties paymentProperties;
  private final IdempotencyService idempotencyService;
  private final UserProfileService userProfileService;
  private final ObjectMapper objectMapper;
  private final PlatformSettingService platformSettingService;
  private final UserConsentService userConsentService;
  private final DomainEventPublisher domainEventPublisher;

  @Transactional
  public PaymentTransactionResponse createPayment(
      org.springframework.security.core.userdetails.UserDetails userDetails,
      String dealId,
      String idempotencyKey,
      PaymentCreateRequest request) {
    User customer = userProfileService.currentUser(userDetails);
    if (!platformSettingService.booleanValue(PlatformSettingKey.PAYMENTS_ENABLED)) {
      throw new FeatureDisabledException("Payments");
    }
    userConsentService.requireCurrent(customer, ConsentType.PAYMENT_POLICY);
    String requestBody = toJson(request);
    String endpoint = "/api/v1/deals/" + dealId + "/payments";
    IdempotencyDecision decision =
        idempotencyService.begin(customer, idempotencyKey, endpoint, requestBody);
    if (decision.replay()) {
      PaymentTransaction existing =
          paymentTransactionRepository
              .findByIdempotencyKeyAndCustomerId(decision.key().getKey(), customer.getId())
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException("PaymentTransaction", decision.key().getKey()));
      return toResponse(existing);
    }

    Deal deal =
        dealRepository
            .findByPublicIdForUpdate(dealId)
            .orElseThrow(() -> new ResourceNotFoundException("Deal", dealId));
    assertDealCustomer(deal, customer);

    PaymentMode paymentMode =
        request.paymentMode() == null ? PaymentMode.ON_PLATFORM_ESCROW : request.paymentMode();
    if (paymentMode == PaymentMode.OFF_PLATFORM) {
      throw new BadRequestBusinessException("Payment intent requires an on-platform payment mode");
    }
    if (paymentMode == PaymentMode.ON_PLATFORM_ESCROW
        && !platformSettingService.booleanValue(PlatformSettingKey.ESCROW_ENABLED)) {
      throw new FeatureDisabledException("Escrow");
    }

    PaymentTransaction transaction =
        paymentTransactionRepository.save(
            new PaymentTransaction(
                deal,
                request.amount(),
                deal.getTask().getCurrency(),
                paymentMode,
                decision.key().getKey()));
    PaymentGatewayReference gatewayReference = paymentGateway.createPayment(transaction);
    transaction.attachPspReference(
        gatewayReference.provider(),
        gatewayReference.pspPaymentId(),
        gatewayReference.pspReference());
    transaction.transitionTo(PaymentStatus.PENDING, Instant.now(), null);
    deal.updatePayment(paymentMode, PaymentStatus.PENDING);
    publishPaymentEvent(transaction);

    PaymentTransactionResponse response = toResponse(transaction);
    idempotencyService.complete(
        decision.key(),
        201,
        toJson(response),
        RESOURCE_PAYMENT_TRANSACTION,
        transaction.getPublicId());
    return response;
  }

  @Transactional(readOnly = true)
  public List<PaymentTransactionResponse> dealPayments(
      org.springframework.security.core.userdetails.UserDetails userDetails, String dealId) {
    User user = userProfileService.currentUser(userDetails);
    Deal deal =
        dealRepository
            .findByPublicId(dealId)
            .orElseThrow(() -> new ResourceNotFoundException("Deal", dealId));
    assertDealParticipant(deal, user);
    return paymentTransactionRepository.findAllByDealPublicIdOrderByCreatedAtDesc(dealId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Transactional
  public PaymentTransactionResponse applyWebhook(
      String provider, String idempotencyKey, String webhookToken, PaymentWebhookRequest request) {
    verifyWebhookToken(webhookToken);
    String normalizedProvider = normalizeProvider(provider);
    String endpoint = "/api/v1/payments/webhooks/" + normalizedProvider;
    IdempotencyDecision decision =
        idempotencyService.begin(null, idempotencyKey, endpoint, toJson(request));

    PaymentTransaction transaction =
        paymentTransactionRepository
            .findByProviderPaymentIdForUpdate(normalizedProvider, request.pspPaymentId())
            .orElseThrow(
                () -> new ResourceNotFoundException("PaymentTransaction", request.pspPaymentId()));
    if (decision.replay()) {
      return toResponse(transaction);
    }

    transaction.transitionTo(request.status(), Instant.now(), blankToNull(request.failureReason()));
    if (request.pspReference() != null && !request.pspReference().isBlank()) {
      transaction.attachPspReference(
          normalizedProvider, transaction.getPspPaymentId(), request.pspReference().trim());
    }
    transaction.getDeal().updatePayment(transaction.getPaymentMode(), transaction.getStatus());
    publishPaymentEvent(transaction);

    PaymentTransactionResponse response = toResponse(transaction);
    idempotencyService.complete(
        decision.key(),
        200,
        toJson(response),
        RESOURCE_PAYMENT_TRANSACTION,
        transaction.getPublicId());
    return response;
  }

  @Transactional
  public PaymentTransactionResponse authorizePayment(
      org.springframework.security.core.userdetails.UserDetails userDetails,
      String paymentId,
      String idempotencyKey,
      PaymentTransitionRequest request) {
    return transitionPayment(
        userDetails, paymentId, idempotencyKey, PaymentStatus.AUTHORIZED, request);
  }

  @Transactional
  public PaymentTransactionResponse holdPayment(
      org.springframework.security.core.userdetails.UserDetails userDetails,
      String paymentId,
      String idempotencyKey,
      PaymentTransitionRequest request) {
    return transitionPayment(userDetails, paymentId, idempotencyKey, PaymentStatus.HELD, request);
  }

  @Transactional
  public PaymentTransactionResponse releasePayment(
      org.springframework.security.core.userdetails.UserDetails userDetails,
      String paymentId,
      String idempotencyKey,
      PaymentTransitionRequest request) {
    return transitionPayment(
        userDetails, paymentId, idempotencyKey, PaymentStatus.RELEASED, request);
  }

  @Transactional
  public PaymentTransactionResponse refundPayment(
      org.springframework.security.core.userdetails.UserDetails userDetails,
      String paymentId,
      String idempotencyKey,
      PaymentTransitionRequest request) {
    return transitionPayment(
        userDetails, paymentId, idempotencyKey, PaymentStatus.REFUNDED, request);
  }

  @Transactional
  public PaymentTransactionResponse failPayment(
      org.springframework.security.core.userdetails.UserDetails userDetails,
      String paymentId,
      String idempotencyKey,
      PaymentTransitionRequest request) {
    return transitionPayment(userDetails, paymentId, idempotencyKey, PaymentStatus.FAILED, request);
  }

  @Transactional
  public PaymentTransactionResponse cancelPayment(
      org.springframework.security.core.userdetails.UserDetails userDetails,
      String paymentId,
      String idempotencyKey,
      PaymentTransitionRequest request) {
    return transitionPayment(
        userDetails, paymentId, idempotencyKey, PaymentStatus.CANCELED, request);
  }

  public PaymentTransactionResponse toResponse(PaymentTransaction transaction) {
    return new PaymentTransactionResponse(
        transaction.getPublicId(),
        transaction.getDeal().getPublicId(),
        transaction.getTask().getPublicId(),
        transaction.getCustomer().getPublicId(),
        transaction.getPerformer().getPublicId(),
        transaction.getAmount(),
        transaction.getPlatformFeeAmount(),
        transaction.getPerformerAmount(),
        transaction.getCurrency(),
        transaction.getStatus().name(),
        transaction.getPaymentMode().name(),
        transaction.getPspProvider(),
        transaction.getPspPaymentId(),
        transaction.getPspReference(),
        transaction.getFailureReason(),
        transaction.getAuthorizedAt(),
        transaction.getHeldAt(),
        transaction.getReleasedAt(),
        transaction.getRefundedAt(),
        transaction.getCreatedAt(),
        transaction.getUpdatedAt());
  }

  private void assertDealCustomer(Deal deal, User user) {
    if (!deal.getCustomer().getId().equals(user.getId())) {
      throw new AccessDeniedBusinessException("Only deal customer can create payment intents");
    }
  }

  private void assertDealParticipant(Deal deal, User user) {
    boolean customer = deal.getCustomer().getId().equals(user.getId());
    boolean performer = deal.getPerformer().getUser().getId().equals(user.getId());
    if (!customer && !performer) {
      throw new AccessDeniedBusinessException("Only deal participants can access payments");
    }
  }

  private PaymentTransactionResponse transitionPayment(
      org.springframework.security.core.userdetails.UserDetails userDetails,
      String paymentId,
      String idempotencyKey,
      PaymentStatus targetStatus,
      PaymentTransitionRequest request) {
    User user = userProfileService.currentUser(userDetails);
    PaymentTransitionRequest normalizedRequest =
        request == null ? new PaymentTransitionRequest(null) : request;
    String endpoint = "/api/v1/payments/" + paymentId + "/" + transitionAction(targetStatus);
    IdempotencyDecision decision =
        idempotencyService.begin(user, idempotencyKey, endpoint, toJson(normalizedRequest));
    if (decision.replay()) {
      PaymentTransaction existing =
          paymentTransactionRepository
              .findByPublicId(replayPaymentId(decision, paymentId))
              .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", paymentId));
      assertPaymentCustomerOrAdmin(existing, user);
      return toResponse(existing);
    }

    PaymentTransaction transaction =
        paymentTransactionRepository
            .findByPublicIdForUpdate(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("PaymentTransaction", paymentId));
    assertPaymentCustomerOrAdmin(transaction, user);
    transaction.transitionTo(
        targetStatus, Instant.now(), blankToNull(normalizedRequest.failureReason()));
    transaction.getDeal().updatePayment(transaction.getPaymentMode(), transaction.getStatus());
    publishPaymentEvent(transaction);

    PaymentTransactionResponse response = toResponse(transaction);
    idempotencyService.complete(
        decision.key(),
        200,
        toJson(response),
        RESOURCE_PAYMENT_TRANSACTION,
        transaction.getPublicId());
    return response;
  }

  private void assertPaymentCustomerOrAdmin(PaymentTransaction transaction, User user) {
    boolean customer = transaction.getCustomer().getId().equals(user.getId());
    boolean admin = user.getRoles().contains(RoleName.ADMIN);
    if (!customer && !admin) {
      throw new AccessDeniedBusinessException(
          "Only payment customer or admin can change payment status");
    }
  }

  private String replayPaymentId(IdempotencyDecision decision, String fallbackPaymentId) {
    if (RESOURCE_PAYMENT_TRANSACTION.equals(decision.key().getResponseResourceType())
        && decision.key().getResponseResourceId() != null) {
      return decision.key().getResponseResourceId();
    }
    return fallbackPaymentId;
  }

  private String transitionAction(PaymentStatus targetStatus) {
    return switch (targetStatus) {
      case AUTHORIZED -> "authorize";
      case HELD -> "hold";
      case RELEASED -> "release";
      case REFUNDED -> "refund";
      case FAILED -> "fail";
      case CANCELED -> "cancel";
      default ->
          throw new IllegalArgumentException("Unsupported payment transition: " + targetStatus);
    };
  }

  private void publishPaymentEvent(PaymentTransaction transaction) {
    DomainEventType eventType =
        switch (transaction.getStatus()) {
          case PENDING -> DomainEventType.PAYMENT_PENDING;
          case AUTHORIZED -> DomainEventType.PAYMENT_AUTHORIZED;
          case HELD, CAPTURED -> DomainEventType.PAYMENT_HELD;
          case RELEASED -> DomainEventType.PAYMENT_RELEASED;
          case REFUNDED, PARTIALLY_REFUNDED -> DomainEventType.PAYMENT_REFUNDED;
          case FAILED -> DomainEventType.PAYMENT_FAILED;
          case CANCELED -> DomainEventType.PAYMENT_CANCELED;
          case CREATED, NOT_REQUIRED ->
              throw new IllegalStateException("Payment status is not publishable");
        };
    domainEventPublisher.publishToUsers(
        eventType,
        "PaymentTransaction",
        transaction.getPublicId(),
        List.of(transaction.getCustomer(), transaction.getPerformer().getUser()),
        NotificationType.PAYMENT_EVENT,
        "Статус оплаты изменён",
        "Новый статус оплаты: " + transaction.getStatus().name(),
        "PAYMENT_TRANSACTION",
        transaction.getPublicId(),
        Map.of(
            "status",
            transaction.getStatus().name(),
            "dealId",
            transaction.getDeal().getPublicId()));
  }

  private void verifyWebhookToken(String webhookToken) {
    String expectedToken = paymentProperties.getWebhookToken();
    if (expectedToken == null || expectedToken.isBlank()) {
      throw new AccessDeniedBusinessException("Payment webhook token is not configured");
    }
    if (!expectedToken.equals(webhookToken)) {
      throw new AccessDeniedBusinessException("Invalid payment webhook token");
    }
  }

  private String normalizeProvider(String provider) {
    if (provider == null || provider.isBlank()) {
      throw new BadRequestBusinessException("Payment provider is required");
    }
    return provider.trim().toUpperCase();
  }

  private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize payment payload", exception);
    }
  }
}
