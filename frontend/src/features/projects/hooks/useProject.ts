import { useQuery } from "@tanstack/react-query";

import { getProjectById } from "../api/projectApi";

export function useProject(projectId: string | undefined) {
  return useQuery({
    queryKey: ["project", projectId],

    queryFn: ({ signal }) => {
      if (!projectId) {
        throw new Error("Project ID is required");
      }

      return getProjectById(projectId, signal);
    },

    enabled: Boolean(projectId),
  });
}
