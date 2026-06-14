import { ArrowLeft, CheckCircle2 } from "lucide-react";
import { getLocale, getTranslations } from "next-intl/server";
import Link from "next/link";
import { notFound } from "next/navigation";

import { CategoryIcon } from "@/entities/category/category-icon";
import { flattenCategories, getCategories } from "@/shared/api/public-data";
import { serviceModeLabel } from "@/shared/lib/labels";
import { Badge } from "@/shared/ui/badge";
import { Button } from "@/shared/ui/button";
import { ErrorState } from "@/shared/ui/page-state";

export const dynamic = "force-dynamic";

export default async function CategoryDetailPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const [{ slug }, locale, t, labels] = await Promise.all([
    params,
    getLocale(),
    getTranslations("categories"),
    getTranslations("labels"),
  ]);
  const result = await getCategories(locale);
  if (!result.data) {
    return (
      <section className="container-page page-section">
        <ErrorState message={t("unavailable")} />
      </section>
    );
  }

  const category = flattenCategories(result.data).find(
    (item) => item.slug === slug,
  );
  if (!category) notFound();

  return (
    <section className="container-page page-section">
      <Link
        className="inline-flex items-center gap-2 text-sm font-semibold text-[var(--muted)]"
        href="/categories"
      >
        <ArrowLeft className="size-4" /> {t("back")}
      </Link>
      <div className="mt-8 grid gap-8 lg:grid-cols-[1fr_320px]">
        <div>
          <div className="grid size-14 place-items-center rounded-[6px] bg-[var(--primary-soft)] text-[var(--primary)]">
            <CategoryIcon className="size-7" slug={category.slug} />
          </div>
          <h1 className="mt-5 text-3xl font-black sm:text-4xl">
            {category.title}
          </h1>
          <div className="mt-4 flex flex-wrap gap-2">
            <Badge tone="green">
              {serviceModeLabel(category.serviceMode, labels)}
            </Badge>
            {category.requiresManualApproval ? (
              <Badge tone="amber">{t("verificationRequired")}</Badge>
            ) : null}
          </div>
          {category.children?.length ? (
            <div className="mt-9">
              <h2 className="text-xl font-bold">{t("subcategories")}</h2>
              <div className="mt-4 grid gap-px overflow-hidden border border-[var(--border)] bg-[var(--border)] sm:grid-cols-2">
                {category.children.map((child) => (
                  <Link
                    className="flex items-center gap-3 bg-white p-4 hover:bg-[var(--surface-muted)]"
                    href={`/tasks?categoryId=${child.publicId}`}
                    key={child.publicId}
                  >
                    <CheckCircle2 className="size-5 text-[var(--primary)]" />
                    <span className="font-semibold">{child.title}</span>
                  </Link>
                ))}
              </div>
            </div>
          ) : null}
        </div>
        <aside className="surface-panel h-fit p-5">
          <h2 className="font-bold">{t("needService")}</h2>
          <p className="mt-2 text-sm leading-6 text-[var(--muted)]">
            {t("needServiceDescription")}
          </p>
          <div className="mt-5 grid gap-2">
            <Button asChild>
              <Link href={`/tasks?categoryId=${category.publicId}`}>
                {t("viewTasks")}
              </Link>
            </Button>
            <Button asChild variant="secondary">
              <Link href={`/performers?categoryId=${category.publicId}`}>
                {t("findPerformer")}
              </Link>
            </Button>
          </div>
        </aside>
      </div>
    </section>
  );
}
