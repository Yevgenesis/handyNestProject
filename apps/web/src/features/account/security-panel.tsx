"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { MailCheck, Phone, ShieldCheck } from "lucide-react";
import { useTranslations } from "next-intl";
import { useState } from "react";
import { toast } from "sonner";

import {
  getUserProfile,
  requestEmailVerification,
  requestPhoneOtp,
  updateUserProfile,
  verifyPhoneOtp,
} from "@/shared/api/session-client";
import { getErrorMessage } from "@/shared/api/errors";
import { Badge } from "@/shared/ui/badge";
import { Button } from "@/shared/ui/button";
import { Field } from "@/shared/ui/field";
import { Input } from "@/shared/ui/input";
import { Skeleton } from "@/shared/ui/skeleton";

export function SecurityPanel() {
  const t = useTranslations("account");
  const apiErrors = useTranslations("apiErrors");
  const queryClient = useQueryClient();
  const [phone, setPhone] = useState("");
  const [otpCode, setOtpCode] = useState("");
  const profile = useQuery({
    queryKey: ["user-profile"],
    queryFn: getUserProfile,
  });

  const updatePhone = useMutation({
    mutationFn: () => updateUserProfile({ phone }),
    onSuccess(data) {
      queryClient.setQueryData(["user-profile"], data);
      toast.success(t("phoneUpdated"));
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });

  const requestOtp = useMutation({
    mutationFn: requestPhoneOtp,
    onSuccess: (data) =>
      toast.success(
        t("otpSent", {
          phone: data.maskedPhone ?? "",
          seconds: data.retryAfterSeconds ?? 0,
        }),
      ),
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });

  const verifyOtp = useMutation({
    mutationFn: () => verifyPhoneOtp(otpCode),
    onSuccess() {
      void queryClient.invalidateQueries({ queryKey: ["user-profile"] });
      setOtpCode("");
      toast.success(t("phoneVerifiedSuccess"));
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });

  const requestEmail = useMutation({
    mutationFn: requestEmailVerification,
    onSuccess: (data) =>
      toast.success(t("emailSent", { email: data.maskedEmail ?? "" })),
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });

  if (profile.isLoading) return <Skeleton className="h-80 w-full" />;
  if (!profile.data)
    return <p className="text-[var(--red)]">{t("profileLoadError")}</p>;

  const user = profile.data;
  return (
    <div className="grid gap-8">
      <section className="surface-panel p-5 sm:p-6">
        <div className="flex items-center justify-between gap-4">
          <div>
            <h2 className="flex items-center gap-2 text-lg font-bold">
              <Phone className="size-5 text-[var(--primary)]" />
              {t("phone")}
            </h2>
            <p className="mt-1 text-sm text-[var(--muted)]">
              {t("phoneDescription")}
            </p>
          </div>
          <Badge tone={user.phoneVerified ? "green" : "amber"}>
            {user.phoneVerified ? t("verified") : t("notVerified")}
          </Badge>
        </div>
        <div className="mt-5 grid gap-3 sm:grid-cols-[1fr_auto]">
          <Field label={t("phoneNumber")}>
            <Input
              onChange={(event) => setPhone(event.target.value)}
              placeholder={user.phone || "+998 90 000 00 00"}
              value={phone}
            />
          </Field>
          <Button
            className="self-end"
            disabled={!phone || updatePhone.isPending}
            onClick={() => updatePhone.mutate()}
            variant="secondary"
          >
            {t("save")}
          </Button>
        </div>
        <div className="mt-5 border-t border-[var(--border)] pt-5">
          <Button
            disabled={!user.phone || requestOtp.isPending}
            onClick={() => requestOtp.mutate()}
            variant="secondary"
          >
            {t("requestCode")}
          </Button>
          <div className="mt-3 grid gap-3 sm:grid-cols-[1fr_auto]">
            <Field label={t("smsCode")}>
              <Input
                inputMode="numeric"
                maxLength={6}
                onChange={(event) =>
                  setOtpCode(event.target.value.replace(/\D/g, ""))
                }
                placeholder="000000"
                value={otpCode}
              />
            </Field>
            <Button
              className="self-end"
              disabled={otpCode.length !== 6 || verifyOtp.isPending}
              onClick={() => verifyOtp.mutate()}
            >
              <ShieldCheck className="size-4" />
              {t("confirm")}
            </Button>
          </div>
        </div>
      </section>

      <section className="surface-panel flex flex-col justify-between gap-5 p-5 sm:flex-row sm:items-center sm:p-6">
        <div>
          <h2 className="flex items-center gap-2 text-lg font-bold">
            <MailCheck className="size-5 text-[var(--blue)]" />
            {t("email")}
          </h2>
          <p className="mt-1 text-sm text-[var(--muted)]">
            {user.email} ·{" "}
            {user.emailVerified ? t("emailVerified") : t("emailNotVerified")}
          </p>
        </div>
        <Button
          disabled={user.emailVerified || requestEmail.isPending}
          onClick={() => requestEmail.mutate()}
          variant="secondary"
        >
          {t("sendEmail")}
        </Button>
      </section>
    </div>
  );
}
