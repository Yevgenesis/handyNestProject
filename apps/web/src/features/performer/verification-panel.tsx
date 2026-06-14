"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { FileCheck2, ShieldCheck, Upload } from "lucide-react";
import { useTranslations } from "next-intl";
import Link from "next/link";
import { useState } from "react";
import { toast } from "sonner";

import { StatusBadge } from "@/entities/marketplace/status-badge";
import { getErrorMessage } from "@/shared/api/errors";
import {
  getVerificationDocuments,
  getVerificationRequests,
  submitVerificationRequest,
  uploadVerificationDocument,
} from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import { getUserProfile } from "@/shared/api/session-client";
import { Button } from "@/shared/ui/button";
import { EmptyState, ErrorState } from "@/shared/ui/page-state";
import { Skeleton } from "@/shared/ui/skeleton";

export function VerificationPanel() {
  const t = useTranslations("verification");
  const apiErrors = useTranslations("apiErrors");
  const queryClient = useQueryClient();
  const [identity, setIdentity] = useState<File | null>(null);
  const [selfie, setSelfie] = useState<File | null>(null);
  const profile = useQuery({
    queryKey: queryKeys.userProfile,
    queryFn: getUserProfile,
  });
  const documents = useQuery({
    queryKey: queryKeys.verificationDocuments,
    queryFn: getVerificationDocuments,
  });
  const requests = useQuery({
    queryKey: queryKeys.verificationRequests,
    queryFn: getVerificationRequests,
  });
  const submit = useMutation({
    mutationFn: async () => {
      if (!identity || !selfie) throw new Error(t("twoDocumentsRequired"));
      const identityAttachment = await uploadVerificationDocument(
        identity,
        "IDENTITY_DOCUMENT",
      );
      const selfieAttachment = await uploadVerificationDocument(
        selfie,
        "SELFIE",
      );
      return submitVerificationRequest([
        identityAttachment?.publicId ?? "",
        selfieAttachment?.publicId ?? "",
      ]);
    },
    onSuccess() {
      setIdentity(null);
      setSelfie(null);
      void queryClient.invalidateQueries({
        queryKey: queryKeys.verificationDocuments,
      });
      void queryClient.invalidateQueries({
        queryKey: queryKeys.verificationRequests,
      });
      toast.success(t("submitted"));
    },
    onError: (error) => toast.error(getErrorMessage(error, apiErrors)),
  });
  if (profile.isLoading || documents.isLoading || requests.isLoading)
    return <Skeleton className="h-96" />;
  if (profile.isError) return <ErrorState title={t("loadError")} />;
  if (!profile.data?.phoneVerified)
    return (
      <section className="surface-panel p-6 text-center">
        <ShieldCheck className="mx-auto size-9 text-[var(--amber)]" />
        <h2 className="mt-4 text-xl font-bold">{t("phoneRequired")}</h2>
        <p className="mt-2 text-sm text-[var(--muted)]">
          {t("phoneRequiredDescription")}
        </p>
        <Button asChild className="mt-5">
          <Link href="/account/security">{t("verifyPhone")}</Link>
        </Button>
      </section>
    );
  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_380px]">
      <section className="surface-panel p-5 sm:p-6">
        <Upload className="size-7 text-[var(--primary)]" />
        <h2 className="mt-4 text-xl font-bold">{t("documentsTitle")}</h2>
        <p className="mt-2 text-sm text-[var(--muted)]">
          {t("documentsDescription")}
        </p>
        <div className="mt-6 grid gap-5">
          <label className="grid gap-2 text-sm font-medium">
            <span>{t("identityDocument")}</span>
            <input
              accept="application/pdf,image/jpeg,image/png"
              onChange={(event) => setIdentity(event.target.files?.[0] ?? null)}
              type="file"
            />
          </label>
          <label className="grid gap-2 text-sm font-medium">
            <span>{t("selfie")}</span>
            <input
              accept="image/jpeg,image/png"
              onChange={(event) => setSelfie(event.target.files?.[0] ?? null)}
              type="file"
            />
          </label>
          <Button
            disabled={!identity || !selfie || submit.isPending}
            onClick={() => submit.mutate()}
          >
            <FileCheck2 className="size-4" />
            {submit.isPending ? t("submitting") : t("submit")}
          </Button>
        </div>
        <p className="mt-5 border-t border-[var(--border)] pt-4 text-xs text-[var(--muted)]">
          {t("privacy")}
        </p>
      </section>
      <aside className="surface-panel self-start p-5 sm:p-6">
        <h2 className="text-xl font-bold">{t("history")}</h2>
        {!requests.data?.length ? (
          <EmptyState
            title={t("emptyHistory")}
            description={t("emptyHistoryDescription")}
          />
        ) : (
          <div className="mt-5 grid gap-4">
            {requests.data.map((request) => (
              <article
                className="rounded-[8px] border border-[var(--border)] p-4"
                key={request.publicId}
              >
                <div className="flex justify-between gap-3">
                  <p className="font-bold">
                    {t(
                      `level.${request.requestedLevel ?? "IDENTITY_VERIFIED"}`,
                    )}
                  </p>
                  <StatusBadge status={request.status} />
                </div>
                {request.rejectionReason ? (
                  <p className="mt-3 text-sm text-[var(--red)]">
                    {request.rejectionReason}
                  </p>
                ) : null}
                <p className="mt-3 text-xs text-[var(--muted)]">
                  {t("documentsCount", {
                    count: request.documents?.length ?? 0,
                  })}
                </p>
              </article>
            ))}
          </div>
        )}
      </aside>
    </div>
  );
}
