"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ArrowLeft, ArrowRight, Check, ImagePlus, Send } from "lucide-react";
import { useLocale, useTranslations } from "next-intl";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { useForm, useWatch } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

import { useAuth } from "@/features/auth/auth-provider";
import {
  clearTaskDraft,
  loadTaskDraft,
  saveTaskDraft,
  type TaskDraft,
} from "@/features/tasks/task-draft";
import { getErrorMessage } from "@/shared/api/errors";
import {
  acceptUserConsents,
  createTask,
  getCategoriesClient,
  getCitiesClient,
  getDistrictsClient,
  getMarketConfig,
  getUserConsents,
  newIdempotencyKey,
  uploadTaskImage,
} from "@/shared/api/marketplace-client";
import { getConsentRequirements } from "@/shared/api/session-client";
import { queryKeys } from "@/shared/api/query-keys";
import type { Category } from "@/shared/api/types";
import { Button } from "@/shared/ui/button";
import { Field } from "@/shared/ui/field";
import { Input } from "@/shared/ui/input";
import { Select } from "@/shared/ui/select";
import { Textarea } from "@/shared/ui/textarea";

const schema = z
  .object({
    title: z.string().trim().min(5).max(160),
    description: z.string().trim().min(20).max(4000),
    categoryId: z.string().min(1),
    serviceMode: z.enum(["ONSITE", "REMOTE", "HYBRID"]),
    cityId: z.string().min(1),
    districtId: z.string(),
    addressText: z.string().max(500),
    expiresAt: z.string().min(1),
    priceType: z.enum(["FIXED", "HOURLY", "NEGOTIABLE"]),
    fixedPrice: z.string(),
    budgetMin: z.string(),
    budgetMax: z.string(),
  })
  .superRefine((values, context) => {
    if (values.priceType === "FIXED" && Number(values.fixedPrice) <= 0) {
      context.addIssue({
        code: "custom",
        path: ["fixedPrice"],
        message: "required",
      });
    }
    if (
      values.budgetMin &&
      values.budgetMax &&
      Number(values.budgetMin) > Number(values.budgetMax)
    ) {
      context.addIssue({
        code: "custom",
        path: ["budgetMax"],
        message: "range",
      });
    }
  });

const defaults: TaskDraft = {
  title: "",
  description: "",
  categoryId: "",
  serviceMode: "ONSITE",
  cityId: "",
  districtId: "",
  addressText: "",
  expiresAt: new Date(Date.now() + 30 * 86_400_000).toISOString().slice(0, 10),
  priceType: "FIXED",
  fixedPrice: "",
  budgetMin: "",
  budgetMax: "",
};

const stepFields: (keyof TaskDraft)[][] = [
  ["title", "description", "categoryId"],
  ["serviceMode", "cityId", "districtId", "addressText"],
  ["expiresAt"],
  ["priceType", "fixedPrice", "budgetMin", "budgetMax"],
  [],
  [],
];

function flatten(categories: Category[]): Category[] {
  return categories.flatMap((category) => [
    category,
    ...flatten(category.children ?? []),
  ]);
}

