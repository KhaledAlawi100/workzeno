import { apiClient, refreshAccessToken, type ApiResponse } from "../../lib/api";

import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
} from "./auth.types";

export async function registerUser(
  request: RegisterRequest,
): Promise<RegisterResponse> {
  const response = await apiClient.post<ApiResponse<RegisterResponse>>(
    "/auth/register",
    request,
  );

  return response.data.data;
}

export async function loginUser(request: LoginRequest): Promise<LoginResponse> {
  const response = await apiClient.post<ApiResponse<LoginResponse>>(
    "/auth/login",
    request,
    { withCredentials: true },
  );

  return response.data.data;
}

export type UserProfile = {
  id: string;
  email: string;
  role: string;
  status: string;
  createdAt: string;
};

export async function getCurrentUser(): Promise<UserProfile> {
  const response = await apiClient.get<ApiResponse<UserProfile>>("/users/me");

  return response.data.data;
}

// Reuse the shared refresh request from lib/api.ts.
export { refreshAccessToken };

export async function logoutUser(): Promise<void> {
  await apiClient.post("/auth/logout", {}, { withCredentials: true });
}
