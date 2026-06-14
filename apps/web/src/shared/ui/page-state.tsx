import { AlertCircle, Inbox } from "lucide-react";
import Link from "next/link";

import { Button } from "@/shared/ui/button";

export function EmptyState({
  title,
  description,
  actionHref,
  actionLabel,
}: {
  title: string;
  description: string;
  actionHref?: string;
  actionLabel?: string;
}) {
  return (
    <div className="flex min-h-52 flex-col items-center justify-center border-y border-[var(--border)] px-6 py-12 text-center">
      <Inbox className="mb-4 size-8 text-[var(--primary)]" aria-hidden />
      <h2 className="text-lg font-bold">{title}</h2>
      <p className="mt-2 max-w-lg text-sm text-[var(--muted)]">{description}</p>
      {actionHref && actionLabel ? (
        <Button asChild className="mt-5">
          <Link href={actionHref}>{actionLabel}</Link>
        </Button>
      ) : null}
    </div>
  );
}

export function ErrorState({
  message,
  title,
}: {
  message?: string;
  title?: string;
}) {
  return (
    <div className="flex min-h-44 items-center gap-3 border border-red-200 bg-red-50 p-5 text-red-900">
      <AlertCircle className="size-5 shrink-0" aria-hidden />
      <p>{message ?? title}</p>
    </div>
  );
}
