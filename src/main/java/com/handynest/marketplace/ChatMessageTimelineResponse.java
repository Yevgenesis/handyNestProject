package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Cursor-based chronological chat message timeline.")
public record ChatMessageTimelineResponse(
    List<ChatMessageResponse> messages,
    String oldestCursor,
    String newestCursor,
    boolean hasMoreOlder,
    boolean hasMoreNewer) {

  public ChatMessageTimelineResponse {
    messages = messages == null ? List.of() : List.copyOf(messages);
  }
}
