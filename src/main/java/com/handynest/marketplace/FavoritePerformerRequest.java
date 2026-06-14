package com.handynest.marketplace;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Optional note stored with a favorite performer.")
public record FavoritePerformerRequest(@Size(max = 500) String note) {}
