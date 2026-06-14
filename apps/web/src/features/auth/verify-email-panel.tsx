"use client";

import { CheckCircle2, MailWarning } from "lucide-react";
import { useTranslations } from "next-intl";
import { useSearchParams } from "next/navigation";
import { useState } from "react";

import { verifyEmail } from "@/shared/api/session-client";
import { getErrorMessage } from "@/shared/api/errors";
import { Button } from "@/shared/ui/button";

export function VerifyEmailPanel() {
  const t = useTranslations("auth");
  const apiErrors = useTranslations("apiErrors");
  const searchParams = useSearchParams();
  const [state, setState] = useState<"idle" | "loading" | "success" | "error">(
    "idle",
  );
  const [message, setMessage] = useState("");
  const token = searchParams.get("token");

  async function verify() {
    if (!token) return;
    setState("loading");
    try {
      await verifyEmail(token);
      setState("success");
    } catch (error) {
      setMessage(getErrorMessage(error, apiErrors));
      setState("error");
    }
  }

  if (state === "success")
    return (
      <div className="text-center">
        <CheckCircle2 className="mx-auto size-10 text-[var(--primary)]" />
        <h1 className="mt-4 text-2xl font-black">{t("emailVerified")}</h1>
        <p className="mt-2 text-[var(--muted)]">
          {t("emailVerifiedDescription")}
        </p>
      </div>
    );
  return (
    <div className="text-center">
      <MailWarning className="mx-auto size-10 text-[var(--amber)]" />
      <h1 className="mt-4 text-2xl font-black">{t("verifyEmailTitle")}</h1>
      <p className="mt-2 mb-6 text-[var(--muted)]">
        {token ? t("verifyEmailPrompt") : t("verifyEmailMissingToken")}
      </p>
      {state === "error" ? (
        <p className="mb-4 text-sm text-[var(--red)]">{message}</p>
      ) : null}
      <Button
        disabled={!token || state === "loading"}
        onClick={() => void verify()}
      >
        {state === "loading" ? t("verifyingEmail") : t("verifyEmailButton")}
      </Button>
    </div>
  );
}
