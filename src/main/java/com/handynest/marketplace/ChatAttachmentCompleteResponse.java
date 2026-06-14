package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Completed chat attachment and its visible chat message.")
public record ChatAttachmentCompleteResponse(
    AttachmentResponse attachment, ChatMessageResponse message) {}
