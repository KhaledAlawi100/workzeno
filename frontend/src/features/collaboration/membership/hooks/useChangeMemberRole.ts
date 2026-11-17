import { useMutation, useQueryClient } from "@tanstack/react-query";

import { updateMemberRole } from "../api/membershipApi";
import type { ProjectMembershipRole } from "../membership.types";

/**
 * Changes a project member's role.
 *
 * After a successful update, refresh both team-related queries so
 * every screen using this information receives the latest roles.
 */
export function useChangeMemberRole(projectId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      userId,
      role,
    }: {
      userId: string;
      role: ProjectMembershipRole;
    }) => updateMemberRole(projectId, userId, { role }),

    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ["project-team", projectId],
        }),
        queryClient.invalidateQueries({
          queryKey: ["project-members", projectId],
        }),
      ]);
    },
  });
}
