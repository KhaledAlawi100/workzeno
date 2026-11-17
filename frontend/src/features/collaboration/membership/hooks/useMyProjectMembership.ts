import { useQuery } from "@tanstack/react-query";

import { getMyProjectMembership } from "../api/membershipApi";

export function useMyProjectMembership(
  projectId: string | undefined,
  enabled = true,
) {
  return useQuery({
    queryKey: ["my-project-membership", projectId],

    queryFn: ({ signal }) => {
      if (!projectId) {
        throw new Error("Project ID is required");
      }

      return getMyProjectMembership(projectId, signal);
    },

    enabled: Boolean(projectId) && enabled,
  });
}
