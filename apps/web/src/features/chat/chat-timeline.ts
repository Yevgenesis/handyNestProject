import type { ChatMessage } from "@/shared/api/types";

export function mergeChatMessages(
  current: ChatMessage[],
  incoming: ChatMessage[],
) {
  const messages = new Map<string, ChatMessage>();
  for (const message of [...current, ...incoming]) {
    if (message.publicId) messages.set(message.publicId, message);
  }
  return [...messages.values()].sort((left, right) => {
    const time =
      new Date(left.createdAt ?? 0).getTime() -
      new Date(right.createdAt ?? 0).getTime();
    return time || (left.publicId ?? "").localeCompare(right.publicId ?? "");
  });
}
