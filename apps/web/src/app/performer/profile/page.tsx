import { getTranslations } from "next-intl/server";

import { ProtectedRoute } from "@/features/auth/protected-route";
import { PerformerProfileForm } from "@/features/performer/performer-profile-form";

export default async function PerformerProfilePage() {
  const t = await getTranslations("performerProfile");
  return (
    <ProtectedRoute>
      <main className="container-page page-section">
        <p className="font-semibold text-[var(--primary)]">{t("eyebrow")}</p>
        <h1 className="mt-2 text-3xl font-black">{t("title")}</h1>
        <p className="mt-2 max-w-2xl text-[var(--muted)]">
          {t("pageDescription")}
        </p>
        <div className="mt-8">
          <PerformerProfileForm />
        </div>
      </main>
    </ProtectedRoute>
  );
}
