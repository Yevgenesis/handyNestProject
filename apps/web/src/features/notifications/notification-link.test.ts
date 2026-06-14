import { describe, expect, it } from "vitest";

import { notificationHref } from "@/features/notifications/notification-link";

describe("notificationHref", () => {
  it("routes direct deal and chat targets", () => {
    expect(notificationHref({ targetType: "Deal", targetId: "deal-1" })).toBe(
      "/deals/deal-1",
    );
    expect(notificationHref({ targetType: "Chat", targetId: "chat-1" })).toBe(
      "/chats/chat-1",
    );
  });

  it("uses safe public ids from metadata for indirect targets", () => {
    expect(
      notificationHref({
        targetType: "Feedback",
        targetId: "feedback-1",
        metadataJson: JSON.stringify({ taskId: "task-1" }),
      }),
    ).toBe("/tasks/task-1");
  });

  it("falls back safely when metadata is malformed", () => {
    expect(
      notificationHref({ targetType: "Unknown", metadataJson: "not-json" }),
    ).toBe("/notifications");
  });
});
