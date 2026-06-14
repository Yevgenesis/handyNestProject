import { getTranslations } from "next-intl/server";

import { ProtectedRoute } from "@/features/auth/protected-route";
import { VerificationPanel } from "@/features/performer/verification-panel";

export default async function VerificationPage() {
  const t = await getTranslations("verification");
  return (
    <ProtectedRoute>
      <main className="container-page page-section">
        <p className="font-semibold text-[var(--primary)]">{t("eyebrow")}</p>
        <h1 className="mt-2 text-3xl font-black">{t("title")}</h1>
        <p className="mt-2 max-w-2xl text-[var(--muted)]">{t("description")}</p>
        <div className="mt-8">
          <VerificationPanel />
        </div>
      </main>
    </ProtectedRoute>
  );
}
