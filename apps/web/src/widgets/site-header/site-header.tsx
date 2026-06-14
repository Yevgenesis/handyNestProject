"use client";

import {
  Bell,
  LayoutDashboard,
  LogIn,
  LogOut,
  Menu,
  MessageSquare,
  ShieldCheck,
  UserPlus,
  Wrench,
} from "lucide-react";
import { useTranslations } from "next-intl";
import Link from "next/link";
import { useState } from "react";

import { useAuth } from "@/features/auth/auth-provider";
import { LocaleSwitcher } from "@/features/i18n/locale-switcher";
import { NotificationBell } from "@/features/notifications/notification-bell";
import { Button } from "@/shared/ui/button";

const publicLinks = [
  { href: "/tasks", key: "tasks" },
  { href: "/performers", key: "performers" },
  { href: "/categories", key: "categories" },
] as const;

export function SiteHeader() {
  const t = useTranslations("nav");
  const { status, user, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const dashboardLabel = t(workspaceLabelKey(user?.roles));

  return (
    <header className="sticky top-0 z-40 border-b border-[var(--border)] bg-white/95 backdrop-blur">
      <div className="container-page flex h-16 items-center justify-between gap-4">
        <Link
          className="flex items-center gap-2 font-black text-[var(--primary-strong)]"
          href="/"
        >
          <span className="grid size-9 place-items-center rounded-[6px] bg-[var(--primary)] text-white">
            <Wrench className="size-5" aria-hidden />
          </span>
          <span className="text-xl">HandyNest</span>
        </Link>

        <nav
          className="hidden items-center gap-1 md:flex"
          aria-label={t("mainNavigation")}
        >
          {publicLinks.map((link) => (
            <Button asChild key={link.href} variant="ghost">
              <Link href={link.href}>{t(link.key)}</Link>
            </Button>
          ))}
        </nav>

        <div className="hidden items-center gap-2 md:flex">
          <LocaleSwitcher />
          {status === "authenticated" ? (
            <>
              <NotificationBell />
              <Button asChild variant="ghost">
                <Link href="/chats">
                  <MessageSquare className="size-4" aria-hidden /> {t("chats")}
                </Link>
              </Button>
              <Button asChild variant="secondary">
                <Link href="/dashboard">
                  <LayoutDashboard className="size-4" aria-hidden />{" "}
                  {dashboardLabel}
                </Link>
              </Button>
              <Button
                aria-label={t("logout")}
                onClick={() => void logout()}
                size="icon"
                variant="ghost"
              >
                <LogOut className="size-5" aria-hidden />
              </Button>
            </>
          ) : (
            <>
              <Button asChild variant="ghost">
                <Link href="/login">
                  <LogIn className="size-4" aria-hidden />
                  {t("login")}
                </Link>
              </Button>
              <Button asChild>
                <Link href="/register">
                  <UserPlus className="size-4" aria-hidden />
                  {t("register")}
                </Link>
              </Button>
            </>
          )}
        </div>

        <Button
          aria-expanded={open}
          aria-label={t("openMenu")}
          className="md:hidden"
          onClick={() => setOpen((value) => !value)}
          size="icon"
          variant="ghost"
        >
          <Menu className="size-5" aria-hidden />
        </Button>
      </div>

      {open ? (
        <nav
          className="container-page grid gap-1 border-t border-[var(--border)] py-3 md:hidden"
          aria-label={t("mobileNavigation")}
        >
          <div className="px-3 py-2">
            <LocaleSwitcher />
          </div>
          {publicLinks.map((link) => (
            <Link
              className="px-3 py-2 font-medium"
              href={link.href}
              key={link.href}
              onClick={() => setOpen(false)}
            >
              {t(link.key)}
            </Link>
          ))}
          {status === "authenticated" ? (
            <>
              <Link
                className="flex items-center gap-2 px-3 py-2 font-medium"
                href="/notifications"
                onClick={() => setOpen(false)}
              >
                <Bell className="size-4" />
                {t("notifications")}
              </Link>
              <Link
                className="flex items-center gap-2 px-3 py-2 font-medium"
                href="/dashboard"
                onClick={() => setOpen(false)}
              >
                <LayoutDashboard className="size-4" />
                {dashboardLabel}
              </Link>
              <Link
                className="flex items-center gap-2 px-3 py-2 font-medium"
                href="/chats"
                onClick={() => setOpen(false)}
              >
                <MessageSquare className="size-4" />
                {t("chats")}
              </Link>
              <Link
                className="flex items-center gap-2 px-3 py-2 font-medium"
                href="/account/security"
                onClick={() => setOpen(false)}
              >
                <ShieldCheck className="size-4" />
                {t("security")}
              </Link>
              <button
                className="flex items-center gap-2 px-3 py-2 text-left font-medium"
                onClick={() => void logout()}
              >
                <LogOut className="size-4" />
                {t("logout")}
              </button>
            </>
          ) : (
            <>
              <Link className="px-3 py-2 font-medium" href="/login">
                {t("login")}
              </Link>
              <Link
                className="px-3 py-2 font-medium text-[var(--primary)]"
                href="/register"
              >
                {t("register")}
              </Link>
            </>
          )}
        </nav>
      ) : null}
    </header>
  );
}

export function workspaceLabelKey(roles: string[] | undefined) {
  if (roles?.some((role) => role.includes("ADMIN"))) return "adminDashboard";
  if (roles?.some((role) => role.includes("PERFORMER")))
    return "performerDashboard";
  if (roles?.length) return "customerDashboard";
  return "dashboard";
}
