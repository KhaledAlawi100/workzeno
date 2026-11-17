import { useMutation, useQueryClient } from "@tanstack/react-query";

import { revokeInvitation } from "../api/invitationApi";
import { collaborationQueryKeys } from "../../collaboration.queryKeys";

interface RevokeInvitationVariables {
  invitationId: string;
}

export function useRevokeInvitation(projectId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ invitationId }: RevokeInvitationVariables) =>
      revokeInvitation(projectId, invitationId),

    onSuccess: async (_, variables) => {
      // Refresh all cached pages of this project's invitation list.
      await queryClient.invalidateQueries({
        queryKey: collaborationQueryKeys.projectInvitations(projectId),
      });

      // Refresh the details of the invitation that was revoked.
      await queryClient.invalidateQueries({
        queryKey: collaborationQueryKeys.projectInvitation(
          projectId,
          variables.invitationId,
        ),
      });
    },
  });
}
