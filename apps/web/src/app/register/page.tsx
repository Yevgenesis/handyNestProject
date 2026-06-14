import Link from "next/link";
import { getTranslations } from "next-intl/server";

import { RegisterForm } from "@/features/auth/register-form";

export default async function RegisterPage() {
  const t = await getTranslations("auth");
  return (
    <section className="container-page page-section grid place-items-center">
      <div className="surface-panel w-full max-w-2xl p-6 sm:p-8">
        <h1 className="text-2xl font-black">{t("registerTitle")}</h1>
        <p className="mt-2 mb-7 text-sm text-[var(--muted)]">
          {t("registerDescription")}
        </p>
        <RegisterForm />
        <p className="mt-6 text-center text-sm text-[var(--muted)]">
          {t("hasAccount")}{" "}
          <Link className="font-semibold text-[var(--primary)]" href="/login">
            {t("goToLogin")}
          </Link>
        </p>
      </div>
    </section>
  );
}
