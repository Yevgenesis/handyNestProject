package com.handynest.catalog.category;

import java.util.List;

public record CategoryResponse(
        String publicId,
        String title,
        String slug,
        String icon,
        String riskLevel,
        String serviceMode,
        String launchPhase,
        boolean active,
        boolean publicVisible,
        int sortOrder,
        String requiresVerificationLevel,
        boolean requiresManualApproval,
        boolean requiresLicense,
        boolean allowsRemote,
        boolean allowsOnsite,
        boolean allowsEscrow,
        boolean allowsCash,
        boolean allowsMilestones,
        boolean allowsAttachments,
        boolean allowsContactReveal,
        int weight,
        List<CategoryResponse> children
) {

    public CategoryResponse {
        children = children == null ? List.of() : List.copyOf(children);
    }
}
