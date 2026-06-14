package com.handynest.catalog.category;

import java.time.Instant;
import java.util.List;

public record CategoryAdminResponse(
    String publicId,
    String parentId,
    String slug,
    String icon,
    CategoryRiskLevel riskLevel,
    CategoryServiceMode serviceMode,
    CategoryLaunchPhase launchPhase,
    boolean active,
    boolean publicVisible,
    int sortOrder,
    RequiredVerificationLevel requiresVerificationLevel,
    boolean requiresManualApproval,
    boolean requiresLicense,
    boolean allowsRemote,
    boolean allowsOnsite,
    boolean allowsEscrow,
    boolean allowsCash,
    boolean allowsMilestones,
    boolean allowsAttachments,
    boolean allowsContactReveal,
    boolean requiresOnsiteCoordination,
    ContactRevealStage contactRevealStage,
    List<CategoryTranslationResponse> translations,
    List<CategorySynonymResponse> synonyms,
    long version,
    Instant createdAt,
    Instant updatedAt) {}
