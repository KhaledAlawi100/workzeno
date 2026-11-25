import { useQuery } from "@tanstack/react-query";

import { getDashboard } from "../api/dashboardApi";
import { dashboardQueryKeys } from "../dashboard.queryKeys";

export function useDashboard() {
  return useQuery({
    queryKey: dashboardQueryKeys.summary(),

    queryFn: ({ signal }) => getDashboard(signal),
  });
}
