import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";

import { FeedbackPanel } from "@/features/feedback/feedback-panel";
import { renderWithProviders } from "@/test/render";

const { createTaskFeedback, getTaskFeedbacks } = vi.hoisted(() => ({
  createTaskFeedback: vi.fn(),
  getTaskFeedbacks: vi.fn(),
}));

vi.mock("@/features/auth/auth-provider", () => ({
  useAuth: () => ({ user: { publicId: "user-1" } }),
}));
vi.mock("@/shared/api/marketplace-client", async () => {
  const actual = await vi.importActual<
    typeof import("@/shared/api/marketplace-client")
  >("@/shared/api/marketplace-client");
  return { ...actual, createTaskFeedback, getTaskFeedbacks };
});

describe("FeedbackPanel", () => {
  beforeEach(() => {
    createTaskFeedback.mockReset();
    getTaskFeedbacks.mockReset();
    getTaskFeedbacks.mockResolvedValue([]);
    createTaskFeedback.mockResolvedValue({ publicId: "feedback-1" });
  });

  it("is shown only for a completed deal and submits an idempotent request", async () => {
    renderWithProviders(
      <FeedbackPanel dealStatus="COMPLETED" taskId="task-1" />,
    );

    await userEvent.click(
      await screen.findByRole("radio", { name: "Оценка 5 из 5" }),
    );
    await userEvent.type(
      screen.getByLabelText("Комментарий"),
      "Работа выполнена аккуратно",
    );
    await userEvent.click(
      screen.getByRole("button", { name: "Опубликовать отзыв" }),
    );

    await waitFor(() =>
      expect(createTaskFeedback).toHaveBeenCalledWith(
        "task-1",
        { grade: 5, text: "Работа выполнена аккуратно" },
        expect.stringMatching(/^feedback-/),
      ),
    );
  });
});
