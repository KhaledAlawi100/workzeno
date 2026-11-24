
import { useQuery } from "@tanstack/react-query";

import { collaborationQueryKeys } from "../../collaboration.queryKeys";

import { searchProjectMembers } from "../api/membershipApi";

export function useProjectMemberSearch(
  projectId: string,
  query: string,
  enabled = true,
) {
  const normalizedQuery = query.trim();

  return useQuery({
    queryKey: collaborationQueryKeys.projectMemberSearch(
      projectId,
      normalizedQuery,
      10,
    ),

    queryFn: ({ signal }) =>
      searchProjectMembers(projectId, normalizedQuery, 10, signal),

    enabled: Boolean(projectId) && normalizedQuery.length >= 2 && enabled,

    // Avoid showing old suggestions for too long.
    staleTime: 30_000,
  });
}
