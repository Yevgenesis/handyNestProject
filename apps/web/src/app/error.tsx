"use client";

import { AlertTriangle, RotateCcw } from "lucide-react";
import { useTranslations } from "next-intl";
import { useEffect } from "react";

import { Button } from "@/shared/ui/button";

export default function ErrorPage({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  const t = useTranslations("states");

  useEffect(() => {
    console.error("Frontend route error", {
      name: error.name,
      digest: error.digest,
    });
  }, [error]);

  return (
    <section className="container-page page-section grid min-h-[60vh] place-items-center text-center">
      <div>
        <AlertTriangle
          className="mx-auto size-12 text-[var(--amber)]"
          aria-hidden
        />
        <h1 className="mt-5 text-3xl font-black">{t("errorTitle")}</h1>
        <p className="mt-3 text-[var(--muted)]">{t("errorDescription")}</p>
        <Button className="mt-6" onClick={reset}>
          <RotateCcw className="size-4" />
          {t("retry")}
        </Button>
      </div>
    </section>
  );
}
