import { ProtectedRoute } from "@/features/auth/protected-route";
import { TaskOwnerPanel } from "@/features/tasks/task-owner-panel";

export default async function MyTaskPage({
  params,
}: {
  params: Promise<{ taskId: string }>;
}) {
  const { taskId } = await params;
  return (
    <ProtectedRoute>
      <main className="container-page page-section">
        <TaskOwnerPanel taskId={taskId} />
      </main>
    </ProtectedRoute>
  );
}
