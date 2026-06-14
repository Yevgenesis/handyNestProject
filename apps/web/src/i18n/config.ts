export const supportedLocales = ["ru", "uz"] as const;
export type SupportedLocale = (typeof supportedLocales)[number];
export const defaultLocale: SupportedLocale = "ru";

export function isSupportedLocale(
  value: string | undefined,
): value is SupportedLocale {
  return supportedLocales.includes(value as SupportedLocale);
}
