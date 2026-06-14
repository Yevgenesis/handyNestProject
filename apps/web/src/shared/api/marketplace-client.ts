import type { operations } from "@/shared/api/generated/schema";
import { mapApiErrorPayload } from "@/shared/api/errors";
import { apiClient } from "@/shared/api/session-client";

type MyTasksQuery = NonNullable<
  operations["listMyMarketplaceTasks"]["parameters"]["query"]
>;

function requireData<T>(result: {
  data?: T;
  error?: unknown;
  response: Response;
}): T {
  if (result.data !== undefined) return result.data;
  throw mapApiErrorPayload(result.response, result.error);
}

export function newIdempotencyKey(prefix: string) {
  return `${prefix}-${crypto.randomUUID()}`;
}

export async function getMarketConfig() {
  return requireData(await apiClient.GET("/api/v1/market/config", {}));
}

export async function getMyTasks(query: MyTasksQuery = {}) {
  return requireData(
    await apiClient.GET("/api/v1/my/tasks", { params: { query } }),
  );
}

export async function getTask(taskId: string) {
  return requireData(
    await apiClient.GET("/api/v1/tasks/{taskId}", {
      params: { path: { taskId } },
    }),
  );
}

export async function createTask(
  body: operations["createMarketplaceTask"]["requestBody"]["content"]["application/json"],
  idempotencyKey: string,
) {
  return requireData(
    await apiClient.POST("/api/v1/tasks", {
      body,
      params: { header: { "Idempotency-Key": idempotencyKey } },
    }),
  );
}

export async function cancelTask(taskId: string) {
  return requireData(
    await apiClient.POST("/api/v1/tasks/{taskId}/cancel", {
      params: { path: { taskId } },
    }),
  );
}

export async function repeatTask(taskId: string, idempotencyKey: string) {
  return requireData(
    await apiClient.POST("/api/v1/tasks/{taskId}/repeat", {
      params: {
        path: { taskId },
        header: { "Idempotency-Key": idempotencyKey },
      },
    }),
  );
}

export async function getTaskOffers(taskId: string) {
  return requireData(
    await apiClient.GET("/api/v1/tasks/{taskId}/offers", {
      params: { path: { taskId } },
    }),
  );
}

export async function createOffer(
  taskId: string,
  body: operations["createTaskOffer"]["requestBody"]["content"]["application/json"],
  idempotencyKey: string,
) {
  return requireData(
    await apiClient.POST("/api/v1/tasks/{taskId}/offers", {
      body,
      params: {
        path: { taskId },
        header: { "Idempotency-Key": idempotencyKey },
      },
    }),
  );
}

export async function acceptOffer(
  taskId: string,
  offerId: string,
  idempotencyKey: string,
) {
  return requireData(
    await apiClient.POST("/api/v1/tasks/{taskId}/offers/{offerId}/accept", {
      params: {
        path: { taskId, offerId },
        header: { "Idempotency-Key": idempotencyKey },
      },
    }),
  );
}

export async function cancelOffer(
  taskId: string,
  offerId: string,
  idempotencyKey: string,
) {
  return requireData(
    await apiClient.POST("/api/v1/tasks/{taskId}/offers/{offerId}/cancel", {
      params: {
        path: { taskId, offerId },
        header: { "Idempotency-Key": idempotencyKey },
      },
    }),
  );
}

export async function getMyOffers() {
  return requireData(await apiClient.GET("/api/v1/my/offers"));
}

export async function getMyDeals() {
  return requireData(await apiClient.GET("/api/v1/my/deals"));
}

export async function getDeal(dealId: string) {
  return requireData(
    await apiClient.GET("/api/v1/deals/{dealId}", {
      params: { path: { dealId } },
    }),
  );
}

export async function getTaskFeedbacks(taskId: string) {
  return requireData(
    await apiClient.GET("/api/v1/tasks/{taskId}/feedbacks", {
      params: { path: { taskId } },
    }),
  );
}

export async function createTaskFeedback(
  taskId: string,
  body: operations["createTaskFeedback"]["requestBody"]["content"]["application/json"],
  idempotencyKey: string,
) {
  return requireData(
    await apiClient.POST("/api/v1/tasks/{taskId}/feedbacks", {
      body,
      params: {
        path: { taskId },
        header: { "Idempotency-Key": idempotencyKey },
      },
    }),
  );
}

export async function getNotifications(
  query: { unreadOnly?: boolean; page?: number; size?: number } = {},
) {
  return requireData(
    await apiClient.GET("/api/v1/notifications", { params: { query } }),
  );
}

