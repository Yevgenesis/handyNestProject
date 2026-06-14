import { ArrowLeft, CheckCircle2, MapPin, Star } from "lucide-react";
import { getLocale, getTranslations } from "next-intl/server";
import Link from "next/link";
import { notFound } from "next/navigation";

import { FeedbackList } from "@/entities/feedback/feedback-list";
import {
  getPublicPerformer,
  getPublicPerformerFeedbacks,
} from "@/shared/api/public-data";
import { formatMoney } from "@/shared/lib/format";
import { serviceModeLabel, verificationLabel } from "@/shared/lib/labels";
import { Badge } from "@/shared/ui/badge";
import { Button } from "@/shared/ui/button";
import { ErrorState } from "@/shared/ui/page-state";

export const dynamic = "force-dynamic";

export default async function PerformerDetailPage({
  params,
}: {
  params: Promise<{ performerId: string }>;
}) {
  const [{ performerId }, locale, t, common, labels, feedbackT] =
    await Promise.all([
      params,
      getLocale(),
      getTranslations("performers"),
      getTranslations("common"),
      getTranslations("labels"),
      getTranslations("feedback"),
    ]);
  const [result, feedbackResult] = await Promise.all([
    getPublicPerformer(locale, performerId),
    getPublicPerformerFeedbacks(locale, performerId),
  ]);
  if ("status" in result && result.status === 404) notFound();
  if (!result.data)
    return (
      <section className="container-page page-section">
        <ErrorState message={t("loadError")} />
      </section>
    );

  const performer = result.data;
  const mode =
    performer.worksOnsite && performer.worksRemotely
      ? "HYBRID"
      : performer.worksRemotely
        ? "REMOTE"
        : "ONSITE";

  return (
    <section className="container-page page-section">
      <Link
        className="inline-flex items-center gap-2 text-sm font-semibold text-[var(--muted)]"
        href="/performers"
      >
        <ArrowLeft className="size-4" />
        {t("back")}
      </Link>
      <div className="mt-8 grid gap-8 lg:grid-cols-[1fr_340px]">
        <article>
          <div className="flex items-start gap-5">
            <div className="grid size-20 shrink-0 place-items-center rounded-[8px] bg-[var(--primary-soft)] text-2xl font-black text-[var(--primary-strong)]">
              {performer.displayName?.slice(0, 1).toUpperCase()}
            </div>
            <div>
              <div className="flex flex-wrap items-center gap-2">
                <h1 className="text-3xl font-black sm:text-4xl">
                  {performer.displayName}
                </h1>
                {performer.topPerformer ? (
                  <Badge tone="amber">{t("top")}</Badge>
                ) : null}
              </div>
              <div className="mt-3 flex flex-wrap gap-4 text-sm text-[var(--muted)]">
                <span className="flex items-center gap-1.5">
                  <Star className="size-4 fill-amber-400 text-amber-500" />
                  {performer.ratingAverage?.toFixed(1) ?? "—"} ·{" "}
                  {t("reviews", { count: performer.ratingCount ?? 0 })}
                </span>
                <span className="flex items-center gap-1.5">
                  <MapPin className="size-4" />
                  {performer.cityName || common("uzbekistan")}
                </span>
              </div>
            </div>
          </div>
          <div className="mt-8 border-y border-[var(--border)] py-7">
            <h2 className="text-xl font-bold">{t("about")}</h2>
            <p className="mt-4 leading-7 whitespace-pre-line text-[var(--muted)]">
              {performer.description || t("noDescription")}
            </p>
            {performer.skillsDescription ? (
              <p className="mt-4 leading-7 text-[var(--muted)]">
                {performer.skillsDescription}
              </p>
            ) : null}
          </div>
          <div className="mt-8">
            <h2 className="text-xl font-bold">{t("servicesAndPrices")}</h2>
            <div className="mt-4 divide-y divide-[var(--border)] border-y border-[var(--border)]">
              {performer.categories?.map((category) => (
                <div
                  className="flex flex-col justify-between gap-2 py-4 sm:flex-row sm:items-center"
                  key={category.categoryId}
                >
                  <div>
                    <p className="font-semibold">{category.title}</p>
                    <p className="mt-1 text-xs text-[var(--muted)]">
                      {t("experience", {
                        years: category.experienceYears ?? 0,
                      })}
                    </p>
                  </div>
                  <strong>
                    {category.priceFrom != null
                      ? common("fromPrice", {
                          price: formatMoney(
                            category.priceFrom,
                            category.currency ?? "UZS",
                            locale,
                          ),
                        })
                      : common("negotiablePrice")}
                  </strong>
                </div>
              ))}
            </div>
          </div>
          <div className="mt-8">
            <h2 className="text-xl font-bold">{feedbackT("reviewsTitle")}</h2>
            <p className="mt-2 text-sm text-[var(--muted)]">
              {feedbackT("reviewsDescription")}
            </p>
            <div className="mt-4">
              {feedbackResult.data?.length ? (
                <FeedbackList
                  anonymousLabel={feedbackT("anonymous")}
                  feedbacks={feedbackResult.data}
                  locale={locale}
                  noTextLabel={feedbackT("withoutText")}
                />
              ) : (
                <p className="border-y border-[var(--border)] py-6 text-sm text-[var(--muted)]">
                  {feedbackT("empty")}
                </p>
              )}
            </div>
          </div>
        </article>
        <aside className="surface-panel h-fit p-5">
          <div className="flex items-center justify-between gap-3">
            <Badge tone={performer.available ? "green" : "neutral"}>
              {performer.available ? t("availableNow") : t("busyNow")}
            </Badge>
            <span className="flex items-center gap-1 text-xs text-[var(--muted)]">
              <CheckCircle2 className="size-4 text-[var(--primary)]" />
              {verificationLabel(performer.verificationLevel, labels)}
            </span>
          </div>
          <dl className="mt-6 grid gap-4 text-sm">
            <div>
              <dt className="text-[var(--muted)]">{t("workMode")}</dt>
              <dd className="mt-1 font-semibold">
                {serviceModeLabel(mode, labels)}
              </dd>
            </div>
            <div>
              <dt className="text-[var(--muted)]">{t("completedTasks")}</dt>
              <dd className="mt-1 font-semibold">
                {performer.completedTasksCount ?? 0}
              </dd>
            </div>
            {performer.serviceRadiusKm ? (
              <div>
                <dt className="text-[var(--muted)]">{t("serviceRadius")}</dt>
                <dd className="mt-1 font-semibold">
                  {t("serviceRadiusValue", {
                    distance: performer.serviceRadiusKm,
                  })}
                </dd>
              </div>
            ) : null}
          </dl>
          <Button asChild className="mt-6 w-full" size="lg">
            <Link href="/login?next=/dashboard">{t("loginToContact")}</Link>
          </Button>
          <p className="mt-4 text-xs leading-5 text-[var(--muted)]">
            {t("privacyNote")}
          </p>
        </aside>
      </div>
    </section>
  );
}
