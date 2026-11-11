import { useQuery } from "@tanstack/react-query";

import { getProjects } from "../api/projectApi";

interface UseProjectsOptions {
  page?: number;
  size?: number;
}

export function useProjects({ page = 0, size = 20 }: UseProjectsOptions = {}) {
  return useQuery({
    queryKey: ["projects", { page, size }],
    queryFn: ({ signal }) => getProjects(page, size, signal),
  });
}
