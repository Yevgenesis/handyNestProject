"use client";

import { CheckCircle2, MapPin, Star } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import Link from "next/link";

import { formatMoney } from "@/shared/lib/format";
import { verificationLabel } from "@/shared/lib/labels";
import type { PerformerProfile } from "@/shared/api/types";
import { Badge } from "@/shared/ui/badge";

export function PerformerCard({ performer }: { performer: PerformerProfile }) {
  const locale = useLocale();
  const t = useTranslations("performers");
  const common = useTranslations("common");
  const labels = useTranslations("labels");
  const primaryCategory =
    performer.categories?.find((category) => category.primary) ??
    performer.categories?.[0];
  return (
    <article className="border-b border-[var(--border)] bg-white p-5 last:border-b-0 sm:p-6">
      <div className="flex items-start gap-4">
        <div className="grid size-12 shrink-0 place-items-center rounded-[6px] bg-[var(--primary-soft)] text-lg font-black text-[var(--primary-strong)]">
          {performer.displayName?.slice(0, 1).toUpperCase() || "H"}
        </div>
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-2">
            <h2 className="text-lg font-bold">
              <Link
                className="hover:text-[var(--primary)]"
                href={`/performers/${performer.publicId}`}
              >
                {performer.displayName}
              </Link>
            </h2>
            {performer.topPerformer ? (
              <Badge tone="amber">{t("top")}</Badge>
            ) : null}
          </div>
          <p className="mt-1 text-sm text-[var(--muted)]">
            {primaryCategory?.title || t("specialist")}
          </p>
        </div>
        {performer.available ? (
          <Badge tone="green">{t("available")}</Badge>
        ) : (
          <Badge>{t("busy")}</Badge>
        )}
      </div>
      <p className="mt-4 line-clamp-2 text-sm leading-6 text-[var(--muted)]">
        {performer.description ||
          performer.skillsDescription ||
          t("noDescription")}
      </p>
      <div className="mt-5 flex flex-wrap items-center gap-x-5 gap-y-2 text-xs text-[var(--muted)]">
        <span className="flex items-center gap-1.5">
          <Star className="size-4 fill-amber-400 text-amber-500" />
          {performer.ratingAverage?.toFixed(1) ?? "—"} (
          {performer.ratingCount ?? 0})
        </span>
        <span className="flex items-center gap-1.5">
          <MapPin className="size-4" />
          {performer.cityName || common("uzbekistan")}
        </span>
        <span className="flex items-center gap-1.5">
          <CheckCircle2 className="size-4 text-[var(--primary)]" />
          {verificationLabel(performer.verificationLevel, labels)}
        </span>
        {primaryCategory?.priceFrom != null ? (
          <strong className="text-[var(--foreground)]">
            {common("fromPrice", {
              price: formatMoney(
                primaryCategory.priceFrom,
                primaryCategory.currency ?? "UZS",
                locale,
              ),
            })}
          </strong>
        ) : null}
      </div>
    </article>
  );
}
