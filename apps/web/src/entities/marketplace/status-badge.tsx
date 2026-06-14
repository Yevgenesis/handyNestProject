"use client";

import { useTranslations } from "next-intl";

import { Badge } from "@/shared/ui/badge";

export function StatusBadge({ status }: { status?: string }) {
  const t = useTranslations("status");
  const tone =
    status === "COMPLETED" || status === "ACCEPTED"
      ? "green"
      : status === "PENDING" || status === "WORK_SUBMITTED"
        ? "amber"
        : status === "ACTIVE" || status === "IN_PROGRESS"
          ? "blue"
          : "neutral";
  return (
    <Badge tone={tone}>
      {t.has(status ?? "") ? t(status ?? "") : t("UNKNOWN")}
    </Badge>
  );
}
