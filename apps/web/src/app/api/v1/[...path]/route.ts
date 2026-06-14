const HOP_BY_HOP_HEADERS = new Set([
  "connection",
  "content-encoding",
  "content-length",
  "host",
  "keep-alive",
  "transfer-encoding",
  "upgrade",
]);

async function proxy(
  request: Request,
  context: { params: Promise<{ path: string[] }> },
) {
  const { path } = await context.params;
  const incomingUrl = new URL(request.url);
  const backendBaseUrl =
    process.env.HANDYNEST_BACKEND_URL ?? "http://localhost:8080";
  const targetUrl = new URL(
    `/api/v1/${path.join("/")}${incomingUrl.search}`,
    backendBaseUrl,
  );
  const requestHeaders = new Headers(request.headers);

  for (const header of HOP_BY_HOP_HEADERS) requestHeaders.delete(header);
  requestHeaders.set("accept-encoding", "identity");

  const init: RequestInit = {
    method: request.method,
    headers: requestHeaders,
    cache: "no-store",
    redirect: "manual",
  };

  if (request.method !== "GET" && request.method !== "HEAD") {
    init.body = await request.arrayBuffer();
  }

  try {
    const backendResponse = await fetch(targetUrl, init);
    const responseHeaders = new Headers(backendResponse.headers);
    for (const header of HOP_BY_HOP_HEADERS) responseHeaders.delete(header);

    return new Response(backendResponse.body, {
      status: backendResponse.status,
      statusText: backendResponse.statusText,
      headers: responseHeaders,
    });
  } catch {
    return Response.json(
      {
        status: 503,
        error: "Service Unavailable",
        code: "BACKEND_UNAVAILABLE",
        message: "Backend is temporarily unavailable",
        path: incomingUrl.pathname,
        details: [],
      },
      { status: 503 },
    );
  }
}

export const dynamic = "force-dynamic";
export const GET = proxy;
export const POST = proxy;
export const PUT = proxy;
export const PATCH = proxy;
export const DELETE = proxy;
export const HEAD = proxy;
export const OPTIONS = proxy;
