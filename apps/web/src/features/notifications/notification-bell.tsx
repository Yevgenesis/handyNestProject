"use client";

import { useQuery } from "@tanstack/react-query";
import { Bell } from "lucide-react";
import { useTranslations } from "next-intl";
import Link from "next/link";

import { getNotifications } from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import { Button } from "@/shared/ui/button";

export function NotificationBell() {
  const t = useTranslations("notifications");
  const notifications = useQuery({
    queryKey: queryKeys.unreadNotifications,
    queryFn: () => getNotifications({ unreadOnly: true, page: 0, size: 1 }),
    refetchInterval: 30_000,
    refetchOnWindowFocus: true,
  });
  const count = notifications.data?.totalElements ?? 0;

  return (
    <Button asChild className="relative" size="icon" variant="ghost">
      <Link aria-label={t("open", { count })} href="/notifications">
        <Bell className="size-5" aria-hidden />
        {count > 0 ? (
          <span className="absolute top-0.5 right-0.5 grid min-h-4 min-w-4 place-items-center rounded-full bg-[var(--red)] px-1 text-[10px] leading-none font-bold text-white">
            {count > 99 ? "99+" : count}
          </span>
        ) : null}
      </Link>
    </Button>
  );
}