export function TaskWizard() {
  const t = useTranslations("taskWizard");
  const apiErrors = useTranslations("apiErrors");
  const locale = useLocale();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const userId = user?.publicId;
  const [step, setStep] = useState(0);
  const [files, setFiles] = useState<File[]>([]);
  const [customerRulesAccepted, setCustomerRulesAccepted] = useState(false);
  const initialValues = useMemo(
    () => (userId ? (loadTaskDraft(userId) ?? defaults) : defaults),
    [userId],
  );
  const form = useForm<TaskDraft>({
    resolver: zodResolver(schema),
    defaultValues: initialValues,
    mode: "onBlur",
  });

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
  const values = useWatch({ control: form.control }) as TaskDraft;
  const districts = useQuery({
    queryKey: queryKeys.districts(values.cityId, locale),
    queryFn: () => getDistrictsClient(values.cityId, locale),
    enabled: Boolean(values.cityId),
  });

  const customerRule = requirements.data?.find(
    (requirement) => requirement.type === "CUSTOMER_RULES",
  );
  const hasCustomerRule = consents.data?.some(
    (consent) =>
      consent.type === "CUSTOMER_RULES" &&
      consent.documentVersion === customerRule?.documentVersion,
  );

  useEffect(() => {
    if (!form.getValues("cityId") && market.data?.defaultCityId) {
      form.setValue("cityId", market.data.defaultCityId, {
        shouldValidate: true,
      });
    }
  }, [form, market.data?.defaultCityId]);

  useEffect(() => {
    if (!userId) return;
    saveTaskDraft(userId, values);
  }, [userId, values]);

  const publish = useMutation({
    mutationFn: async (values: TaskDraft) => {
      if (!hasCustomerRule) {
        if (!customerRulesAccepted || !customerRule?.documentVersion) {
          throw new Error(t("acceptRulesError"));
        }
        await acceptUserConsents([
          {
            type: "CUSTOMER_RULES",
            documentVersion: customerRule.documentVersion,
          },
        ]);
      }
      const task = await createTask(
        {
          title: values.title.trim(),
          description: values.description.trim(),
          categoryId: values.categoryId,
          serviceMode: values.serviceMode,
          priceType: values.priceType,
          fixedPrice:
            values.priceType === "FIXED"
              ? Number(values.fixedPrice)
              : undefined,
          budgetMin: values.budgetMin ? Number(values.budgetMin) : undefined,
          budgetMax: values.budgetMax ? Number(values.budgetMax) : undefined,
          currency: market.data?.defaultCurrency,
          countryCode: market.data?.defaultCountryCode,
          cityId: values.cityId,
          districtId: values.districtId || undefined,
          addressText: values.addressText || undefined,
          expiresAt: new Date(
            `${values.expiresAt}T23:59:59+05:00`,
          ).toISOString(),
        },
        newIdempotencyKey("task"),
      );
      const failedFiles: string[] = [];
      for (const file of files) {
        try {
          await uploadTaskImage(task.publicId ?? "", file);
        } catch {
          failedFiles.push(file.name);
        }
      }
      return { task, failedFiles };
    },
    onSuccess({ task, failedFiles }) {
      if (userId) clearTaskDraft(userId);
      void queryClient.invalidateQueries({ queryKey: ["my-tasks"] });
      if (failedFiles.length) toast.warning(t("partialUpload"));
      else toast.success(t("published"));
      router.push(`/my/tasks/${task.publicId}`);
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });

  const flatCategories = useMemo(
    () => flatten(categories.data ?? []).filter((category) => category.active),
    [categories.data],
  );
  async function next() {
    const valid = await form.trigger(stepFields[step]);
    if (valid) setStep((current) => Math.min(current + 1, 5));
  }

  return (
    <div className="grid gap-6 lg:grid-cols-[220px_1fr]">
      <ol className="grid gap-2 self-start lg:sticky lg:top-24">
        {Array.from({ length: 6 }, (_, index) => (
          <li
            className={`flex items-center gap-3 rounded-[6px] px-3 py-2 text-sm ${
              index === step
                ? "bg-[var(--primary-soft)] font-bold text-[var(--primary-strong)]"
                : "text-[var(--muted)]"
            }`}
            key={index}
          >
            <span className="grid size-7 shrink-0 place-items-center rounded-full border border-current">
              {index < step ? <Check className="size-4" /> : index + 1}
            </span>
            {t(`steps.${index}`)}
          </li>
        ))}
      </ol>

      <form
        className="surface-panel p-5 sm:p-7"
        onSubmit={form.handleSubmit((data) => publish.mutate(data))}
      >
        <p className="text-sm font-semibold text-[var(--primary)]">
          {t("step", { current: step + 1 })}
        </p>
        <h2 className="mt-1 text-2xl font-black">{t(`titles.${step}`)}</h2>

        <div className="mt-6 grid gap-5">
          {step === 0 ? (
            <>
              <Field
                label={t("titleLabel")}
                error={form.formState.errors.title?.message && t("fieldError")}
              >
                <Input {...form.register("title")} />
              </Field>
              <Field
                label={t("descriptionLabel")}
                error={
                  form.formState.errors.description?.message && t("fieldError")
                }
              >
                <Textarea {...form.register("description")} />
              </Field>
              <Field
                label={t("categoryLabel")}
                error={
                  form.formState.errors.categoryId?.message && t("fieldError")
                }
              >
                <Select {...form.register("categoryId")}>
                  <option value="">{t("chooseCategory")}</option>
                  {flatCategories.map((category) => (
                    <option key={category.publicId} value={category.publicId}>
                      {category.title}
                    </option>
                  ))}
                </Select>
              </Field>
            </>
          ) : null}

          {step === 1 ? (
            <>
              <Field label={t("modeLabel")}>
                <Select {...form.register("serviceMode")}>
                  <option value="ONSITE">{t("onsite")}</option>
                  <option value="REMOTE">{t("remote")}</option>
                  <option value="HYBRID">{t("hybrid")}</option>
                </Select>
              </Field>
              <Field
                label={t("cityLabel")}
                error={form.formState.errors.cityId?.message && t("fieldError")}
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
              {values.serviceMode !== "REMOTE" ? (
                <Field label={t("districtLabel")}>
                  <Select {...form.register("districtId")}>
                    <option value="">{t("districtOptional")}</option>
                    {districts.data?.map((district) => (
                      <option key={district.publicId} value={district.publicId}>
                        {district.name}
                      </option>
                    ))}
                  </Select>
                </Field>
              ) : null}
              {values.serviceMode !== "REMOTE" ? (
                <Field label={t("addressLabel")}>
                  <Input {...form.register("addressText")} />
                </Field>
              ) : null}
            </>
          ) : null}

          {step === 2 ? (
            <Field
              label={t("expiresLabel")}
              error={
                form.formState.errors.expiresAt?.message && t("fieldError")
              }
            >
              <Input
                min={new Date().toISOString().slice(0, 10)}
                type="date"
                {...form.register("expiresAt")}
              />
            </Field>
          ) : null}

          {step === 3 ? (
            <>
              <Field label={t("priceTypeLabel")}>
                <Select {...form.register("priceType")}>
                  <option value="FIXED">{t("fixed")}</option>
                  <option value="HOURLY">{t("hourly")}</option>
                  <option value="NEGOTIABLE">{t("negotiable")}</option>
                </Select>
              </Field>
              {values.priceType === "FIXED" ? (
                <Field
                  label={t("fixedPriceLabel")}
                  error={
                    form.formState.errors.fixedPrice?.message && t("fieldError")
                  }
                >
                  <Input
                    min="1"
                    type="number"
                    {...form.register("fixedPrice")}
                  />
                </Field>
              ) : (
                <div className="grid gap-4 sm:grid-cols-2">
                  <Field label={t("budgetMinLabel")}>
                    <Input
                      min="0"
                      type="number"
                      {...form.register("budgetMin")}
                    />
                  </Field>
                  <Field
                    label={t("budgetMaxLabel")}
                    error={
                      form.formState.errors.budgetMax?.message &&
                      t("rangeError")
                    }
                  >
                    <Input
                      min="0"
                      type="number"
                      {...form.register("budgetMax")}
                    />
                  </Field>
                </div>
              )}
            </>
          ) : null}

          {step === 4 ? (
            <div className="rounded-[8px] border border-dashed border-[var(--border)] p-6 text-center">
              <ImagePlus className="mx-auto size-8 text-[var(--primary)]" />
              <p className="mt-3 font-bold">{t("imagesTitle")}</p>
              <p className="mt-1 text-sm text-[var(--muted)]">
                {t("imagesDescription")}
              </p>
              <Input
                accept="image/jpeg,image/png,image/webp"
                className="mt-4"
                multiple
                onChange={(event) =>
                  setFiles(Array.from(event.target.files ?? []).slice(0, 5))
                }
                type="file"
              />
              {files.length ? (
                <p className="mt-3 text-sm">
                  {t("filesSelected", { count: files.length })}
                </p>
              ) : null}
            </div>
          ) : null}

          {step === 5 ? (
            <div className="grid gap-4">
              <dl className="grid gap-3 rounded-[8px] bg-[var(--surface-muted)] p-4 text-sm sm:grid-cols-2">
                <div>
                  <dt className="text-[var(--muted)]">{t("titleLabel")}</dt>
                  <dd className="font-semibold">{values.title}</dd>
                </div>
                <div>
                  <dt className="text-[var(--muted)]">{t("cityLabel")}</dt>
                  <dd className="font-semibold">
                    {
                      cities.data?.find(
                        (city) => city.publicId === values.cityId,
                      )?.name
                    }
                  </dd>
                </div>
                <div>
                  <dt className="text-[var(--muted)]">{t("priceTypeLabel")}</dt>
                  <dd className="font-semibold">
                    {t(`priceType.${values.priceType}`)}
                  </dd>
                </div>
                <div>
                  <dt className="text-[var(--muted)]">{t("imagesTitle")}</dt>
                  <dd className="font-semibold">{files.length}</dd>
                </div>
              </dl>
              {!hasCustomerRule ? (
                <label className="flex items-start gap-3 text-sm">
                  <input
                    checked={customerRulesAccepted}
                    className="mt-1"
                    onChange={(event) =>
                      setCustomerRulesAccepted(event.target.checked)
                    }
                    type="checkbox"
                  />
                  <span>
                    {t("acceptCustomerRules", {
                      version: customerRule?.documentVersion ?? "",
                    })}
                  </span>
                </label>
              ) : null}
            </div>
          ) : null}
        </div>

        <div className="mt-8 flex flex-wrap justify-between gap-3 border-t border-[var(--border)] pt-5">
          <Button
            disabled={step === 0}
            onClick={() => setStep((current) => current - 1)}
            type="button"
            variant="secondary"
          >
            <ArrowLeft className="size-4" /> {t("back")}
          </Button>
          {step < 5 ? (
            <Button onClick={() => void next()} type="button">
              {t("next")} <ArrowRight className="size-4" />
            </Button>
          ) : (
            <Button disabled={publish.isPending} type="submit">
              <Send className="size-4" />{" "}
              {publish.isPending ? t("publishing") : t("publish")}
            </Button>
          )}
        </div>
      </form>
    </div>
  );
}
