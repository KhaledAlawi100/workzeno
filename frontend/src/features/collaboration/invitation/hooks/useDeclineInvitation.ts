import { useMutation, useQueryClient } from "@tanstack/react-query";

import { declineInvitation } from "../api/invitationApi";
import { collaborationQueryKeys } from "../../collaboration.queryKeys";

interface DeclineInvitationVariables {
  invitationId: string;
}

export function useDeclineInvitation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ invitationId }: DeclineInvitationVariables) =>
      declineInvitation(invitationId),

    onSuccess: async () => {
      // The declined invitation should disappear from the pending inbox.
      await queryClient.invalidateQueries({
        queryKey: collaborationQueryKeys.myInvitations(),
      });
    },
  });
}
