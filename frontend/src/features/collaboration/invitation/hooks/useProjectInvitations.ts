import { useQuery } from "@tanstack/react-query";

import { getProjectInvitations } from "../api/invitationApi";
import { collaborationQueryKeys } from "../../collaboration.queryKeys";

const DEFAULT_PAGE_SIZE = 20;

export function useProjectInvitations(
  projectId: string | undefined,
  page = 0,
  size = DEFAULT_PAGE_SIZE,
  enabled = true,
) {
  return useQuery({
    queryKey: projectId
      ? collaborationQueryKeys.projectInvitationsPage(projectId, page, size)
      : ["project-invitations", undefined, { page, size }],

    queryFn: ({ signal }) => {
      if (!projectId) {
        throw new Error("Project ID is required");
      }

      return getProjectInvitations(projectId, page, size, signal);
    },

    enabled: Boolean(projectId) && enabled,
  });
}
