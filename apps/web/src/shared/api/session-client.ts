import createClient from "openapi-fetch";

import type { paths } from "@/shared/api/generated/schema";
import { mapApiErrorPayload } from "@/shared/api/errors";
import type {
  ApiSchemas,
  AuthLoginRequest,
  AuthRegisterRequest,
  AuthTokenResponse,
  AuthUserResponse,
  ConsentRequirement,
  UserProfile,
} from "@/shared/api/types";

let accessToken: string | null = null;
let refreshPromise: Promise<AuthUserResponse | null> | null = null;
const browserBaseUrl =
  typeof window === "undefined" ? "http://localhost" : window.location.origin;

async function cookieFetch(input: Request) {
  return fetch(input, { credentials: "include" });
}

const publicApiClient = createClient<paths>({
  baseUrl: browserBaseUrl,
  fetch: cookieFetch,
});

function acceptSession(payload: AuthTokenResponse): AuthUserResponse | null {
  accessToken = payload.accessToken ?? null;
  return payload.user ?? null;
}

async function requestRefresh(): Promise<AuthUserResponse | null> {
  const result = await publicApiClient.POST("/api/v1/auth/refresh", {
    body: {},
  });
  if (!result.data) {
    accessToken = null;
    return null;
  }
  return acceptSession(result.data);
}

export function restoreSession() {
  if (!refreshPromise) {
    refreshPromise = requestRefresh().finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}

export function clearSession() {
  accessToken = null;
}

export async function authenticatedFetch(
  input: RequestInfo | URL,
  init: RequestInit = {},
) {
  const request = input instanceof Request ? input : new Request(input, init);
  const headers = new Headers(request.headers);
  new Headers(init.headers).forEach((value, key) => headers.set(key, value));
  if (accessToken) headers.set("Authorization", `Bearer ${accessToken}`);

  const send = () =>
    fetch(new Request(request.clone(), { headers }), {
      credentials: "include",
    });
  let response = await send();
  const url = request.url;

  if (response.status === 401 && !url.includes("/auth/refresh")) {
    const user = await restoreSession();
    if (user && accessToken) {
      headers.set("Authorization", `Bearer ${accessToken}`);
      response = await send();
    }
  }
  return response;
}

export const apiClient = createClient<paths>({
  baseUrl: browserBaseUrl,
  fetch: authenticatedFetch,
});

function requireData<T>(result: {
  data?: T;
  error?: unknown;
  response: Response;
}): T {
  if (result.data !== undefined) return result.data;
  throw mapApiErrorPayload(result.response, result.error);
}

export async function loginSession(
  request: AuthLoginRequest,
): Promise<AuthTokenResponse> {
  const payload = requireData(
    await publicApiClient.POST("/api/v1/auth/login", { body: request }),
  );
  acceptSession(payload);
  return payload;
}

export async function registerSession(
  request: AuthRegisterRequest,
): Promise<AuthTokenResponse> {
  const payload = requireData(
    await publicApiClient.POST("/api/v1/auth/register", { body: request }),
  );
  acceptSession(payload);
  return payload;
}

export async function logoutSession(): Promise<void> {
  const result = await apiClient.POST("/api/v1/auth/logout", { body: {} });
  if (!result.response.ok)
    throw mapApiErrorPayload(result.response, result.error);
}

export async function getConsentRequirements(): Promise<ConsentRequirement[]> {
  return requireData(
    await publicApiClient.GET("/api/v1/legal/consent-requirements"),
  );
}

export async function getUserProfile(): Promise<UserProfile> {
  return requireData(await apiClient.GET("/api/v1/users/me"));
}

export async function updateUserProfile(
  body: ApiSchemas["UserProfileUpdateRequest"],
): Promise<UserProfile> {
  return requireData(await apiClient.PATCH("/api/v1/users/me", { body }));
}

export async function requestPhoneOtp(): Promise<
  ApiSchemas["PhoneOtpRequestResponse"]
> {
  return requireData(await apiClient.POST("/api/v1/auth/request-phone-otp"));
}

export async function verifyPhoneOtp(
  code: string,
): Promise<ApiSchemas["PhoneOtpVerifyResponse"]> {
  return requireData(
    await apiClient.POST("/api/v1/auth/verify-phone-otp", { body: { code } }),
  );
}

export async function requestEmailVerification(): Promise<
  ApiSchemas["EmailVerificationRequestResponse"]
> {
  return requireData(
    await apiClient.POST("/api/v1/auth/request-email-verification"),
  );
}

export async function verifyEmail(
  token: string,
): Promise<ApiSchemas["EmailVerificationVerifyResponse"]> {
  return requireData(
    await publicApiClient.POST("/api/v1/auth/verify-email", {
      body: { token },
    }),
  );
}
