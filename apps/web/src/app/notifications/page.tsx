import { ProtectedRoute } from "@/features/auth/protected-route";
import { NotificationsPanel } from "@/features/notifications/notifications-panel";

export default function NotificationsPage() {
  return (
    <ProtectedRoute>
      <section className="container-page page-section">
        <NotificationsPanel />
      </section>
    </ProtectedRoute>
  );
}
