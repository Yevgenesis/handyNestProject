"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { LogIn } from "lucide-react";
import { useTranslations } from "next-intl";
import { useRouter, useSearchParams } from "next/navigation";
import { useMemo } from "react";
import { useForm } from "react-hook-form";
import { toast } from "sonner";
import { z } from "zod";

import { useAuth } from "@/features/auth/auth-provider";
import { getErrorMessage } from "@/shared/api/errors";
import { Button } from "@/shared/ui/button";
import { Field } from "@/shared/ui/field";
import { Input } from "@/shared/ui/input";

type LoginValues = { email: string; password: string };

export function LoginForm() {
  const t = useTranslations("auth");
  const apiErrors = useTranslations("apiErrors");
  const { login } = useAuth();
  const router = useRouter();
  const searchParams = useSearchParams();
  const loginSchema = useMemo(
    () =>
      z.object({
        email: z.email(t("emailInvalid")),
        password: z.string().min(1, t("passwordRequired")),
      }),
    [t],
  );
  const form = useForm<LoginValues>({ resolver: zodResolver(loginSchema) });

  async function onSubmit(values: LoginValues) {
    try {
      await login(values);
      toast.success(t("loginSuccess"));
      router.replace(searchParams.get("next") || "/dashboard");
    } catch (error) {
      toast.error(getErrorMessage(error, apiErrors));
    }
  }

  return (
    <form className="grid gap-5" onSubmit={form.handleSubmit(onSubmit)}>
      <Field label={t("email")} error={form.formState.errors.email?.message}>
        <Input autoComplete="email" type="email" {...form.register("email")} />
      </Field>
      <Field
        label={t("password")}
        error={form.formState.errors.password?.message}
      >
        <Input
          autoComplete="current-password"
          type="password"
          {...form.register("password")}
        />
      </Field>
      <Button
        className="w-full"
        disabled={form.formState.isSubmitting}
        size="lg"
        type="submit"
      >
        <LogIn className="size-4" aria-hidden />
        {form.formState.isSubmitting ? t("loggingIn") : t("submitLogin")}
      </Button>
    </form>
  );
}
