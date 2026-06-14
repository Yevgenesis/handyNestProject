"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ImagePlus, Star } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { toast } from "sonner";

import { StatusBadge } from "@/entities/marketplace/status-badge";
import { TaskImages } from "@/features/tasks/task-images";
import { getErrorMessage } from "@/shared/api/errors";
import {
  acceptOffer,
  getTask,
  getTaskOffers,
  newIdempotencyKey,
  uploadTaskImage,
} from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import { formatDate, formatMoney } from "@/shared/lib/format";
import { Button } from "@/shared/ui/button";
import { ConfirmDialog } from "@/shared/ui/confirm-dialog";
import { EmptyState, ErrorState } from "@/shared/ui/page-state";
import { Skeleton } from "@/shared/ui/skeleton";

export function TaskOwnerPanel({ taskId }: { taskId: string }) {
  const t = useTranslations("taskOwner");
  const apiErrors = useTranslations("apiErrors");
  const locale = useLocale();
  const router = useRouter();
  const queryClient = useQueryClient();
  const [files, setFiles] = useState<File[]>([]);
  const task = useQuery({
    queryKey: queryKeys.task(taskId),
    queryFn: () => getTask(taskId),
  });
  const offers = useQuery({
    queryKey: queryKeys.taskOffers(taskId),
    queryFn: () => getTaskOffers(taskId),
    enabled: task.data?.status === "OPEN",
  });
  const accept = useMutation({
    mutationFn: (offerId: string) =>
      acceptOffer(taskId, offerId, newIdempotencyKey("accept-offer")),
    onSuccess(deal) {
      void queryClient.invalidateQueries({ queryKey: queryKeys.task(taskId) });
      void queryClient.invalidateQueries({
        queryKey: queryKeys.taskOffers(taskId),
      });
      void queryClient.invalidateQueries({ queryKey: queryKeys.myDeals });
      toast.success(t("offerAccepted"));
      router.push(`/deals/${deal.publicId}`);
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });
  const upload = useMutation({
    mutationFn: async () =>
      Promise.all(files.map((file) => uploadTaskImage(taskId, file))),
    onSuccess() {
      setFiles([]);
      void queryClient.invalidateQueries({
        queryKey: queryKeys.taskAttachments(taskId),
      });
      toast.success(t("imagesUploaded"));
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });

  if (task.isLoading) return <Skeleton className="h-96 w-full" />;
  if (!task.data) return <ErrorState title={t("loadError")} />;
  const data = task.data;
  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
      <div className="grid gap-6">
        <section className="surface-panel p-5 sm:p-6">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <StatusBadge status={data.status} />
              <h1 className="mt-3 text-3xl font-black">{data.title}</h1>
              <p className="mt-2 text-sm text-[var(--muted)]">
                {data.categoryTitle} · {data.cityName}
              </p>
            </div>
            <p className="text-xl font-black">
              {data.fixedPrice
                ? formatMoney(data.fixedPrice, data.currency ?? "UZS", locale)
                : t("negotiable")}
            </p>
          </div>
          <p className="mt-6 whitespace-pre-wrap">{data.description}</p>
          <dl className="mt-6 grid gap-4 border-t border-[var(--border)] pt-5 text-sm sm:grid-cols-3">
            <div>
              <dt className="text-[var(--muted)]">{t("mode")}</dt>
              <dd className="font-semibold">
                {t(`serviceMode.${data.serviceMode ?? "UNKNOWN"}`)}
              </dd>
            </div>
            <div>
              <dt className="text-[var(--muted)]">{t("expires")}</dt>
              <dd className="font-semibold">
                {formatDate(data.expiresAt, locale, t("notSpecified"))}
              </dd>
            </div>
            <div>
              <dt className="text-[var(--muted)]">{t("address")}</dt>
              <dd className="font-semibold">
                {data.addressText || t("notSpecified")}
              </dd>
            </div>
          </dl>
        </section>
        <section className="surface-panel p-5 sm:p-6">
          <h2 className="text-xl font-bold">{t("images")}</h2>
          <div className="mt-4">
            <TaskImages taskId={taskId} />
          </div>
          {data.status === "OPEN" ? (
            <div className="mt-5 grid gap-3 border-t border-[var(--border)] pt-5 sm:grid-cols-[1fr_auto]">
              <input
                accept="image/jpeg,image/png,image/webp"
                aria-label={t("addImages")}
                className="block w-full text-sm"
                multiple
                onChange={(event) =>
                  setFiles(Array.from(event.target.files ?? []).slice(0, 5))
                }
                type="file"
              />
              <Button
                disabled={!files.length || upload.isPending}
                onClick={() => upload.mutate()}
              >
                <ImagePlus className="size-4" />
                {t("upload")}
              </Button>
            </div>
          ) : null}
        </section>
      </div>
      <aside className="surface-panel self-start p-5 sm:p-6">
        <h2 className="text-xl font-bold">{t("offers")}</h2>
        <p className="mt-1 text-sm text-[var(--muted)]">
          {t("offersDescription")}
        </p>
        {offers.isLoading ? <Skeleton className="mt-5 h-40" /> : null}
        {offers.data && !offers.data.length ? (
          <EmptyState
            title={t("noOffers")}
            description={t("noOffersDescription")}
          />
        ) : null}
        <div className="mt-5 grid gap-4">
          {offers.data?.map((offer) => (
            <article
              className="rounded-[8px] border border-[var(--border)] p-4"
              key={offer.publicId}
            >
              <div className="flex justify-between gap-3">
                <div>
                  <p className="font-bold">{offer.performerDisplayName}</p>
                  <p className="mt-1 flex items-center gap-1 text-sm text-[var(--muted)]">
                    <Star className="size-4 fill-amber-400 text-amber-400" />
                    {offer.performerRatingAverage ?? 0} ·{" "}
                    {t("reviews", { count: offer.performerRatingCount ?? 0 })}
                  </p>
                </div>
                <StatusBadge status={offer.status} />
              </div>
              <p className="mt-3 text-sm">{offer.message}</p>
              <dl className="mt-3 grid gap-2 text-sm">
                <div className="flex justify-between">
                  <dt className="text-[var(--muted)]">{t("price")}</dt>
                  <dd className="font-bold">
                    {formatMoney(
                      offer.proposedPrice ?? 0,
                      offer.currency ?? data.currency ?? "UZS",
                      locale,
                    )}
                  </dd>
                </div>
                <div className="flex justify-between">
                  <dt className="text-[var(--muted)]">{t("duration")}</dt>
                  <dd>{offer.estimatedDuration || t("notSpecified")}</dd>
                </div>
              </dl>
              {offer.status === "PENDING" ? (
                <ConfirmDialog
                  trigger={
                    <Button className="mt-4 w-full">{t("choose")}</Button>
                  }
                  title={t("chooseTitle")}
                  description={t("chooseDescription", {
                    name: offer.performerDisplayName ?? "",
                  })}
                  confirmLabel={t("choose")}
                  pending={accept.isPending}
                  onConfirm={() => accept.mutate(offer.publicId ?? "")}
                />
              ) : null}
            </article>
          ))}
        </div>
      </aside>
    </div>
  );
}