export async function markNotificationRead(notificationId: string) {
  return requireData(
    await apiClient.POST("/api/v1/notifications/{notificationId}/read", {
      params: { path: { notificationId } },
    }),
  );
}

export async function markAllNotificationsRead() {
  return requireData(
    await apiClient.POST("/api/v1/notifications/read-all", {}),
  );
}

export async function revealDealContact(
  dealId: string,
  idempotencyKey: string,
) {
  return requireData(
    await apiClient.POST("/api/v1/deals/{dealId}/contact-reveals", {
      params: {
        path: { dealId },
        header: { "Idempotency-Key": idempotencyKey },
      },
      body: { contactType: "PHONE" },
    }),
  );
}

export async function cancelDeal(
  dealId: string,
  body: {
    reason:
      | "CUSTOMER_CHANGED_MIND"
      | "PERFORMER_NOT_RESPONDING"
      | "CUSTOMER_NOT_RESPONDING"
      | "PRICE_NOT_ACCEPTED"
      | "SCHEDULE_NOT_ACCEPTED"
      | "TASK_NO_LONGER_ACTUAL"
      | "WRONG_PERFORMER_SELECTED"
      | "SAFETY_CONCERN"
      | "DUPLICATE_TASK"
      | "OTHER";
    comment?: string;
  },
  idempotencyKey: string,
) {
  return requireData(
    await apiClient.POST("/api/v1/deals/{dealId}/cancel", {
      params: {
        path: { dealId },
        header: { "Idempotency-Key": idempotencyKey },
      },
      body,
    }),
  );
}

export async function getMyChats() {
  return requireData(await apiClient.GET("/api/v1/chats"));
}

export async function getChat(chatId: string) {
  return requireData(
    await apiClient.GET("/api/v1/chats/{chatId}", {
      params: { path: { chatId } },
    }),
  );
}

export async function getChatTimeline(
  chatId: string,
  query: { before?: string; after?: string; limit?: number } = {},
) {
  return requireData(
    await apiClient.GET("/api/v1/chats/{chatId}/messages/timeline", {
      params: { path: { chatId }, query },
    }),
  );
}

export async function sendChatMessage(chatId: string, text: string) {
  return requireData(
    await apiClient.POST("/api/v1/chats/{chatId}/messages", {
      params: { path: { chatId } },
      body: { text },
    }),
  );
}

export async function markChatRead(chatId: string) {
  const result = await apiClient.POST("/api/v1/chats/{chatId}/mark-read", {
    params: { path: { chatId } },
  });
  if (!result.response.ok)
    throw mapApiErrorPayload(result.response, result.error);
}

export async function uploadChatAttachment(chatId: string, file: File) {
  const upload = requireData(
    await apiClient.POST("/api/v1/chats/{chatId}/attachments", {
      params: { path: { chatId } },
      body: {
        originalFilename: file.name,
        contentType: file.type || "application/octet-stream",
        sizeBytes: file.size,
      },
    }),
  );
  if (!upload.uploadUrl || !upload.attachment?.publicId) {
    throw new Error("Upload contract is incomplete");
  }
  const uploaded = await fetch(upload.uploadUrl, {
    method: upload.uploadMethod ?? "PUT",
    headers: upload.uploadHeaders,
    body: file,
  });
  if (!uploaded.ok) throw new Error(`File upload failed: ${uploaded.status}`);
  return requireData(
    await apiClient.POST(
      "/api/v1/chats/{chatId}/attachments/{attachmentId}/complete",
      {
        params: {
          path: { chatId, attachmentId: upload.attachment.publicId },
        },
      },
    ),
  );
}

export async function getAttachmentDownloadUrl(attachmentId: string) {
  return requireData(
    await apiClient.POST("/api/v1/attachments/{attachmentId}/download-url", {
      params: { path: { attachmentId } },
    }),
  );
}

export async function submitWork(
  chatId: string,
  message: string,
  idempotencyKey: string,
) {
  return requireData(
    await apiClient.POST("/api/v1/chats/{chatId}/submit-work", {
      params: {
        path: { chatId },
        header: { "Idempotency-Key": idempotencyKey },
      },
      body: { message: message || undefined },
    }),
  );
}

export async function acceptWork(
  chatId: string,
  message: string,
  idempotencyKey: string,
) {
  return requireData(
    await apiClient.POST("/api/v1/chats/{chatId}/accept-work", {
      params: {
        path: { chatId },
        header: { "Idempotency-Key": idempotencyKey },
      },
      body: { message: message || undefined },
    }),
  );
}

export async function requestRevision(
  chatId: string,
  reason: string,
  idempotencyKey: string,
) {
  return requireData(
    await apiClient.POST("/api/v1/chats/{chatId}/request-revision", {
      params: {
        path: { chatId },
        header: { "Idempotency-Key": idempotencyKey },
      },
      body: { reason },
    }),
  );
}

