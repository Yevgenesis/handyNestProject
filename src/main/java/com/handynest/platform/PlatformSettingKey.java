package com.handynest.platform;

public enum PlatformSettingKey {
  PHONE_VERIFICATION_ENABLED("feature.phone-verification.enabled"),
  EMAIL_VERIFICATION_ENABLED("feature.email-verification.enabled"),
  PAYMENTS_ENABLED("feature.payments.enabled"),
  ESCROW_ENABLED("feature.escrow.enabled"),
  PUBLIC_TASKS_ENABLED("feature.public-tasks.enabled"),
  CUSTOMER_RATING_ENABLED("feature.customer-rating.enabled"),
  PERFORMER_COMPLAINTS_ENABLED("feature.performer-complaints.enabled"),
  CUSTOMER_RISK_BLOCKING_ENABLED("feature.customer-risk-blocking.enabled"),
  MARKET_DEFAULT_COUNTRY_CODE("market.default-country-code"),
  MARKET_DEFAULT_CITY_PUBLIC_ID("market.default-city-public-id"),
  MARKET_DEFAULT_LOCALE("market.default-locale"),
  MARKET_SUPPORTED_LOCALES("market.supported-locales"),
  FREE_OFFER_LIMIT_MONTHLY("marketplace.free-offer-limit-monthly"),
  MILESTONE_THRESHOLD("marketplace.milestone-threshold"),
  MILESTONE_THRESHOLD_KZT("marketplace.milestone-threshold-kzt"),
  TASK_EXPIRATION_DAYS("marketplace.task-expiration-days"),
  MAX_ACTIVE_TASKS("risk.max-active-tasks"),
  OUTBOX_RETRY_BASE_DELAY("notifications.outbox-retry-base-delay"),
  TERMS_VERSION("legal.terms.version"),
  PRIVACY_POLICY_VERSION("legal.privacy-policy.version"),
  PERSONAL_DATA_VERSION("legal.personal-data-processing.version"),
  PERFORMER_RULES_VERSION("legal.performer-rules.version"),
  CUSTOMER_RULES_VERSION("legal.customer-rules.version"),
  PROHIBITED_SERVICES_POLICY_VERSION("legal.prohibited-services-policy.version"),
  PAYMENT_POLICY_VERSION("legal.payment-policy.version");

  private final String key;

  PlatformSettingKey(String key) {
    this.key = key;
  }

  public String key() {
    return key;
  }
}
