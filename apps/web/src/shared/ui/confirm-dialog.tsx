"use client";

import * as Dialog from "@radix-ui/react-dialog";
import { X } from "lucide-react";
import { useTranslations } from "next-intl";

import { Button } from "@/shared/ui/button";

export function ConfirmDialog({
  trigger,
  title,
  description,
  confirmLabel,
  pending,
  danger,
  onConfirm,
}: {
  trigger: React.ReactNode;
  title: string;
  description: string;
  confirmLabel: string;
  pending?: boolean;
  danger?: boolean;
  onConfirm: () => void;
}) {
  const t = useTranslations("common");
  return (
    <Dialog.Root>
      <Dialog.Trigger asChild>{trigger}</Dialog.Trigger>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 z-50 bg-black/35" />
        <Dialog.Content className="fixed top-1/2 left-1/2 z-50 w-[min(440px,calc(100%-32px))] -translate-x-1/2 -translate-y-1/2 rounded-[8px] border border-[var(--border)] bg-white p-6 shadow-xl">
          <Dialog.Title className="pr-10 text-xl font-black">
            {title}
          </Dialog.Title>
          <Dialog.Description className="mt-2 text-sm text-[var(--muted)]">
            {description}
          </Dialog.Description>
          <div className="mt-6 flex justify-end gap-3">
            <Dialog.Close asChild>
              <Button variant="secondary">{t("cancel")}</Button>
            </Dialog.Close>
            <Dialog.Close asChild>
              <Button
                disabled={pending}
                onClick={onConfirm}
                variant={danger ? "danger" : "primary"}
              >
                {confirmLabel}
              </Button>
            </Dialog.Close>
          </div>
          <Dialog.Close asChild>
            <Button
              aria-label={t("close")}
              className="absolute top-3 right-3"
              size="icon"
              variant="ghost"
            >
              <X className="size-4" />
            </Button>
          </Dialog.Close>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  );
}
