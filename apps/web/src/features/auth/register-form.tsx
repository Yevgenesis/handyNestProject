"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { useQuery } from "@tanstack/react-query";
import { UserPlus } from "lucide-react";
import { useTranslations } from "next-intl";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

import { useAuth } from "@/features/auth/auth-provider";
import { PASSWORD_PATTERN } from "@/features/auth/password-policy";
import { getConsentRequirements } from "@/shared/api/session-client";
import { getErrorMessage } from "@/shared/api/errors";
import type { ConsentRequirement } from "@/shared/api/types";
import { Button } from "@/shared/ui/button";
import { Field } from "@/shared/ui/field";
import { Input } from "@/shared/ui/input";
import { Skeleton } from "@/shared/ui/skeleton";

type RegisterValues = {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  passwordConfirmation: string;
  consentTypes: string[];
};

export function buildRegistrationConsents(
  requirements: ConsentRequirement[],
  selectedTypes: string[],
) {
  return requirements
    .filter(
      (item) =>
        item.requiredAtRegistration &&
        item.type &&
        item.documentVersion &&
        selectedTypes.includes(item.type),
    )
    .map((item) => ({
      type: item.type!,
      documentVersion: item.documentVersion!,
    }));
}

export function RegisterForm() {
  const t = useTranslations("auth");
  const apiErrors = useTranslations("apiErrors");
  const { register } = useAuth();
  const router = useRouter();
  const [redirectPending, setRedirectPending] = useState(false);
  const requirements = useQuery({
    queryKey: ["consent-requirements"],
    queryFn: getConsentRequirements,
  });
  const registerSchema = useMemo(
    () =>
      z
        .object({
          firstName: z.string().trim().min(2, t("nameMin")).max(50),
          lastName: z.string().trim().min(2, t("nameMin")).max(50),
          email: z.email(t("emailInvalid")),
          password: z.string().regex(PASSWORD_PATTERN, t("passwordRule")),
          passwordConfirmation: z.string(),
          consentTypes: z.array(z.string()),
        })
        .refine((data) => data.password === data.passwordConfirmation, {
          path: ["passwordConfirmation"],
          message: t("passwordMismatch"),
        }),
    [t],
  );
  const form = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { consentTypes: [] },
  });

  useEffect(() => {
    if (redirectPending) router.replace("/dashboard");
  }, [redirectPending, router]);

  async function onSubmit(values: RegisterValues) {
    const required = (requirements.data ?? []).filter(
      (item) => item.requiredAtRegistration,
    );
    const missing = required.filter(
      (item) => !values.consentTypes.includes(item.type ?? ""),
    );
    if (missing.length) {
      form.setError("consentTypes", { message: t("missingConsents") });
      return;
    }

    try {
      await register({
        firstName: values.firstName,
        lastName: values.lastName,
        email: values.email,
        password: values.password,
        passwordConfirmation: values.passwordConfirmation,
        consents: buildRegistrationConsents(required, values.consentTypes),
      });
      toast.success(t("registerSuccess"));
      setRedirectPending(true);
    } catch (error) {
      toast.error(getErrorMessage(error, apiErrors));
    }
  }

  return (
    <form className="grid gap-5" onSubmit={form.handleSubmit(onSubmit)}>
      <div className="grid gap-4 sm:grid-cols-2">
        <Field
          label={t("firstName")}
          error={form.formState.errors.firstName?.message}
        >
          <Input autoComplete="given-name" {...form.register("firstName")} />
        </Field>
        <Field
          label={t("lastName")}
          error={form.formState.errors.lastName?.message}
        >
          <Input autoComplete="family-name" {...form.register("lastName")} />
        </Field>
      </div>
      <Field label={t("email")} error={form.formState.errors.email?.message}>
        <Input autoComplete="email" type="email" {...form.register("email")} />
      </Field>
      <Field
        label={t("password")}
        error={form.formState.errors.password?.message}
      >
        <Input
          autoComplete="new-password"
          type="password"
          {...form.register("password")}
        />
      </Field>
      <Field
        label={t("passwordConfirmation")}
        error={form.formState.errors.passwordConfirmation?.message}
      >
        <Input
          autoComplete="new-password"
          type="password"
          {...form.register("passwordConfirmation")}
        />
      </Field>

      <fieldset className="grid gap-3 border-t border-[var(--border)] pt-5">
        <legend className="mb-2 text-sm font-bold">{t("consents")}</legend>
        {requirements.isLoading ? (
          <Skeleton className="h-16 w-full" />
        ) : (
          (requirements.data ?? [])
            .filter((item) => item.requiredAtRegistration)
            .map((item) => (
              <label
                className="flex items-start gap-3 text-sm"
                key={`${item.type}-${item.documentVersion}`}
              >
                <input
                  className="mt-0.5 size-4 accent-[var(--primary)]"
                  type="checkbox"
                  value={item.type}
                  {...form.register("consentTypes")}
                />
                <span>
                  {t("acceptConsent", {
                    document: consentLabel(item.type, t) ?? "",
                    version: item.documentVersion ?? "",
                  })}
                </span>
              </label>
            ))
        )}
        {form.formState.errors.consentTypes?.message ? (
          <p className="text-xs text-[var(--red)]">
            {form.formState.errors.consentTypes.message}
          </p>
        ) : null}
      </fieldset>

      <Button
        className="w-full"
        disabled={form.formState.isSubmitting || requirements.isLoading}
        size="lg"
        type="submit"
      >
        <UserPlus className="size-4" aria-hidden />
        {form.formState.isSubmitting
          ? t("creatingAccount")
          : t("submitRegister")}
      </Button>
    </form>
  );
}

function consentLabel(
  type: string | undefined,
  translate: (key: string) => string,
) {
  const labels: Record<string, string> = {
    TERMS_OF_SERVICE: "terms",
    PRIVACY_POLICY: "privacy",
    PERSONAL_DATA_PROCESSING: "personalData",
  };
  return type && labels[type] ? translate(labels[type]) : type;
}
