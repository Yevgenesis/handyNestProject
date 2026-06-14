"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useCallback, useEffect, useState } from "react";

import { getChatTimeline, markChatRead } from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import type { ChatMessage } from "@/shared/api/types";

import { mergeChatMessages } from "./chat-timeline";

export function useChatTimeline(chatId: string) {
  const queryClient = useQueryClient();
  const [olderMessages, setOlderMessages] = useState<ChatMessage[]>([]);
  const [newerMessages, setNewerMessages] = useState<ChatMessage[]>([]);
  const [oldestCursor, setOldestCursor] = useState<string>();
  const [newestCursor, setNewestCursor] = useState<string>();
  const [olderPageAvailable, setOlderPageAvailable] = useState<boolean>();

  const initial = useQuery({
    queryKey: queryKeys.chatTimeline(chatId),
    queryFn: () => getChatTimeline(chatId, { limit: 40 }),
  });

  const effectiveOldestCursor = oldestCursor ?? initial.data?.oldestCursor;
  const effectiveNewestCursor = newestCursor ?? initial.data?.newestCursor;
  const messages = mergeChatMessages(
    mergeChatMessages(olderMessages, initial.data?.messages ?? []),
    newerMessages,
  );
  const hasMoreOlder =
    olderPageAvailable ?? Boolean(initial.data?.hasMoreOlder);

  useEffect(() => {
    let active = true;
    const poll = async () => {
      if (document.visibilityState !== "visible") return;
      const data = await getChatTimeline(chatId, {
        after: effectiveNewestCursor || undefined,
        limit: 100,
      });
      if (!active || !data.messages?.length) return;
      setNewerMessages((current) =>
        mergeChatMessages(current, data.messages ?? []),
      );
      setNewestCursor(data.newestCursor ?? effectiveNewestCursor);
    };
    const timer = window.setInterval(() => void poll(), 4000);
    return () => {
      active = false;
      window.clearInterval(timer);
    };
  }, [chatId, effectiveNewestCursor]);

  useEffect(() => {
    if (!messages.length) return;
    void markChatRead(chatId).then(() =>
      queryClient.invalidateQueries({ queryKey: queryKeys.chats }),
    );
  }, [chatId, messages.length, queryClient]);

  const older = useMutation({
    mutationFn: () =>
      getChatTimeline(chatId, { before: effectiveOldestCursor, limit: 40 }),
    onSuccess(data) {
      setOlderMessages((current) =>
        mergeChatMessages(current, data.messages ?? []),
      );
      setOldestCursor(data.oldestCursor ?? effectiveOldestCursor);
      setOlderPageAvailable(Boolean(data.hasMoreOlder));
    },
  });

  const append = useCallback((incoming: ChatMessage[]) => {
    setNewerMessages((current) => mergeChatMessages(current, incoming));
    const cursor = incoming.at(-1)?.publicId;
    if (cursor) setNewestCursor(cursor);
  }, []);

  return {
    messages,
    isLoading: initial.isLoading,
    isError: initial.isError,
    hasMoreOlder,
    loadOlder: older.mutate,
    isLoadingOlder: older.isPending,
    append,
  };
}
