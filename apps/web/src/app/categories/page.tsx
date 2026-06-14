import type { Metadata } from "next";
import { getLocale, getTranslations } from "next-intl/server";

import { CategoryGrid } from "@/entities/category/category-grid";
import { getCategories } from "@/shared/api/public-data";
import { ErrorState } from "@/shared/ui/page-state";

export const dynamic = "force-dynamic";
export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations("categories");
  return { title: t("metadataTitle") };
}

export default async function CategoriesPage() {
  const [locale, t] = await Promise.all([
    getLocale(),
    getTranslations("categories"),
  ]);
  const categories = await getCategories(locale);

  return (
    <section className="container-page page-section">
      <p className="font-semibold text-[var(--primary)]">{t("eyebrow")}</p>
      <h1 className="mt-2 text-3xl font-black sm:text-4xl">{t("title")}</h1>
      <p className="mt-3 mb-8 max-w-2xl text-[var(--muted)]">
        {t("description")}
      </p>
      {categories.data ? (
        <CategoryGrid categories={categories.data} />
      ) : (
        <ErrorState message={t("unavailable")} />
      )}
    </section>
  );
}
