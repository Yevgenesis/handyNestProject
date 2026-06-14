"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  ArrowUp,
  Download,
  FileText,
  Loader2,
  MessageSquare,
  Paperclip,
  ShieldAlert,
  UserRound,
} from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import Link from "next/link";
import { useEffect, useRef } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

import { useAuth } from "@/features/auth/auth-provider";
import {
  getAttachmentDownloadUrl,
  getChat,
  getDeal,
  sendChatMessage,
  uploadChatAttachment,
} from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import type { ChatMessage } from "@/shared/api/types";
import { formatDateTime } from "@/shared/lib/format";
import { Button } from "@/shared/ui/button";
import { ErrorState } from "@/shared/ui/page-state";
import { Skeleton } from "@/shared/ui/skeleton";
import { Textarea } from "@/shared/ui/textarea";

import { DealActionPanel } from "./deal-action-panel";
import { useChatTimeline } from "./use-chat-timeline";

const messageSchema = z.object({ text: z.string().trim().min(1).max(4000) });
type MessageValues = z.infer<typeof messageSchema>;

export function ChatPanel({ chatId }: { chatId: string }) {
  const t = useTranslations("chat");
  const chat = useQuery({
    queryKey: queryKeys.chat(chatId),
    queryFn: () => getChat(chatId),
  });
  const deal = useQuery({
    queryKey: queryKeys.deal(chat.data?.dealId ?? "pending"),
    queryFn: () => getDeal(chat.data!.dealId!),
    enabled: Boolean(chat.data?.dealId),
  });
  const timeline = useChatTimeline(chatId);

  if (chat.isLoading || deal.isLoading || timeline.isLoading) {
    return <Skeleton className="h-[680px]" />;
  }
  if (!chat.data || !deal.data || timeline.isError) {
    return <ErrorState title={t("loadError")} />;
  }

  const readOnly =
    chat.data.status === "READ_ONLY" ||
    deal.data.status === "COMPLETED" ||
    deal.data.status === "CANCELED";

  return (
    <div className="grid min-w-0 gap-5 lg:grid-cols-[minmax(0,1fr)_330px]">
      <section className="surface-panel order-2 flex min-h-[680px] min-w-0 flex-col overflow-hidden lg:order-1">
        <header className="border-b border-[var(--border)] p-4 sm:p-5">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div className="min-w-0">
              <p className="text-sm font-semibold text-[var(--primary)]">
                {t("protectedChat")}
              </p>
              <h1 className="mt-1 truncate text-xl font-black sm:text-2xl">
                {chat.data.taskTitle}
              </h1>
              <p className="mt-1 flex items-center gap-2 text-sm text-[var(--muted)]">
                <UserRound className="size-4" />
                {chat.data.participantRole === "CUSTOMER"
                  ? chat.data.performerDisplayName
                  : chat.data.customerDisplayName}
              </p>
            </div>
            <Button asChild size="sm" variant="secondary">
              <Link href={`/deals/${chat.data.dealId}`}>{t("openDeal")}</Link>
            </Button>
          </div>
        </header>

        <MessageTimeline
          hasMoreOlder={timeline.hasMoreOlder}
          isLoadingOlder={timeline.isLoadingOlder}
          loadOlder={timeline.loadOlder}
          messages={timeline.messages}
        />

        <div className="sticky bottom-0 z-10 border-t border-[var(--border)] bg-white p-3 sm:p-4">
          {readOnly ? (
            <p className="rounded-[6px] bg-[var(--surface-muted)] p-3 text-center text-sm text-[var(--muted)]">
              {t("readOnly")}
            </p>
          ) : (
            <MessageComposer appendMessages={timeline.append} chatId={chatId} />
          )}
        </div>
      </section>

      <aside className="order-1 grid content-start gap-4 lg:order-2">
        <DealActionPanel
          appendMessages={timeline.append}
          chat={chat.data}
          deal={deal.data}
        />
        <section className="surface-panel hidden p-5 lg:block">
          <MessageSquare className="size-6 text-[var(--primary)]" />
          <h2 className="mt-3 font-bold">{t("dealSummary")}</h2>
          <dl className="mt-4 grid gap-3 text-sm">
            <div>
              <dt className="text-[var(--muted)]">{t("customer")}</dt>
              <dd className="font-semibold">{chat.data.customerDisplayName}</dd>
            </div>
            <div>
              <dt className="text-[var(--muted)]">{t("performer")}</dt>
              <dd className="font-semibold">
                {chat.data.performerDisplayName}
              </dd>
            </div>
            <div>
              <dt className="text-[var(--muted)]">{t("revisions")}</dt>
              <dd className="font-semibold">
                {deal.data.revisionCount ?? 0} / 2
              </dd>
            </div>
          </dl>
          <div className="mt-5 flex gap-2 rounded-[6px] bg-amber-50 p-3 text-xs text-amber-900">
            <ShieldAlert className="size-4 shrink-0" />
            <span>{t("safetyHint")}</span>
          </div>
        </section>
      </aside>
    </div>
  );
}

