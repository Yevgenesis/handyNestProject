import { beforeEach, describe, expect, it, vi } from "vitest";

describe("session restoration", () => {
  beforeEach(() => {
    vi.resetModules();
  });

  it("uses one refresh request for concurrent callers", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      Response.json({
        accessToken: "access",
        refreshToken: "ignored",
        user: { publicId: "usr_1", email: "a@b.kz" },
      }),
    );
    vi.stubGlobal("fetch", fetchMock);
    const { restoreSession } = await import("@/shared/api/session-client");

    const [first, second] = await Promise.all([
      restoreSession(),
      restoreSession(),
    ]);

    expect(fetchMock).toHaveBeenCalledTimes(1);
    expect(first?.publicId).toBe("usr_1");
    expect(second?.publicId).toBe("usr_1");
  });

  it("preserves generated request headers and body for authenticated mutations", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        Response.json({ accessToken: "access", user: { publicId: "usr_1" } }),
      )
      .mockResolvedValueOnce(Response.json({ ok: true }));
    vi.stubGlobal("fetch", fetchMock);
    const { authenticatedFetch, restoreSession } =
      await import("@/shared/api/session-client");

    await restoreSession();
    await authenticatedFetch(
      new Request("http://localhost/api/v1/auth/logout", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: "{}",
      }),
    );

    const sentRequest = fetchMock.mock.calls[1]?.[0] as Request;
    expect(sentRequest.headers.get("Content-Type")).toBe("application/json");
    expect(sentRequest.headers.get("Authorization")).toBe("Bearer access");
    expect(await sentRequest.text()).toBe("{}");
  });
});
