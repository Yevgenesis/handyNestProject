"use client";

import { CalendarDays, MapPin } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import Link from "next/link";

import { formatDate, formatTaskBudget } from "@/shared/lib/format";
import { serviceModeLabel } from "@/shared/lib/labels";
import type { MarketplaceTask } from "@/shared/api/types";
import { Badge } from "@/shared/ui/badge";

export function TaskCard({ task }: { task: MarketplaceTask }) {
  const locale = useLocale();
  const t = useTranslations("tasks");
  const common = useTranslations("common");
  const labels = useTranslations("labels");
  const budget = formatTaskBudget(task, locale, {
    negotiable: common("negotiablePrice"),
    from: (price) => common("fromPrice", { price }),
    to: (price) => common("toPrice", { price }),
  });

  return (
    <article className="border-b border-[var(--border)] bg-white p-5 last:border-b-0 sm:p-6">
      <div className="flex flex-wrap items-center gap-2">
        <Badge tone="green">{task.categoryTitle || common("service")}</Badge>
        <Badge>{serviceModeLabel(task.serviceMode, labels)}</Badge>
      </div>
      <div className="mt-4 flex flex-col justify-between gap-4 sm:flex-row">
        <div className="min-w-0">
          <h2 className="text-lg font-bold">
            <Link
              className="hover:text-[var(--primary)]"
              href={`/tasks/${task.publicId}`}
            >
              {task.title}
            </Link>
          </h2>
          <p className="mt-2 line-clamp-2 text-sm leading-6 text-[var(--muted)]">
            {task.description}
          </p>
        </div>
        <p className="shrink-0 text-lg font-black text-[var(--primary-strong)]">
          {budget}
        </p>
      </div>
      <div className="mt-5 flex flex-wrap gap-x-5 gap-y-2 text-xs text-[var(--muted)]">
        <span className="flex items-center gap-1.5">
          <MapPin className="size-4" />
          {task.cityName || task.countryName || common("uzbekistan")}
        </span>
        <span className="flex items-center gap-1.5">
          <CalendarDays className="size-4" />
          {t("deadline", {
            date: formatDate(task.expiresAt, locale, common("notSpecified")),
          })}
        </span>
      </div>
    </article>
  );
}
