import { useQuery } from "@tanstack/react-query";

import { getProjectTeam } from "../api/membershipApi";

const DEFAULT_PAGE_SIZE = 6;

export function useProjectTeam(
  projectId: string | undefined,
  page = 0,
  size = DEFAULT_PAGE_SIZE,
) {
  return useQuery({
    queryKey: ["project-team", projectId, { page, size }],

    queryFn: ({ signal }) => {
      if (!projectId) {
        throw new Error("Project ID is required");
      }

      return getProjectTeam(projectId, page, size, signal);
    },

    enabled: Boolean(projectId),
  });
}
