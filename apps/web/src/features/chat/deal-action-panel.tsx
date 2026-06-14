"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  AlertTriangle,
  CheckCircle2,
  Phone,
  RotateCcw,
  Send,
  ShieldCheck,
  XCircle,
} from "lucide-react";
import { useTranslations } from "next-intl";
import { useMemo, useRef, useState } from "react";
import { useForm, useWatch } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

import {
  acceptWork,
  cancelDeal,
  newIdempotencyKey,
  openDispute,
  revealDealContact,
  requestRevision,
  submitWork,
  uploadChatAttachment,
} from "@/shared/api/marketplace-client";
import { getErrorMessage } from "@/shared/api/errors";
import { queryKeys } from "@/shared/api/query-keys";
import type { ChatMessage, Deal, DealChat } from "@/shared/api/types";
import { Button } from "@/shared/ui/button";
import { Field } from "@/shared/ui/field";
import { Input } from "@/shared/ui/input";
import { Textarea } from "@/shared/ui/textarea";

type Action = "submit" | "accept" | "revision" | "dispute";
const actionSchema = z.object({
  message: z.string().max(2000),
  reason: z.string().max(2000),
  description: z.string().max(4000),
});
type Values = z.infer<typeof actionSchema>;

function schemaFor(action: Action) {
  return actionSchema.superRefine((values, context) => {
    if (
      (action === "revision" || action === "dispute") &&
      !values.reason.trim()
    ) {
      context.addIssue({
        code: "custom",
        message: "required",
        path: ["reason"],
      });
    }
    if (action === "dispute" && values.reason.length > 160) {
      context.addIssue({
        code: "too_big",
        maximum: 160,
        origin: "string",
        inclusive: true,
        message: "too_long",
        path: ["reason"],
      });
    }
  });
}

