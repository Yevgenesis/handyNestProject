type LabelTranslator = (key: string) => string;

const serviceModeKeys: Record<string, string> = {
  ONSITE: "onsite",
  REMOTE: "remote",
  HYBRID: "hybrid",
};

const verificationKeys: Record<string, string> = {
  NONE: "verificationNone",
  PHONE_VERIFIED: "verificationPhone",
  ID_VERIFIED: "verificationIdentity",
  PAYMENT_VERIFIED: "verificationPayment",
  BUSINESS_VERIFIED: "verificationBusiness",
};

export function serviceModeLabel(
  value: string | undefined,
  translate: LabelTranslator,
) {
  return translate((value && serviceModeKeys[value]) || "modeUnknown");
}

export function verificationLabel(
  value: string | undefined,
  translate: LabelTranslator,
) {
  return translate((value && verificationKeys[value]) || "verificationUnknown");
}
