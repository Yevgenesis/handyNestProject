import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { LoginForm } from "@/features/auth/login-form";
import { renderWithProviders } from "@/test/render";

const { login } = vi.hoisted(() => ({ login: vi.fn() }));

vi.mock("@/features/auth/auth-provider", () => ({
  useAuth: () => ({ login, status: "anonymous" }),
}));
vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn() }),
  useSearchParams: () => new URLSearchParams(),
}));

describe("LoginForm", () => {
  beforeEach(() => login.mockReset());

  it("shows localized validation errors and does not submit invalid data", async () => {
    renderWithProviders(<LoginForm />);
    await userEvent.click(screen.getByRole("button", { name: "Войти" }));

    expect(
      await screen.findByText("Введите корректный email"),
    ).toBeInTheDocument();
    expect(screen.getByText("Введите пароль")).toBeInTheDocument();
    expect(login).not.toHaveBeenCalled();
  });
});
