import type { Notification } from "@/shared/api/types";

type NotificationMetadata = {
  taskId?: string;
  dealId?: string;
  chatId?: string;
};

export function notificationHref(notification: Notification) {
  const targetId = notification.targetId;
  const metadata = parseMetadata(notification.metadataJson);

  switch (notification.targetType) {
    case "Task":
      return targetId ? `/tasks/${targetId}` : "/my/tasks";
    case "Deal":
      return targetId ? `/deals/${targetId}` : "/my/deals";
    case "Chat":
    case "DealChat":
      return targetId ? `/chats/${targetId}` : "/chats";
    case "TaskOffer":
      return metadata.taskId ? `/tasks/${metadata.taskId}` : "/my/offers";
    case "Feedback":
      return metadata.taskId ? `/tasks/${metadata.taskId}` : "/notifications";
    case "DisputeCase":
      return metadata.dealId ? `/deals/${metadata.dealId}` : "/my/deals";
    case "VerificationRequest":
      return "/performer/verification";
    default:
      if (metadata.chatId) return `/chats/${metadata.chatId}`;
      if (metadata.dealId) return `/deals/${metadata.dealId}`;
      if (metadata.taskId) return `/tasks/${metadata.taskId}`;
      return "/notifications";
  }
}

function parseMetadata(value: string | undefined): NotificationMetadata {
  if (!value) return {};
  try {
    const parsed: unknown = JSON.parse(value);
    if (!parsed || typeof parsed !== "object") return {};
    return parsed as NotificationMetadata;
  } catch {
    return {};
  }
}
