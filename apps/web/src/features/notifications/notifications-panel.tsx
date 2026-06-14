"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Bell, CheckCheck, ChevronLeft, ChevronRight } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { toast } from "sonner";

import { notificationHref } from "@/features/notifications/notification-link";
import {
  getNotifications,
  markAllNotificationsRead,
  markNotificationRead,
} from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import type { Notification } from "@/shared/api/types";
import { formatDateTime } from "@/shared/lib/format";
import { cn } from "@/shared/lib/cn";
import { Button } from "@/shared/ui/button";
import { EmptyState, ErrorState } from "@/shared/ui/page-state";
import { Skeleton } from "@/shared/ui/skeleton";

export function NotificationsPanel() {
  const t = useTranslations("notifications");
  const common = useTranslations("common");
  const locale = useLocale();
  const router = useRouter();
  const queryClient = useQueryClient();
  const [unreadOnly, setUnreadOnly] = useState(false);
  const [page, setPage] = useState(0);
  const notifications = useQuery({
    queryKey: queryKeys.notifications(unreadOnly, page),
    queryFn: () => getNotifications({ unreadOnly, page, size: 20 }),
    refetchInterval: 30_000,
    refetchOnWindowFocus: true,
  });

  const refresh = () =>
    queryClient.invalidateQueries({ queryKey: ["notifications"] });
  const read = useMutation({
    mutationFn: markNotificationRead,
    onSuccess: refresh,
  });
  const readAll = useMutation({
    mutationFn: markAllNotificationsRead,
    onSuccess: (result) => {
      toast.success(t("markedAll", { count: result.updatedCount ?? 0 }));
      void refresh();
    },
  });

  async function openNotification(notification: Notification) {
    if (notification.publicId && !notification.readAt) {
      await read.mutateAsync(notification.publicId);
    }
    router.push(notificationHref(notification));
  }

  const items = notifications.data?.content ?? [];
  const totalPages = notifications.data?.totalPages ?? 0;

  return (
    <div>
      <div className="flex flex-col justify-between gap-4 border-b border-[var(--border)] pb-5 sm:flex-row sm:items-end">
        <div>
          <p className="text-sm font-bold text-[var(--primary-strong)]">
            {t("eyebrow")}
          </p>
          <h1 className="mt-2 text-3xl font-black sm:text-4xl">{t("title")}</h1>
          <p className="mt-2 text-[var(--muted)]">{t("description")}</p>
        </div>
        <Button
          disabled={readAll.isPending || items.length === 0}
          onClick={() => readAll.mutate()}
          variant="secondary"
        >
          <CheckCheck className="size-4" aria-hidden />
          {t("markAllRead")}
        </Button>
      </div>

      <div
        className="mt-5 flex gap-2"
        role="group"
        aria-label={t("filterLabel")}
      >
        <Button
          onClick={() => {
            setUnreadOnly(false);
            setPage(0);
          }}
          variant={unreadOnly ? "ghost" : "secondary"}
        >
          {t("all")}
        </Button>
        <Button
          onClick={() => {
            setUnreadOnly(true);
            setPage(0);
          }}
          variant={unreadOnly ? "secondary" : "ghost"}
        >
          {t("unreadOnly")}
        </Button>
      </div>

      <div className="mt-5">
        {notifications.isLoading ? (
          <div className="grid gap-2">
            {Array.from({ length: 5 }, (_, index) => (
              <Skeleton className="h-24" key={index} />
            ))}
          </div>
        ) : notifications.isError ? (
          <ErrorState message={t("loadError")} />
        ) : items.length === 0 ? (
          <EmptyState
            title={t("emptyTitle")}
            description={t("emptyDescription")}
          />
        ) : (
          <div className="divide-y divide-[var(--border)] border-y border-[var(--border)]">
            {items.map((notification) => (
              <button
                className={cn(
                  "flex w-full items-start gap-4 px-3 py-4 text-left transition-colors hover:bg-[var(--surface-muted)]",
                  !notification.readAt && "bg-[var(--primary-soft)]/45",
                )}
                disabled={read.isPending}
                key={notification.publicId}
                onClick={() => void openNotification(notification)}
                type="button"
              >
                <span className="mt-0.5 grid size-9 shrink-0 place-items-center rounded-[6px] bg-white text-[var(--primary-strong)] shadow-sm">
                  <Bell className="size-4" aria-hidden />
                </span>
                <span className="min-w-0 flex-1">
                  <span className="flex flex-wrap items-center justify-between gap-2">
                    <strong>{notificationTitle(notification, t)}</strong>
                    <time className="text-xs text-[var(--muted)]">
                      {formatDateTime(notification.createdAt, locale, "")}
                    </time>
                  </span>
                  <span className="mt-1 block text-sm leading-6 text-[var(--muted)]">
                    {notificationBody(notification, t)}
                  </span>
                </span>
                {!notification.readAt ? (
                  <span
                    className="mt-2 size-2 shrink-0 rounded-full bg-[var(--primary)]"
                    aria-label={t("unread")}
                  />
                ) : null}
              </button>
            ))}
          </div>
        )}
      </div>

      {totalPages > 1 ? (
        <nav
          className="mt-6 flex items-center justify-between"
          aria-label={t("pagination")}
        >
          <Button
            disabled={page <= 0}
            onClick={() => setPage((value) => Math.max(0, value - 1))}
            variant="secondary"
          >
            <ChevronLeft className="size-4" aria-hidden />
            {common("previous")}
          </Button>
          <span className="text-sm text-[var(--muted)]">
            {common("pageOf", { page: page + 1, totalPages })}
          </span>
          <Button
            disabled={page + 1 >= totalPages}
            onClick={() => setPage((value) => value + 1)}
            variant="secondary"
          >
            {common("next")}
            <ChevronRight className="size-4" aria-hidden />
          </Button>
        </nav>
      ) : null}
    </div>
  );
}

type NotificationTranslator = ReturnType<
  typeof useTranslations<"notifications">
>;

function notificationTitle(
  notification: Notification,
  t: NotificationTranslator,
) {
  const key = `types.${notification.type}.title` as Parameters<typeof t>[0];
  return t.has(key) ? t(key) : (notification.title ?? t("unknownTitle"));
}

function notificationBody(
  notification: Notification,
  t: NotificationTranslator,
) {
  const key = `types.${notification.type}.body` as Parameters<typeof t>[0];
  return t.has(key) ? t(key) : (notification.body ?? t("unknownBody"));
}
