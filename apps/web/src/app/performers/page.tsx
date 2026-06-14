import type { Metadata } from "next";
import { getLocale, getTranslations } from "next-intl/server";

import { PerformerCard } from "@/entities/performer/performer-card";
import {
  firstParam,
  pageParam,
  type SearchParams,
} from "@/features/marketplace/filter-params";
import {
  flattenCategories,
  getCategories,
  getMarketCities,
  getPublicPerformers,
} from "@/shared/api/public-data";
import { Button } from "@/shared/ui/button";
import { EmptyState, ErrorState } from "@/shared/ui/page-state";
import { Input } from "@/shared/ui/input";
import { Select } from "@/shared/ui/select";
import { Pagination } from "@/widgets/pagination/pagination";

export const dynamic = "force-dynamic";
export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations("performers");
  return { title: t("metadataTitle") };
}

export default async function PerformersPage({
  searchParams,
}: {
  searchParams: Promise<SearchParams>;
}) {
  const [params, locale, t, common] = await Promise.all([
    searchParams,
    getLocale(),
    getTranslations("performers"),
    getTranslations("common"),
  ]);
  const page = pageParam(params.page);
  const [categories, cities, performers] = await Promise.all([
    getCategories(locale),
    getMarketCities(locale),
    getPublicPerformers(locale, {
      categoryId: firstParam(params.categoryId),
      cityId: firstParam(params.cityId),
      districtId: firstParam(params.districtId),
      serviceMode: firstParam(params.serviceMode) as
        | "ONSITE"
        | "REMOTE"
        | "HYBRID"
        | undefined,
      verificationLevel: firstParam(params.verificationLevel) as
        | "NONE"
        | "PHONE_VERIFIED"
        | "ID_VERIFIED"
        | "PAYMENT_VERIFIED"
        | "BUSINESS_VERIFIED"
        | undefined,
      isAvailable: firstParam(params.isAvailable) === "true" ? true : undefined,
      isTopPerformer:
        firstParam(params.isTopPerformer) === "true" ? true : undefined,
      ratingMin: numberParam(params.ratingMin),
      priceMin: numberParam(params.priceMin),
      priceMax: numberParam(params.priceMax),
      page: page - 1,
      size: 10,
    }),
  ]);

  return (
    <section className="container-page page-section">
      <p className="font-semibold text-[var(--blue)]">{t("eyebrow")}</p>
      <h1 className="mt-2 text-3xl font-black sm:text-4xl">{t("title")}</h1>
      <p className="mt-3 text-[var(--muted)]">{t("description")}</p>
      <form
        className="mt-8 grid gap-3 border-y border-[var(--border)] py-5 sm:grid-cols-2 lg:grid-cols-4"
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
        <Select
          defaultValue={firstParam(params.verificationLevel) ?? ""}
          name="verificationLevel"
        >
          <option value="">{t("anyVerification")}</option>
          <option value="PHONE_VERIFIED">{t("phoneVerification")}</option>
          <option value="ID_VERIFIED">{t("identityVerification")}</option>
          <option value="BUSINESS_VERIFIED">{t("businessVerification")}</option>
        </Select>
        <Input
          aria-label={t("ratingFrom")}
          defaultValue={firstParam(params.ratingMin)}
          inputMode="decimal"
          name="ratingMin"
          placeholder={t("ratingFrom")}
        />
        <Input
          aria-label={t("priceFrom")}
          defaultValue={firstParam(params.priceMin)}
          inputMode="numeric"
          name="priceMin"
          placeholder={t("priceFrom")}
        />
        <Input
          aria-label={t("priceTo")}
          defaultValue={firstParam(params.priceMax)}
          inputMode="numeric"
          name="priceMax"
          placeholder={t("priceTo")}
        />
        <div className="grid grid-cols-[1fr_auto] gap-3">
          <Select
            defaultValue={firstParam(params.isAvailable) ?? ""}
            name="isAvailable"
          >
            <option value="">{t("anyAvailability")}</option>
            <option value="true">{t("availableOnly")}</option>
          </Select>
          <Button type="submit">{common("apply")}</Button>
        </div>
      </form>
      <div className="mt-8">
        {performers.error ? (
          <ErrorState message={t("loadError")} />
        ) : performers.data?.content?.length ? (
          <div className="surface-panel overflow-hidden">
            {performers.data.content.map((performer) => (
              <PerformerCard key={performer.publicId} performer={performer} />
            ))}
          </div>
        ) : (
          <EmptyState
            description={t("emptyDescription")}
            title={t("emptyTitle")}
          />
        )}
      </div>
      {performers.data ? (
        <Pagination
          page={page}
          pathname="/performers"
          searchParams={params}
          totalPages={performers.data.totalPages ?? 0}
        />
      ) : null}
    </section>
  );
}

function numberParam(value: SearchParams[string]) {
  const parsed = Number(firstParam(value));
  return Number.isFinite(parsed) ? parsed : undefined;
}
