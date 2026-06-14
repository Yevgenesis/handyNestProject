"use client";

import { createContext, useContext, useEffect, useMemo, useState } from "react";

import {
  clearSession,
  loginSession,
  logoutSession,
  registerSession,
  restoreSession,
} from "@/shared/api/session-client";
import type {
  AuthLoginRequest,
  AuthRegisterRequest,
  AuthUserResponse,
} from "@/shared/api/types";

type AuthStatus = "restoring" | "authenticated" | "anonymous";

type AuthContextValue = {
  status: AuthStatus;
  user: AuthUserResponse | null;
  login: (request: AuthLoginRequest) => Promise<void>;
  register: (request: AuthRegisterRequest) => Promise<void>;
  refresh: () => Promise<AuthUserResponse | null>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [status, setStatus] = useState<AuthStatus>("restoring");
  const [user, setUser] = useState<AuthUserResponse | null>(null);

  useEffect(() => {
    let active = true;
    restoreSession().then((restoredUser) => {
      if (!active) return;
      setUser(restoredUser);
      setStatus(restoredUser ? "authenticated" : "anonymous");
    });
    return () => {
      active = false;
    };
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      status,
      user,
      async login(request) {
        if (status === "restoring") await restoreSession();
        const response = await loginSession(request);
        setUser(response.user ?? null);
        setStatus("authenticated");
      },
      async register(request) {
        if (status === "restoring") await restoreSession();
        const response = await registerSession(request);
        setUser(response.user ?? null);
        setStatus("authenticated");
      },
      async refresh() {
        const restoredUser = await restoreSession();
        setUser(restoredUser);
        setStatus(restoredUser ? "authenticated" : "anonymous");
        return restoredUser;
      },
      async logout() {
        try {
          await logoutSession();
        } finally {
          clearSession();
          setUser(null);
          setStatus("anonymous");
        }
      },
    }),
    [status, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used inside AuthProvider");
  return context;
}
