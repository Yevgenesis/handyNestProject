"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Plus, Save, Trash2 } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { useEffect, useMemo, useState } from "react";
import { useFieldArray, useForm, useWatch } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

import { useAuth } from "@/features/auth/auth-provider";
import { ApiRequestError, getErrorMessage } from "@/shared/api/errors";
import {
  acceptUserConsents,
  getCategoriesClient,
  getCitiesClient,
  getDistrictsClient,
  getMarketConfig,
  getPerformerMe,
  getUserConsents,
  savePerformerProfile,
  updatePerformerAvailability,
} from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import { getConsentRequirements } from "@/shared/api/session-client";
import type { Category } from "@/shared/api/types";
import { Badge } from "@/shared/ui/badge";
import { Button } from "@/shared/ui/button";
import { Field } from "@/shared/ui/field";
import { Input } from "@/shared/ui/input";
import { Select } from "@/shared/ui/select";
import { Skeleton } from "@/shared/ui/skeleton";
import { Textarea } from "@/shared/ui/textarea";

const categorySchema = z.object({
  categoryId: z.string().min(1),
  experienceYears: z.number().min(0),
  priceFrom: z.number().min(0),
  priceTo: z.number().min(0),
  primary: z.boolean(),
});
const schema = z
  .object({
    displayName: z.string().trim().min(2).max(120),
    description: z.string().max(2000),
    skillsDescription: z.string().max(2000),
    cityId: z.string().min(1),
    districtId: z.string(),
    serviceRadiusKm: z.number().min(0).max(500),
    worksRemotely: z.boolean(),
    worksOnsite: z.boolean(),
    travelFeePolicy: z.string().max(500),
    categories: z.array(categorySchema).min(1),
  })
  .superRefine((values, context) => {
    if (!values.worksRemotely && !values.worksOnsite)
      context.addIssue({
        code: "custom",
        path: ["worksOnsite"],
        message: "mode",
      });
    values.categories.forEach((category, index) => {
      if (category.priceFrom > category.priceTo)
        context.addIssue({
          code: "custom",
          path: ["categories", index, "priceTo"],
          message: "range",
        });
    });
  });
type Values = z.infer<typeof schema>;

function flatten(categories: Category[]): Category[] {
  return categories.flatMap((category) => [
    category,
    ...flatten(category.children ?? []),
  ]);
}

