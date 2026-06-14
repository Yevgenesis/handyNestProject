import { describe, expect, it } from "vitest";

import {
  ApiRequestError,
  getErrorMessage,
  mapApiError,
} from "@/shared/api/errors";

describe("mapApiError", () => {
  it("maps backend field errors and retry header", async () => {
    const response = Response.json(
      {
        status: 429,
        code: "RATE_LIMITED",
        message: "Too many requests",
        details: [{ field: "email", message: "Invalid email" }],
      },
      { status: 429, headers: { "Retry-After": "45" } },
    );

    const error = await mapApiError(response);

    expect(error.code).toBe("RATE_LIMITED");
    expect(
      getErrorMessage(
        error,
        (key) => ({ RATE_LIMITED: "Слишком много попыток" })[key] ?? key,
      ),
    ).toBe("Слишком много попыток");
    expect(error.fieldErrors).toEqual({ email: "Invalid email" });
    expect(error.retryAfterSeconds).toBe(45);
  });

  it("falls back to the backend message when a translation key is missing", () => {
    const error = new ApiRequestError("District not found", 404, "NOT_FOUND");

    expect(
      getErrorMessage(error, () => {
        throw new Error("missing translation");
      }),
    ).toBe("District not found");
  });
});