export async function openDispute(
  chatId: string,
  body: { reason: string; description?: string },
  idempotencyKey: string,
) {
  return requireData(
    await apiClient.POST("/api/v1/chats/{chatId}/open-dispute", {
      params: {
        path: { chatId },
        header: { "Idempotency-Key": idempotencyKey },
      },
      body,
    }),
  );
}

export async function getPerformerMe() {
  return requireData(await apiClient.GET("/api/v1/performers/me"));
}

export async function savePerformerProfile(
  body: operations["createMe"]["requestBody"]["content"]["application/json"],
  exists: boolean,
) {
  const result = exists
    ? await apiClient.PATCH("/api/v1/performers/me", { body })
    : await apiClient.POST("/api/v1/performers/me", { body });
  return requireData(result);
}

export async function updatePerformerAvailability(available: boolean) {
  return requireData(
    await apiClient.POST("/api/v1/performers/me/availability", {
      body: { available },
    }),
  );
}

export async function getUserConsents() {
  return requireData(await apiClient.GET("/api/v1/users/me/consents"));
}

export async function acceptUserConsents(
  body: operations["acceptUserConsents"]["requestBody"]["content"]["application/json"],
) {
  return requireData(
    await apiClient.POST("/api/v1/users/me/consents", { body }),
  );
}

export async function getCategoriesClient(locale = "ru") {
  return requireData(
    await apiClient.GET("/api/v1/categories", {
      params: { query: { locale } },
    }),
  );
}

export async function getCitiesClient(locale = "ru") {
  return requireData(
    await apiClient.GET("/api/v1/geo/cities", {
      params: { query: { locale } },
    }),
  );
}

export async function getDistrictsClient(cityId: string, locale = "ru") {
  return requireData(
    await apiClient.GET("/api/v1/geo/cities/{cityId}/districts", {
      params: { path: { cityId }, query: { locale } },
    }),
  );
}

export async function getTaskAttachments(taskId: string) {
  return requireData(
    await apiClient.GET("/api/v1/tasks/{taskId}/attachments", {
      params: { path: { taskId } },
    }),
  );
}

export async function uploadTaskImage(taskId: string, file: File) {
  const metadata = requireData(
    await apiClient.POST("/api/v1/tasks/{taskId}/attachments", {
      params: { path: { taskId } },
      body: {
        originalFilename: file.name,
        contentType: file.type,
        sizeBytes: file.size,
      },
    }),
  );
  if (!metadata.uploadUrl) throw new Error("Upload URL is missing");
  const response = await fetch(metadata.uploadUrl, {
    method: metadata.uploadMethod ?? "PUT",
    headers: metadata.uploadHeaders,
    body: file,
  });
  if (!response.ok) throw new Error(`File upload failed: ${response.status}`);
  return metadata.attachment;
}

export async function getTaskAttachmentDownloadUrl(
  taskId: string,
  attachmentId: string,
) {
  return requireData(
    await apiClient.GET(
      "/api/v1/tasks/{taskId}/attachments/{attachmentId}/download-url",
      { params: { path: { taskId, attachmentId } } },
    ),
  );
}

export async function getVerificationDocuments() {
  return requireData(await apiClient.GET("/api/v1/verification/documents"));
}

export async function createVerificationDocumentUpload(
  body: operations["createVerificationDocumentUploadUrl"]["requestBody"]["content"]["application/json"],
) {
  return requireData(
    await apiClient.POST("/api/v1/verification/documents/upload-url", { body }),
  );
}

export async function uploadVerificationDocument(
  file: File,
  documentType: "IDENTITY_DOCUMENT" | "SELFIE",
) {
  const metadata = await createVerificationDocumentUpload({
    originalFilename: file.name,
    contentType: file.type,
    sizeBytes: file.size,
    documentType,
  });
  if (!metadata.uploadUrl) throw new Error("Upload URL is missing");
  const response = await fetch(metadata.uploadUrl, {
    method: metadata.uploadMethod ?? "PUT",
    headers: metadata.uploadHeaders,
    body: file,
  });
  if (!response.ok) throw new Error(`File upload failed: ${response.status}`);
  return metadata.attachment;
}

export async function getVerificationRequests() {
  return requireData(
    await apiClient.GET("/api/v1/performers/me/verification-requests"),
  );
}

export async function submitVerificationRequest(documentIds: string[]) {
  return requireData(
    await apiClient.POST("/api/v1/performers/me/verification-requests", {
      body: { requestedLevel: "ID_VERIFIED", documentIds },
    }),
  );
}
