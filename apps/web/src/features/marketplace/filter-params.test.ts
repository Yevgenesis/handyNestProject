import { describe, expect, it } from "vitest";

import {
  pageParam,
  withSearchParams,
} from "@/features/marketplace/filter-params";

describe("marketplace filter params", () => {
  it("keeps filters while changing page", () => {
    expect(
      withSearchParams("/tasks", { cityId: "city_1", page: "2" }, { page: 3 }),
    ).toBe("/tasks?cityId=city_1&page=3");
  });

  it("normalizes invalid pages", () => {
    expect(pageParam("0")).toBe(1);
    expect(pageParam("abc")).toBe(1);
  });
});
