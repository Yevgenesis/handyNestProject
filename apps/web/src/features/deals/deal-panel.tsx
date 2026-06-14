"use client";

import { useQuery } from "@tanstack/react-query";
import { MessageSquare, UserRound } from "lucide-react";
import { useTranslations } from "next-intl";
import Link from "next/link";

import { StatusBadge } from "@/entities/marketplace/status-badge";
import { FeedbackPanel } from "@/features/feedback/feedback-panel";
import { getDeal } from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import { Button } from "@/shared/ui/button";
import { ErrorState } from "@/shared/ui/page-state";
import { Skeleton } from "@/shared/ui/skeleton";

export function DealPanel({ dealId }: { dealId: string }) {
  const t = useTranslations("deals");
  const deal = useQuery({
    queryKey: queryKeys.deal(dealId),
    queryFn: () => getDeal(dealId),
  });
  if (deal.isLoading) return <Skeleton className="h-96" />;
  if (!deal.data) return <ErrorState title={t("loadError")} />;
  const data = deal.data;
  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_340px]">
      <section className="surface-panel p-6">
        <StatusBadge status={data.status} />
        <h1 className="mt-3 text-3xl font-black">{data.taskTitle}</h1>
        <p className="mt-2 text-[var(--muted)]">
          {data.categoryTitle} · {data.cityName}
        </p>
        <dl className="mt-7 grid gap-5 border-t border-[var(--border)] pt-6 sm:grid-cols-2">
          <div>
            <dt className="text-sm text-[var(--muted)]">{t("customer")}</dt>
            <dd className="mt-1 flex items-center gap-2 font-bold">
              <UserRound className="size-4" />
              {data.customerDisplayName}
            </dd>
          </div>
          <div>
            <dt className="text-sm text-[var(--muted)]">{t("performer")}</dt>
            <dd className="mt-1 flex items-center gap-2 font-bold">
              <UserRound className="size-4" />
              {data.performerDisplayName}
            </dd>
          </div>
          <div>
            <dt className="text-sm text-[var(--muted)]">{t("payment")}</dt>
            <dd className="font-bold">
              {t(`paymentMode.${data.paymentMode ?? "OFF_PLATFORM"}`)}
            </dd>
          </div>
          <div>
            <dt className="text-sm text-[var(--muted)]">{t("revisions")}</dt>
            <dd className="font-bold">{data.revisionCount ?? 0}</dd>
          </div>
        </dl>
        <FeedbackPanel dealStatus={data.status} taskId={data.taskId} />
      </section>
      <aside className="surface-panel self-start p-6">
        <MessageSquare className="size-7 text-[var(--primary)]" />
        <h2 className="mt-4 text-xl font-bold">{t("dealChat")}</h2>
        <p className="mt-2 text-sm text-[var(--muted)]">
          {t("chatDescription")}
        </p>
        <Button asChild className="mt-5 w-full">
          <Link href={`/chats/${data.chatId}`}>{t("openChat")}</Link>
        </Button>
      </aside>
    </div>
  );
}
