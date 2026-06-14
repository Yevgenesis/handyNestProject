import { ArrowLeft, CalendarDays, MapPin, ShieldCheck } from "lucide-react";
import { getLocale, getTranslations } from "next-intl/server";
import Link from "next/link";
import { notFound } from "next/navigation";

import { getPublicTask } from "@/shared/api/public-data";
import { CreateOfferPanel } from "@/features/offers/create-offer-panel";
import { TaskImages } from "@/features/tasks/task-images";
import { formatDate, formatTaskBudget } from "@/shared/lib/format";
import { serviceModeLabel } from "@/shared/lib/labels";
import { Badge } from "@/shared/ui/badge";
import { ErrorState } from "@/shared/ui/page-state";

export const dynamic = "force-dynamic";

export default async function TaskDetailPage({
  params,
}: {
  params: Promise<{ taskId: string }>;
}) {
  const [{ taskId }, locale, t, common, labels] = await Promise.all([
    params,
    getLocale(),
    getTranslations("tasks"),
    getTranslations("common"),
    getTranslations("labels"),
  ]);
  const result = await getPublicTask(locale, taskId);
  if ("status" in result && result.status === 404) notFound();
  if (!result.data)
    return (
      <section className="container-page page-section">
        <ErrorState message={t("loadError")} />
      </section>
    );

  const task = result.data;
  const budget = formatTaskBudget(task, locale, {
    negotiable: common("negotiablePrice"),
    from: (price) => common("fromPrice", { price }),
    to: (price) => common("toPrice", { price }),
  });

  return (
    <section className="container-page page-section">
      <Link
        className="inline-flex items-center gap-2 text-sm font-semibold text-[var(--muted)]"
        href="/tasks"
      >
        <ArrowLeft className="size-4" />
        {t("back")}
      </Link>
      <div className="mt-8 grid gap-8 lg:grid-cols-[1fr_340px]">
        <article>
          <div className="flex flex-wrap gap-2">
            <Badge tone="green">
              {task.categoryTitle || common("service")}
            </Badge>
            <Badge>{serviceModeLabel(task.serviceMode, labels)}</Badge>
          </div>
          <h1 className="mt-5 text-3xl leading-tight font-black sm:text-4xl">
            {task.title}
          </h1>
          <div className="mt-5 flex flex-wrap gap-5 text-sm text-[var(--muted)]">
            <span className="flex items-center gap-2">
              <MapPin className="size-4" />
              {task.cityName || task.countryName || common("uzbekistan")}
            </span>
            <span className="flex items-center gap-2">
              <CalendarDays className="size-4" />
              {t("published", {
                date: formatDate(
                  task.createdAt,
                  locale,
                  common("notSpecified"),
                ),
              })}
            </span>
          </div>
          <div className="mt-8 border-y border-[var(--border)] py-7">
            <h2 className="text-xl font-bold">{t("taskDescription")}</h2>
            <p className="mt-4 leading-7 whitespace-pre-line text-[var(--muted)]">
              {task.description}
            </p>
          </div>
          <div className="mt-7">
            <TaskImages taskId={taskId} />
          </div>
          {task.addressText ? (
            <div className="mt-7">
              <h2 className="text-xl font-bold">{t("workplace")}</h2>
              <p className="mt-3 text-[var(--muted)]">{task.addressText}</p>
            </div>
          ) : null}
        </article>
        <aside className="surface-panel h-fit p-5">
          <p className="text-sm text-[var(--muted)]">{t("budget")}</p>
          <p className="mt-1 text-2xl font-black text-[var(--primary-strong)]">
            {budget}
          </p>
          <p className="mt-4 text-sm text-[var(--muted)]">
            {t("offersUntil", {
              date: formatDate(task.expiresAt, locale, common("notSpecified")),
            })}
          </p>
          <CreateOfferPanel
            taskId={taskId}
            customerId={task.customerId}
            status={task.status}
            currency={task.currency}
          />
          <div className="mt-5 flex gap-2 border-t border-[var(--border)] pt-5 text-xs leading-5 text-[var(--muted)]">
            <ShieldCheck className="mt-0.5 size-4 shrink-0 text-[var(--primary)]" />
            <span>{t("privacyNote")}</span>
          </div>
        </aside>
      </div>
    </section>
  );
}
