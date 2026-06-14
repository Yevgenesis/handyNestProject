import { Plus } from "lucide-react";
import { getTranslations } from "next-intl/server";
import Link from "next/link";

import { ProtectedRoute } from "@/features/auth/protected-route";
import { MyTasksPanel } from "@/features/tasks/my-tasks-panel";
import { Button } from "@/shared/ui/button";

export default async function MyTasksPage() {
  const t = await getTranslations("myTasks");
  return (
    <ProtectedRoute>
      <main className="container-page page-section">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="font-semibold text-[var(--primary)]">
              {t("eyebrow")}
            </p>
            <h1 className="mt-2 text-3xl font-black">{t("title")}</h1>
            <p className="mt-2 text-[var(--muted)]">{t("description")}</p>
          </div>
          <Button asChild>
            <Link href="/tasks/new">
              <Plus className="size-4" />
              {t("create")}
            </Link>
          </Button>
        </div>
        <div className="mt-8">
          <MyTasksPanel />
        </div>
      </main>
    </ProtectedRoute>
  );
}
