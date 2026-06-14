import { describe, expect, it } from "vitest";

import type { ChatMessage } from "@/shared/api/types";

import { mergeChatMessages } from "./chat-timeline";

function message(publicId: string, createdAt: string): ChatMessage {
  return { publicId, createdAt, text: publicId };
}

describe("mergeChatMessages", () => {
  it("deduplicates polling responses and keeps chronological order", () => {
    const result = mergeChatMessages(
      [message("b", "2026-06-13T10:00:02Z")],
      [
        message("b", "2026-06-13T10:00:02Z"),
        message("a", "2026-06-13T10:00:01Z"),
        message("c", "2026-06-13T10:00:03Z"),
      ],
    );

    expect(result.map((item) => item.publicId)).toEqual(["a", "b", "c"]);
  });
});
