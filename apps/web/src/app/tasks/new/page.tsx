import { getTranslations } from "next-intl/server";

import { ProtectedRoute } from "@/features/auth/protected-route";
import { TaskWizard } from "@/features/tasks/task-wizard";

export default async function NewTaskPage() {
  const t = await getTranslations("taskWizard");
  return (
    <ProtectedRoute>
      <main className="container-page page-section">
        <p className="font-semibold text-[var(--primary)]">{t("eyebrow")}</p>
        <h1 className="mt-2 text-3xl font-black">{t("pageTitle")}</h1>
        <p className="mt-2 max-w-2xl text-[var(--muted)]">
          {t("pageDescription")}
        </p>
        <div className="mt-8">
          <TaskWizard />
        </div>
      </main>
    </ProtectedRoute>
  );
}
