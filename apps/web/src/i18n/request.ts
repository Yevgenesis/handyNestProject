import { getRequestConfig } from "next-intl/server";
import { cookies } from "next/headers";

import { defaultLocale, isSupportedLocale } from "@/i18n/config";

type Messages = Record<string, unknown>;

function mergeMessages(base: Messages, overrides: Messages): Messages {
  return Object.fromEntries(
    Object.entries(base).map(([key, value]) => {
      const override = overrides[key];
      if (
        typeof value === "object" &&
        value !== null &&
        !Array.isArray(value) &&
        typeof override === "object" &&
        override !== null &&
        !Array.isArray(override)
      ) {
        return [key, mergeMessages(value as Messages, override as Messages)];
      }
      return [key, override ?? value];
    }),
  );
}

export default getRequestConfig(async () => {
  const cookieStore = await cookies();
  const requestedLocale = cookieStore.get("NEXT_LOCALE")?.value;
  const locale = isSupportedLocale(requestedLocale)
    ? requestedLocale
    : defaultLocale;

  const baseMessages = (await import("@/messages/ru.json")).default;
  const localeMessages =
    locale === defaultLocale
      ? baseMessages
      : (await import(`@/messages/${locale}.json`)).default;

  return { locale, messages: mergeMessages(baseMessages, localeMessages) };
});