export function PerformerProfileForm() {
  const t = useTranslations("performerProfile");
  const apiErrors = useTranslations("apiErrors");
  const locale = useLocale();
  const queryClient = useQueryClient();
  const { refresh } = useAuth();
  const [acceptRules, setAcceptRules] = useState(false);
  const performer = useQuery({
    queryKey: queryKeys.performerMe,
    queryFn: getPerformerMe,
    retry: false,
  });
  const exists = Boolean(performer.data);
  const categories = useQuery({
    queryKey: queryKeys.categories(locale),
    queryFn: () => getCategoriesClient(locale),
  });
  const cities = useQuery({
    queryKey: queryKeys.cities(locale),
    queryFn: () => getCitiesClient(locale),
  });
  const market = useQuery({
    queryKey: queryKeys.marketConfig,
    queryFn: getMarketConfig,
  });
  const requirements = useQuery({
    queryKey: queryKeys.consentRequirements,
    queryFn: getConsentRequirements,
  });
  const consents = useQuery({
    queryKey: queryKeys.userConsents,
    queryFn: getUserConsents,
  });
  const form = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: {
      displayName: "",
      description: "",
      skillsDescription: "",
      cityId: "",
      districtId: "",
      serviceRadiusKm: 20,
      worksRemotely: false,
      worksOnsite: true,
      travelFeePolicy: "",
      categories: [
        {
          categoryId: "",
          experienceYears: 0,
          priceFrom: 0,
          priceTo: 0,
          primary: true,
        },
      ],
    },
  });
  const rows = useFieldArray({ control: form.control, name: "categories" });
  const cityId = useWatch({ control: form.control, name: "cityId" });
  const districts = useQuery({
    queryKey: queryKeys.districts(cityId, locale),
    queryFn: () => getDistrictsClient(cityId, locale),
    enabled: Boolean(cityId),
  });

  useEffect(() => {
    if (!performer.data) return;
    form.reset({
      displayName: performer.data.displayName ?? "",
      description: performer.data.description ?? "",
      skillsDescription: performer.data.skillsDescription ?? "",
      cityId: performer.data.cityId ?? "",
      districtId: performer.data.districtId ?? "",
      serviceRadiusKm: performer.data.serviceRadiusKm ?? 20,
      worksRemotely: performer.data.worksRemotely ?? false,
      worksOnsite: performer.data.worksOnsite ?? false,
      travelFeePolicy: performer.data.travelFeePolicy ?? "",
      categories:
        performer.data.categories?.map((category) => ({
          categoryId: category.categoryId ?? "",
          experienceYears: category.experienceYears ?? 0,
          priceFrom: category.priceFrom ?? 0,
          priceTo: category.priceTo ?? 0,
          primary: category.primary ?? false,
        })) ?? [],
    });
  }, [form, performer.data]);

  useEffect(() => {
    if (
      !performer.data &&
      !form.getValues("cityId") &&
      market.data?.defaultCityId
    ) {
      form.setValue("cityId", market.data.defaultCityId, {
        shouldValidate: true,
      });
    }
  }, [form, market.data?.defaultCityId, performer.data]);

  const requiredRules =
    requirements.data?.filter(
      (item) =>
        item.type === "PERFORMER_RULES" ||
        item.type === "PROHIBITED_SERVICES_POLICY",
    ) ?? [];
  const missingRules = requiredRules.filter(
    (rule) =>
      !consents.data?.some(
        (consent) =>
          consent.type === rule.type &&
          consent.documentVersion === rule.documentVersion,
      ),
  );
  const mutation = useMutation({
    mutationFn: async (values: Values) => {
      if (!exists && missingRules.length) {
        if (!acceptRules) throw new Error(t("acceptRulesError"));
        await acceptUserConsents(
          missingRules.map((rule) => ({
            type: rule.type!,
            documentVersion: rule.documentVersion ?? "",
          })),
        );
      }
      return savePerformerProfile(
        {
          ...values,
          countryCode: market.data?.defaultCountryCode,
          categories: values.categories.map((category, index) => ({
            ...category,
            currency: market.data?.defaultCurrency,
            primary: category.primary || index === 0,
          })),
        },
        exists,
      );
    },
    onSuccess(data) {
      queryClient.setQueryData(queryKeys.performerMe, data);
      void queryClient.invalidateQueries({ queryKey: queryKeys.userConsents });
      void refresh();
      toast.success(t(exists ? "updated" : "created"));
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });
  const availability = useMutation({
    mutationFn: updatePerformerAvailability,
    onSuccess(data) {
      queryClient.setQueryData(queryKeys.performerMe, data);
      toast.success(t("availabilityUpdated"));
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });
  const flatCategories = useMemo(
    () => flatten(categories.data ?? []).filter((category) => category.active),
    [categories.data],
  );

  if (performer.isLoading || categories.isLoading || cities.isLoading)
    return <Skeleton className="h-[620px]" />;
  if (
    performer.error &&
    (!(performer.error instanceof ApiRequestError) ||
      performer.error.status !== 404)
  )
    return <p className="text-[var(--red)]">{t("loadError")}</p>;
  return (
    <form
      className="grid gap-6"
      onSubmit={form.handleSubmit((values) => mutation.mutate(values))}
    >
      {exists ? (
        <section className="surface-panel flex flex-wrap items-center justify-between gap-4 p-5">
          <div>
            <p className="font-bold">{t("availability")}</p>
            <p className="text-sm text-[var(--muted)]">
              {performer.data?.available ? t("available") : t("busy")}
            </p>
          </div>
          <Button
            onClick={() => availability.mutate(!performer.data?.available)}
            type="button"
            variant={performer.data?.available ? "secondary" : "primary"}
          >
            {performer.data?.available ? t("setBusy") : t("setAvailable")}
          </Button>
        </section>
      ) : null}
      <section className="surface-panel grid gap-5 p-5 sm:p-6">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h2 className="text-xl font-bold">{t("mainInfo")}</h2>
          {performer.data?.verificationLevel ? (
            <Badge tone="green">
              {t(`verificationLevel.${performer.data.verificationLevel}`)}
            </Badge>
          ) : null}
        </div>
        <Field
          label={t("displayName")}
          error={form.formState.errors.displayName && t("fieldError")}
        >
          <Input {...form.register("displayName")} />
        </Field>
        <Field label={t("description")}>
          <Textarea {...form.register("description")} />
        </Field>
        <Field label={t("skills")}>
          <Textarea {...form.register("skillsDescription")} />
        </Field>
        <div className="grid gap-4 sm:grid-cols-2">
          <Field
            label={t("city")}
            error={form.formState.errors.cityId && t("fieldError")}
          >
            <Select {...form.register("cityId")}>
              <option value="">{t("chooseCity")}</option>
              {cities.data?.map((city) => (
                <option key={city.publicId} value={city.publicId}>
                  {city.name}
                </option>
              ))}
            </Select>
          </Field>
          <Field label={t("radius")}>
            <Input
              min="0"
              max="500"
              type="number"
              {...form.register("serviceRadiusKm", { valueAsNumber: true })}
            />
          </Field>
        </div>
        <Field label={t("district")}>
          <Select {...form.register("districtId")}>
            <option value="">{t("districtOptional")}</option>
            {districts.data?.map((district) => (
              <option key={district.publicId} value={district.publicId}>
                {district.name}
              </option>
            ))}
          </Select>
        </Field>
        <div className="flex flex-wrap gap-5">
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" {...form.register("worksOnsite")} />
            {t("onsite")}
          </label>
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" {...form.register("worksRemotely")} />
            {t("remote")}
          </label>
        </div>
        <Field label={t("travelPolicy")}>
          <Input {...form.register("travelFeePolicy")} />
        </Field>
      </section>
      <section className="surface-panel p-5 sm:p-6">
        <div className="flex items-center justify-between gap-3">
          <div>
            <h2 className="text-xl font-bold">{t("categories")}</h2>
            <p className="mt-1 text-sm text-[var(--muted)]">
              {t("categoriesDescription")}
            </p>
          </div>
          <Button
            onClick={() =>
              rows.append({
                categoryId: "",
                experienceYears: 0,
                priceFrom: 0,
                priceTo: 0,
                primary: false,
              })
            }
            size="sm"
            type="button"
            variant="secondary"
          >
            <Plus className="size-4" />
            {t("addCategory")}
          </Button>
        </div>
        <div className="mt-5 grid gap-4">
          {rows.fields.map((row, index) => (
            <div
              className="grid gap-3 rounded-[8px] border border-[var(--border)] p-4 lg:grid-cols-[2fr_1fr_1fr_1fr_auto]"
              key={row.id}
            >
              <Field label={t("category")}>
                <Select {...form.register(`categories.${index}.categoryId`)}>
                  <option value="">{t("chooseCategory")}</option>
                  {flatCategories.map((category) => (
                    <option key={category.publicId} value={category.publicId}>
                      {category.title}
                    </option>
                  ))}
                </Select>
              </Field>
              <Field label={t("experience")}>
                <Input
                  min="0"
                  type="number"
                  {...form.register(`categories.${index}.experienceYears`, {
                    valueAsNumber: true,
                  })}
                />
              </Field>
              <Field label={t("priceFrom")}>
                <Input
                  min="0"
                  type="number"
                  {...form.register(`categories.${index}.priceFrom`, {
                    valueAsNumber: true,
                  })}
                />
              </Field>
              <Field
                label={t("priceTo")}
                error={
                  form.formState.errors.categories?.[index]?.priceTo &&
                  t("rangeError")
                }
              >
                <Input
                  min="0"
                  type="number"
                  {...form.register(`categories.${index}.priceTo`, {
                    valueAsNumber: true,
                  })}
                />
              </Field>
              <Button
                aria-label={t("removeCategory")}
                className="self-end"
                disabled={rows.fields.length === 1}
                onClick={() => rows.remove(index)}
                size="icon"
                type="button"
                variant="ghost"
              >
                <Trash2 className="size-4" />
              </Button>
            </div>
          ))}
        </div>
      </section>
      {!exists && missingRules.length ? (
        <label className="surface-panel flex items-start gap-3 p-5 text-sm">
          <input
            checked={acceptRules}
            className="mt-1"
            onChange={(event) => setAcceptRules(event.target.checked)}
            type="checkbox"
          />
          <span>
            {t("acceptRules", {
              versions: missingRules
                .map((rule) => rule.documentVersion)
                .join(", "),
            })}
          </span>
        </label>
      ) : null}
      <Button
        className="justify-self-end"
        disabled={mutation.isPending}
        size="lg"
        type="submit"
      >
        <Save className="size-4" />
        {mutation.isPending ? t("saving") : t(exists ? "save" : "create")}
      </Button>
    </form>
  );
}
