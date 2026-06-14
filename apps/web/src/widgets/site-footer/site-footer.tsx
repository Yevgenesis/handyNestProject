import Link from "next/link";
import { getTranslations } from "next-intl/server";

export async function SiteFooter() {
  const t = await getTranslations("footer");
  return (
    <footer className="border-t border-[var(--border)] bg-white py-8">
      <div className="container-page flex flex-col justify-between gap-4 text-sm text-[var(--muted)] sm:flex-row">
        <p>
          © {new Date().getFullYear()} HandyNest. {t("tagline")}
        </p>
        <div className="flex gap-5">
          <Link href="/categories">{t("categories")}</Link>
          <Link href="/tasks">{t("tasks")}</Link>
          <Link href="/performers">{t("performers")}</Link>
        </div>
      </div>
    </footer>
  );
}
