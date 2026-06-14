import { ProtectedRoute } from "@/features/auth/protected-route";
import { ChatPanel } from "@/features/chat/chat-panel";

export default async function ChatPage({
  params,
}: {
  params: Promise<{ chatId: string }>;
}) {
  const { chatId } = await params;
  return (
    <ProtectedRoute>
      <main className="container-page py-5 sm:py-8">
        <ChatPanel chatId={chatId} />
      </main>
    </ProtectedRoute>
  );
}
