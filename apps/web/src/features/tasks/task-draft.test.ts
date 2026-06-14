import { beforeEach, describe, expect, it } from "vitest";

import {
  clearTaskDraft,
  loadTaskDraft,
  saveTaskDraft,
  TASK_DRAFT_VERSION,
  taskDraftKey,
  type TaskDraft,
} from "@/features/tasks/task-draft";

const draft: TaskDraft = {
  title: "Установить светильник",
  description: "Нужно установить потолочный светильник в гостиной.",
  categoryId: "category-public-id",
  serviceMode: "ONSITE",
  cityId: "city-public-id",
  districtId: "district-public-id",
  addressText: "Ташкент, Юнусабадский район",
  expiresAt: "2026-07-01",
  priceType: "FIXED",
  fixedPrice: "25000",
  budgetMin: "",
  budgetMax: "",
};

describe("task draft storage", () => {
  beforeEach(() => localStorage.clear());

  it("restores only the current schema version for the same public user id", () => {
    saveTaskDraft("user-a", draft);

    expect(loadTaskDraft("user-a")).toEqual(draft);
    expect(loadTaskDraft("user-b")).toBeNull();

    localStorage.setItem(
      taskDraftKey("user-a"),
      JSON.stringify({
        version: TASK_DRAFT_VERSION + 1,
        userId: "user-a",
        values: draft,
      }),
    );
    expect(loadTaskDraft("user-a")).toBeNull();
  });

  it("clears a published draft", () => {
    saveTaskDraft("user-a", draft);
    clearTaskDraft("user-a");

    expect(loadTaskDraft("user-a")).toBeNull();
  });
});
