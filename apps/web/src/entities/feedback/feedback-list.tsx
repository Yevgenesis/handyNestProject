import { Star } from "lucide-react";

import type { Feedback } from "@/shared/api/types";
import { formatDate } from "@/shared/lib/format";

export function FeedbackList({
  feedbacks,
  locale,
  anonymousLabel,
  noTextLabel,
}: {
  feedbacks: Feedback[];
  locale: string;
  anonymousLabel: string;
  noTextLabel: string;
}) {
  return (
    <div className="divide-y divide-[var(--border)] border-y border-[var(--border)]">
      {feedbacks.map((feedback) => (
        <article className="py-5" key={feedback.publicId}>
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <p className="font-bold">
                {feedback.senderDisplayName || anonymousLabel}
              </p>
              <div
                aria-label={`${feedback.grade ?? 0} / 5`}
                className="mt-1 flex gap-0.5"
              >
                {Array.from({ length: 5 }, (_, index) => (
                  <Star
                    aria-hidden
                    className={
                      index < (feedback.grade ?? 0)
                        ? "size-4 fill-amber-400 text-amber-500"
                        : "size-4 text-neutral-300"
                    }
                    key={index}
                  />
                ))}
              </div>
            </div>
            <time className="text-xs text-[var(--muted)]">
              {formatDate(feedback.createdAt, locale, "")}
            </time>
          </div>
          <p className="mt-3 text-sm leading-6 text-[var(--muted)]">
            {feedback.text || noTextLabel}
          </p>
        </article>
      ))}
    </div>
  );
}
