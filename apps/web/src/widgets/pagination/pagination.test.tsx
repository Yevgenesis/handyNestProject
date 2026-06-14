import { screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

import { renderWithProviders } from "@/test/render";
import { Pagination } from "@/widgets/pagination/pagination";

describe("Pagination", () => {
  it("preserves filters in previous and next links", () => {
    renderWithProviders(
      <Pagination
        page={2}
        pathname="/tasks"
        searchParams={{ categoryId: "cat_1", page: "2" }}
        totalPages={4}
      />,
    );

    expect(screen.getByRole("link", { name: /Назад/ })).toHaveAttribute(
      "href",
      "/tasks?categoryId=cat_1&page=1",
    );
    expect(screen.getByRole("link", { name: /Далее/ })).toHaveAttribute(
      "href",
      "/tasks?categoryId=cat_1&page=3",
    );
  });
});
