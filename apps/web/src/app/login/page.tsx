import { Suspense } from "react";
import { getTranslations } from "next-intl/server";
import Link from "next/link";

import { LoginForm } from "@/features/auth/login-form";

export default async function LoginPage() {
  const t = await getTranslations("auth");
  return (
    <section className="container-page page-section grid place-items-center">
      <div className="surface-panel w-full max-w-md p-6 sm:p-8">
        <h1 className="text-2xl font-black">{t("loginTitle")}</h1>
        <p className="mt-2 mb-7 text-sm text-[var(--muted)]">
          {t("loginDescription")}
        </p>
        <Suspense>
          <LoginForm />
        </Suspense>
        <p className="mt-6 text-center text-sm text-[var(--muted)]">
          {t("noAccount")}{" "}
          <Link
            className="font-semibold text-[var(--primary)]"
            href="/register"
          >
            {t("goToRegister")}
          </Link>
        </p>
      </div>
    </section>
  );
}
