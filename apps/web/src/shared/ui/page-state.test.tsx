import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { EmptyState } from "@/shared/ui/page-state";

describe("EmptyState", () => {
  it("renders a clear title and recovery hint", () => {
    render(
      <EmptyState description="Измените фильтры" title="Ничего не найдено" />,
    );
    expect(
      screen.getByRole("heading", { name: "Ничего не найдено" }),
    ).toBeInTheDocument();
    expect(screen.getByText("Измените фильтры")).toBeInTheDocument();
  });
});
