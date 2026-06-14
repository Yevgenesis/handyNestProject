import type { Metadata } from "next";
import { getLocale, getTranslations } from "next-intl/server";

import { TaskCard } from "@/entities/task/task-card";
import {
  firstParam,
  pageParam,
  type SearchParams,
} from "@/features/marketplace/filter-params";
import {
  flattenCategories,
  getCategories,
  getMarketCities,
  getPublicTasks,
} from "@/shared/api/public-data";
import { Button } from "@/shared/ui/button";
import { EmptyState, ErrorState } from "@/shared/ui/page-state";
import { Select } from "@/shared/ui/select";
import { Pagination } from "@/widgets/pagination/pagination";

export const dynamic = "force-dynamic";
export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations("tasks");
  return { title: t("metadataTitle") };
}

export default async function TasksPage({
  searchParams,
}: {
  searchParams: Promise<SearchParams>;
}) {
  const [params, locale, t, common] = await Promise.all([
    searchParams,
    getLocale(),
    getTranslations("tasks"),
    getTranslations("common"),
  ]);
  const page = pageParam(params.page);
  const [categories, cities, tasks] = await Promise.all([
    getCategories(locale),
    getMarketCities(locale),
    getPublicTasks(locale, {
      status: "OPEN",
      categoryId: firstParam(params.categoryId),
      cityId: firstParam(params.cityId),
      serviceMode: firstParam(params.serviceMode) as
        | "ONSITE"
        | "REMOTE"
        | "HYBRID"
        | undefined,
      page: page - 1,
      size: 10,
    }),
  ]);

  return (
    <section className="container-page page-section">
      <p className="font-semibold text-[var(--primary)]">{t("eyebrow")}</p>
      <h1 className="mt-2 text-3xl font-black sm:text-4xl">{t("title")}</h1>
      <p className="mt-3 text-[var(--muted)]">{t("description")}</p>
      <form
        className="mt-8 grid gap-3 border-y border-[var(--border)] py-5 md:grid-cols-[1fr_1fr_1fr_auto]"
        method="get"
      >
        <Select
          defaultValue={firstParam(params.categoryId) ?? ""}
          name="categoryId"
        >
          <option value="">{t("allCategories")}</option>
          {flattenCategories(categories.data ?? []).map((category) => (
            <option key={category.publicId} value={category.publicId}>
              {category.title}
            </option>
          ))}
        </Select>
        <Select defaultValue={firstParam(params.cityId) ?? ""} name="cityId">
          <option value="">{t("allTashkent")}</option>
          {(cities.data ?? []).map((city) => (
            <option key={city.publicId} value={city.publicId}>
              {city.name}
            </option>
          ))}
        </Select>
        <Select
          defaultValue={firstParam(params.serviceMode) ?? ""}
          name="serviceMode"
        >
          <option value="">{t("anyMode")}</option>
          <option value="ONSITE">{t("onsite")}</option>
          <option value="REMOTE">{t("remote")}</option>
          <option value="HYBRID">{t("hybrid")}</option>
        </Select>
        <Button type="submit">{common("apply")}</Button>
      </form>
      <div className="mt-8">
        {tasks.error ? (
          <ErrorState message={t("loadError")} />
        ) : tasks.data?.content?.length ? (
          <div className="surface-panel overflow-hidden">
            {tasks.data.content.map((task) => (
              <TaskCard key={task.publicId} task={task} />
            ))}
          </div>
        ) : (
          <EmptyState
            description={t("emptyDescription")}
            title={t("emptyTitle")}
          />
        )}
      </div>
      {tasks.data ? (
        <Pagination
          page={page}
          pathname="/tasks"
          searchParams={params}
          totalPages={tasks.data.totalPages ?? 0}
        />
      ) : null}
    </section>
  );
}
