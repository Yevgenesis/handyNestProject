package com.handynest.marketplace;

import jakarta.validation.constraints.Size;

public record FavoritePerformerRequest(
        @Size(max = 500)
        String note
) {
}