function MessageTimeline({
  messages,
  hasMoreOlder,
  loadOlder,
  isLoadingOlder,
}: {
  messages: ChatMessage[];
  hasMoreOlder: boolean;
  loadOlder: () => void;
  isLoadingOlder: boolean;
}) {
  const t = useTranslations("chat");
  const locale = useLocale();
  const { user } = useAuth();
  const end = useRef<HTMLDivElement>(null);

  useEffect(() => {
    end.current?.scrollIntoView({ block: "end" });
  }, [messages.length]);

  return (
    <div className="min-h-0 flex-1 overflow-y-auto bg-[#f5f8f7] p-3 sm:p-5">
      {hasMoreOlder ? (
        <div className="mb-4 text-center">
          <Button
            disabled={isLoadingOlder}
            onClick={loadOlder}
            size="sm"
            variant="secondary"
          >
            {isLoadingOlder ? (
              <Loader2 className="size-4 animate-spin" />
            ) : null}
            {t("loadOlder")}
          </Button>
        </div>
      ) : null}
      {!messages.length ? (
        <p className="py-20 text-center text-sm text-[var(--muted)]">
          {t("startConversation")}
        </p>
      ) : null}
      <div className="grid gap-3">
        {messages.map((message) => {
          const system =
            message.messageType !== "TEXT" &&
            message.messageType !== "ATTACHMENT";
          if (system) {
            return (
              <div
                className="mx-auto max-w-xl rounded-[6px] border border-[var(--border)] bg-white px-4 py-2 text-center text-sm"
                key={message.publicId}
              >
                <p className="font-semibold">
                  {systemMessageLabel(message, t)}
                </p>
                {message.text ? (
                  <p className="mt-1 text-[var(--muted)]">{message.text}</p>
                ) : null}
              </div>
            );
          }
          const mine = message.senderId === user?.publicId;
          return (
            <article
              className={`max-w-[85%] rounded-[8px] px-4 py-3 sm:max-w-[70%] ${
                mine
                  ? "ml-auto bg-[var(--primary)] text-white"
                  : "mr-auto border border-[var(--border)] bg-white"
              }`}
              key={message.publicId}
            >
              <p
                className={`text-xs font-semibold ${mine ? "text-white/80" : "text-[var(--muted)]"}`}
              >
                {mine ? t("you") : message.senderDisplayName}
              </p>
              {message.messageType === "ATTACHMENT" && message.attachmentId ? (
                <AttachmentMessage
                  attachmentId={message.attachmentId}
                  filename={message.text || t("attachment")}
                  mine={mine}
                />
              ) : (
                <p className="mt-1 text-sm break-words whitespace-pre-wrap">
                  {message.text}
                </p>
              )}
              {message.riskFlag ? (
                <p
                  className={`mt-2 flex gap-1 text-xs ${mine ? "text-amber-100" : "text-[var(--amber)]"}`}
                >
                  <ShieldAlert className="size-3.5 shrink-0" />{" "}
                  {t("riskWarning")}
                </p>
              ) : null}
              <p
                className={`mt-2 text-right text-[11px] ${mine ? "text-white/70" : "text-[var(--muted)]"}`}
              >
                {formatDateTime(message.createdAt, locale, "")}
              </p>
            </article>
          );
        })}
      </div>
      <div ref={end} />
    </div>
  );
}

