import { apiClient, type ApiResponse } from "../../../lib/api";

import type { DashboardData } from "../dashboard.types";

export async function getDashboard(
  signal?: AbortSignal,
): Promise<DashboardData> {
  const response = await apiClient.get<ApiResponse<DashboardData>>(
    "/dashboard",
    { signal },
  );

  return response.data.data;
}
