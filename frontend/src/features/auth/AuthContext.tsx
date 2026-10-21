import { createContext } from "react";
import { useContext } from "react";
import { useEffect } from "react";
import { useState } from "react";
import type { ReactNode } from "react";

import { apiClient, setOnRefreshFailure } from "../../lib/api";

import {
  getCurrentUser,
  loginUser,
  logoutUser,
  refreshAccessToken,
} from "./authApi";

import type { UserProfile } from "./authApi";

type AuthStatus = "loading" | "authenticated" | "unauthenticated";

type AuthContextValue = {
  user: UserProfile | null;
  accessToken: string | null;
  isAuthenticated: boolean;
  status: AuthStatus;
  login: (credentials: Parameters<typeof loginUser>[0]) => Promise<void>;
  logout: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserProfile | null>(null);
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [status, setStatus] = useState<AuthStatus>("loading");

  function setAuthorizationHeader(token: string): void {
    apiClient.defaults.headers.common.Authorization = `Bearer ${token}`;
  }

  function clearAuthorizationHeader(): void {
    delete apiClient.defaults.headers.common.Authorization;
  }

  // Clear React authentication state if automatic token refresh fails.
  useEffect(() => {
    setOnRefreshFailure(() => {
      clearAuthorizationHeader();
      setAccessToken(null);
      setUser(null);
      setStatus("unauthenticated");
    });

    return () => {
      setOnRefreshFailure(null);
    };
  }, []);

  // Restore the user's session when the app opens.
  useEffect(() => {
    async function restoreSession(): Promise<void> {
      try {
        const token = await refreshAccessToken();

        setAuthorizationHeader(token);

        const profile = await getCurrentUser();

        setAccessToken(token);
        setUser(profile);
        setStatus("authenticated");
      } catch {
        clearAuthorizationHeader();
        setAccessToken(null);
        setUser(null);
        setStatus("unauthenticated");
      }
    }

    void restoreSession();
  }, []);

  // Log in and load the user's profile.
  async function login(
    credentials: Parameters<typeof loginUser>[0],
  ): Promise<void> {
    try {
      const result = await loginUser(credentials);

      setAuthorizationHeader(result.accessToken);

      const profile = await getCurrentUser();

      setAccessToken(result.accessToken);
      setUser(profile);
      setStatus("authenticated");
    } catch (error) {
      clearAuthorizationHeader();
      setAccessToken(null);
      setUser(null);
      setStatus("unauthenticated");

      throw error;
    }
  }

  // Log out on the backend, then always clear the frontend session.
  async function logout(): Promise<void> {
    try {
      await logoutUser();
    } catch {
      // Still clear the local session if the backend request fails.
    } finally {
      clearAuthorizationHeader();
      setAccessToken(null);
      setUser(null);
      setStatus("unauthenticated");
    }
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        accessToken,
        status,
        isAuthenticated: Boolean(accessToken && user),
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);

  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }

  return context;
}
