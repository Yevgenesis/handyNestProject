"use client";

import { useTranslations } from "next-intl";
import Link from "next/link";

import { CategoryIcon } from "@/entities/category/category-icon";
import type { Category } from "@/shared/api/types";

export function CategoryGrid({
  categories,
  limit,
}: {
  categories: Category[];
  limit?: number;
}) {
  const t = useTranslations("categories");
  return (
    <div className="grid grid-cols-2 gap-px overflow-hidden border border-[var(--border)] bg-[var(--border)] sm:grid-cols-3 lg:grid-cols-6">
      {categories.slice(0, limit).map((category) => (
        <Link
          className="group min-h-32 bg-white p-4 transition-colors hover:bg-[var(--primary-soft)]"
          href={`/categories/${category.slug}`}
          key={category.publicId}
        >
          <span className="grid size-10 place-items-center rounded-[6px] bg-[var(--surface-muted)] text-[var(--primary)] group-hover:bg-white">
            <CategoryIcon slug={category.slug} />
          </span>
          <h3 className="mt-4 text-sm leading-snug font-bold">
            {category.title}
          </h3>
          <p className="mt-1 text-xs text-[var(--muted)]">
            {t("directions", { count: category.children?.length ?? 0 })}
          </p>
        </Link>
      ))}
    </div>
  );
}
