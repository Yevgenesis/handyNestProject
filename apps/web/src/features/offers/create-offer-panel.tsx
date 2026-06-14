"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Send } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import Link from "next/link";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

import { useAuth } from "@/features/auth/auth-provider";
import { getErrorMessage } from "@/shared/api/errors";
import {
  createOffer,
  getMyOffers,
  getPerformerMe,
  newIdempotencyKey,
} from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import { formatMoney } from "@/shared/lib/format";
import { Button } from "@/shared/ui/button";
import { Field } from "@/shared/ui/field";
import { Input } from "@/shared/ui/input";
import { Textarea } from "@/shared/ui/textarea";

const schema = z.object({
  proposedPrice: z.number().positive(),
  estimatedDuration: z.string().max(120),
  message: z.string().trim().min(10).max(2000),
  includesMaterials: z.boolean(),
});
type Values = z.infer<typeof schema>;

export function CreateOfferPanel({
  taskId,
  customerId,
  status,
  currency = "UZS",
}: {
  taskId: string;
  customerId?: string;
  status?: string;
  currency?: string;
}) {
  const t = useTranslations("createOffer");
  const apiErrors = useTranslations("apiErrors");
  const locale = useLocale();
  const { status: authStatus, user } = useAuth();
  const queryClient = useQueryClient();
  const performer = useQuery({
    queryKey: queryKeys.performerMe,
    queryFn: getPerformerMe,
    enabled: authStatus === "authenticated" && user?.publicId !== customerId,
    retry: false,
  });
  const myOffers = useQuery({
    queryKey: queryKeys.myOffers,
    queryFn: getMyOffers,
    enabled: Boolean(performer.data),
  });
  const existing = myOffers.data?.find(
    (offer) => offer.taskId === taskId && offer.status === "PENDING",
  );
  const form = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: {
      proposedPrice: 0,
      estimatedDuration: "",
      message: "",
      includesMaterials: false,
    },
  });
  const mutation = useMutation({
    mutationFn: (values: Values) =>
      createOffer(taskId, { ...values, currency }, newIdempotencyKey("offer")),
    onSuccess() {
      void queryClient.invalidateQueries({ queryKey: queryKeys.myOffers });
      toast.success(t("success"));
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });

  if (status !== "OPEN") return null;
  if (authStatus !== "authenticated")
    return (
      <Button asChild className="mt-6 w-full" size="lg">
        <Link href={`/login?next=/tasks/${taskId}`}>{t("login")}</Link>
      </Button>
    );
  if (user?.publicId === customerId)
    return (
      <Button asChild className="mt-6 w-full" variant="secondary">
        <Link href={`/my/tasks/${taskId}`}>{t("manageOwn")}</Link>
      </Button>
    );
  if (performer.isError)
    return (
      <div className="mt-6 rounded-[8px] bg-[var(--surface-muted)] p-4 text-sm">
        <p className="font-bold">{t("profileRequired")}</p>
        <p className="mt-1 text-[var(--muted)]">
          {t("profileRequiredDescription")}
        </p>
        <Button asChild className="mt-4">
          <Link href="/performer/profile">{t("createProfile")}</Link>
        </Button>
      </div>
    );
  if (existing)
    return (
      <div className="mt-6 rounded-[8px] bg-[var(--primary-soft)] p-4">
        <p className="font-bold text-[var(--primary-strong)]">
          {t("alreadySent")}
        </p>
        <p className="mt-1 text-sm">
          {formatMoney(
            existing.proposedPrice ?? 0,
            existing.currency ?? currency,
            locale,
          )}
        </p>
        <Button asChild className="mt-3" variant="secondary">
          <Link href="/my/offers">{t("myOffers")}</Link>
        </Button>
      </div>
    );
  return (
    <form
      className="mt-6 grid gap-4 border-t border-[var(--border)] pt-5"
      onSubmit={form.handleSubmit((values) => mutation.mutate(values))}
    >
      <h2 className="text-lg font-bold">{t("title")}</h2>
      <Field
        label={t("price")}
        error={form.formState.errors.proposedPrice && t("fieldError")}
      >
        <Input
          min="1"
          type="number"
          {...form.register("proposedPrice", { valueAsNumber: true })}
        />
      </Field>
      <Field label={t("duration")}>
        <Input
          placeholder={t("durationPlaceholder")}
          {...form.register("estimatedDuration")}
        />
      </Field>
      <Field
        label={t("message")}
        error={form.formState.errors.message && t("fieldError")}
      >
        <Textarea {...form.register("message")} />
      </Field>
      <label className="flex items-center gap-2 text-sm">
        <input type="checkbox" {...form.register("includesMaterials")} />
        {t("materials")}
      </label>
      <Button disabled={mutation.isPending} type="submit">
        <Send className="size-4" />
        {mutation.isPending ? t("sending") : t("send")}
      </Button>
    </form>
  );
}
