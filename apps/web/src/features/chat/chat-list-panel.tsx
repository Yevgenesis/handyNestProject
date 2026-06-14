"use client";

import { useQuery } from "@tanstack/react-query";
import { MessageSquare, UserRound } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import Link from "next/link";

import { getMyChats } from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import { formatDateTime } from "@/shared/lib/format";
import { Badge } from "@/shared/ui/badge";
import { EmptyState, ErrorState } from "@/shared/ui/page-state";
import { Skeleton } from "@/shared/ui/skeleton";

export function ChatListPanel() {
  const t = useTranslations("chat");
  const locale = useLocale();
  const chats = useQuery({ queryKey: queryKeys.chats, queryFn: getMyChats });

  if (chats.isLoading) {
    return (
      <div className="grid gap-3">
        <Skeleton className="h-28" />
        <Skeleton className="h-28" />
      </div>
    );
  }
  if (chats.isError) return <ErrorState title={t("loadError")} />;
  if (!chats.data?.length) {
    return (
      <EmptyState title={t("emptyTitle")} description={t("emptyDescription")} />
    );
  }

  return (
    <div className="grid gap-3">
      {chats.data.map((chat) => {
        const counterpart =
          chat.participantRole === "CUSTOMER"
            ? chat.performerDisplayName
            : chat.customerDisplayName;
        return (
          <Link
            className="surface-panel grid gap-3 p-5 transition-colors hover:border-[var(--primary)] sm:grid-cols-[1fr_auto]"
            href={`/chats/${chat.publicId}`}
            key={chat.publicId}
          >
            <div className="min-w-0">
              <div className="flex items-center gap-2">
                <MessageSquare className="size-5 shrink-0 text-[var(--primary)]" />
                <h2 className="truncate text-lg font-bold">{chat.taskTitle}</h2>
              </div>
              <p className="mt-2 flex items-center gap-2 text-sm text-[var(--muted)]">
                <UserRound className="size-4" />
                {counterpart}
              </p>
              <p className="mt-3 truncate text-sm">
                {chat.lastMessageText || t("noMessages")}
              </p>
            </div>
            <div className="flex items-center justify-between gap-3 sm:flex-col sm:items-end">
              <span className="text-xs text-[var(--muted)]">
                {formatDateTime(
                  chat.lastMessageAt ?? chat.createdAt,
                  locale,
                  "",
                )}
              </span>
              {(chat.unreadCount ?? 0) > 0 ? (
                <Badge tone="green">
                  {t("unread", { count: chat.unreadCount ?? 0 })}
                </Badge>
              ) : null}
            </div>
          </Link>
        );
      })}
    </div>
  );
}
