"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Copy, XCircle } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { toast } from "sonner";

import { StatusBadge } from "@/entities/marketplace/status-badge";
import { getErrorMessage } from "@/shared/api/errors";
import {
  cancelTask,
  getMyTasks,
  newIdempotencyKey,
  repeatTask,
} from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import { formatDate, formatMoney } from "@/shared/lib/format";
import { Button } from "@/shared/ui/button";
import { ConfirmDialog } from "@/shared/ui/confirm-dialog";
import { EmptyState, ErrorState } from "@/shared/ui/page-state";
import { Skeleton } from "@/shared/ui/skeleton";

const statuses = [
  "OPEN",
  "IN_PROGRESS",
  "WORK_SUBMITTED",
  "COMPLETED",
  "CANCELED",
  "EXPIRED",
] as const;

export function MyTasksPanel() {
  const t = useTranslations("myTasks");
  const locale = useLocale();
  const apiErrors = useTranslations("apiErrors");
  const searchParams = useSearchParams();
  const status = searchParams.get("status") || undefined;
  const page = Math.max(Number(searchParams.get("page") ?? 0), 0);
  const queryClient = useQueryClient();
  const tasks = useQuery({
    queryKey: queryKeys.myTasks(status, page),
    queryFn: () => getMyTasks({ status: status as never, page, size: 12 }),
  });
  const cancelMutation = useMutation({
    mutationFn: cancelTask,
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["my-tasks"] });
      toast.success(t("canceled"));
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });
  const repeatMutation = useMutation({
    mutationFn: (taskId: string) =>
      repeatTask(taskId, newIdempotencyKey("repeat")),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ["my-tasks"] });
      toast.success(t("repeated"));
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });

  return (
    <div>
      <div className="flex flex-wrap gap-2">
        <Button asChild size="sm" variant={!status ? "primary" : "secondary"}>
          <Link href="/my/tasks">{t("all")}</Link>
        </Button>
        {statuses.map((item) => (
          <Button
            asChild
            key={item}
            size="sm"
            variant={status === item ? "primary" : "secondary"}
          >
            <Link href={`/my/tasks?status=${item}`}>
              {t(`statuses.${item}`)}
            </Link>
          </Button>
        ))}
      </div>
      {tasks.isLoading ? (
        <div className="mt-6 grid gap-4">
          <Skeleton className="h-40" />
          <Skeleton className="h-40" />
        </div>
      ) : null}
      {tasks.isError ? (
        <div className="mt-6">
          <ErrorState title={t("loadError")} />
        </div>
      ) : null}
      {tasks.data && !tasks.data.content?.length ? (
        <div className="mt-6">
          <EmptyState
            title={t("emptyTitle")}
            description={t("emptyDescription")}
            actionHref="/tasks/new"
            actionLabel={t("create")}
          />
        </div>
      ) : null}
      <div className="mt-6 grid gap-4">
        {tasks.data?.content?.map((task) => (
          <article className="surface-panel p-5" key={task.publicId}>
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div>
                <StatusBadge status={task.status} />
                <h2 className="mt-3 text-xl font-bold">
                  <Link
                    className="hover:text-[var(--primary)]"
                    href={`/my/tasks/${task.publicId}`}
                  >
                    {task.title}
                  </Link>
                </h2>
                <p className="mt-1 text-sm text-[var(--muted)]">
                  {task.categoryTitle} · {task.cityName}
                </p>
              </div>
              <p className="font-bold">
                {task.fixedPrice
                  ? formatMoney(task.fixedPrice, task.currency ?? "UZS", locale)
                  : t("negotiable")}
              </p>
            </div>
            <p className="mt-4 line-clamp-2 text-sm">{task.description}</p>
            <div className="mt-5 flex flex-wrap items-center gap-2 border-t border-[var(--border)] pt-4">
              <Button asChild size="sm" variant="secondary">
                <Link href={`/my/tasks/${task.publicId}`}>{t("manage")}</Link>
              </Button>
              {task.status === "OPEN" ? (
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
                  onConfirm={() => cancelMutation.mutate(task.publicId ?? "")}
                />
              ) : null}
              {task.status === "COMPLETED" ||
              task.status === "CANCELED" ||
              task.status === "EXPIRED" ? (
                <Button
                  size="sm"
                  variant="secondary"
                  onClick={() => repeatMutation.mutate(task.publicId ?? "")}
                >
                  <Copy className="size-4" />
                  {t("repeat")}
                </Button>
              ) : null}
              <span className="ml-auto text-xs text-[var(--muted)]">
                {t("created", {
                  date: formatDate(task.createdAt, locale, t("notSpecified")),
                })}
              </span>
            </div>
          </article>
        ))}
      </div>
      {(tasks.data?.totalPages ?? 0) > 1 ? (
        <div className="mt-6 flex justify-end gap-2">
          <Button asChild disabled={page === 0} variant="secondary">
            <Link
              href={`/my/tasks?${status ? `status=${status}&` : ""}page=${Math.max(page - 1, 0)}`}
            >
              {t("previous")}
            </Link>
          </Button>
          <Button asChild disabled={tasks.data?.last} variant="secondary">
            <Link
              href={`/my/tasks?${status ? `status=${status}&` : ""}page=${page + 1}`}
            >
              {t("next")}
            </Link>
          </Button>
        </div>
      ) : null}
    </div>
  );
}
