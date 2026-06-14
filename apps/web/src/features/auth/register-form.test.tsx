import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import {
  buildRegistrationConsents,
  RegisterForm,
} from "@/features/auth/register-form";
import { renderWithProviders } from "@/test/render";

const { register, getConsentRequirements } = vi.hoisted(() => ({
  register: vi.fn(),
  getConsentRequirements: vi.fn(),
}));

vi.mock("@/features/auth/auth-provider", () => ({
  useAuth: () => ({ register, status: "anonymous" }),
}));
vi.mock("@/shared/api/session-client", () => ({ getConsentRequirements }));
vi.mock("next/navigation", () => ({ useRouter: () => ({ replace: vi.fn() }) }));

describe("registration consents", () => {
  beforeEach(() => {
    register.mockReset();
    getConsentRequirements.mockResolvedValue([
      {
        type: "TERMS_OF_SERVICE",
        documentVersion: "1.2",
        requiredAtRegistration: true,
      },
    ]);
  });

  it("submits current backend document versions only for selected required consents", () => {
    const requirements = [
      {
        type: "TERMS_OF_SERVICE" as const,
        documentVersion: "1.2",
        requiredAtRegistration: true,
      },
      {
        type: "PRIVACY_POLICY" as const,
        documentVersion: "2.0",
        requiredAtRegistration: true,
      },
      {
        type: "CUSTOMER_RULES" as const,
        documentVersion: "1.0",
        requiredAtRegistration: false,
      },
    ];

    expect(
      buildRegistrationConsents(requirements, [
        "TERMS_OF_SERVICE",
        "PRIVACY_POLICY",
      ]),
    ).toEqual([
      { type: "TERMS_OF_SERVICE", documentVersion: "1.2" },
      { type: "PRIVACY_POLICY", documentVersion: "2.0" },
    ]);
  });

  it("renders the current consent version returned by backend", async () => {
    renderWithProviders(<RegisterForm />);
    expect(
      await screen.findByText(/Условия использования.*1.2/),
    ).toBeInTheDocument();
  });

  it("submits a valid password containing a common exclamation mark", async () => {
    register.mockResolvedValue(undefined);
    renderWithProviders(<RegisterForm />);

    await userEvent.type(screen.getByLabelText("Имя"), "Иван");
    await userEvent.type(screen.getByLabelText("Фамилия"), "Иванов");
    await userEvent.type(
      screen.getByLabelText("Электронная почта"),
      "ivan@example.com",
    );
    await userEvent.type(screen.getByLabelText("Пароль"), "ValidPass1!");
    await userEvent.type(
      screen.getByLabelText("Повторите пароль"),
      "ValidPass1!",
    );
    await userEvent.click(
      await screen.findByRole("checkbox", {
        name: /Условия использования.*1.2/,
      }),
    );
    await userEvent.click(
      screen.getByRole("button", { name: "Создать аккаунт" }),
    );

    expect(register).toHaveBeenCalledWith(
      expect.objectContaining({ password: "ValidPass1!" }),
    );
  });
});
