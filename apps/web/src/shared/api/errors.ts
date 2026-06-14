import type { ApiErrorResponse } from "@/shared/api/types";

export class ApiRequestError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly code?: string,
    public readonly fieldErrors: Record<string, string> = {},
    public readonly retryAfterSeconds?: number,
  ) {
    super(message);
    this.name = "ApiRequestError";
  }
}

function isApiErrorResponse(value: unknown): value is ApiErrorResponse {
  return typeof value === "object" && value !== null;
}

export function mapApiErrorPayload(
  response: Response,
  value: unknown,
): ApiRequestError {
  const payload = isApiErrorResponse(value) ? value : undefined;
  const fieldErrors = Object.fromEntries(
    (payload?.details ?? [])
      .filter((detail) => detail.field && detail.message)
      .map((detail) => [detail.field as string, detail.message as string]),
  );
  const retryAfter = response.headers.get("Retry-After");
  const code = payload?.code;

  return new ApiRequestError(
    payload?.message || "Не удалось выполнить запрос",
    response.status,
    code,
    fieldErrors,
    retryAfter ? Number(retryAfter) : undefined,
  );
}

export async function mapApiError(
  response: Response,
): Promise<ApiRequestError> {
  let payload: ApiErrorResponse | undefined;
  try {
    payload = (await response.json()) as ApiErrorResponse;
  } catch {
    payload = undefined;
  }

  return mapApiErrorPayload(response, payload);
}

export function getErrorMessage(
  error: unknown,
  translate?: (key: string) => string,
) {
  if (error instanceof ApiRequestError && error.code && translate) {
    try {
      return translate(error.code);
    } catch {
      return error.message;
    }
  }
  if (error instanceof Error) return error.message;
  if (translate) {
    try {
      return translate("UNEXPECTED");
    } catch {
      // The backend message remains the final fallback for incomplete dictionaries.
    }
  }
  return "Произошла непредвиденная ошибка";
}
