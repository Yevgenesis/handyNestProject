"use client";

import { useQuery } from "@tanstack/react-query";
import { MessageSquare } from "lucide-react";
import { useTranslations } from "next-intl";
import Link from "next/link";

import { StatusBadge } from "@/entities/marketplace/status-badge";
import { getMyDeals } from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import { Button } from "@/shared/ui/button";
import { EmptyState, ErrorState } from "@/shared/ui/page-state";
import { Skeleton } from "@/shared/ui/skeleton";

export function MyDealsPanel() {
  const t = useTranslations("deals");
  const deals = useQuery({ queryKey: queryKeys.myDeals, queryFn: getMyDeals });
  if (deals.isLoading)
    return (
      <div className="grid gap-4">
        <Skeleton className="h-36" />
        <Skeleton className="h-36" />
      </div>
    );
  if (deals.isError) return <ErrorState title={t("loadError")} />;
  if (!deals.data?.length)
    return (
      <EmptyState title={t("emptyTitle")} description={t("emptyDescription")} />
    );
  return (
    <div className="grid gap-4">
      {deals.data.map((deal) => (
        <article className="surface-panel p-5" key={deal.publicId}>
          <div className="flex flex-wrap justify-between gap-3">
            <div>
              <StatusBadge status={deal.status} />
              <h2 className="mt-3 text-xl font-bold">{deal.taskTitle}</h2>
              <p className="mt-1 text-sm text-[var(--muted)]">
                {deal.categoryTitle} · {deal.cityName}
              </p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Button asChild variant="ghost">
                <Link href={`/chats/${deal.chatId}`}>{t("openChat")}</Link>
              </Button>
              <Button asChild variant="secondary">
                <Link href={`/deals/${deal.publicId}`}>{t("open")}</Link>
              </Button>
            </div>
          </div>
          <div className="mt-4 flex items-center gap-2 border-t border-[var(--border)] pt-4 text-sm text-[var(--muted)]">
            <MessageSquare className="size-4" />
            {t("chatReady")}
          </div>
        </article>
      ))}
    </div>
  );
}
