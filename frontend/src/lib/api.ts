import axios, { type InternalAxiosRequestConfig } from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

if (!API_BASE_URL) {
  throw new Error(
    "VITE_API_BASE_URL is not defined in the environment variables.",
  );
}

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10_000,
});

// Separate client so refreshing a token does not trigger
// the automatic-refresh interceptor again.
const refreshClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 10_000,
  withCredentials: true,
});

// AuthContext registers this callback to clear authentication state
// if refreshing the session fails.
let onRefreshFailure: (() => void) | null = null;

export function setOnRefreshFailure(callback: (() => void) | null): void {
  onRefreshFailure = callback;
}

interface RefreshResponse {
  success: boolean;
  message: string;
  data: {
    accessToken: string;
    email: string;
    role: string;
    tokenType: string;
    expiresIn: number;
  };
  timestamp: string;
  path: string;
  requestId: string;
}

// Share one refresh request if multiple API calls receive 401 together.
let refreshPromise: Promise<string> | null = null;

export async function refreshAccessToken(): Promise<string> {
  
  if (!refreshPromise) {
    refreshPromise =
     refreshClient
      .post<RefreshResponse>("/auth/refresh", {})
      .then((response) => response.data.data.accessToken)
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
}

// Axios request config type with an optional _retry property to track if a request has already been retried.
type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean;
};

// Interceptor to handle automatic token refresh on 401 Unauthorized responses.
apiClient.interceptors.response.use(
  // Return the response directly if successful.
  (response) => response
  ,
  // Handle 401 Unauthorized responses by attempting to refresh the access token.
  async (error: unknown) => {

    // If the error is not an Axios error or does not have a config, reject it.
    if (!axios.isAxiosError(error) || !error.config) {
      return Promise.reject(error);
    }


    
    const originalRequest = error.config as RetryableRequestConfig;
    const requestUrl = originalRequest.url ?? "";

    const isAuthEndpoint =
      requestUrl.includes("/auth/login") ||
      requestUrl.includes("/auth/register") ||
      requestUrl.includes("/auth/refresh") ||
      requestUrl.includes("/auth/logout");

    // If the request is for an authentication endpoint, do not attempt to refresh the token.
    if (
      error.response?.status !== 401 ||
      originalRequest._retry ||
      isAuthEndpoint
    ) {
      return Promise.reject(error);
    }

    
    originalRequest._retry = true;

    try {
      const newToken = await refreshAccessToken();

      originalRequest.headers.set("Authorization", `Bearer ${newToken}`);

      apiClient.defaults.headers.common.Authorization = `Bearer ${newToken}`;

      return apiClient(originalRequest);
    } catch (refreshError) {
      delete apiClient.defaults.headers.common.Authorization;

      onRefreshFailure?.();

      return Promise.reject(refreshError);
    }
  },
);

// Preserve the existing health-check API.
export async function apiGet<T>(path: string): Promise<T> {
  const response = await apiClient.get<T>(path);
  return response.data;
}


export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
  path: string;
  requestId: string;
}

export function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as
      | { detail?: string; message?: string }
      | undefined;

    if (data?.detail) return data.detail;
    if (data?.message) return data.message;

    if (!error.response) {
      return "Unable to connect to the server. Please try again.";
    }
  }

  return "Something went wrong. Please try again.";
}
