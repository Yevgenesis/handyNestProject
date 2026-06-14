package com.handynest.catalog.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Localized category name and description")
public record CategoryTranslationRequest(
    @NotBlank
        @Pattern(regexp = "ru|kk|en", message = "locale must be ru, kk or en")
        @Schema(example = "ru")
        String locale,
    @NotBlank @Size(max = 255) @Schema(example = "Сантехник") String name,
    @Size(max = 2000) String description) {}
