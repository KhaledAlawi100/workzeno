import { useMutation, useQueryClient } from "@tanstack/react-query";

import { createInvitation } from "../api/invitationApi";
import { collaborationQueryKeys } from "../../collaboration.queryKeys";

import type { CreateInvitationRequest } from "../invitation.types";

export function useCreateInvitation(projectId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateInvitationRequest) =>
      createInvitation(projectId, request),

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: collaborationQueryKeys.projectInvitations(projectId),
      });
    },
  });
}
