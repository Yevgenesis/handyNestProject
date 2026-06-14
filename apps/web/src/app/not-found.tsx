import { SearchX } from "lucide-react";
import { getTranslations } from "next-intl/server";
import Link from "next/link";

import { Button } from "@/shared/ui/button";

export default async function NotFound() {
  const t = await getTranslations("states");
  return (
    <section className="container-page page-section grid min-h-[60vh] place-items-center text-center">
      <div>
        <SearchX
          className="mx-auto size-12 text-[var(--primary)]"
          aria-hidden
        />
        <h1 className="mt-5 text-3xl font-black">{t("notFoundTitle")}</h1>
        <p className="mt-3 text-[var(--muted)]">{t("notFoundDescription")}</p>
        <Button asChild className="mt-6">
          <Link href="/">{t("backHome")}</Link>
        </Button>
      </div>
    </section>
  );
}
