import { screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import { AuthProvider, useAuth } from "@/features/auth/auth-provider";
import { renderWithProviders } from "@/test/render";

vi.mock("@/shared/api/session-client", () => ({
  restoreSession: vi
    .fn()
    .mockResolvedValue({ publicId: "usr_1", firstName: "Айжан" }),
  clearSession: vi.fn(),
  loginSession: vi.fn(),
  logoutSession: vi.fn(),
  registerSession: vi.fn(),
}));

function SessionProbe() {
  const { status, user } = useAuth();
  return (
    <p>
      {status}:{user?.firstName}
    </p>
  );
}

describe("AuthProvider", () => {
  it("restores an authenticated session on application start", async () => {
    renderWithProviders(
      <AuthProvider>
        <SessionProbe />
      </AuthProvider>,
    );
    expect(await screen.findByText("authenticated:Айжан")).toBeInTheDocument();
  });
});
