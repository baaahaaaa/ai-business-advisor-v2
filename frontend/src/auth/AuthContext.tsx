import { useEffect, useState } from "react";
import type { ReactNode } from "react";

import type { AuthUser } from "../api/authApi";
import { loginUser } from "../api/authApi";
import { AuthContext } from "./useAuth";

interface AuthSession {
  accessToken: string;
  user: AuthUser;
  expiresAt: number;
}

export function AuthProvider({
  children,
}: {
  children: ReactNode;
}) {
  const [session, setSession] =
    useState<AuthSession | null>(null);

  async function signIn(
    email: string,
    password: string
  ): Promise<void> {
    const response = await loginUser(email, password);

    setSession({
      accessToken: response.accessToken,
      user: response.user,
      expiresAt:
        Date.now() + response.expiresInSeconds * 1000,
    });
  }

  function signOut(): void {
    setSession(null);
  }

  // Expiration automatique hors du rendu React
  useEffect(() => {
    if (!session) return;

    const remaining = Math.max(
      0,
      session.expiresAt - Date.now()
    );

    const timer = window.setTimeout(() => {
      setSession((current) =>
        current === session ? null : current
      );
    }, remaining);

    return () => window.clearTimeout(timer);
  }, [session]);

  return (
    <AuthContext.Provider
      value={{
        user: session?.user ?? null,
        accessToken: session?.accessToken ?? null,
        isAuthenticated: session !== null,
        signIn,
        signOut,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
