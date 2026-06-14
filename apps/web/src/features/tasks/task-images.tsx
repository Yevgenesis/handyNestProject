"use client";

/* eslint-disable @next/next/no-img-element */

import { useQueries, useQuery } from "@tanstack/react-query";
import { useTranslations } from "next-intl";

import {
  getTaskAttachmentDownloadUrl,
  getTaskAttachments,
} from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import { Skeleton } from "@/shared/ui/skeleton";

export function TaskImages({ taskId }: { taskId: string }) {
  const t = useTranslations("taskOwner");
  const attachments = useQuery({
    queryKey: queryKeys.taskAttachments(taskId),
    queryFn: () => getTaskAttachments(taskId),
  });
  const downloads = useQueries({
    queries: (attachments.data ?? []).map((attachment) => ({
      queryKey: ["task-image-url", taskId, attachment.publicId],
      queryFn: () =>
        getTaskAttachmentDownloadUrl(taskId, attachment.publicId ?? ""),
      staleTime: 4 * 60_000,
    })),
  });
  if (attachments.isLoading)
    return <Skeleton className="aspect-[16/6] w-full" />;
  if (!attachments.data?.length) return null;
  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
      {attachments.data.map((attachment, index) => {
        const url = downloads[index]?.data?.downloadUrl;
        return url ? (
          <img
            alt={attachment.originalFilename ?? t("imageAlt")}
            className="aspect-[4/3] w-full rounded-[6px] border border-[var(--border)] object-cover"
            key={attachment.publicId}
            src={url}
          />
        ) : (
          <Skeleton className="aspect-[4/3] w-full" key={attachment.publicId} />
        );
      })}
    </div>
  );
}
