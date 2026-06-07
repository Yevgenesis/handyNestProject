package com.handynest.catalog.category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "category")
@EntityListeners(AuditingEntityListener.class)
@Schema(description = "Entity representing a category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Category id", example = "1")
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 26)
    @Schema(description = "Public category id", example = "06F5Z8NMS7YQE4Q2F4R1HN9EZG")
    private String publicId;

    @Schema(description = "Category title", example = "Ремонт")
    private String title;

    @Schema(description = "Parent category id", example = "1")
    @Column(name = "parent_id")
    private Long parentId;

    @Schema(description = "Weight category", example = "1")
    private int weight;

    @Column(nullable = false, unique = true, length = 120)
    @Schema(description = "Stable category slug", example = "repair")
    private String slug;

    @Column(length = 80)
    @Schema(description = "Icon key for frontend rendering", example = "wrench")
    private String icon;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    @Schema(description = "Category risk level", example = "LOW")
    private CategoryRiskLevel riskLevel = CategoryRiskLevel.LOW;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "service_mode", nullable = false, length = 20)
    @Schema(description = "Category service mode", example = "HYBRID")
    private CategoryServiceMode serviceMode = CategoryServiceMode.HYBRID;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "launch_phase", nullable = false, length = 40)
    @Schema(description = "Launch phase", example = "MVP")
    private CategoryLaunchPhase launchPhase = CategoryLaunchPhase.MVP;

    @Builder.Default
    @Column(nullable = false)
    @Schema(description = "Whether category is active", example = "true")
    private boolean active = true;

    @Builder.Default
    @Column(name = "public_visible", nullable = false)
    @Schema(description = "Whether category is visible in public catalog", example = "true")
    private boolean publicVisible = true;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    @Schema(description = "Public sort order", example = "10")
    private int sortOrder = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "requires_verification_level", nullable = false, length = 32)
    @Schema(description = "Required verification level", example = "NONE")
    private RequiredVerificationLevel requiresVerificationLevel = RequiredVerificationLevel.NONE;

    @Builder.Default
    @Column(name = "requires_manual_approval", nullable = false)
    @Schema(description = "Whether category requires manual approval", example = "false")
    private boolean requiresManualApproval = false;

    @Builder.Default
    @Column(name = "requires_license", nullable = false)
    @Schema(description = "Whether category requires a license", example = "false")
    private boolean requiresLicense = false;

    @Builder.Default
    @Column(name = "allows_remote", nullable = false)
    @Schema(description = "Whether remote work is allowed", example = "true")
    private boolean allowsRemote = true;

    @Builder.Default
    @Column(name = "allows_onsite", nullable = false)
    @Schema(description = "Whether onsite work is allowed", example = "true")
    private boolean allowsOnsite = true;

    @Builder.Default
    @Column(name = "allows_escrow", nullable = false)
    @Schema(description = "Whether escrow is allowed", example = "false")
    private boolean allowsEscrow = false;

    @Builder.Default
    @Column(name = "allows_cash", nullable = false)
    @Schema(description = "Whether cash payment is allowed", example = "true")
    private boolean allowsCash = true;

    @Builder.Default
    @Column(name = "allows_milestones", nullable = false)
    @Schema(description = "Whether milestones are allowed", example = "false")
    private boolean allowsMilestones = false;

    @Builder.Default
    @Column(name = "allows_attachments", nullable = false)
    @Schema(description = "Whether attachments are allowed", example = "true")
    private boolean allowsAttachments = true;

    @Builder.Default
    @Column(name = "allows_contact_reveal", nullable = false)
    @Schema(description = "Whether contact reveal is allowed by deal rules", example = "false")
    private boolean allowsContactReveal = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
