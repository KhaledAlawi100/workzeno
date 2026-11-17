import { useQuery } from "@tanstack/react-query";

import { getMyInvitations } from "../api/invitationApi";
import { collaborationQueryKeys } from "../../collaboration.queryKeys";

const DEFAULT_PAGE_SIZE = 20;

export function useMyInvitations(page = 0, size = DEFAULT_PAGE_SIZE) {
  return useQuery({
    queryKey: collaborationQueryKeys.myInvitationsPage(page, size),

    queryFn: ({ signal }) => getMyInvitations(page, size, signal),
  });
}
