import { Suspense } from "react";

import { VerifyEmailPanel } from "@/features/auth/verify-email-panel";

export default function VerifyEmailPage() {
  return (
    <section className="container-page page-section grid place-items-center">
      <div className="surface-panel w-full max-w-lg p-8">
        <Suspense>
          <VerifyEmailPanel />
        </Suspense>
      </div>
    </section>
  );
}
