import { getTranslations } from "next-intl/server";

import { ProtectedRoute } from "@/features/auth/protected-route";
import { ChatListPanel } from "@/features/chat/chat-list-panel";

export default async function ChatsPage() {
  const t = await getTranslations("chat");
  return (
    <ProtectedRoute>
      <main className="container-page page-section">
        <p className="font-semibold text-[var(--primary)]">{t("eyebrow")}</p>
        <h1 className="mt-2 text-3xl font-black">{t("title")}</h1>
        <p className="mt-2 text-[var(--muted)]">{t("description")}</p>
        <div className="mt-8">
          <ChatListPanel />
        </div>
      </main>
    </ProtectedRoute>
  );
}
