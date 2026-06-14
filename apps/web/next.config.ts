import createNextIntlPlugin from "next-intl/plugin";
import type { NextConfig } from "next";

const withNextIntl = createNextIntlPlugin("./src/i18n/request.ts");
const isDevelopment = process.env.NODE_ENV === "development";

function readStorageOrigins(): string[] {
  const configuredOrigins = process.env.HANDYNEST_WEB_STORAGE_ORIGINS;
  if (!configuredOrigins) {
    return [];
  }

  return [
    ...new Set(
      configuredOrigins.split(",").map((value) => {
        const url = new URL(value.trim());
        if (url.protocol !== "http:" && url.protocol !== "https:") {
          throw new Error(
            `HANDYNEST_WEB_STORAGE_ORIGINS supports only HTTP(S) origins: ${value}`,
          );
        }
        return url.origin;
      }),
    ),
  ];
}

const storageOrigins = readStorageOrigins();
const storageSourceList =
  storageOrigins.length > 0 ? ` ${storageOrigins.join(" ")}` : "";
const contentSecurityPolicy = [
  "default-src 'self'",
  `script-src 'self' 'unsafe-inline'${isDevelopment ? " 'unsafe-eval'" : ""}`,
  "style-src 'self' 'unsafe-inline'",
  `img-src 'self' data: blob:${storageSourceList}`,
  "font-src 'self' data:",
  `connect-src 'self'${storageSourceList}${isDevelopment ? " ws: wss:" : ""}`,
  "object-src 'none'",
  "base-uri 'self'",
  "form-action 'self'",
  "frame-ancestors 'none'",
].join("; ");

const securityHeaders = [
  { key: "Content-Security-Policy", value: contentSecurityPolicy },
  { key: "X-Content-Type-Options", value: "nosniff" },
  { key: "X-Frame-Options", value: "DENY" },
  { key: "Referrer-Policy", value: "strict-origin-when-cross-origin" },
  {
    key: "Permissions-Policy",
    value: "camera=(), microphone=(), geolocation=(self)",
  },
  { key: "Cross-Origin-Opener-Policy", value: "same-origin" },
  ...(isDevelopment
    ? []
    : [
        {
          key: "Strict-Transport-Security",
          value: "max-age=31536000; includeSubDomains",
        },
      ]),
];

const nextConfig: NextConfig = {
  allowedDevOrigins: ["127.0.0.1"],
  poweredByHeader: false,
  reactStrictMode: true,
  async headers() {
    return [{ source: "/:path*", headers: securityHeaders }];
  },
};

export default withNextIntl(nextConfig);