export function DealActionPanel({
  chat,
  deal,
  appendMessages,
}: {
  chat: DealChat;
  deal: Deal;
  appendMessages: (messages: ChatMessage[]) => void;
}) {
  const t = useTranslations("chat.actions");
  const apiErrors = useTranslations("apiErrors");
  const queryClient = useQueryClient();
  const [action, setAction] = useState<Action>();
  const [showCancel, setShowCancel] = useState(false);
  const [contactValue, setContactValue] = useState<string>();
  const revealKey = useRef(newIdempotencyKey("contact-reveal"));
  const canSubmit =
    chat.participantRole === "PERFORMER" &&
    (deal.status === "ACTIVE" || deal.status === "REVISION_REQUESTED");
  const canReview =
    chat.participantRole === "CUSTOMER" && deal.status === "WORK_SUBMITTED";
  const canReveal =
    deal.status === "ACTIVE" ||
    deal.status === "WORK_SUBMITTED" ||
    deal.status === "REVISION_REQUESTED";
  const canCancel =
    deal.status === "ACTIVE" || deal.status === "REVISION_REQUESTED";
  const reveal = useMutation({
    mutationFn: () => revealDealContact(deal.publicId!, revealKey.current),
    onSuccess(data) {
      setContactValue(data.contactValue);
      toast.success(t("contactRevealed"));
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });

  const coordination =
    canReveal || canCancel || contactValue ? (
      <section className="rounded-[8px] border border-[var(--border)] bg-white p-4">
        <div className="flex items-start gap-3">
          <ShieldCheck className="mt-0.5 size-5 shrink-0 text-[var(--primary)]" />
          <div>
            <h2 className="font-bold">{t("coordinationTitle")}</h2>
            <p className="mt-1 text-sm text-[var(--muted)]">
              {t("coordinationHint")}
            </p>
          </div>
        </div>
        {contactValue ? (
          <div className="mt-4 rounded-[6px] bg-[var(--primary-soft)] p-3">
            <p className="text-xs text-[var(--muted)]">{t("contactLabel")}</p>
            <a className="mt-1 block font-bold" href={`tel:${contactValue}`}>
              {contactValue}
            </a>
            <p className="mt-1 text-xs text-[var(--muted)]">
              {t("contactSessionOnly")}
            </p>
          </div>
        ) : null}
        <div className="mt-4 flex flex-wrap gap-2">
          {canReveal && !contactValue ? (
            <Button
              disabled={reveal.isPending}
              onClick={() => reveal.mutate()}
              size="sm"
              variant="secondary"
            >
              <Phone className="size-4" />
              {reveal.isPending ? t("pending") : t("revealContact")}
            </Button>
          ) : null}
          {canCancel ? (
            <Button
              onClick={() => setShowCancel((current) => !current)}
              size="sm"
              variant="danger"
            >
              <XCircle className="size-4" /> {t("cancelDeal")}
            </Button>
          ) : null}
        </div>
        {showCancel ? (
          <CancelDealForm
            dealId={deal.publicId!}
            onClose={() => setShowCancel(false)}
            onSuccess={async () => {
              await Promise.all([
                queryClient.invalidateQueries({
                  queryKey: queryKeys.deal(deal.publicId!),
                }),
                queryClient.invalidateQueries({
                  queryKey: queryKeys.chat(chat.publicId!),
                }),
                queryClient.invalidateQueries({ queryKey: queryKeys.chats }),
                queryClient.invalidateQueries({ queryKey: queryKeys.myDeals }),
              ]);
            }}
          />
        ) : null}
      </section>
    ) : null;

  if (deal.status === "DISPUTED") {
    return (
      <div className="grid gap-4">
        {coordination}
        <div className="rounded-[8px] border border-red-200 bg-red-50 p-4 text-sm text-red-900">
          <div className="flex items-center gap-2 font-bold">
            <AlertTriangle className="size-5" /> {t("disputedTitle")}
          </div>
          <p className="mt-2">{t("disputedDescription")}</p>
        </div>
      </div>
    );
  }
  if (!canSubmit && !canReview) return coordination;

  return (
    <div className="grid gap-4">
      {coordination}
      <section className="rounded-[8px] border border-[var(--border)] bg-[var(--surface-muted)] p-4">
        <h2 className="font-bold">{t("title")}</h2>
        <p className="mt-1 text-sm text-[var(--muted)]">
          {canSubmit ? t("performerHint") : t("customerHint")}
        </p>
        <div className="mt-4 flex flex-wrap gap-2">
          {canSubmit ? (
            <Button onClick={() => setAction("submit")} size="sm">
              <Send className="size-4" /> {t("submit")}
            </Button>
          ) : (
            <>
              <Button onClick={() => setAction("accept")} size="sm">
                <CheckCircle2 className="size-4" /> {t("accept")}
              </Button>
              <Button
                disabled={(deal.revisionCount ?? 0) >= 2}
                onClick={() => setAction("revision")}
                size="sm"
                variant="secondary"
              >
                <RotateCcw className="size-4" /> {t("revision")}
              </Button>
              <Button
                onClick={() => setAction("dispute")}
                size="sm"
                variant="danger"
              >
                <AlertTriangle className="size-4" /> {t("dispute")}
              </Button>
            </>
          )}
        </div>
        {(deal.revisionCount ?? 0) >= 2 && canReview ? (
          <p className="mt-3 text-sm text-[var(--amber)]">
            {t("revisionLimit")}
          </p>
        ) : null}
        {action ? (
          <ActionForm
            action={action}
            appendMessages={appendMessages}
            chatId={chat.publicId!}
            dealId={deal.publicId!}
            onClose={() => setAction(undefined)}
          />
        ) : null}
      </section>
    </div>
  );
}

const cancelSchema = z.object({
  reason: z.enum([
    "CUSTOMER_CHANGED_MIND",
    "PERFORMER_NOT_RESPONDING",
    "CUSTOMER_NOT_RESPONDING",
    "PRICE_NOT_ACCEPTED",
    "SCHEDULE_NOT_ACCEPTED",
    "TASK_NO_LONGER_ACTUAL",
    "WRONG_PERFORMER_SELECTED",
    "SAFETY_CONCERN",
    "DUPLICATE_TASK",
    "OTHER",
  ]),
  comment: z.string().max(1000),
});
type CancelValues = z.infer<typeof cancelSchema>;

function CancelDealForm({
  dealId,
  onClose,
  onSuccess,
}: {
  dealId: string;
  onClose: () => void;
  onSuccess: () => Promise<void>;
}) {
  const t = useTranslations("chat.actions");
  const apiErrors = useTranslations("apiErrors");
  const idempotencyKey = useRef(newIdempotencyKey("deal-cancel"));
  const form = useForm<CancelValues>({
    resolver: zodResolver(cancelSchema),
    defaultValues: { reason: "CUSTOMER_CHANGED_MIND", comment: "" },
  });
  const reason = useWatch({ control: form.control, name: "reason" });
  const mutation = useMutation({
    mutationFn: (values: CancelValues) =>
      cancelDeal(
        dealId,
        { reason: values.reason, comment: values.comment || undefined },
        idempotencyKey.current,
      ),
    async onSuccess() {
      await onSuccess();
      toast.success(t("dealCanceled"));
      onClose();
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });

  return (
    <form
      className="mt-4 grid gap-4 border-t border-[var(--border)] pt-4"
      onSubmit={form.handleSubmit((values) => mutation.mutate(values))}
    >
      <Field label={t("cancelReason")}>
        <select
          className="h-10 w-full rounded-[6px] border border-[var(--border)] bg-white px-3 text-sm"
          {...form.register("reason")}
        >
          {cancelSchema.shape.reason.options.map((value) => (
            <option key={value} value={value}>
              {t(`cancelReasons.${value}`)}
            </option>
          ))}
        </select>
      </Field>
      <Field label={t("cancelComment")}>
        <Textarea rows={3} {...form.register("comment")} />
      </Field>
      {reason === "SAFETY_CONCERN" ? (
        <p className="rounded-[6px] bg-amber-50 p-3 text-sm text-amber-900">
          {t("safetyComplaintHint")}
        </p>
      ) : null}
      <div className="flex justify-end gap-2">
        <Button onClick={onClose} type="button" variant="ghost">
          {t("cancel")}
        </Button>
        <Button disabled={mutation.isPending} type="submit" variant="danger">
          {mutation.isPending ? t("pending") : t("confirmCancel")}
        </Button>
      </div>
    </form>
  );
}

function ActionForm({
  action,
  chatId,
  dealId,
  appendMessages,
  onClose,
}: {
  action: Action;
  chatId: string;
  dealId: string;
  appendMessages: (messages: ChatMessage[]) => void;
  onClose: () => void;
}) {
  const t = useTranslations("chat.actions");
  const queryClient = useQueryClient();
  const [files, setFiles] = useState<File[]>([]);
  const idempotencyKey = useRef(newIdempotencyKey(`chat-${action}`));
  const schema = useMemo(() => schemaFor(action), [action]);
  const form = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: { message: "", reason: "", description: "" },
  });
  const mutation = useMutation({
    async mutationFn(values: Values) {
      if (action === "submit") {
        for (const file of files) {
          const completed = await uploadChatAttachment(chatId, file);
          if (completed.message) appendMessages([completed.message]);
        }
        return submitWork(chatId, values.message, idempotencyKey.current);
      }
      if (action === "accept") {
        return acceptWork(chatId, values.message, idempotencyKey.current);
      }
      if (action === "revision") {
        return requestRevision(chatId, values.reason, idempotencyKey.current);
      }
      return openDispute(
        chatId,
        { reason: values.reason, description: values.description || undefined },
        idempotencyKey.current,
      );
    },
    async onSuccess() {
      await Promise.all([
        queryClient.invalidateQueries({ queryKey: queryKeys.deal(dealId) }),
        queryClient.invalidateQueries({ queryKey: queryKeys.chat(chatId) }),
        queryClient.invalidateQueries({ queryKey: queryKeys.chats }),
        queryClient.invalidateQueries({ queryKey: queryKeys.myDeals }),
      ]);
      toast.success(t("success"));
      onClose();
    },
    onError() {
      toast.error(t("error"));
    },
  });

  return (
    <form
      className="mt-4 grid gap-4 border-t border-[var(--border)] pt-4"
      onSubmit={form.handleSubmit((values) => mutation.mutate(values))}
    >
      {action === "submit" || action === "accept" ? (
        <Field
          label={t(action === "submit" ? "submissionMessage" : "acceptMessage")}
        >
          <Textarea rows={3} {...form.register("message")} />
        </Field>
      ) : null}
      {action === "revision" || action === "dispute" ? (
        <Field
          error={form.formState.errors.reason ? t("reasonRequired") : undefined}
          label={t(action === "revision" ? "revisionReason" : "disputeReason")}
        >
          <Input {...form.register("reason")} />
        </Field>
      ) : null}
      {action === "dispute" ? (
        <Field label={t("disputeDescription")}>
          <Textarea rows={4} {...form.register("description")} />
        </Field>
      ) : null}
      {action === "submit" ? (
        <Field label={t("workFiles")}>
          <Input
            accept="image/*,application/pdf,text/plain,.doc,.docx,.xls,.xlsx,.zip"
            multiple
            onChange={(event) => setFiles(Array.from(event.target.files ?? []))}
            type="file"
          />
        </Field>
      ) : null}
      <div className="flex justify-end gap-2">
        <Button onClick={onClose} type="button" variant="ghost">
          {t("cancel")}
        </Button>
        <Button disabled={mutation.isPending} type="submit">
          {mutation.isPending ? t("pending") : t("confirm")}
        </Button>
      </div>
    </form>
  );
}
