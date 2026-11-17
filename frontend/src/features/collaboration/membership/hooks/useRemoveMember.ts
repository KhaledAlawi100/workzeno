import { useMutation, useQueryClient } from "@tanstack/react-query";

import { removeProjectMember } from "../api/membershipApi";

/**
 * Removes a member from a project.
 *
 * The team and members queries are invalidated after a successful
 * deletion so the removed member disappears from the UI.
 */
export function useRemoveMember(projectId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (userId: string) => removeProjectMember(projectId, userId),

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
