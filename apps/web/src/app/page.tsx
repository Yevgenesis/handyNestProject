import { ArrowRight, Search, ShieldCheck } from "lucide-react";
import { getLocale, getTranslations } from "next-intl/server";
import Image from "next/image";
import Link from "next/link";

import { CategoryGrid } from "@/entities/category/category-grid";
import { TaskCard } from "@/entities/task/task-card";
import {
  getCategories,
  getMarketCities,
  getPublicTasks,
} from "@/shared/api/public-data";
import { Button } from "@/shared/ui/button";
import { ErrorState } from "@/shared/ui/page-state";
import { Select } from "@/shared/ui/select";

export const dynamic = "force-dynamic";

export default async function HomePage() {
  const [locale, t, common] = await Promise.all([
    getLocale(),
    getTranslations("home"),
    getTranslations("common"),
  ]);
  const [categories, cities, tasks] = await Promise.all([
    getCategories(locale),
    getMarketCities(locale),
    getPublicTasks(locale, { status: "OPEN", page: 0, size: 4 }),
  ]);

  return (
    <>
      <section className="relative h-[calc(100svh-112px)] max-h-[620px] min-h-[440px] overflow-hidden bg-slate-900 text-white">
        <Image
          alt={t("imageAlt")}
          className="object-cover object-center"
          fill
          priority
          sizes="100vw"
          src="/images/handynest-hero.png"
        />
        <div className="absolute inset-0 bg-slate-950/55" />
        <div className="container-page relative flex h-full items-center py-10">
          <div className="max-w-3xl">
            <p className="mb-3 font-semibold text-emerald-300">
              {t("eyebrow")}
            </p>
            <h1 className="text-4xl leading-tight font-black text-balance sm:text-5xl lg:text-6xl">
              {t("title")}
            </h1>
            <p className="mt-4 max-w-2xl text-lg leading-7 text-balance text-slate-100 sm:text-xl">
              {t("description")}
            </p>
            <form
              action="/tasks"
              className="mt-7 grid gap-2 bg-white p-3 text-[var(--foreground)] shadow-2xl sm:grid-cols-[1fr_1fr_auto]"
              method="get"
            >
              <Select
                aria-label={t("categoryLabel")}
                defaultValue=""
                name="categoryId"
              >
                <option value="">{t("anyCategory")}</option>
                {(categories.data ?? []).map((category) => (
                  <option key={category.publicId} value={category.publicId}>
                    {category.title}
                  </option>
                ))}
              </Select>
              <Select aria-label={t("cityLabel")} defaultValue="" name="cityId">
                <option value="">{t("allTashkent")}</option>
                {(cities.data ?? []).map((city) => (
                  <option key={city.publicId} value={city.publicId}>
                    {city.name}
                  </option>
                ))}
              </Select>
              <Button size="lg" type="submit">
                <Search className="size-4" />
                {common("search")}
              </Button>
            </form>
            <div className="mt-4 flex flex-wrap gap-3">
              <Button asChild size="lg">
                <Link href="/tasks/new">{t("createTask")}</Link>
              </Button>
              <Button asChild size="lg" variant="secondary">
                <Link href="/performer/profile">{t("becomePerformer")}</Link>
              </Button>
            </div>
            <div className="mt-5 flex items-center gap-2 text-sm text-slate-100">
              <ShieldCheck className="size-4 text-emerald-300" />
              <span>{t("privacyNote")}</span>
            </div>
          </div>
        </div>
      </section>

      <section className="page-section bg-white">
        <div className="container-page">
          <div className="mb-7 flex items-end justify-between gap-4">
            <div>
              <p className="font-semibold text-[var(--primary)]">
                {t("popularServices")}
              </p>
              <h2 className="mt-2 text-2xl font-black sm:text-3xl">
                {t("chooseCategory")}
              </h2>
            </div>
            <Link
              className="hidden items-center gap-1 text-sm font-semibold text-[var(--primary)] sm:flex"
              href="/categories"
            >
              {t("allCategories")} <ArrowRight className="size-4" />
            </Link>
          </div>
          {categories.data ? (
            <CategoryGrid categories={categories.data} limit={5} />
          ) : (
            <ErrorState message={t("catalogUnavailable")} />
          )}
        </div>
      </section>

      <section className="page-section">
        <div className="container-page">
          <div className="mb-7 flex items-end justify-between gap-4">
            <div>
              <p className="font-semibold text-[var(--blue)]">
                {t("newTasks")}
              </p>
              <h2 className="mt-2 text-2xl font-black sm:text-3xl">
                {t("currentTasks")}
              </h2>
            </div>
            <Button asChild variant="secondary">
              <Link href="/tasks">
                {t("viewAll")} <ArrowRight className="size-4" />
              </Link>
            </Button>
          </div>
          {tasks.data?.content?.length ? (
            <div className="surface-panel overflow-hidden">
              {tasks.data.content.map((task) => (
                <TaskCard key={task.publicId} task={task} />
              ))}
            </div>
          ) : (
            <ErrorState message={t("tasksUnavailable")} />
          )}
        </div>
      </section>
    </>
  );
}
