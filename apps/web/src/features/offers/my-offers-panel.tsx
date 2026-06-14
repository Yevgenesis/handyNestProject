"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { XCircle } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import Link from "next/link";
import { toast } from "sonner";

import { StatusBadge } from "@/entities/marketplace/status-badge";
import { getErrorMessage } from "@/shared/api/errors";
import {
  cancelOffer,
  getMyOffers,
  newIdempotencyKey,
} from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import { formatMoney } from "@/shared/lib/format";
import { Button } from "@/shared/ui/button";
import { ConfirmDialog } from "@/shared/ui/confirm-dialog";
import { EmptyState, ErrorState } from "@/shared/ui/page-state";
import { Skeleton } from "@/shared/ui/skeleton";

export function MyOffersPanel() {
  const t = useTranslations("myOffers");
  const apiErrors = useTranslations("apiErrors");
  const locale = useLocale();
  const queryClient = useQueryClient();
  const offers = useQuery({
    queryKey: queryKeys.myOffers,
    queryFn: getMyOffers,
  });
  const cancel = useMutation({
    mutationFn: ({ taskId, offerId }: { taskId: string; offerId: string }) =>
      cancelOffer(taskId, offerId, newIdempotencyKey("cancel-offer")),
    onSuccess() {
      void queryClient.invalidateQueries({ queryKey: queryKeys.myOffers });
      toast.success(t("canceled"));
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });
  if (offers.isLoading) return <Skeleton className="h-64" />;
  if (offers.isError) return <ErrorState title={t("loadError")} />;
  if (!offers.data?.length)
    return (
      <EmptyState
        title={t("emptyTitle")}
        description={t("emptyDescription")}
        actionHref="/tasks"
        actionLabel={t("findTasks")}
      />
    );
  return (
    <div className="grid gap-4">
      {offers.data.map((offer) => (
        <article className="surface-panel p-5" key={offer.publicId}>
          <div className="flex flex-wrap justify-between gap-3">
            <div>
              <StatusBadge status={offer.status} />
              <h2 className="mt-3 text-xl font-bold">
                <Link href={`/tasks/${offer.taskId}`}>{offer.taskTitle}</Link>
              </h2>
              <p className="mt-1 text-sm text-[var(--muted)]">
                {offer.categoryTitle} · {offer.cityName}
              </p>
            </div>
            <p className="text-xl font-black">
              {formatMoney(
                offer.proposedPrice ?? 0,
                offer.currency ?? "UZS",
                locale,
              )}
            </p>
          </div>
          <p className="mt-4 text-sm">{offer.message}</p>
          <div className="mt-5 flex flex-wrap items-center gap-3 border-t border-[var(--border)] pt-4">
            <Button asChild size="sm" variant="secondary">
              <Link href={`/tasks/${offer.taskId}`}>{t("viewTask")}</Link>
            </Button>
            {offer.status === "PENDING" ? (
              <ConfirmDialog
                trigger={
                  <Button size="sm" variant="danger">
                    <XCircle className="size-4" />
                    {t("cancel")}
                  </Button>
                }
                title={t("cancelTitle")}
                description={t("cancelDescription")}
                confirmLabel={t("cancel")}
                danger
                pending={cancel.isPending}
                onConfirm={() =>
                  cancel.mutate({
                    taskId: offer.taskId ?? "",
                    offerId: offer.publicId ?? "",
                  })
                }
              />
            ) : null}
          </div>
        </article>
      ))}
    </div>
  );
}
