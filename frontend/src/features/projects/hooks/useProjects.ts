import { useQuery } from "@tanstack/react-query";

import { getProjects } from "../api/projectApi";

interface UseProjectsOptions {
  page?: number;
  size?: number;
  search?: string;
}

export function useProjects({
  page = 0,
  size = 20,
  search = "",
}: UseProjectsOptions = {}) {
  const normalizedSearch = search.trim();

  return useQuery({
    queryKey: ["projects", { page, size, search: normalizedSearch }],

    queryFn: ({ signal }) => getProjects(page, size, normalizedSearch, signal),
  });
}
