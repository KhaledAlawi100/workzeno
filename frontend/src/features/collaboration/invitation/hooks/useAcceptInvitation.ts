import { useMutation, useQueryClient } from "@tanstack/react-query";

import { acceptInvitation } from "../api/invitationApi";
import { collaborationQueryKeys } from "../../collaboration.queryKeys";

interface AcceptInvitationVariables {
  invitationId: string;
}

export function useAcceptInvitation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ invitationId }: AcceptInvitationVariables) =>
      acceptInvitation(invitationId),

    onSuccess: async (acceptedInvitation) => {
      const { projectId } = acceptedInvitation;

      // Remove the accepted invitation from every cached inbox page.
      await queryClient.invalidateQueries({
        queryKey: collaborationQueryKeys.myInvitations(),
      });

      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ["projects"],
        }),

        queryClient.invalidateQueries({
          queryKey: ["project", projectId],
        }),

        // Refresh team and member information.
        queryClient.invalidateQueries({
          queryKey: collaborationQueryKeys.projectTeam(projectId),
        }),

        queryClient.invalidateQueries({
          queryKey: collaborationQueryKeys.projectMembers(projectId),
        }),

        // The accepted user now has a project membership and role.
        queryClient.invalidateQueries({
          queryKey: ["my-project-membership", projectId],
        }),
      ]);
    },
  });
}
