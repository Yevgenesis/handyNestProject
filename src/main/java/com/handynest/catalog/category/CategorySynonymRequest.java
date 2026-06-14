package com.handynest.catalog.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Localized category search synonym")
public record CategorySynonymRequest(
    @NotBlank
        @Pattern(regexp = "ru|kk|en", message = "locale must be ru, kk or en")
        @Schema(example = "ru")
        String locale,
    @NotBlank @Size(max = 160) @Schema(example = "ремонт крана") String value) {}
