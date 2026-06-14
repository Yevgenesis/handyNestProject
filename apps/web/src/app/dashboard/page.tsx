"use client";

import { useQuery } from "@tanstack/react-query";
import {
  BriefcaseBusiness,
  FileCheck2,
  Handshake,
  ListTodo,
  MessageSquare,
  Plus,
  Search,
  ShieldCheck,
  UserRound,
} from "lucide-react";
import { useTranslations } from "next-intl";
import Link from "next/link";
import { useState } from "react";

import { useAuth } from "@/features/auth/auth-provider";
import { ProtectedRoute } from "@/features/auth/protected-route";
import {
  getMyDeals,
  getMyOffers,
  getMyTasks,
  getPerformerMe,
} from "@/shared/api/marketplace-client";
import { queryKeys } from "@/shared/api/query-keys";
import { Button } from "@/shared/ui/button";

export default function DashboardPage() {
  const { user } = useAuth();
  const t = useTranslations("dashboard");
  const performer = useQuery({
    queryKey: queryKeys.performerMe,
    queryFn: getPerformerMe,
    retry: false,
  });
  const [mode, setMode] = useState<"customer" | "performer">(
    user?.roles?.includes("PERFORMER") ? "performer" : "customer",
  );
  const tasks = useQuery({
    queryKey: queryKeys.myTasks(undefined, 0),
    queryFn: () => getMyTasks({ size: 5 }),
    enabled: mode === "customer",
  });
  const offers = useQuery({
    queryKey: queryKeys.myOffers,
    queryFn: getMyOffers,
    enabled: mode === "performer" && Boolean(performer.data),
  });
  const deals = useQuery({ queryKey: queryKeys.myDeals, queryFn: getMyDeals });
  const activeDeals =
    deals.data?.filter(
      (deal) => deal.status !== "COMPLETED" && deal.status !== "CANCELED",
    ).length ?? 0;
  return (
    <ProtectedRoute>
      <main className="container-page page-section">
        <div className="flex flex-wrap items-end justify-between gap-4">
          <div>
            <p className="font-semibold text-[var(--primary)]">
              {t("eyebrow")}
            </p>
            <h1 className="mt-2 text-3xl font-black">
              {t("greeting", { name: user?.firstName ?? "" })}
            </h1>
            <p className="mt-2 text-[var(--muted)]">{t("description")}</p>
          </div>
          <div
            className="inline-flex rounded-[8px] border border-[var(--border)] bg-white p-1"
            role="group"
            aria-label={t("modeLabel")}
          >
            <Button
              onClick={() => setMode("customer")}
              size="sm"
              variant={mode === "customer" ? "primary" : "ghost"}
            >
              <UserRound className="size-4" />
              {t("customerMode")}
            </Button>
            <Button
              onClick={() => setMode("performer")}
              size="sm"
              variant={mode === "performer" ? "primary" : "ghost"}
            >
              <BriefcaseBusiness className="size-4" />
              {t("performerMode")}
            </Button>
          </div>
        </div>
        <section className="mt-8 grid gap-4 sm:grid-cols-3">
          <div className="surface-panel p-5">
            <p className="text-sm text-[var(--muted)]">
              {mode === "customer" ? t("myTasksCount") : t("myOffersCount")}
            </p>
            <p className="mt-2 text-3xl font-black">
              {mode === "customer"
                ? (tasks.data?.totalElements ?? 0)
                : (offers.data?.length ?? 0)}
            </p>
          </div>
          <div className="surface-panel p-5">
            <p className="text-sm text-[var(--muted)]">{t("activeDeals")}</p>
            <p className="mt-2 text-3xl font-black">{activeDeals}</p>
          </div>
          <div className="surface-panel p-5">
            <p className="text-sm text-[var(--muted)]">{t("accountStatus")}</p>
            <p className="mt-2 font-bold text-[var(--primary)]">
              {mode === "performer" && performer.data
                ? t("performerReady")
                : t("customerReady")}
            </p>
          </div>
        </section>
        {mode === "customer" ? (
          <section className="mt-8">
            <h2 className="text-xl font-bold">{t("customerActions")}</h2>
            <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <DashboardLink
                href="/tasks/new"
                icon={<Plus />}
                title={t("createTask")}
                description={t("createTaskDescription")}
              />
              <DashboardLink
                href="/my/tasks"
                icon={<ListTodo />}
                title={t("myTasks")}
                description={t("myTasksDescription")}
              />
              <DashboardLink
                href="/performers"
                icon={<Search />}
                title={t("findPerformer")}
                description={t("findPerformerDescription")}
              />
              <DashboardLink
                href="/my/deals"
                icon={<Handshake />}
                title={t("deals")}
                description={t("dealsDescription")}
              />
              <DashboardLink
                href="/chats"
                icon={<MessageSquare />}
                title={t("chats")}
                description={t("chatsDescription")}
              />
            </div>
          </section>
        ) : (
          <section className="mt-8">
            <h2 className="text-xl font-bold">{t("performerActions")}</h2>
            {performer.data ? (
              <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                <DashboardLink
                  href="/tasks"
                  icon={<Search />}
                  title={t("findTask")}
                  description={t("findTaskDescription")}
                />
                <DashboardLink
                  href="/my/offers"
                  icon={<ListTodo />}
                  title={t("myOffers")}
                  description={t("myOffersDescription")}
                />
                <DashboardLink
                  href="/performer/profile"
                  icon={<BriefcaseBusiness />}
                  title={t("performerProfile")}
                  description={t("performerProfileDescription")}
                />
                <DashboardLink
                  href="/performer/verification"
                  icon={<FileCheck2 />}
                  title={t("verification")}
                  description={t("verificationDescription")}
                />
                <DashboardLink
                  href="/chats"
                  icon={<MessageSquare />}
                  title={t("chats")}
                  description={t("chatsDescription")}
                />
              </div>
            ) : (
              <div className="surface-panel mt-4 p-6">
                <h3 className="text-xl font-bold">{t("becomePerformer")}</h3>
                <p className="mt-2 text-[var(--muted)]">
                  {t("becomePerformerDescription")}
                </p>
                <Button asChild className="mt-5">
                  <Link href="/performer/profile">
                    {t("createPerformerProfile")}
                  </Link>
                </Button>
              </div>
            )}
          </section>
        )}
        <section className="mt-8 border-t border-[var(--border)] pt-6">
          <Button asChild variant="ghost">
            <Link href="/account/security">
              <ShieldCheck className="size-4" />
              {t("security")}
            </Link>
          </Button>
        </section>
      </main>
    </ProtectedRoute>
  );
}

function DashboardLink({
  href,
  icon,
  title,
  description,
}: {
  href: string;
  icon: React.ReactNode;
  title: string;
  description: string;
}) {
  return (
    <Link
      className="surface-panel p-5 transition-transform hover:-translate-y-0.5"
      href={href}
    >
      <span className="text-[var(--primary)] [&_svg]:size-6">{icon}</span>
      <h3 className="mt-4 font-bold">{title}</h3>
      <p className="mt-1 text-sm text-[var(--muted)]">{description}</p>
    </Link>
  );
}
