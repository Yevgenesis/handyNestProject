export const TASK_DRAFT_VERSION = 2;

export type TaskDraft = {
  title: string;
  description: string;
  categoryId: string;
  serviceMode: "ONSITE" | "REMOTE" | "HYBRID";
  cityId: string;
  districtId: string;
  addressText: string;
  expiresAt: string;
  priceType: "FIXED" | "HOURLY" | "NEGOTIABLE";
  fixedPrice: string;
  budgetMin: string;
  budgetMax: string;
};

type StoredTaskDraft = {
  version: number;
  userId: string;
  values: TaskDraft;
};

export function taskDraftKey(userId: string) {
  return `handynest:task-draft:${userId}`;
}

export function saveTaskDraft(userId: string, values: TaskDraft) {
  const payload: StoredTaskDraft = {
    version: TASK_DRAFT_VERSION,
    userId,
    values,
  };
  localStorage.setItem(taskDraftKey(userId), JSON.stringify(payload));
}

export function loadTaskDraft(userId: string): TaskDraft | null {
  const raw = localStorage.getItem(taskDraftKey(userId));
  if (!raw) return null;
  try {
    const parsed = JSON.parse(raw) as StoredTaskDraft;
    if (parsed.version !== TASK_DRAFT_VERSION || parsed.userId !== userId)
      return null;
    return parsed.values;
  } catch {
    return null;
  }
}

export function clearTaskDraft(userId: string) {
  localStorage.removeItem(taskDraftKey(userId));
}
