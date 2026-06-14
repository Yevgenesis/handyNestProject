"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Star } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { useMemo, useRef } from "react";
import { useForm, useWatch } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

import { FeedbackList } from "@/entities/feedback/feedback-list";
import { useAuth } from "@/features/auth/auth-provider";
import {
  createTaskFeedback,
  getTaskFeedbacks,
  newIdempotencyKey,
} from "@/shared/api/marketplace-client";
import { getErrorMessage } from "@/shared/api/errors";
import { queryKeys } from "@/shared/api/query-keys";
import { Button } from "@/shared/ui/button";
import { Field } from "@/shared/ui/field";
import { Skeleton } from "@/shared/ui/skeleton";
import { Textarea } from "@/shared/ui/textarea";

type FeedbackValues = { grade: number; text: string };

export function FeedbackPanel({
  taskId,
  dealStatus,
}: {
  taskId?: string;
  dealStatus?: string;
}) {
  const t = useTranslations("feedback");
  const apiErrors = useTranslations("apiErrors");
  const locale = useLocale();
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const retry = useRef<{ fingerprint: string; key: string } | null>(null);
  const schema = useMemo(
    () =>
      z.object({
        grade: z.number().int().min(1, t("ratingRequired")).max(5),
        text: z.string().trim().max(2000, t("textTooLong")),
      }),
    [t],
  );
  const form = useForm<FeedbackValues>({
    resolver: zodResolver(schema),
    defaultValues: { grade: 0, text: "" },
  });
  const selectedGrade = useWatch({ control: form.control, name: "grade" });
  const feedbacks = useQuery({
    queryKey: queryKeys.taskFeedbacks(taskId ?? ""),
    queryFn: () => getTaskFeedbacks(taskId!),
    enabled: dealStatus === "COMPLETED" && Boolean(taskId),
  });
  const create = useMutation({
    mutationFn: async (values: FeedbackValues) => {
      const body = {
        grade: values.grade,
        text: values.text.trim() || undefined,
      };
      const fingerprint = JSON.stringify(body);
      if (!retry.current || retry.current.fingerprint !== fingerprint) {
        retry.current = { fingerprint, key: newIdempotencyKey("feedback") };
      }
      return createTaskFeedback(taskId!, body, retry.current.key);
    },
    onSuccess: async () => {
      retry.current = null;
      toast.success(t("created"));
      await queryClient.invalidateQueries({
        queryKey: queryKeys.taskFeedbacks(taskId ?? ""),
      });
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });

  if (dealStatus !== "COMPLETED" || !taskId) return null;
  if (feedbacks.isLoading) return <Skeleton className="h-64" />;

  const ownFeedback = (feedbacks.data ?? []).find(
    (feedback) => feedback.senderId === user?.publicId,
  );
  if (ownFeedback) {
    return (
      <section className="mt-6 border-t border-[var(--border)] pt-6">
        <h2 className="text-xl font-bold">{t("yourFeedback")}</h2>
        <p className="mt-2 text-sm text-[var(--muted)]">
          {t("alreadyCreated")}
        </p>
        <div className="mt-4">
          <FeedbackList
            anonymousLabel={t("anonymous")}
            feedbacks={[ownFeedback]}
            locale={locale}
            noTextLabel={t("withoutText")}
          />
        </div>
      </section>
    );
  }

  return (
    <section className="mt-6 border-t border-[var(--border)] pt-6">
      <h2 className="text-xl font-bold">{t("leaveFeedback")}</h2>
      <p className="mt-2 text-sm text-[var(--muted)]">{t("description")}</p>
      <form
        className="mt-5 grid gap-5"
        onSubmit={form.handleSubmit((values) => create.mutate(values))}
      >
        <Field label={t("rating")} error={form.formState.errors.grade?.message}>
          <div
            className="flex gap-1"
            role="radiogroup"
            aria-label={t("rating")}
          >
            {Array.from({ length: 5 }, (_, index) => {
              const grade = index + 1;
              const selected = grade <= selectedGrade;
              return (
                <button
                  aria-checked={selectedGrade === grade}
                  aria-label={t("ratingValue", { grade })}
                  className="grid size-10 place-items-center rounded-[6px] hover:bg-amber-50 focus-visible:outline-2 focus-visible:outline-amber-500"
                  key={grade}
                  onClick={() =>
                    form.setValue("grade", grade, { shouldValidate: true })
                  }
                  role="radio"
                  type="button"
                >
                  <Star
                    className={
                      selected
                        ? "size-6 fill-amber-400 text-amber-500"
                        : "size-6 text-neutral-300"
                    }
                  />
                </button>
              );
            })}
          </div>
        </Field>
        <Field label={t("comment")} error={form.formState.errors.text?.message}>
          <Textarea
            maxLength={2000}
            placeholder={t("commentPlaceholder")}
            {...form.register("text")}
          />
        </Field>
        <Button className="w-fit" disabled={create.isPending} type="submit">
          {create.isPending ? t("submitting") : t("submit")}
        </Button>
      </form>
    </section>
  );
}