function AttachmentMessage({
  attachmentId,
  filename,
  mine,
}: {
  attachmentId: string;
  filename: string;
  mine: boolean;
}) {
  const t = useTranslations("chat");
  const download = useMutation({
    mutationFn: () => getAttachmentDownloadUrl(attachmentId),
    onSuccess(data) {
      if (data.downloadUrl)
        window.open(data.downloadUrl, "_blank", "noopener,noreferrer");
    },
    onError() {
      toast.error(t("downloadError"));
    },
  });
  return (
    <button
      className={`mt-2 flex w-full items-center gap-3 rounded-[6px] border p-3 text-left ${
        mine
          ? "border-white/30 bg-white/10"
          : "border-[var(--border)] bg-[var(--surface-muted)]"
      }`}
      onClick={() => download.mutate()}
      type="button"
    >
      <FileText className="size-5 shrink-0" />
      <span className="min-w-0 flex-1 truncate text-sm font-semibold">
        {filename}
      </span>
      <Download className="size-4 shrink-0" />
    </button>
  );
}

function MessageComposer({
  chatId,
  appendMessages,
}: {
  chatId: string;
  appendMessages: (messages: ChatMessage[]) => void;
}) {
  const t = useTranslations("chat");
  const queryClient = useQueryClient();
  const fileInput = useRef<HTMLInputElement>(null);
  const form = useForm<MessageValues>({
    resolver: zodResolver(messageSchema),
    defaultValues: { text: "" },
  });
  const message = useMutation({
    mutationFn: ({ text }: MessageValues) => sendChatMessage(chatId, text),
    onSuccess(data) {
      appendMessages([data]);
      form.reset();
      void queryClient.invalidateQueries({ queryKey: queryKeys.chats });
    },
    onError() {
      toast.error(t("sendError"));
    },
  });
  const attachment = useMutation({
    mutationFn: (file: File) => uploadChatAttachment(chatId, file),
    onSuccess(data) {
      if (data.message) appendMessages([data.message]);
      toast.success(t("attachmentSent"));
      void queryClient.invalidateQueries({ queryKey: queryKeys.chats });
    },
    onError() {
      toast.error(t("uploadError"));
    },
  });

  return (
    <form
      className="flex items-end gap-2"
      onSubmit={form.handleSubmit((values) => message.mutate(values))}
    >
      <input
        accept="image/*,application/pdf,text/plain,.doc,.docx,.xls,.xlsx,.zip"
        className="hidden"
        onChange={(event) => {
          const file = event.target.files?.[0];
          if (file) attachment.mutate(file);
          event.target.value = "";
        }}
        ref={fileInput}
        type="file"
      />
      <Button
        aria-label={t("attachFile")}
        disabled={attachment.isPending}
        onClick={() => fileInput.current?.click()}
        size="icon"
        type="button"
        variant="ghost"
      >
        {attachment.isPending ? (
          <Loader2 className="size-5 animate-spin" />
        ) : (
          <Paperclip className="size-5" />
        )}
      </Button>
      <Textarea
        aria-label={t("messageLabel")}
        className="min-h-11 flex-1 resize-none"
        placeholder={t("messagePlaceholder")}
        rows={1}
        {...form.register("text")}
      />
      <Button
        aria-label={t("send")}
        disabled={message.isPending}
        size="icon"
        type="submit"
      >
        <ArrowUp className="size-5" />
      </Button>
    </form>
  );
}

function systemMessageLabel(
  message: ChatMessage,
  t: {
    (key: string): string;
    has(key: string): boolean;
  },
) {
  const code = message.systemCode || message.messageType || "SYSTEM";
  return t.has(`system.${code}`) ? t(`system.${code}`) : t("system.SYSTEM");
}
