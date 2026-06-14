import type { Metadata } from "next";
import { NextIntlClientProvider } from "next-intl";
import { getLocale, getMessages, getTranslations } from "next-intl/server";

import { AppProviders } from "@/app/providers";
import { SiteFooter } from "@/widgets/site-footer/site-footer";
import { SiteHeader } from "@/widgets/site-header/site-header";

import "./globals.css";

export async function generateMetadata(): Promise<Metadata> {
  const t = await getTranslations("metadata");
  return {
    title: { default: t("title"), template: "%s | HandyNest" },
    description: t("description"),
  };
}

export default async function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  const [locale, messages] = await Promise.all([getLocale(), getMessages()]);

  return (
    <html data-scroll-behavior="smooth" lang={locale}>
      <body>
        <NextIntlClientProvider messages={messages}>
          <AppProviders>
            <SiteHeader />
            <main className="min-h-[calc(100vh-160px)]">{children}</main>
            <SiteFooter />
          </AppProviders>
        </NextIntlClientProvider>
      </body>
    </html>
  );
}
