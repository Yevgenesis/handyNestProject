import createClient from "openapi-fetch";

import type { paths } from "@/shared/api/generated/schema";

export type ServerApiResult<T> =
  | { data: T; error?: never }
  | { data?: never; error: string; status?: number };

export function createServerApiClient(
  locale: string,
  revalidateSeconds?: number,
) {
  return createClient<paths>({
    baseUrl: process.env.HANDYNEST_BACKEND_URL ?? "http://localhost:8080",
    headers: { Accept: "application/json", "Accept-Language": locale },
    fetch: (request) =>
      fetch(
        request,
        revalidateSeconds
          ? { next: { revalidate: revalidateSeconds } }
          : { cache: "no-store" },
      ),
  });
}

export async function toServerResult<T>(
  request: Promise<{ data?: T; response: Response }>,
): Promise<ServerApiResult<T>> {
  try {
    const result = await request;
    if (result.data !== undefined) return { data: result.data };
    return {
      error: `Backend returned ${result.response.status}`,
      status: result.response.status,
    };
  } catch {
    return { error: "Backend is unavailable" };
  }
}
