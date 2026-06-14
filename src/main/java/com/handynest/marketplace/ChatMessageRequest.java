package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to send a text message in a deal chat.")
public record ChatMessageRequest(@NotBlank @Size(max = 4000) String text) {}
