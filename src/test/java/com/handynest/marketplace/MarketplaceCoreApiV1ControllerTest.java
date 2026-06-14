package com.handynest.marketplace;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.handynest.HandyNestProjectApplication;
import com.handynest.identity.RoleName;
import com.handynest.identity.User;
import com.handynest.identity.UserRepository;
import com.handynest.notification.OutboxEventWorker;
import com.handynest.risk.RiskEventRepository;
import com.handynest.risk.RiskEventService;
import com.handynest.testsupport.AuthTestPayloads;
import com.handynest.testsupport.TestDatabaseConfig;
import com.handynest.verification.VerificationAuditAction;
import com.handynest.verification.VerificationAuditEventRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class MarketplaceCoreApiV1ControllerTest {

  private static final String PASSWORD = "Test121314#";
  private static final String TASHKENT_CITY_PUBLIC_ID = "06UZCT00000000000000000001";
  private static final String REPAIR_CATEGORY_PUBLIC_ID = "06CAT000000000000000000011";
  private static final AtomicInteger CLIENT_IP_SEQUENCE = new AtomicInteger(201);

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private UserRepository userRepository;

  @Autowired private OutboxEventWorker outboxEventWorker;

  @Autowired private RiskEventService riskEventService;

  @Autowired private RiskEventRepository riskEventRepository;

  @Autowired private MarketplaceService marketplaceService;

  @Autowired private MarketplaceTaskRepository taskRepository;

  @Autowired private VerificationAuditEventRepository verificationAuditEventRepository;

  @Autowired private TaskOfferRepository offerRepository;

  @Autowired private DealRepository dealRepository;

  @Autowired private DealChatRepository chatRepository;

  @Autowired private ChatMessageRepository chatMessageRepository;

  private MockHttpServletRequestBuilder idempotentPost(String urlTemplate, Object... uriVars) {
    return post(urlTemplate, uriVars).header("Idempotency-Key", idempotencyKey());
  }

  private String idempotencyKey() {
    return "test-" + UUID.randomUUID();
  }

  @Test
  void myTasksReturnsOnlyCurrentCustomersTasksWithPagination() throws Exception {
    String ownerToken = register(newEmail("owner")).get("accessToken").asText();
    String otherToken = register(newEmail("other")).get("accessToken").asText();
    String ownerTaskId = createTask(ownerToken).get("publicId").asText();
    String otherTaskId = createTask(otherToken).get("publicId").asText();

    JsonNode page =
        objectMapper.readTree(
            mockMvc
                .perform(
                    get("/api/v1/my/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .param("status", "OPEN")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].publicId").value(ownerTaskId))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andReturn()
                .getResponse()
                .getContentAsString());

    assertEquals(ownerTaskId, page.get("content").get(0).get("publicId").asText());
    assertTrue(page.toString().contains(ownerTaskId));
    assertTrue(!page.toString().contains(otherTaskId));
  }

  @Test
  void performerCanCancelOwnPendingOfferIdempotently() throws Exception {
    String customerToken = register(newEmail("customer")).get("accessToken").asText();
    String performerToken = register(newEmail("performer")).get("accessToken").asText();
    String otherPerformerToken = register(newEmail("other-performer")).get("accessToken").asText();
    createPerformer(performerToken, "Cancel Offer Pro");
    createPerformer(otherPerformerToken, "Other Offer Pro");
    String taskId = createTask(customerToken).get("publicId").asText();
    String offerId = createOffer(performerToken, taskId);
    String key = idempotencyKey();

    mockMvc
        .perform(
            post("/api/v1/tasks/{taskId}/offers/{offerId}/cancel", taskId, offerId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherPerformerToken)
                .header("Idempotency-Key", idempotencyKey()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    for (int attempt = 0; attempt < 2; attempt++) {
      mockMvc
          .perform(
              post("/api/v1/tasks/{taskId}/offers/{offerId}/cancel", taskId, offerId)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                  .header("Idempotency-Key", key))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.publicId").value(offerId))
          .andExpect(jsonPath("$.status").value("CANCELED"));
    }
  }

  @Test
  void taskImagesExposeSafePublicMetadataAndRespectOwnership() throws Exception {
    String ownerToken = register(newEmail("image-owner")).get("accessToken").asText();
    String otherToken = register(newEmail("image-other")).get("accessToken").asText();
    String taskId = createTask(ownerToken).get("publicId").asText();
    Map<String, Object> metadata =
        Map.of(
            "originalFilename", "kitchen.webp",
            "contentType", "image/webp",
            "sizeBytes", 2048);

    mockMvc
        .perform(
            post("/api/v1/tasks/{taskId}/attachments", taskId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metadata)))
        .andExpect(status().isForbidden());

    JsonNode upload =
        objectMapper.readTree(
            mockMvc
                .perform(
                    post("/api/v1/tasks/{taskId}/attachments", taskId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(metadata)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attachment.publicId").isString())
                .andExpect(jsonPath("$.attachment.taskId").value(taskId))
                .andExpect(jsonPath("$.attachment.bucket").doesNotExist())
                .andExpect(jsonPath("$.attachment.storageKey").doesNotExist())
                .andExpect(jsonPath("$.uploadUrl").isString())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String attachmentId = upload.get("attachment").get("publicId").asText();

    mockMvc
        .perform(get("/api/v1/tasks/{taskId}/attachments", taskId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].publicId").value(attachmentId))
        .andExpect(jsonPath("$[0].bucket").doesNotExist())
        .andExpect(jsonPath("$[0].storageKey").doesNotExist());

    mockMvc
        .perform(
            get(
                "/api/v1/tasks/{taskId}/attachments/{attachmentId}/download-url",
                taskId,
                attachmentId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.attachmentId").value(attachmentId))
        .andExpect(jsonPath("$.downloadUrl").isString());
  }

  @Test
  void taskOfferAcceptCreatesDealAndChatInOneFlow() throws Exception {
    String customerToken = register(newEmail("customer")).get("accessToken").asText();
    String performerToken = register(newEmail("performer")).get("accessToken").asText();
    String performerId =
        createPerformer(performerToken, "Marketplace Pro").get("publicId").asText();

    JsonNode task = createTask(customerToken);
    String taskId = task.get("publicId").asText();

    mockMvc
        .perform(get("/api/v1/tasks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].publicId", hasItem(taskId)))
        .andExpect(jsonPath("$.content[0].id").doesNotExist());

    JsonNode offer =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/tasks/{taskId}/offers", taskId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerPayload())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.taskId").value(taskId))
                .andExpect(jsonPath("$.performerId").value(performerId))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String offerId = offer.get("publicId").asText();

    mockMvc
        .perform(
            get("/api/v1/tasks/{taskId}/offers", taskId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].publicId", hasItem(offerId)));

    JsonNode deal =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost(
                            "/api/v1/tasks/{taskId}/offers/{offerId}/accept", taskId, offerId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.taskId").value(taskId))
                .andExpect(jsonPath("$.acceptedOfferId").value(offerId))
                .andExpect(jsonPath("$.performerId").value(performerId))
                .andExpect(jsonPath("$.chatId").isString())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.paymentMode").value("OFF_PLATFORM"))
                .andExpect(jsonPath("$.paymentStatus").value("NOT_REQUIRED"))
                .andReturn()
                .getResponse()
                .getContentAsString());
    String dealId = deal.get("publicId").asText();
    String chatId = deal.get("chatId").asText();

    mockMvc
        .perform(get("/api/v1/tasks/{taskId}", taskId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
        .andExpect(jsonPath("$.selectedOfferId").value(offerId))
        .andExpect(jsonPath("$.selectedPerformerId").value(performerId));

    mockMvc
        .perform(
            get("/api/v1/deals/{dealId}", dealId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(dealId))
        .andExpect(jsonPath("$.chatId").value(chatId));

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}", chatId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(chatId))
        .andExpect(jsonPath("$.dealId").value(dealId))
        .andExpect(jsonPath("$.status").value("ACTIVE"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks/{taskId}/offers/{offerId}/accept", taskId, offerId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
  }

  @Test
  void contactRevealRequiresVerifiedPhoneAndNeverLeaksItThroughHistoryOrEvents() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();
    String revealKey = "contact-reveal-" + UUID.randomUUID();

    mockMvc
        .perform(
            post("/api/v1/deals/{dealId}/contact-reveals", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .header("Idempotency-Key", revealKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("contactType", "PHONE"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

    String phone = "+998901112233";
    markPhoneVerified(flow.performerToken(), phone);
    JsonNode revealed =
        objectMapper.readTree(
            mockMvc
                .perform(
                    post("/api/v1/deals/{dealId}/contact-reveals", flow.dealId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                        .header("Idempotency-Key", revealKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("contactType", "PHONE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactValue").value(phone))
                .andExpect(jsonPath("$.contactType").value("PHONE"))
                .andReturn()
                .getResponse()
                .getContentAsString());

    mockMvc
        .perform(
            post("/api/v1/deals/{dealId}/contact-reveals", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .header("Idempotency-Key", revealKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("contactType", "PHONE"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(revealed.get("publicId").asText()))
        .andExpect(jsonPath("$.contactValue").value(phone));

    mockMvc
        .perform(
            get("/api/v1/deals/{dealId}/contact-reveals", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
        .andExpect(jsonPath("$[0].publicId").value(revealed.get("publicId").asText()))
        .andExpect(jsonPath("$[0].contactValue").doesNotExist());

    Integer auditLeaks =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM audit_log WHERE metadata_json LIKE ?",
            Integer.class,
            "%" + phone + "%");
    Integer outboxLeaks =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM outbox_event WHERE payload_json LIKE ?",
            Integer.class,
            "%" + phone + "%");
    assertEquals(0, auditLeaks);
    assertEquals(0, outboxLeaks);
  }

  @Test
  void chatAllowsPhoneOnlyAfterRevealAndStillFlagsExternalPayment() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();
    String phone = "+998909998877";
    markPhoneVerified(flow.performerToken(), phone);

    mockMvc
        .perform(
            post("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("text", phone))))
        .andExpect(status().isBadRequest());

    mockMvc
        .perform(
            idempotentPost("/api/v1/deals/{dealId}/contact-reveals", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("contactType", "PHONE"))))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("text", phone))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.riskFlag").value(false));

    mockMvc
        .perform(
            post("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("text", "Оплата вне платформы на карту"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.riskFlag").value(true));
  }

  @Test
  void cancelAfterContactRevealClosesMarketplaceFlowAndCreatesRiskSignals() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();
    markPhoneVerified(flow.performerToken(), "+998907776655");

    mockMvc
        .perform(
            idempotentPost("/api/v1/deals/{dealId}/contact-reveals", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("contactType", "PHONE"))))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            idempotentPost("/api/v1/deals/{dealId}/cancel", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("reason", "OTHER", "comment", "Планы изменились"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELED"))
        .andExpect(jsonPath("$.cancelReason").value("OTHER"))
        .andExpect(jsonPath("$.canceledAfterContactReveal").value(true));

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("READ_ONLY"));

    assertEquals(
        2,
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM risk_event WHERE deal_id = (SELECT id FROM deal WHERE public_id = ?)",
            Integer.class,
            flow.dealId()));
  }

  @Test
  void marketplaceEventsCreateNotificationsAndOutboxDelivery() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("text", "Здравствуйте, начинаю работу"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.messageType").value("TEXT"));

    outboxEventWorker.processDueEvents();

    JsonNode customerNotifications =
        objectMapper.readTree(
            mockMvc
                .perform(
                    get("/api/v1/notifications")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                        .param("unreadOnly", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].type", hasItem("NEW_OFFER")))
                .andExpect(jsonPath("$.content[*].type", hasItem("NEW_CHAT_MESSAGE")))
                .andExpect(jsonPath("$.content[0].id").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String notificationId = customerNotifications.get("content").get(0).get("publicId").asText();

    mockMvc
        .perform(
            get("/api/v1/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].type", hasItem("OFFER_ACCEPTED")));

    mockMvc
        .perform(
            get("/api/v1/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.content[?(@.publicId == '" + notificationId + "')].deliveredAt")
                .isNotEmpty());

    mockMvc
        .perform(
            idempotentPost("/api/v1/notifications/{notificationId}/read", notificationId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(notificationId))
        .andExpect(jsonPath("$.readAt").isNotEmpty());

    mockMvc
        .perform(
            idempotentPost("/api/v1/notifications/read-all")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.updatedCount").value(2));

    mockMvc
        .perform(
            get("/api/v1/notifications")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .param("unreadOnly", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty());
  }

  @Test
  void customerCanCreatePaymentIntentAndWebhookIsIdempotent() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();
    Map<String, Object> paymentPayload =
        Map.of(
            "amount", 30000,
            "currency", "UZS",
            "paymentMode", "ON_PLATFORM_ESCROW");

    JsonNode payment =
        objectMapper.readTree(
            mockMvc
                .perform(
                    post("/api/v1/deals/{dealId}/payments", flow.dealId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                        .header("Idempotency-Key", "payment-create-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentPayload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.dealId").value(flow.dealId()))
                .andExpect(jsonPath("$.amount").value(30000.00))
                .andExpect(jsonPath("$.currency").value("UZS"))
                .andExpect(jsonPath("$.paymentMode").value("ON_PLATFORM_ESCROW"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.pspProvider").value("HANDYNEST_MVP"))
                .andExpect(jsonPath("$.pspPaymentId").isString())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String paymentId = payment.get("publicId").asText();
    String pspPaymentId = payment.get("pspPaymentId").asText();

    mockMvc
        .perform(
            post("/api/v1/deals/{dealId}/payments", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .header("Idempotency-Key", "payment-create-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentPayload)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.publicId").value(paymentId));

    mockMvc
        .perform(
            post("/api/v1/deals/{dealId}/payments", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .header("Idempotency-Key", "payment-create-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "amount", 31000,
                            "currency", "UZS",
                            "paymentMode", "ON_PLATFORM_ESCROW"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_CONFLICT"));

    mockMvc
        .perform(
            get("/api/v1/deals/{dealId}/payments", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].publicId", hasItem(paymentId)));

    Map<String, Object> authorizedWebhook =
        Map.of(
            "pspPaymentId", pspPaymentId,
            "pspReference", "psp-ref-authorized",
            "status", "AUTHORIZED");
    mockMvc
        .perform(
            post("/api/v1/payments/webhooks/{provider}", "HANDYNEST_MVP")
                .header("Idempotency-Key", "webhook-1")
                .header("X-HandyNest-Webhook-Token", "wrong-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorizedWebhook)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    mockMvc
        .perform(
            post("/api/v1/payments/webhooks/{provider}", "HANDYNEST_MVP")
                .header("Idempotency-Key", "webhook-1")
                .header("X-HandyNest-Webhook-Token", "test-payment-webhook-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorizedWebhook)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(paymentId))
        .andExpect(jsonPath("$.status").value("AUTHORIZED"))
        .andExpect(jsonPath("$.authorizedAt").isNotEmpty());

    mockMvc
        .perform(
            post("/api/v1/payments/webhooks/{provider}", "HANDYNEST_MVP")
                .header("Idempotency-Key", "webhook-1")
                .header("X-HandyNest-Webhook-Token", "test-payment-webhook-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authorizedWebhook)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(paymentId))
        .andExpect(jsonPath("$.status").value("AUTHORIZED"));

    mockMvc
        .perform(
            post("/api/v1/payments/webhooks/{provider}", "HANDYNEST_MVP")
                .header("Idempotency-Key", "webhook-1")
                .header("X-HandyNest-Webhook-Token", "test-payment-webhook-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("pspPaymentId", pspPaymentId, "status", "HELD"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_CONFLICT"));

    mockMvc
        .perform(
            post("/api/v1/payments/webhooks/{provider}", "HANDYNEST_MVP")
                .header("Idempotency-Key", "webhook-2")
                .header("X-HandyNest-Webhook-Token", "test-payment-webhook-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("pspPaymentId", pspPaymentId, "status", "HELD"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(paymentId))
        .andExpect(jsonPath("$.status").value("HELD"))
        .andExpect(jsonPath("$.heldAt").isNotEmpty());

    mockMvc
        .perform(
            get("/api/v1/deals/{dealId}", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentMode").value("ON_PLATFORM_ESCROW"))
        .andExpect(jsonPath("$.paymentStatus").value("HELD"));
  }

  @Test
  void customerCanDrivePaymentLifecycleWithIdempotentCommands() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();
    JsonNode payment = createPayment(flow, 42000, "payment-lifecycle-create");
    String paymentId = payment.get("publicId").asText();

    mockMvc
        .perform(
            idempotentPost("/api/v1/payments/{paymentId}/hold", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/payments/{paymentId}/authorize", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    String authorizeKey = idempotencyKey();
    mockMvc
        .perform(
            post("/api/v1/payments/{paymentId}/authorize", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .header("Idempotency-Key", authorizeKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(paymentId))
        .andExpect(jsonPath("$.status").value("AUTHORIZED"))
        .andExpect(jsonPath("$.authorizedAt").isNotEmpty());

    mockMvc
        .perform(
            post("/api/v1/payments/{paymentId}/authorize", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .header("Idempotency-Key", authorizeKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(paymentId))
        .andExpect(jsonPath("$.status").value("AUTHORIZED"));

    mockMvc
        .perform(
            post("/api/v1/payments/{paymentId}/authorize", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .header("Idempotency-Key", authorizeKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(Map.of("failureReason", "different body"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_CONFLICT"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/payments/{paymentId}/hold", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("HELD"))
        .andExpect(jsonPath("$.heldAt").isNotEmpty());

    mockMvc
        .perform(
            idempotentPost("/api/v1/payments/{paymentId}/release", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("RELEASED"))
        .andExpect(jsonPath("$.releasedAt").isNotEmpty());

    mockMvc
        .perform(
            get("/api/v1/deals/{dealId}", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.paymentStatus").value("RELEASED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/payments/{paymentId}/refund", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REFUNDED"))
        .andExpect(jsonPath("$.refundedAt").isNotEmpty());
  }

  @Test
  void customerCanFailPendingPaymentAndAdminCanCancelAuthorizedPayment() throws Exception {
    AcceptedFlow failedFlow = createAcceptedFlow();
    JsonNode failedPayment = createPayment(failedFlow, 18000, "payment-fail-create");

    mockMvc
        .perform(
            idempotentPost(
                    "/api/v1/payments/{paymentId}/fail", failedPayment.get("publicId").asText())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + failedFlow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("failureReason", "Card rejected"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("FAILED"))
        .andExpect(jsonPath("$.failureReason").value("Card rejected"));

    AcceptedFlow canceledFlow = createAcceptedFlow();
    JsonNode canceledPayment = createPayment(canceledFlow, 22000, "payment-cancel-create");
    String paymentId = canceledPayment.get("publicId").asText();
    String adminToken = adminToken();

    mockMvc
        .perform(
            idempotentPost("/api/v1/payments/{paymentId}/authorize", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + canceledFlow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("AUTHORIZED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/payments/{paymentId}/cancel", paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELED"));
  }

  @Test
  void criticalMarketplacePostsAreIdempotent() throws Exception {
    String customerToken = register(newEmail("idempotent-customer")).get("accessToken").asText();
    String performerToken = register(newEmail("idempotent-performer")).get("accessToken").asText();
    String performerId = createPerformer(performerToken, "Idempotent Pro").get("publicId").asText();

    Map<String, Object> taskPayload = taskPayload();
    String taskKey = idempotencyKey();
    JsonNode task =
        objectMapper.readTree(
            mockMvc
                .perform(
                    post("/api/v1/tasks")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                        .header("Idempotency-Key", taskKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String taskId = task.get("publicId").asText();

    mockMvc
        .perform(
            post("/api/v1/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                .header("Idempotency-Key", taskKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskPayload)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.publicId").value(taskId));

    Map<String, Object> changedTaskPayload = taskPayload();
    changedTaskPayload.put("title", "Другой заголовок");
    mockMvc
        .perform(
            post("/api/v1/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                .header("Idempotency-Key", taskKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changedTaskPayload)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_CONFLICT"));

    String offerKey = idempotencyKey();
    JsonNode offer =
        objectMapper.readTree(
            mockMvc
                .perform(
                    post("/api/v1/tasks/{taskId}/offers", taskId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                        .header("Idempotency-Key", offerKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerPayload())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.performerId").value(performerId))
                .andReturn()
                .getResponse()
                .getContentAsString());
    String offerId = offer.get("publicId").asText();

    mockMvc
        .perform(
            post("/api/v1/tasks/{taskId}/offers", taskId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                .header("Idempotency-Key", offerKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(offerPayload())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.publicId").value(offerId));

    String acceptOfferKey = idempotencyKey();
    JsonNode deal =
        objectMapper.readTree(
            mockMvc
                .perform(
                    post("/api/v1/tasks/{taskId}/offers/{offerId}/accept", taskId, offerId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                        .header("Idempotency-Key", acceptOfferKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString());
    String dealId = deal.get("publicId").asText();
    String chatId = deal.get("chatId").asText();

    mockMvc
        .perform(
            post("/api/v1/tasks/{taskId}/offers/{offerId}/accept", taskId, offerId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                .header("Idempotency-Key", acceptOfferKey))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(dealId))
        .andExpect(jsonPath("$.status").value("ACTIVE"));

    Map<String, Object> submitPayload = Map.of("message", "Готово идемпотентно");
    String submitKey = idempotencyKey();
    mockMvc
        .perform(
            post("/api/v1/chats/{chatId}/submit-work", chatId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                .header("Idempotency-Key", submitKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submitPayload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(dealId))
        .andExpect(jsonPath("$.status").value("WORK_SUBMITTED"));

    mockMvc
        .perform(
            post("/api/v1/chats/{chatId}/submit-work", chatId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                .header("Idempotency-Key", submitKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(submitPayload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(dealId))
        .andExpect(jsonPath("$.status").value("WORK_SUBMITTED"));

    mockMvc
        .perform(
            post("/api/v1/chats/{chatId}/submit-work", chatId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                .header("Idempotency-Key", submitKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "Другая работа"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_CONFLICT"));

    Map<String, Object> acceptPayload = Map.of("message", "Принято идемпотентно");
    String acceptWorkKey = idempotencyKey();
    mockMvc
        .perform(
            post("/api/v1/chats/{chatId}/accept-work", chatId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                .header("Idempotency-Key", acceptWorkKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acceptPayload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(dealId))
        .andExpect(jsonPath("$.status").value("COMPLETED"));

    mockMvc
        .perform(
            post("/api/v1/chats/{chatId}/accept-work", chatId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                .header("Idempotency-Key", acceptWorkKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(acceptPayload)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(dealId))
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  void acceptingOneOfferCreatesSingleDealChatAndRejectsOtherPendingOffers() throws Exception {
    String customerToken = register(newEmail("multi-offer-customer")).get("accessToken").asText();
    String performerToken =
        register(newEmail("multi-offer-performer-a")).get("accessToken").asText();
    String secondPerformerToken =
        register(newEmail("multi-offer-performer-b")).get("accessToken").asText();
    createPerformer(performerToken, "Offer Pro A");
    createPerformer(secondPerformerToken, "Offer Pro B");
    String taskId = createTask(customerToken).get("publicId").asText();

    String firstOfferId = createOffer(performerToken, taskId);
    String secondOfferId = createOffer(secondPerformerToken, taskId);

    JsonNode deal =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost(
                            "/api/v1/tasks/{taskId}/offers/{offerId}/accept", taskId, firstOfferId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatId").isString())
                .andReturn()
                .getResponse()
                .getContentAsString());

    mockMvc
        .perform(
            get("/api/v1/tasks/{taskId}/offers", taskId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.publicId == '" + firstOfferId + "')].status").value("ACCEPTED"))
        .andExpect(
            jsonPath("$[?(@.publicId == '" + secondOfferId + "')].status").value("REJECTED"));

    assertEquals(1, offerRepository.countByTaskPublicIdAndStatus(taskId, TaskOfferStatus.ACCEPTED));
    assertEquals(1, dealRepository.countByTaskPublicId(taskId));
    assertEquals(1, chatRepository.countByTaskPublicId(taskId));
    assertTrue(deal.get("publicId").isTextual());
  }

  @Test
  void acceptingSecondOfferAfterDealExistsReturnsConflict() throws Exception {
    String customerToken = register(newEmail("second-accept-customer")).get("accessToken").asText();
    String performerToken =
        register(newEmail("second-accept-performer-a")).get("accessToken").asText();
    String secondPerformerToken =
        register(newEmail("second-accept-performer-b")).get("accessToken").asText();
    createPerformer(performerToken, "Second Accept A");
    createPerformer(secondPerformerToken, "Second Accept B");
    String taskId = createTask(customerToken).get("publicId").asText();
    String firstOfferId = createOffer(performerToken, taskId);
    String secondOfferId = createOffer(secondPerformerToken, taskId);

    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks/{taskId}/offers/{offerId}/accept", taskId, firstOfferId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks/{taskId}/offers/{offerId}/accept", taskId, secondOfferId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
        .andExpect(status().isConflict());
  }

  @Test
  void parallelAcceptanceOfTwoOffersCreatesExactlyOneDealAndChat() throws Exception {
    String customerToken = register(newEmail("parallel-customer")).get("accessToken").asText();
    String performerToken = register(newEmail("parallel-performer-a")).get("accessToken").asText();
    String secondPerformerToken =
        register(newEmail("parallel-performer-b")).get("accessToken").asText();
    createPerformer(performerToken, "Parallel A");
    createPerformer(secondPerformerToken, "Parallel B");
    String taskId = createTask(customerToken).get("publicId").asText();
    String firstOfferId = createOffer(performerToken, taskId);
    String secondOfferId = createOffer(secondPerformerToken, taskId);

    CountDownLatch start = new CountDownLatch(1);
    ExecutorService executorService = Executors.newFixedThreadPool(2);
    List<Future<Integer>> futures = new ArrayList<>();
    futures.add(
        executorService.submit(
            () -> acceptOfferStatus(start, customerToken, taskId, firstOfferId)));
    futures.add(
        executorService.submit(
            () -> acceptOfferStatus(start, customerToken, taskId, secondOfferId)));
    start.countDown();

    List<Integer> statuses =
        List.of(futures.get(0).get(10, TimeUnit.SECONDS), futures.get(1).get(10, TimeUnit.SECONDS));
    executorService.shutdownNow();

    assertEquals(1, statuses.stream().filter(status -> status == 200).count());
    assertEquals(1, statuses.stream().filter(status -> status == 409).count());
    assertEquals(1, offerRepository.countByTaskPublicIdAndStatus(taskId, TaskOfferStatus.ACCEPTED));
    assertEquals(1, dealRepository.countByTaskPublicId(taskId));
    assertEquals(1, chatRepository.countByTaskPublicId(taskId));
  }

  @Test
  void taskOwnerCanCancelOnlyOpenTask() throws Exception {
    String customerToken = register(newEmail("cancel-customer")).get("accessToken").asText();
    String taskId = createTask(customerToken).get("publicId").asText();

    mockMvc
        .perform(
            post("/api/v1/tasks/{taskId}/cancel", taskId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELED"))
        .andExpect(jsonPath("$.publicationStatus").value("UNPUBLISHED"));

    AcceptedFlow acceptedFlow = createAcceptedFlow();
    mockMvc
        .perform(
            post("/api/v1/tasks/{taskId}/cancel", acceptedFlow.taskId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + acceptedFlow.customerToken()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
  }

  @Test
  void adminCanRejectAndPublishTask() throws Exception {
    String customerToken = register(newEmail("moderation-customer")).get("accessToken").asText();
    String taskId = createTask(customerToken).get("publicId").asText();
    String adminToken = adminToken();

    mockMvc
        .perform(
            post("/api/v1/admin/tasks/{taskId}/reject", taskId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reason", "Needs review"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("MODERATION"))
        .andExpect(jsonPath("$.publicationStatus").value("REJECTED_BY_MODERATION"));

    mockMvc
        .perform(
            post("/api/v1/admin/tasks/{taskId}/publish", taskId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("OPEN"))
        .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"));
  }

  @Test
  void moderatorHasSupportReadAccessButCannotRunAdminMutations() throws Exception {
    String moderatorToken = moderatorToken();

    mockMvc
        .perform(
            get("/api/v1/admin/risk-events")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + moderatorToken))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            get("/api/v1/admin/moderation-cases")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + moderatorToken))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            get("/api/v1/admin/disputes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + moderatorToken))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            get("/api/v1/admin/verification-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + moderatorToken))
        .andExpect(status().isOk());

    String customerToken = register(newEmail("moderator-task-owner")).get("accessToken").asText();
    String taskId = createTask(customerToken).get("publicId").asText();
    mockMvc
        .perform(
            post("/api/v1/admin/tasks/{taskId}/reject", taskId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + moderatorToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reason", "Not allowed"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
  }

  @Test
  void expiredTasksAndOffersCannotBeAccepted() throws Exception {
    String customerToken = register(newEmail("expired-customer")).get("accessToken").asText();
    String performerToken = register(newEmail("expired-performer")).get("accessToken").asText();
    createPerformer(performerToken, "Expired Pro");

    String expiredTaskId = createTask(customerToken).get("publicId").asText();
    MarketplaceTask expiredTask = taskRepository.findByPublicId(expiredTaskId).orElseThrow();
    expiredTask.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
    taskRepository.save(expiredTask);
    assertEquals(1, marketplaceService.expireOpenTasksAndOffers(Instant.now()));

    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks/{taskId}/offers", expiredTaskId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(offerPayload())))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

    String activeTaskId = createTask(customerToken).get("publicId").asText();
    String offerId = createOffer(performerToken, activeTaskId);
    TaskOffer expiredOffer = offerRepository.findByPublicId(offerId).orElseThrow();
    expiredOffer.setExpiresAt(Instant.now().minus(1, ChronoUnit.MINUTES));
    offerRepository.save(expiredOffer);

    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks/{taskId}/offers/{offerId}/accept", activeTaskId, offerId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
  }

  @Test
  void revisionFlowResumesDealBeforeResubmissionAndCompletion() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/submit-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "First submission"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("WORK_SUBMITTED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/request-revision", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reason", "Please adjust"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REVISION_REQUESTED"))
        .andExpect(jsonPath("$.revisionCount").value(1));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/submit-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "Second submission"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("WORK_SUBMITTED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/accept-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "Accepted"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  void openDisputeBlocksNormalAcceptanceUntilAdminResolution() throws Exception {
    AcceptedFlow flow = createDisputedFlow("block-accept");

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/accept-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "Try accept disputed"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/admin/disputes/{disputeId}/resolve", flow.disputeId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "status", "RESOLVED_RELEASE",
                            "adminDecision", "Release after review"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("RESOLVED_RELEASE"));

    mockMvc
        .perform(
            get("/api/v1/deals/{dealId}", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  void offerCreationRejectsOwnTaskAndDuplicatePendingOffer() throws Exception {
    String customerToken = register(newEmail("customer")).get("accessToken").asText();
    createPerformer(customerToken, "Customer Performer");
    String taskId = createTask(customerToken).get("publicId").asText();

    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks/{taskId}/offers", taskId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(offerPayload())))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

    String performerToken = register(newEmail("performer")).get("accessToken").asText();
    createPerformer(performerToken, "Duplicate Offer Pro");

    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks/{taskId}/offers", taskId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(offerPayload())))
        .andExpect(status().isCreated());

    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks/{taskId}/offers", taskId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(offerPayload())))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
  }

  @Test
  void createOfferRechecksCategoryVerificationRequirementAtOfferTime() throws Exception {
    String customerToken =
        register(newEmail("runtime-verification-customer")).get("accessToken").asText();
    String performerToken =
        register(newEmail("runtime-verification-performer")).get("accessToken").asText();
    String performerId =
        createPerformer(performerToken, "Runtime Verification Pro").get("publicId").asText();
    String taskId = createTask(customerToken).get("publicId").asText();

    jdbcTemplate.update(
        "UPDATE performer_profile SET verification_level = 'ID_VERIFIED', verification_status = 'APPROVED' "
            + "WHERE public_id = ?",
        performerId);

    jdbcTemplate.update(
        "UPDATE category SET requires_verification_level = 'ID_VERIFIED' WHERE public_id = ?",
        REPAIR_CATEGORY_PUBLIC_ID);

    try {
      mockMvc
          .perform(
              idempotentPost("/api/v1/tasks/{taskId}/offers", taskId)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(offerPayload())))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("VERIFICATION_REQUIRED"))
          .andExpect(jsonPath("$.message").value("Verified phone is required for this category"));
    } finally {
      jdbcTemplate.update(
          "UPDATE category SET requires_verification_level = 'NONE' WHERE public_id = ?",
          REPAIR_CATEGORY_PUBLIC_ID);
    }
  }

  @Test
  void manualApprovalCategoryIsHiddenAndCannotReceiveOffersUntilAdminApproval() throws Exception {
    jdbcTemplate.update(
        "UPDATE category SET requires_manual_approval = TRUE WHERE public_id = ?",
        REPAIR_CATEGORY_PUBLIC_ID);

    try {
      String customerToken =
          register(newEmail("manual-category-customer")).get("accessToken").asText();
      String performerToken =
          register(newEmail("manual-category-performer")).get("accessToken").asText();
      String performerId =
          createPerformer(performerToken, "Manual Category Pro").get("publicId").asText();
      String taskId = createTask(customerToken).get("publicId").asText();

      mockMvc
          .perform(
              get("/api/v1/performers/me")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.categories[0].approvalStatus").value("PENDING"));

      mockMvc
          .perform(get("/api/v1/performers/{performerId}", performerId))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.categories").isEmpty());

      mockMvc
          .perform(
              idempotentPost("/api/v1/tasks/{taskId}/offers", taskId)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(offerPayload())))
          .andExpect(status().isForbidden())
          .andExpect(jsonPath("$.code").value("VERIFICATION_REQUIRED"))
          .andExpect(jsonPath("$.message").value("Category requires manual approval"));

      String adminToken = adminToken();
      mockMvc
          .perform(
              get("/api/v1/admin/performer-categories")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$[*].performerId", hasItem(performerId)));

      mockMvc
          .perform(
              idempotentPost(
                      "/api/v1/admin/performers/{performerId}/categories/{categoryId}/approve",
                      performerId,
                      REPAIR_CATEGORY_PUBLIC_ID)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.approvalStatus").value("APPROVED"));

      mockMvc
          .perform(
              idempotentPost("/api/v1/tasks/{taskId}/offers", taskId)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(offerPayload())))
          .andExpect(status().isCreated());
    } finally {
      jdbcTemplate.update(
          "UPDATE category SET requires_manual_approval = FALSE WHERE public_id = ?",
          REPAIR_CATEGORY_PUBLIC_ID);
    }
  }

  @Test
  void prohibitedCategoryCannotBeUsedForPublicTaskCreation() throws Exception {
    String customerToken =
        register(newEmail("prohibited-category-customer")).get("accessToken").asText();

    jdbcTemplate.update(
        "UPDATE category SET risk_level = 'PROHIBITED' WHERE public_id = ?",
        REPAIR_CATEGORY_PUBLIC_ID);

    try {
      mockMvc
          .perform(
              idempotentPost("/api/v1/tasks")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(objectMapper.writeValueAsString(taskPayload())))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
          .andExpect(jsonPath("$.message").value("Category is prohibited"));
    } finally {
      jdbcTemplate.update(
          "UPDATE category SET risk_level = 'LOW' WHERE public_id = ?", REPAIR_CATEGORY_PUBLIC_ID);
    }
  }

  @Test
  void performerCanSubmitWorkAndCustomerCanAcceptItFromChat() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/submit-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("message", "Работа выполнена, можно проверять"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("WORK_SUBMITTED"));

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].messageType", hasItem("WORK_SUBMITTED")))
        .andExpect(jsonPath("$.content[*].systemCode", hasItem("WORK_SUBMITTED")));

    mockMvc
        .perform(get("/api/v1/tasks/{taskId}", flow.taskId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("WORK_SUBMITTED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/accept-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "Работа принята"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"));

    mockMvc
        .perform(get("/api/v1/tasks/{taskId}", flow.taskId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"));

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("READ_ONLY"));

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].messageType", hasItem("WORK_ACCEPTED")))
        .andExpect(jsonPath("$.content[*].systemCode", hasItem("WORK_ACCEPTED")));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(Map.of("text", "Новые условия после приемки"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/accept-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
  }

  @Test
  void dealMilestonesFollowStateMachineAndBlockCompletionUntilAccepted() throws Exception {
    AcceptedFlow flow = createAcceptedFlow(6_000_000);
    String dueDate = Instant.now().plus(7, ChronoUnit.DAYS).toString();

    JsonNode milestone =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/deals/{dealId}/milestones", flow.dealId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of(
                                    "title", "Первый этап",
                                    "description", "Диагностика и согласование материалов",
                                    "amount", 5000,
                                    "currency", "UZS",
                                    "dueDate", dueDate))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.dealId").value(flow.dealId()))
                .andExpect(jsonPath("$.taskId").value(flow.taskId()))
                .andExpect(jsonPath("$.title").value("Первый этап"))
                .andExpect(jsonPath("$.amount").value(5000))
                .andExpect(jsonPath("$.currency").value("UZS"))
                .andExpect(jsonPath("$.dueDate").value(dueDate))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String milestoneId = milestone.get("publicId").asText();

    mockMvc
        .perform(
            get("/api/v1/deals/{dealId}", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.milestoneEnabled").value(true));

    mockMvc
        .perform(
            get("/api/v1/deals/{dealId}/milestones", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].publicId", hasItem(milestoneId)));

    String outsiderToken = register(newEmail("milestone-outsider")).get("accessToken").asText();
    mockMvc
        .perform(
            get("/api/v1/milestones/{milestoneId}", milestoneId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + outsiderToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/deals/{dealId}/milestones", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("title", "Неверный автор", "amount", 1000, "dueDate", dueDate))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/submit-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(Map.of("message", "Работа готова по сделке"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("WORK_SUBMITTED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/accept-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "Пока нельзя"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/milestones/{milestoneId}/start", milestoneId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/milestones/{milestoneId}/submit", milestoneId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUBMITTED"))
        .andExpect(jsonPath("$.submittedAt").isString());

    mockMvc
        .perform(
            idempotentPost("/api/v1/milestones/{milestoneId}/reject", milestoneId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(Map.of("reason", "Нужно поправить детали"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REJECTED"))
        .andExpect(jsonPath("$.rejectedAt").isString());

    mockMvc
        .perform(
            idempotentPost("/api/v1/milestones/{milestoneId}/start", milestoneId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/milestones/{milestoneId}/submit", milestoneId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUBMITTED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/milestones/{milestoneId}/accept", milestoneId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACCEPTED"))
        .andExpect(jsonPath("$.acceptedAt").isString());

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/accept-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "Теперь принято"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  @Test
  void customerCanRequestRevisionAndPerformerCanSubmitAgain() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/submit-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "Готово"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("WORK_SUBMITTED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/request-revision", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("reason", "Нужно поправить одну деталь"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REVISION_REQUESTED"))
        .andExpect(jsonPath("$.revisionCount").value(1));

    mockMvc
        .perform(get("/api/v1/tasks/{taskId}", flow.taskId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REVISION_REQUESTED"))
        .andExpect(jsonPath("$.revisionCount").value(1));

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].messageType", hasItem("REVISION_REQUESTED")))
        .andExpect(jsonPath("$.content[*].systemCode", hasItem("REVISION_REQUESTED")))
        .andExpect(jsonPath("$.content[*].text", hasItem("Нужно поправить одну деталь")));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/request-revision", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reason", ""))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/submit-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "Доработка готова"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("WORK_SUBMITTED"))
        .andExpect(jsonPath("$.revisionCount").value(1));
  }

  @Test
  void chatParticipantsCanSendListAndMarkMessagesRead() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();
    String text = "Добрый день, уточним детали и время в защищённом чате.";

    JsonNode message =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/chats/{chatId}/messages", flow.chatId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("text", text))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.chatId").value(flow.chatId()))
                .andExpect(jsonPath("$.messageType").value("TEXT"))
                .andExpect(jsonPath("$.text").value(text))
                .andExpect(jsonPath("$.riskFlag").value(false))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString());

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].publicId", hasItem(message.get("publicId").asText())))
        .andExpect(jsonPath("$.content[*].riskFlag", hasItem(false)));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/mark-read", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].readAt").isString());

    String outsiderToken = register(newEmail("outsider")).get("accessToken").asText();
    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + outsiderToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("text", ""))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  void chatTimelineUsesPublicCursorsAndChatSummaryTracksUnreadMessages() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();
    List<String> messageIds = new ArrayList<>();
    for (int index = 1; index <= 3; index++) {
      JsonNode message =
          objectMapper.readTree(
              mockMvc
                  .perform(
                      post("/api/v1/chats/{chatId}/messages", flow.chatId())
                          .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                          .contentType(MediaType.APPLICATION_JSON)
                          .content(
                              objectMapper.writeValueAsString(
                                  Map.of("text", "Timeline message " + index))))
                  .andExpect(status().isOk())
                  .andReturn()
                  .getResponse()
                  .getContentAsString());
      messageIds.add(message.get("publicId").asText());
    }

    JsonNode latest =
        objectMapper.readTree(
            mockMvc
                .perform(
                    get("/api/v1/chats/{chatId}/messages/timeline", flow.chatId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages.length()").value(2))
                .andExpect(jsonPath("$.hasMoreOlder").value(true))
                .andExpect(jsonPath("$.hasMoreNewer").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString());

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}/messages/timeline", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .param("before", latest.get("oldestCursor").asText())
                .param("limit", "2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.messages[0].publicId").value(messageIds.getFirst()));

    mockMvc
        .perform(
            get("/api/v1/chats")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].taskTitle").isString())
        .andExpect(jsonPath("$[0].customerDisplayName").isString())
        .andExpect(jsonPath("$[0].participantRole").value("PERFORMER"))
        .andExpect(jsonPath("$[0].lastMessageText").value("Timeline message 3"))
        .andExpect(jsonPath("$[0].unreadCount").value(3));

    mockMvc
        .perform(
            post("/api/v1/chats/{chatId}/mark-read", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isNoContent());
    mockMvc
        .perform(
            get("/api/v1/chats")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].unreadCount").value(0));
  }

  @Test
  void chatAntifraudCreatesRiskEventsAndAutoModerationForHighRiskMessages() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("text", "Можно обсудить по номеру +998 90 123 45 67"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

    String normalUserToken = register(newEmail("risk-non-admin")).get("accessToken").asText();
    mockMvc
        .perform(
            get("/api/v1/admin/risk-events")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + normalUserToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    String adminToken = adminToken();
    JsonNode highRiskMessage =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/chats/{chatId}/messages", flow.chatId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of(
                                    "text",
                                    "Давайте оплату вне платформы, переведи на карту kaspi"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskFlag").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString());
    String highRiskMessageId = highRiskMessage.get("publicId").asText();

    JsonNode riskEvents =
        objectMapper.readTree(
            mockMvc
                .perform(
                    get("/api/v1/admin/risk-events")
                        .param("riskType", "PAYMENT_OUTSIDE_PLATFORM")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].chatMessageId", hasItem(highRiskMessageId)))
                .andExpect(jsonPath("$[*].severity", hasItem("HIGH")))
                .andReturn()
                .getResponse()
                .getContentAsString());
    String eventId = riskEvents.get(0).get("publicId").asText();

    mockMvc
        .perform(
            get("/api/v1/admin/moderation-cases")
                .param("targetType", "CHAT_MESSAGE")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].targetId", hasItem(highRiskMessageId)))
        .andExpect(jsonPath("$.content[*].priority", hasItem("HIGH")));

    ChatMessage highRiskChatMessage =
        chatMessageRepository.findByPublicIdAndDeletedAtIsNull(highRiskMessageId).orElseThrow();
    riskEventService.recordChatMessageRisk(highRiskChatMessage);
    riskEventService.recordChatMessageRisk(highRiskChatMessage);
    assertEquals(1, riskEventRepository.countByChatMessageId(highRiskChatMessage.getId()));
    Long openCaseCount =
        jdbcTemplate.queryForObject(
            """
                        SELECT COUNT(*)
                        FROM moderation_case
                        WHERE target_type = 'CHAT_MESSAGE'
                          AND target_id = ?
                          AND status IN ('OPEN', 'IN_REVIEW')
                        """,
            Long.class,
            highRiskMessageId);
    assertEquals(1L, openCaseCount);

    mockMvc
        .perform(
            idempotentPost("/api/v1/admin/risk-events/{eventId}/resolve", eventId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("comment", "Проверено вручную"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("RESOLVED"))
        .andExpect(jsonPath("$.resolvedByAdminId").isString())
        .andExpect(jsonPath("$.resolutionComment").value("Проверено вручную"));
  }

  @Test
  void customerRiskScoreIsAuditableReversibleAndCreatesProfileModerationCase() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();
    String customerId = currentUser(flow.customerToken()).get("publicId").asText();

    JsonNode firstMessage =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/chats/{chatId}/messages", flow.chatId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of("text", "Оплата вне платформы, переведи на карту"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("text", "Оплата вне платформы через payme"))))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerRiskScore").value(30.00));

    String adminToken = adminToken();
    mockMvc
        .perform(
            get("/api/v1/admin/moderation-cases")
                .param("targetType", "PROFILE")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].targetId", hasItem(customerId)))
        .andExpect(jsonPath("$.content[*].priority", hasItem("HIGH")));

    JsonNode events =
        objectMapper.readTree(
            mockMvc
                .perform(
                    get("/api/v1/admin/risk-events")
                        .param("riskType", "PAYMENT_OUTSIDE_PLATFORM")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String eventId = null;
    for (JsonNode event : events) {
      if (firstMessage.get("publicId").asText().equals(event.get("chatMessageId").asText())) {
        eventId = event.get("publicId").asText();
        break;
      }
    }
    assertTrue(eventId != null);

    mockMvc
        .perform(
            idempotentPost("/api/v1/admin/risk-events/{eventId}/false-positive", eventId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("comment", "Ложное срабатывание"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("FALSE_POSITIVE"));

    mockMvc
        .perform(
            get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerRiskScore").value(15.00));

    Integer activeAdjustments =
        jdbcTemplate.queryForObject(
            """
                        SELECT COUNT(*)
                        FROM customer_risk_adjustment adjustment
                        JOIN handy_user user_account ON user_account.id = adjustment.user_id
                        WHERE user_account.public_id = ? AND adjustment.reversed_at IS NULL
                        """,
            Integer.class,
            customerId);
    assertEquals(1, activeAdjustments);
  }

  @Test
  void highValueAndHighRiskCustomersAreRestrictedFromCreatingTasks() throws Exception {
    String token = register(newEmail("customer-risk-policy")).get("accessToken").asText();
    JsonNode user = currentUser(token);
    Map<String, Object> highValuePayload = taskPayload();
    highValuePayload.put("fixedPrice", 5000000);

    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(highValuePayload)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("VERIFICATION_REQUIRED"));

    jdbcTemplate.update(
        "UPDATE handy_user SET is_phone_verified = TRUE WHERE public_id = ?",
        user.get("publicId").asText());
    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(highValuePayload)))
        .andExpect(status().isCreated());

    jdbcTemplate.update(
        "UPDATE handy_user SET customer_risk_score = 60.00 WHERE public_id = ?",
        user.get("publicId").asText());
    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskPayload())))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
        .andExpect(
            jsonPath("$.message").value("New task creation is restricted pending risk review"));
  }

  @Test
  void repeatedTaskCancellationsCreateRiskEventAndIncreaseScore() throws Exception {
    String token = register(newEmail("repeat-cancellations")).get("accessToken").asText();
    String thirdTaskId = null;
    for (int index = 0; index < 3; index++) {
      String taskId = createTask(token).get("publicId").asText();
      mockMvc
          .perform(
              post("/api/v1/tasks/{taskId}/cancel", taskId)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("CANCELED"));
      thirdTaskId = taskId;
    }

    mockMvc
        .perform(get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerRiskScore").value(5.00));

    mockMvc
        .perform(
            get("/api/v1/admin/risk-events")
                .param("riskType", "MULTIPLE_CANCELLATIONS")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].taskId", hasItem(thirdTaskId)))
        .andExpect(jsonPath("$[*].severity", hasItem("MEDIUM")));
  }

  @Test
  void customerCannotCreateMoreThanConfiguredActiveTaskLimit() throws Exception {
    String token = register(newEmail("active-task-limit")).get("accessToken").asText();
    for (int index = 0; index < 10; index++) {
      createTask(token);
    }

    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskPayload())))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"))
        .andExpect(jsonPath("$.message").value("Active task limit reached"));
  }

  @Test
  void participantCanOpenDisputeAndItBlocksWorkAcceptance() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/submit-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("message", "Готово, но есть риск спора"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("WORK_SUBMITTED"));

    JsonNode dispute =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/chats/{chatId}/open-dispute", flow.chatId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of(
                                    "reason", "Работа не соответствует договоренности",
                                    "description", "Нужно вмешательство администратора"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.dealId").value(flow.dealId()))
                .andExpect(jsonPath("$.taskId").value(flow.taskId()))
                .andExpect(jsonPath("$.chatId").value(flow.chatId()))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.reason").value("Работа не соответствует договоренности"))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String disputeId = dispute.get("publicId").asText();

    mockMvc
        .perform(get("/api/v1/tasks/{taskId}", flow.taskId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DISPUTED"));

    mockMvc
        .perform(
            get("/api/v1/deals/{dealId}", flow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DISPUTED"));

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].messageType", hasItem("DISPUTE_OPENED")))
        .andExpect(jsonPath("$.content[*].systemCode", hasItem("DISPUTE_OPENED")));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/accept-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

    mockMvc
        .perform(
            get("/api/v1/disputes/{disputeId}", disputeId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(disputeId));

    mockMvc
        .perform(
            get("/api/v1/my/disputes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].publicId", hasItem(disputeId)));

    String outsiderToken = register(newEmail("outsider")).get("accessToken").asText();
    mockMvc
        .perform(
            get("/api/v1/disputes/{disputeId}", disputeId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + outsiderToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/open-dispute", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reason", "Duplicate"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
  }

  @Test
  void adminCanResolveDisputeAndApplyDealOutcome() throws Exception {
    AcceptedFlow releaseFlow = createDisputedFlow("release");
    String normalUserToken = register(newEmail("dispute-non-admin")).get("accessToken").asText();

    mockMvc
        .perform(
            get("/api/v1/admin/disputes")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + normalUserToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    String adminToken = adminToken();
    mockMvc
        .perform(
            get("/api/v1/admin/disputes").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].publicId", hasItem(releaseFlow.disputeId())));

    mockMvc
        .perform(
            post("/api/v1/admin/disputes/{disputeId}/start-review", releaseFlow.disputeId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("comment", "Начинаю проверку"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UNDER_REVIEW"));

    mockMvc
        .perform(
            post(
                    "/api/v1/admin/disputes/{disputeId}/request-customer-evidence",
                    releaseFlow.disputeId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("comment", "Нужны материалы заказчика"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("WAITING_FOR_CUSTOMER"));

    mockMvc
        .perform(
            post(
                    "/api/v1/admin/disputes/{disputeId}/request-performer-evidence",
                    releaseFlow.disputeId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("comment", "Нужны материалы исполнителя"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("WAITING_FOR_PERFORMER"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/admin/disputes/{disputeId}/resolve", releaseFlow.disputeId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "status", "RESOLVED_RELEASE",
                            "adminDecision",
                                "Работа подтверждена, спор закрыт в пользу исполнителя"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("RESOLVED_RELEASE"))
        .andExpect(
            jsonPath("$.adminDecision")
                .value("Работа подтверждена, спор закрыт в пользу исполнителя"))
        .andExpect(jsonPath("$.resolvedAt").isString());

    mockMvc
        .perform(
            get("/api/v1/deals/{dealId}", releaseFlow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + releaseFlow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"));

    mockMvc
        .perform(get("/api/v1/tasks/{taskId}", releaseFlow.taskId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"));

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}", releaseFlow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + releaseFlow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("READ_ONLY"));

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}/messages", releaseFlow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + releaseFlow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].messageType", hasItem("DISPUTE_RESOLVED")))
        .andExpect(jsonPath("$.content[*].systemCode", hasItem("DISPUTE_RESOLVED")));

    mockMvc
        .perform(
            idempotentPost("/api/v1/admin/disputes/{disputeId}/resolve", releaseFlow.disputeId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "status", "RESOLVED_REFUND",
                            "adminDecision", "Повторное решение"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/disputes/{disputeId}/attachments", releaseFlow.disputeId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + releaseFlow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "originalFilename", "late-evidence.jpg",
                            "contentType", "image/jpeg",
                            "sizeBytes", 512))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

    AcceptedFlow refundFlow = createDisputedFlow("refund");
    mockMvc
        .perform(
            idempotentPost("/api/v1/admin/disputes/{disputeId}/resolve", refundFlow.disputeId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "status", "RESOLVED_REFUND",
                            "adminDecision", "Возврат заказчику, результат не принят"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("RESOLVED_REFUND"));

    mockMvc
        .perform(
            get("/api/v1/deals/{dealId}", refundFlow.dealId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + refundFlow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELED"));

    mockMvc
        .perform(get("/api/v1/tasks/{taskId}", refundFlow.taskId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELED"));
  }

  @Test
  void dealParticipantsCanLeaveFeedbackAfterCompletionAndRatingsAreUpdated() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();
    completeWork(flow);

    JsonNode customerFeedback =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/tasks/{taskId}/feedbacks", flow.taskId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of("grade", 5, "text", "Отличная работа, рекомендую"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.taskId").value(flow.taskId()))
                .andExpect(jsonPath("$.dealId").value(flow.dealId()))
                .andExpect(jsonPath("$.performerId").value(flow.performerId()))
                .andExpect(jsonPath("$.grade").value(5))
                .andExpect(jsonPath("$.moderationStatus").value("VISIBLE"))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String performerUserId = customerFeedback.get("receiverId").asText();

    mockMvc
        .perform(get("/api/v1/performers/{performerId}", flow.performerId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ratingAverage").value(5.00))
        .andExpect(jsonPath("$.ratingCount").value(1));

    mockMvc
        .perform(get("/api/v1/tasks/{taskId}/feedbacks", flow.taskId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].publicId", hasItem(customerFeedback.get("publicId").asText())));

    mockMvc
        .perform(get("/api/v1/users/{userId}/feedbacks", performerUserId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].publicId", hasItem(customerFeedback.get("publicId").asText())));

    mockMvc
        .perform(get("/api/v1/performers/{performerId}/feedbacks", flow.performerId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].publicId", hasItem(customerFeedback.get("publicId").asText())));

    JsonNode performerFeedback =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/tasks/{taskId}/feedbacks", flow.taskId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of("grade", 4, "text", "Хороший заказчик"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.grade").value(4))
                .andReturn()
                .getResponse()
                .getContentAsString());
    String customerUserId = performerFeedback.get("receiverId").asText();

    mockMvc
        .perform(
            get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(customerUserId))
        .andExpect(jsonPath("$.customerProfile.ratingAverage").value(4.00))
        .andExpect(jsonPath("$.customerProfile.ratingCount").value(1));

    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks/{taskId}/feedbacks", flow.taskId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("grade", 5))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));
  }

  @Test
  void feedbackRequiresCompletedDealAndParticipant() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();

    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks/{taskId}/feedbacks", flow.taskId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("grade", 5))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

    completeWork(flow);
    String outsiderToken = register(newEmail("outsider")).get("accessToken").asText();

    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks/{taskId}/feedbacks", flow.taskId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + outsiderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("grade", 5))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
  }

  @Test
  void customerCanFavoritePerformerAndRepeatTask() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();

    mockMvc
        .perform(
            idempotentPost("/api/v1/my/favorite-performers/{performerId}", flow.performerId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("note", "Заказать снова"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.performerId").value(flow.performerId()))
        .andExpect(jsonPath("$.displayName").value("Action Flow Pro"))
        .andExpect(jsonPath("$.note").value("Заказать снова"))
        .andExpect(jsonPath("$.id").doesNotExist());

    mockMvc
        .perform(
            get("/api/v1/my/favorite-performers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].performerId", hasItem(flow.performerId())));

    mockMvc
        .perform(
            idempotentPost("/api/v1/my/favorite-performers/{performerId}", flow.performerId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));

    JsonNode repeatedTask =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/tasks/{taskId}/repeat", flow.taskId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.repeatOfTaskId").value(flow.taskId()))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"))
                .andExpect(jsonPath("$.selectedOfferId").doesNotExist())
                .andExpect(jsonPath("$.selectedPerformerId").doesNotExist())
                .andExpect(jsonPath("$.preferredPerformerId").value(flow.performerId()))
                .andReturn()
                .getResponse()
                .getContentAsString());

    mockMvc
        .perform(get("/api/v1/tasks/{taskId}", repeatedTask.get("publicId").asText()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.repeatOfTaskId").value(flow.taskId()))
        .andExpect(jsonPath("$.preferredPerformerId").value(flow.performerId()));

    String outsiderToken = register(newEmail("outsider")).get("accessToken").asText();
    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks/{taskId}/repeat", flow.taskId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + outsiderToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    mockMvc
        .perform(
            delete("/api/v1/my/favorite-performers/{performerId}", flow.performerId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get("/api/v1/my/favorite-performers")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].performerId").isEmpty());
  }

  @Test
  void participantsCanAttachChatFilesAndDisputeEvidence() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();

    JsonNode chatAttachment =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/chats/{chatId}/attachments", flow.chatId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of(
                                    "originalFilename", "estimate.pdf",
                                    "contentType", "application/pdf",
                                    "sizeBytes", 2048,
                                    "checksum", "sha256:chat",
                                    "text", "Смета во вложении"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attachment.publicId").isString())
                .andExpect(jsonPath("$.attachment.taskId").value(flow.taskId()))
                .andExpect(jsonPath("$.attachment.attachmentType").value("CHAT_FILE"))
                .andExpect(jsonPath("$.attachment.storageProvider").doesNotExist())
                .andExpect(jsonPath("$.attachment.bucket").doesNotExist())
                .andExpect(jsonPath("$.attachment.visibility").doesNotExist())
                .andExpect(jsonPath("$.attachment.storageKey").doesNotExist())
                .andExpect(jsonPath("$.attachment.id").doesNotExist())
                .andExpect(jsonPath("$.uploadUrl").isString())
                .andExpect(jsonPath("$.uploadMethod").value("PUT"))
                .andExpect(jsonPath("$.uploadHeaders['Content-Type']").value("application/pdf"))
                .andExpect(jsonPath("$.uploadExpiresAt").isString())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String chatAttachmentId = chatAttachment.get("attachment").get("publicId").asText();

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}/attachments", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].publicId").isEmpty());

    JsonNode completed =
        objectMapper.readTree(
            mockMvc
                .perform(
                    post(
                            "/api/v1/chats/{chatId}/attachments/{attachmentId}/complete",
                            flow.chatId(),
                            chatAttachmentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attachment.publicId").value(chatAttachmentId))
                .andExpect(jsonPath("$.message.messageType").value("ATTACHMENT"))
                .andExpect(jsonPath("$.message.attachmentId").value(chatAttachmentId))
                .andReturn()
                .getResponse()
                .getContentAsString());
    String attachmentMessageId = completed.get("message").get("publicId").asText();

    mockMvc
        .perform(
            post(
                    "/api/v1/chats/{chatId}/attachments/{attachmentId}/complete",
                    flow.chatId(),
                    chatAttachmentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message.publicId").value(attachmentMessageId));

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}/messages", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].messageType", hasItem("ATTACHMENT")))
        .andExpect(jsonPath("$.content[*].attachmentId", hasItem(chatAttachmentId)));

    mockMvc
        .perform(
            get("/api/v1/chats/{chatId}/attachments", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].publicId", hasItem(chatAttachmentId)));

    mockMvc
        .perform(
            idempotentPost("/api/v1/attachments/{attachmentId}/download-url", chatAttachmentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.attachmentId").value(chatAttachmentId))
        .andExpect(jsonPath("$.downloadUrl").isString())
        .andExpect(jsonPath("$.downloadMethod").value("GET"))
        .andExpect(jsonPath("$.downloadExpiresAt").isString());

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/attachments", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "originalFilename", "run.sh",
                            "contentType", "application/x-sh",
                            "sizeBytes", 10))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/submit-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(Map.of("message", "Готово, спорный файл"))))
        .andExpect(status().isOk());

    JsonNode dispute =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/chats/{chatId}/open-dispute", flow.chatId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of("reason", "Нужны доказательства"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String disputeId = dispute.get("publicId").asText();

    JsonNode disputeAttachment =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/disputes/{disputeId}/attachments", disputeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of(
                                    "originalFilename", "before-after.jpg",
                                    "contentType", "image/jpeg",
                                    "sizeBytes", 4096,
                                    "checksum", "sha256:evidence"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attachment.attachmentType").value("DISPUTE_EVIDENCE"))
                .andExpect(jsonPath("$.attachment.disputeCaseId").value(disputeId))
                .andExpect(jsonPath("$.attachment.bucket").doesNotExist())
                .andExpect(jsonPath("$.uploadUrl").isString())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String disputeAttachmentId = disputeAttachment.get("attachment").get("publicId").asText();

    String replayKey = "dispute-evidence-replay-" + UUID.randomUUID();
    String replayPayload =
        objectMapper.writeValueAsString(
            Map.of(
                "originalFilename", "receipt.pdf",
                "contentType", "application/pdf",
                "sizeBytes", 1024));
    JsonNode firstReplay =
        objectMapper.readTree(
            mockMvc
                .perform(
                    post("/api/v1/disputes/{disputeId}/attachments", disputeId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                        .header("Idempotency-Key", replayKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(replayPayload))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString());
    mockMvc
        .perform(
            post("/api/v1/disputes/{disputeId}/attachments", disputeId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .header("Idempotency-Key", replayKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(replayPayload))
        .andExpect(status().isCreated())
        .andExpect(
            jsonPath("$.attachment.publicId")
                .value(firstReplay.get("attachment").get("publicId").asText()));

    mockMvc
        .perform(
            get("/api/v1/disputes/{disputeId}/attachments", disputeId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].publicId", hasItem(disputeAttachmentId)));

    String adminToken = adminToken();
    mockMvc
        .perform(
            get("/api/v1/disputes/{disputeId}", disputeId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.publicId").value(disputeId));
    mockMvc
        .perform(
            get("/api/v1/disputes/{disputeId}/attachments", disputeId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].publicId", hasItem(disputeAttachmentId)));
    mockMvc
        .perform(
            post("/api/v1/attachments/{attachmentId}/download-url", disputeAttachmentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.attachmentId").value(disputeAttachmentId));

    String outsiderToken = register(newEmail("outsider")).get("accessToken").asText();
    mockMvc
        .perform(
            get("/api/v1/disputes/{disputeId}/attachments", disputeId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + outsiderToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
  }

  @Test
  void verificationDocumentsUsePrivateBucketAndAdminOnlyAccess() throws Exception {
    String token = register(newEmail("verification")).get("accessToken").asText();

    JsonNode upload =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/verification/documents/upload-url")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of(
                                    "originalFilename", "id-card.pdf",
                                    "contentType", "application/pdf",
                                    "sizeBytes", 4096,
                                    "checksum", "sha256:verification",
                                    "documentType", "IDENTITY_DOCUMENT"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attachment.publicId").isString())
                .andExpect(jsonPath("$.attachment.attachmentType").value("VERIFICATION_DOCUMENT"))
                .andExpect(jsonPath("$.attachment.visibility").doesNotExist())
                .andExpect(
                    jsonPath("$.attachment.verificationDocumentType").value("IDENTITY_DOCUMENT"))
                .andExpect(jsonPath("$.attachment.bucket").doesNotExist())
                .andExpect(jsonPath("$.uploadUrl").isString())
                .andExpect(jsonPath("$.uploadMethod").value("PUT"))
                .andReturn()
                .getResponse()
                .getContentAsString());
    String attachmentId = upload.get("attachment").get("publicId").asText();

    mockMvc
        .perform(
            get("/api/v1/verification/documents")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].publicId", hasItem(attachmentId)));

    mockMvc
        .perform(
            idempotentPost("/api/v1/attachments/{attachmentId}/download-url", attachmentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/verification/documents/upload-url")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "originalFilename", "malware.exe",
                            "contentType", "application/x-msdownload",
                            "sizeBytes", 100,
                            "documentType", "OTHER"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }

  @Test
  void idVerificationRequiresVerifiedPhoneAndCompleteDocumentSet() throws Exception {
    String performerToken =
        register(newEmail("verification-prerequisites")).get("accessToken").asText();
    createPerformer(performerToken, "Verification Prerequisites Pro");
    String identityId =
        createVerificationDocument(performerToken, "identity.pdf", "IDENTITY_DOCUMENT")
            .get("attachment")
            .get("publicId")
            .asText();
    String selfieId =
        createVerificationDocument(performerToken, "selfie.jpg", "SELFIE")
            .get("attachment")
            .get("publicId")
            .asText();

    mockMvc
        .perform(
            idempotentPost("/api/v1/performers/me/verification-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "requestedLevel",
                            "ID_VERIFIED",
                            "documentIds",
                            List.of(identityId, selfieId)))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("VERIFICATION_REQUIRED"));

    markPhoneVerified(performerToken);

    mockMvc
        .perform(
            idempotentPost("/api/v1/performers/me/verification-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "requestedLevel", "ID_VERIFIED", "documentIds", List.of(identityId)))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(
            jsonPath("$.message").value("ID verification requires IDENTITY_DOCUMENT and SELFIE"));
  }

  @Test
  void performerCanSubmitVerificationRequestAndAdminCanApproveIt() throws Exception {
    String performerToken =
        register(newEmail("verification-performer")).get("accessToken").asText();
    String performerId =
        createPerformer(performerToken, "Verification Pro").get("publicId").asText();
    markPhoneVerified(performerToken);
    String documentId =
        createVerificationDocument(performerToken, "id-card.pdf", "IDENTITY_DOCUMENT")
            .get("attachment")
            .get("publicId")
            .asText();
    String selfieId =
        createVerificationDocument(performerToken, "selfie.jpg", "SELFIE")
            .get("attachment")
            .get("publicId")
            .asText();

    JsonNode request =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/performers/me/verification-requests")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of(
                                    "requestedLevel", "ID_VERIFIED",
                                    "documentIds", List.of(documentId, selfieId),
                                    "comment", "Документы загружены"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.performerId").value(performerId))
                .andExpect(jsonPath("$.requestedLevel").value("ID_VERIFIED"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.documents[*].publicId", hasItem(documentId)))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String requestId = request.get("publicId").asText();

    mockMvc
        .perform(
            get("/api/v1/performers/me/verification-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].publicId", hasItem(requestId)));

    mockMvc
        .perform(
            get("/api/v1/performers/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.verificationStatus").value("PENDING"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/performers/me/verification-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "requestedLevel",
                            "ID_VERIFIED",
                            "documentIds",
                            List.of(
                                createVerificationDocument(
                                        performerToken, "duplicate-id.pdf", "IDENTITY_DOCUMENT")
                                    .get("attachment")
                                    .get("publicId")
                                    .asText(),
                                createVerificationDocument(
                                        performerToken, "duplicate-selfie.jpg", "SELFIE")
                                    .get("attachment")
                                    .get("publicId")
                                    .asText())))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));

    String normalUserToken = register(newEmail("non-admin")).get("accessToken").asText();
    mockMvc
        .perform(
            get("/api/v1/admin/verification-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + normalUserToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    String adminToken = adminToken();
    mockMvc
        .perform(
            get("/api/v1/admin/verification-requests")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[*].publicId", hasItem(requestId)));

    mockMvc
        .perform(
            idempotentPost("/api/v1/admin/verification-requests/{requestId}/approve", requestId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"))
        .andExpect(jsonPath("$.reviewedByUserId").isString());

    mockMvc
        .perform(get("/api/v1/performers/{performerId}", performerId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.verificationLevel").value("ID_VERIFIED"))
        .andExpect(jsonPath("$.verificationStatus").value("APPROVED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/attachments/{attachmentId}/download-url", documentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    mockMvc
        .perform(
            idempotentPost(
                    "/api/v1/admin/verification-requests/{requestId}/documents/{documentId}/download-url",
                    requestId,
                    documentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.attachmentId").value(documentId));
    assertEquals(
        1,
        verificationAuditEventRepository.countByVerificationRequestPublicIdAndAction(
            requestId, VerificationAuditAction.DOCUMENT_DOWNLOAD_URL_ISSUED));

    mockMvc
        .perform(
            idempotentPost("/api/v1/admin/verification-requests/{requestId}/approve", requestId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of())))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
  }

  @Test
  void adminCanRejectVerificationRequestWithReason() throws Exception {
    String performerToken = register(newEmail("reject-performer")).get("accessToken").asText();
    createPerformer(performerToken, "Rejected Verification Pro");
    markPhoneVerified(performerToken);
    String documentId =
        createVerificationDocument(performerToken, "identity.png", "IDENTITY_DOCUMENT")
            .get("attachment")
            .get("publicId")
            .asText();
    String selfieId =
        createVerificationDocument(performerToken, "selfie.png", "SELFIE")
            .get("attachment")
            .get("publicId")
            .asText();

    String requestId =
        objectMapper
            .readTree(
                mockMvc
                    .perform(
                        idempotentPost("/api/v1/performers/me/verification-requests")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                objectMapper.writeValueAsString(
                                    Map.of(
                                        "requestedLevel",
                                        "ID_VERIFIED",
                                        "documentIds",
                                        List.of(documentId, selfieId)))))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("publicId")
            .asText();

    String adminToken = adminToken();
    mockMvc
        .perform(
            idempotentPost("/api/v1/admin/verification-requests/{requestId}/reject", requestId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(Map.of("reason", "Фото документа нечёткое"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REJECTED"))
        .andExpect(jsonPath("$.rejectionReason").value("Фото документа нечёткое"));

    mockMvc
        .perform(
            get("/api/v1/performers/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.verificationStatus").value("REJECTED"))
        .andExpect(jsonPath("$.rejectionReason").value("Фото документа нечёткое"));
  }

  @Test
  void userComplaintCreatesModerationCaseAndAdminCanResolveIt() throws Exception {
    String reporterToken = register(newEmail("complaint-reporter")).get("accessToken").asText();
    String taskOwnerToken =
        register(newEmail("complaint-target-owner")).get("accessToken").asText();
    String taskId = createTask(taskOwnerToken).get("publicId").asText();

    JsonNode complaint =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/complaints")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + reporterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                            objectMapper.writeValueAsString(
                                Map.of(
                                    "targetType", "TASK",
                                    "targetId", taskId,
                                    "reason", "Нарушение правил",
                                    "description", "Проверить описание заказа вручную"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicId").isString())
                .andExpect(jsonPath("$.targetType").value("TASK"))
                .andExpect(jsonPath("$.targetId").value(taskId))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.moderationCaseId").isString())
                .andExpect(jsonPath("$.id").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString());
    String complaintId = complaint.get("publicId").asText();
    String caseId = complaint.get("moderationCaseId").asText();

    mockMvc
        .perform(
            get("/api/v1/my/complaints")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reporterToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].publicId", hasItem(complaintId)));

    String normalUserToken = register(newEmail("moderation-non-admin")).get("accessToken").asText();
    mockMvc
        .perform(
            get("/api/v1/admin/moderation-cases")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + normalUserToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    String adminToken = adminToken();
    mockMvc
        .perform(
            get("/api/v1/admin/moderation-cases")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[*].publicId", hasItem(caseId)));

    mockMvc
        .perform(
            idempotentPost("/api/v1/admin/moderation-cases/{caseId}/start-review", caseId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("IN_REVIEW"))
        .andExpect(jsonPath("$.assignedAdminId").isString());

    String secondAdminToken = adminToken();
    mockMvc
        .perform(
            idempotentPost("/api/v1/admin/moderation-cases/{caseId}/approve", caseId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + secondAdminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("comment", "Чужой case"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/admin/moderation-cases/{caseId}/approve", caseId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("comment", "Проверено"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"))
        .andExpect(jsonPath("$.decision").value("APPROVED"))
        .andExpect(jsonPath("$.decisionComment").value("Проверено"))
        .andExpect(jsonPath("$.resolvedAt").isString());

    mockMvc
        .perform(
            get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + reporterToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.customerRiskScore").value(0.00));

    mockMvc
        .perform(
            get("/api/v1/my/complaints")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reporterToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].status").value("RESOLVED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/admin/moderation-cases/{caseId}/reject", caseId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("comment", "Повторное решение"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
  }

  @Test
  void complaintRequiresExistingTarget() throws Exception {
    String reporterToken =
        register(newEmail("complaint-invalid-target")).get("accessToken").asText();

    mockMvc
        .perform(
            idempotentPost("/api/v1/complaints")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reporterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of(
                            "targetType", "TASK",
                            "targetId", "06INVALIDTARGET000000000000",
                            "reason", "Нарушение правил",
                            "description", "Такой target не должен пройти"))))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("NOT_FOUND"));
  }

  @Test
  void complaintsRejectSelfAndDuplicateTargetsAndCanBeCanceledByReporter() throws Exception {
    String ownerToken = register(newEmail("complaint-owner")).get("accessToken").asText();
    String reporterToken =
        register(newEmail("complaint-second-reporter")).get("accessToken").asText();
    String taskId = createTask(ownerToken).get("publicId").asText();
    String payload =
        objectMapper.writeValueAsString(
            Map.of(
                "targetType", "TASK",
                "targetId", taskId,
                "reason", "Проверить задачу",
                "description", "Подозрительное описание"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/complaints")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));

    String complaintId =
        objectMapper
            .readTree(
                mockMvc
                    .perform(
                        idempotentPost("/api/v1/complaints")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + reporterToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("publicId")
            .asText();

    mockMvc
        .perform(
            idempotentPost("/api/v1/complaints")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reporterToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"));

    mockMvc
        .perform(
            post("/api/v1/complaints/{complaintId}/cancel", complaintId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + reporterToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELED"));
  }

  @Test
  void chatActionsEnforceParticipantRoles() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/submit-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "wrong actor"))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/accept-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken()))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
  }

  @Test
  void thirdRevisionRequestIsBlockedAndCustomerMustOpenDispute() throws Exception {
    AcceptedFlow flow = createAcceptedFlow();

    for (int revision = 1; revision <= 2; revision++) {
      mockMvc
          .perform(
              idempotentPost("/api/v1/chats/{chatId}/submit-work", flow.chatId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      objectMapper.writeValueAsString(Map.of("message", "Submission " + revision))))
          .andExpect(status().isOk());
      mockMvc
          .perform(
              idempotentPost("/api/v1/chats/{chatId}/request-revision", flow.chatId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      objectMapper.writeValueAsString(Map.of("reason", "Revision " + revision))))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.revisionCount").value(revision));
    }

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/submit-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "Third submission"))))
        .andExpect(status().isOk());
    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/request-revision", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("reason", "Third revision"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));
  }

  @Test
  void privateMarketplaceEndpointsRequireAuthentication() throws Exception {
    mockMvc
        .perform(
            idempotentPost("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskPayload())))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

    mockMvc
        .perform(get("/api/v1/my/deals"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  private AcceptedFlow createAcceptedFlow() throws Exception {
    return createAcceptedFlow(11_000);
  }

  private AcceptedFlow createAcceptedFlow(int proposedPrice) throws Exception {
    String customerToken = register(newEmail("customer")).get("accessToken").asText();
    String performerToken = register(newEmail("performer")).get("accessToken").asText();
    String performerId =
        createPerformer(performerToken, "Action Flow Pro").get("publicId").asText();
    String taskId = createTask(customerToken).get("publicId").asText();

    String offerId =
        objectMapper
            .readTree(
                mockMvc
                    .perform(
                        idempotentPost("/api/v1/tasks/{taskId}/offers", taskId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(offerPayload(proposedPrice))))
                    .andExpect(status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("publicId")
            .asText();

    JsonNode deal =
        objectMapper.readTree(
            mockMvc
                .perform(
                    idempotentPost(
                            "/api/v1/tasks/{taskId}/offers/{offerId}/accept", taskId, offerId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

    return new AcceptedFlow(
        customerToken,
        performerToken,
        taskId,
        offerId,
        deal.get("publicId").asText(),
        deal.get("chatId").asText(),
        performerId);
  }

  private AcceptedFlow createDisputedFlow(String prefix) throws Exception {
    AcceptedFlow flow = createAcceptedFlow();
    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/submit-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("message", "Готово для спора " + prefix))))
        .andExpect(status().isOk());

    String disputeId =
        objectMapper
            .readTree(
                mockMvc
                    .perform(
                        idempotentPost("/api/v1/chats/{chatId}/open-dispute", flow.chatId())
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                objectMapper.writeValueAsString(
                                    Map.of(
                                        "reason",
                                        "Спор " + prefix,
                                        "description",
                                        "Нужна оценка администратора"))))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString())
            .get("publicId")
            .asText();

    return flow.withDisputeId(disputeId);
  }

  private void completeWork(AcceptedFlow flow) throws Exception {
    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/submit-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.performerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "Готово"))))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            idempotentPost("/api/v1/chats/{chatId}/accept-work", flow.chatId())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("message", "Принято"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("COMPLETED"));
  }

  private JsonNode createPayment(AcceptedFlow flow, int amount, String idempotencyKeyPrefix)
      throws Exception {
    Map<String, Object> paymentPayload =
        Map.of(
            "amount", amount,
            "currency", "UZS",
            "paymentMode", "ON_PLATFORM_ESCROW");
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/deals/{dealId}/payments", flow.dealId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + flow.customerToken())
                    .header("Idempotency-Key", idempotencyKeyPrefix + "-" + UUID.randomUUID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(paymentPayload)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private String createOffer(String performerToken, String taskId) throws Exception {
    return objectMapper
        .readTree(
            mockMvc
                .perform(
                    idempotentPost("/api/v1/tasks/{taskId}/offers", taskId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + performerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerPayload())))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString())
        .get("publicId")
        .asText();
  }

  private int acceptOfferStatus(
      CountDownLatch start, String customerToken, String taskId, String offerId) throws Exception {
    start.await(5, TimeUnit.SECONDS);
    return mockMvc
        .perform(
            post("/api/v1/tasks/{taskId}/offers/{offerId}/accept", taskId, offerId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + customerToken)
                .header("Idempotency-Key", idempotencyKey()))
        .andReturn()
        .getResponse()
        .getStatus();
  }

  private JsonNode createTask(String accessToken) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                idempotentPost("/api/v1/tasks")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(taskPayload())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.publicId").isString())
            .andExpect(jsonPath("$.categoryId").value(REPAIR_CATEGORY_PUBLIC_ID))
            .andExpect(jsonPath("$.cityId").value(TASHKENT_CITY_PUBLIC_ID))
            .andExpect(jsonPath("$.status").value("OPEN"))
            .andExpect(jsonPath("$.publicationStatus").value("PUBLISHED"))
            .andExpect(jsonPath("$.id").doesNotExist())
            .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private JsonNode currentUser(String accessToken) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                get("/api/v1/users/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private Map<String, Object> taskPayload() {
    return new java.util.LinkedHashMap<>(
        Map.of(
            "title", "Починить розетку",
            "description", "Нужен мастер на небольшую бытовую работу",
            "categoryId", REPAIR_CATEGORY_PUBLIC_ID,
            "serviceMode", "ONSITE",
            "priceType", "FIXED",
            "fixedPrice", 12000,
            "currency", "UZS",
            "cityId", TASHKENT_CITY_PUBLIC_ID,
            "addressText", "Ташкент, тестовый адрес"));
  }

  private Map<String, Object> offerPayload() {
    return offerPayload(11_000);
  }

  private Map<String, Object> offerPayload(int proposedPrice) {
    return Map.of(
        "message", "Готов выполнить сегодня после обеда",
        "proposedPrice", proposedPrice,
        "currency", "UZS",
        "estimatedDuration", "2 hours",
        "includesMaterials", true);
  }

  private JsonNode createPerformer(String accessToken, String displayName) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                idempotentPost("/api/v1/performers/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(performerPayload(displayName))))
            .andExpect(status().isCreated())
            .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private Map<String, Object> performerPayload(String displayName) {
    return new java.util.LinkedHashMap<>(
        Map.of(
            "displayName",
            displayName,
            "description",
            "Профиль исполнителя для marketplace теста",
            "skillsDescription",
            "Ремонт и диагностика",
            "cityId",
            TASHKENT_CITY_PUBLIC_ID,
            "serviceRadiusKm",
            30,
            "worksRemotely",
            false,
            "worksOnsite",
            true,
            "categories",
            List.of(
                Map.of(
                    "categoryId",
                    REPAIR_CATEGORY_PUBLIC_ID,
                    "experienceYears",
                    3,
                    "priceFrom",
                    5000,
                    "priceTo",
                    20000,
                    "currency",
                    "UZS",
                    "primary",
                    true))));
  }

  private JsonNode createVerificationDocument(String accessToken, String originalFilename)
      throws Exception {
    String documentType =
        originalFilename.toLowerCase().contains("selfie") ? "SELFIE" : "IDENTITY_DOCUMENT";
    return createVerificationDocument(accessToken, originalFilename, documentType);
  }

  private JsonNode createVerificationDocument(
      String accessToken, String originalFilename, String documentType) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                idempotentPost("/api/v1/verification/documents/upload-url")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            Map.of(
                                "originalFilename",
                                originalFilename,
                                "contentType",
                                contentTypeFor(originalFilename),
                                "sizeBytes",
                                4096,
                                "checksum",
                                "sha256:" + UUID.randomUUID(),
                                "documentType",
                                documentType))))
            .andExpect(status().isCreated())
            .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private void markPhoneVerified(String accessToken) throws Exception {
    markPhoneVerified(accessToken, "+998900000000");
  }

  private void markPhoneVerified(String accessToken, String phone) throws Exception {
    String userId = currentUser(accessToken).get("publicId").asText();
    jdbcTemplate.update(
        "UPDATE handy_user SET phone = ?, is_phone_verified = TRUE WHERE public_id = ?",
        phone,
        userId);
  }

  private String contentTypeFor(String filename) {
    if (filename.endsWith(".png")) {
      return "image/png";
    }
    if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
      return "image/jpeg";
    }
    if (filename.endsWith(".webp")) {
      return "image/webp";
    }
    return "application/pdf";
  }

  private JsonNode register(String email) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                withClientIp(idempotentPost("/api/v1/auth/register"), newClientIp())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        objectMapper.writeValueAsString(
                            AuthTestPayloads.registrationPayload(
                                "Test", "Marketplace", email, PASSWORD))))
            .andExpect(status().isCreated())
            .andReturn();

    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  private String adminToken() throws Exception {
    String email = newEmail("admin");
    JsonNode auth = register(email);
    User user = userRepository.findByEmail(email).orElseThrow();
    user.getRoles().add(RoleName.ADMIN);
    userRepository.save(user);
    return auth.get("accessToken").asText();
  }

  private String moderatorToken() throws Exception {
    String email = newEmail("moderator");
    JsonNode auth = register(email);
    User user = userRepository.findByEmail(email).orElseThrow();
    user.getRoles().add(RoleName.MODERATOR);
    userRepository.save(user);
    return auth.get("accessToken").asText();
  }

  private MockHttpServletRequestBuilder withClientIp(
      MockHttpServletRequestBuilder requestBuilder, String clientIp) {
    return requestBuilder.header("X-Forwarded-For", clientIp);
  }

  private String newEmail(String prefix) {
    return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 14) + "@e.kz";
  }

  private String newClientIp() {
    return "203.0.113." + CLIENT_IP_SEQUENCE.getAndIncrement();
  }

  private record AcceptedFlow(
      String customerToken,
      String performerToken,
      String taskId,
      String offerId,
      String dealId,
      String chatId,
      String performerId,
      String disputeId) {
    private AcceptedFlow(
        String customerToken,
        String performerToken,
        String taskId,
        String offerId,
        String dealId,
        String chatId,
        String performerId) {
      this(customerToken, performerToken, taskId, offerId, dealId, chatId, performerId, null);
    }

    private AcceptedFlow withDisputeId(String disputeId) {
      return new AcceptedFlow(
          customerToken, performerToken, taskId, offerId, dealId, chatId, performerId, disputeId);
    }
  }
}
