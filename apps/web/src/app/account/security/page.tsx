import { SecurityPanel } from "@/features/account/security-panel";
import { ProtectedRoute } from "@/features/auth/protected-route";

export default async function AccountSecurityPage() {
  const t = await getTranslations("account");
  return (
    <ProtectedRoute>
      <section className="container-page page-section max-w-3xl">
        <p className="font-semibold text-[var(--primary)]">{t("eyebrow")}</p>
        <h1 className="mt-2 text-3xl font-black">{t("securityTitle")}</h1>
        <p className="mt-2 mb-8 text-[var(--muted)]">
          {t("securityDescription")}
        </p>
        <SecurityPanel />
      </section>
    </ProtectedRoute>
  );
}
import { getTranslations } from "next-intl/server";
