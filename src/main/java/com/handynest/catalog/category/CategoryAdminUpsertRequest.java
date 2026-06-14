package com.handynest.catalog.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Complete administrator-managed category configuration")
public record CategoryAdminUpsertRequest(
    @NotBlank
        @Size(max = 120)
        @Pattern(regexp = "[a-z0-9]+(?:-[a-z0-9]+)*", message = "slug must be lowercase kebab-case")
        @Schema(example = "plumbing")
        String slug,
    @Schema(description = "Parent category publicId; null for a root category") String parentId,
    @Size(max = 80) String icon,
    @NotNull CategoryRiskLevel riskLevel,
    @NotNull CategoryServiceMode serviceMode,
    @NotNull CategoryLaunchPhase launchPhase,
    @NotNull Boolean active,
    @NotNull Boolean publicVisible,
    @NotNull @Min(0) Integer sortOrder,
    @NotNull RequiredVerificationLevel requiresVerificationLevel,
    @NotNull Boolean requiresManualApproval,
    @NotNull Boolean requiresLicense,
    @NotNull Boolean allowsRemote,
    @NotNull Boolean allowsOnsite,
    @NotNull Boolean allowsEscrow,
    @NotNull Boolean allowsCash,
    @NotNull Boolean allowsMilestones,
    @NotNull Boolean allowsAttachments,
    @NotNull Boolean allowsContactReveal,
    @NotNull Boolean requiresOnsiteCoordination,
    @NotNull ContactRevealStage contactRevealStage,
    @NotEmpty List<@Valid CategoryTranslationRequest> translations,
    List<@Valid CategorySynonymRequest> synonyms,
    @Schema(description = "Required for updates; ignored during create") Long version) {
  public CategoryAdminUpsertRequest {
    translations = translations == null ? List.of() : List.copyOf(translations);
    synonyms = synonyms == null ? List.of() : List.copyOf(synonyms);
  }
}
