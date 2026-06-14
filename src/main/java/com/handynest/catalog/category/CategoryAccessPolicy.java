package com.handynest.catalog.category;

import com.handynest.common.error.BadRequestBusinessException;
import com.handynest.common.error.VerificationRequiredException;
import com.handynest.performer.PerformerCategory;
import com.handynest.performer.PerformerProfile;
import com.handynest.performer.PerformerVerificationLevel;
import org.springframework.stereotype.Component;

@Component
public class CategoryAccessPolicy {

  public void assertAvailableForPublicTask(Category category) {
    if (!category.isActive() || !category.isPublicVisible()) {
      throw new BadRequestBusinessException("Category is not available for public tasks");
    }
    assertNotProhibited(category);
    if (category.getLaunchPhase() != CategoryLaunchPhase.MVP) {
      throw new BadRequestBusinessException("Category is not available in MVP");
    }
  }

  public void assertPerformerCanServeCategory(
      PerformerProfile performerProfile, Category category) {
    assertNotProhibited(category);
    if (!category.isActive()) {
      throw new BadRequestBusinessException("Category is not active");
    }
    if (category.getLaunchPhase() != CategoryLaunchPhase.MVP) {
      throw new BadRequestBusinessException("Category is not available in MVP");
    }
    int requiredRank = verificationRank(category.getRequiresVerificationLevel());
    if (requiredRank >= verificationRank(PerformerVerificationLevel.PHONE_VERIFIED)
        && !performerProfile.getUser().isPhoneVerified()) {
      throw new VerificationRequiredException("Verified phone is required for this category");
    }
    if (verificationRank(performerProfile.getEffectiveVerificationLevel()) < requiredRank) {
      throw new VerificationRequiredException(
          "Category requires " + category.getRequiresVerificationLevel());
    }
  }

  public void assertPerformerCanServeCategory(
      PerformerProfile performerProfile, PerformerCategory performerCategory) {
    assertPerformerCanServeCategory(performerProfile, performerCategory.getCategory());
    if (!performerCategory.isApprovedForServing()) {
      throw new VerificationRequiredException("Category requires manual approval");
    }
  }

  private void assertNotProhibited(Category category) {
    if (category.getRiskLevel() == CategoryRiskLevel.PROHIBITED
        || category.getLaunchPhase() == CategoryLaunchPhase.PROHIBITED_FOR_MVP
        || category.getLaunchPhase() == CategoryLaunchPhase.PROHIBITED_ALWAYS) {
      throw new BadRequestBusinessException("Category is prohibited");
    }
  }

  private int verificationRank(PerformerVerificationLevel level) {
    return switch (level) {
      case NONE -> 0;
      case PHONE_VERIFIED -> 1;
      case ID_VERIFIED -> 2;
      case PAYMENT_VERIFIED -> 3;
      case BUSINESS_VERIFIED -> 4;
    };
  }

  private int verificationRank(RequiredVerificationLevel level) {
    return switch (level) {
      case NONE -> 0;
      case PHONE_VERIFIED -> 1;
      case ID_VERIFIED -> 2;
      case BUSINESS_VERIFIED -> 4;
    };
  }
}
