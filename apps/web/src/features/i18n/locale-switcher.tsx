"use client";

import { Languages } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { useRouter } from "next/navigation";

import { supportedLocales, type SupportedLocale } from "@/i18n/config";
import { Select } from "@/shared/ui/select";

export function LocaleSwitcher() {
  const locale = useLocale();
  const t = useTranslations("locale");
  const router = useRouter();

  function changeLocale(nextLocale: SupportedLocale) {
    document.cookie = `NEXT_LOCALE=${nextLocale}; Path=/; Max-Age=31536000; SameSite=Lax`;
    router.refresh();
  }

  return (
    <label className="relative flex items-center">
      <Languages
        className="pointer-events-none absolute left-2 size-4 text-[var(--muted)]"
        aria-hidden
      />
      <span className="sr-only">{t("label")}</span>
      <Select
        aria-label={t("label")}
        className="h-9 w-[118px] py-0 pr-2 pl-8"
        onChange={(event) =>
          changeLocale(event.target.value as SupportedLocale)
        }
        value={locale}
      >
        {supportedLocales.map((item) => (
          <option key={item} value={item}>
            {t(item)}
          </option>
        ))}
      </Select>
    </label>
  );
}
