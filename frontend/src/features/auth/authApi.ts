import { apiClient, type ApiResponse } from "../../lib/api";
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
  );

  return response.data.data;
}
