import { ProtectedRoute } from "@/features/auth/protected-route";
import { DealPanel } from "@/features/deals/deal-panel";

export default async function DealPage({
  params,
}: {
  params: Promise<{ dealId: string }>;
}) {
  const { dealId } = await params;
  return (
    <ProtectedRoute>
      <main className="container-page page-section">
        <DealPanel dealId={dealId} />
      </main>
    </ProtectedRoute>
  );
}
