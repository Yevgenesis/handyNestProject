import { screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { TaskCard } from "@/entities/task/task-card";
import { renderWithProviders } from "@/test/render";

describe("TaskCard", () => {
  it("renders localized values without numeric ids", () => {
    renderWithProviders(
      <TaskCard
        task={{
          publicId: "tsk_public",
          title: "Установить розетки",
          categoryTitle: "Электрика",
          cityName: "Ташкент",
          fixedPrice: 25000,
          serviceMode: "ONSITE",
        }}
      />,
    );
    expect(
      screen.getByRole("link", { name: "Установить розетки" }),
    ).toHaveAttribute("href", "/tasks/tsk_public");
    expect(screen.getByText(/25/)).toBeInTheDocument();
    expect(screen.getByText("На месте")).toBeInTheDocument();
  });
});
