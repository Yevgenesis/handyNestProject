"use client";

import { ChevronLeft, ChevronRight } from "lucide-react";
import { useTranslations } from "next-intl";
import Link from "next/link";

import {
  withSearchParams,
  type SearchParams,
} from "@/features/marketplace/filter-params";
import { Button } from "@/shared/ui/button";

export function Pagination({
  pathname,
  searchParams,
  page,
  totalPages,
}: {
  pathname: string;
  searchParams: SearchParams;
  page: number;
  totalPages: number;
}) {
  const common = useTranslations("common");
  if (totalPages <= 1) return null;
  return (
    <nav
      className="mt-6 flex items-center justify-between"
      aria-label="Pagination"
    >
      <Button asChild={page > 1} disabled={page <= 1} variant="secondary">
        {page > 1 ? (
          <Link
            href={withSearchParams(pathname, searchParams, { page: page - 1 })}
          >
            <ChevronLeft className="size-4" />
            {common("previous")}
          </Link>
        ) : (
          <span>
            <ChevronLeft className="size-4" />
            {common("previous")}
          </span>
        )}
      </Button>
      <span className="text-sm text-[var(--muted)]">
        {common("pageOf", { page, totalPages })}
      </span>
      <Button
        asChild={page < totalPages}
        disabled={page >= totalPages}
        variant="secondary"
      >
        {page < totalPages ? (
          <Link
            href={withSearchParams(pathname, searchParams, { page: page + 1 })}
          >
            {common("next")}
            <ChevronRight className="size-4" />
          </Link>
        ) : (
          <span>
            {common("next")}
            <ChevronRight className="size-4" />
          </span>
        )}
      </Button>
    </nav>
  );
}
