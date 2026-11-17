import { useState } from "react";
import { useQuery } from "@tanstack/react-query";

import { getProjectInvitation } from "../api/invitationApi";
import { collaborationQueryKeys } from "../../collaboration.queryKeys";

import { useProjectInvitations } from "./useProjectInvitations";
import { useRevokeInvitation } from "./useRevokeInvitation";

import { getApiErrorMessage } from "../../../../lib/api";

import type { ProjectInvitationSummaryResponse } from "../invitation.types";

const PAGE_SIZE = 20;

export function useProjectInvitationList(projectId: string) {
  const [page, setPage] = useState(0);

  const [selectedInvitationId, setSelectedInvitationId] = useState<
    string | null
  >(null);

  const [invitationToRevoke, setInvitationToRevoke] =
    useState<ProjectInvitationSummaryResponse | null>(null);

  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Fetch only the current page of invitation summaries.
  const invitationsQuery = useProjectInvitations(projectId, page, PAGE_SIZE);

  // Fetch the full invitation DTO only when details are requested.
  const invitationDetailsQuery = useQuery({
    queryKey: selectedInvitationId
      ? collaborationQueryKeys.projectInvitation(
          projectId,
          selectedInvitationId,
        )
      : ["project-invitation", projectId, null],

    queryFn: ({ signal }) => {
      if (!selectedInvitationId) {
        throw new Error("Invitation ID is required");
      }

      return getProjectInvitation(projectId, selectedInvitationId, signal);
    },

    enabled: Boolean(selectedInvitationId),
  });

  const revokeMutation = useRevokeInvitation(projectId);

  function openInvitationDetails(invitationId: string) {
    setSelectedInvitationId(invitationId);
  }

  function closeInvitationDetails() {
    setSelectedInvitationId(null);
  }

  function openRevokeDialog(invitation: ProjectInvitationSummaryResponse) {
    revokeMutation.reset();
    setInvitationToRevoke(invitation);
  }

  function closeRevokeDialog() {
    if (revokeMutation.isPending) {
      return;
    }

    setInvitationToRevoke(null);
    revokeMutation.reset();
  }

  async function confirmRevoke(): Promise<void> {
    if (!invitationToRevoke) {
      return;
    }

    try {
      await revokeMutation.mutateAsync({
        invitationId: invitationToRevoke.invitationId,
      });

      setSuccessMessage(
        `Invitation to ${invitationToRevoke.inviteeEmail} was revoked.`,
      );

      setInvitationToRevoke(null);

      // If the last item on this page was removed, go back one page.
      const invitationsPage = invitationsQuery.data;

      if (invitationsPage && invitationsPage.content.length === 1 && page > 0) {
        setPage((currentPage) => currentPage - 1);
      }
    } catch {
      // Keep the dialog open so the user can see the error.
    }
  }

  function changePage(selectedPage: number) {
    // MUI Pagination is one-based; the API uses zero-based pages.
    setPage(selectedPage - 1);
  }

  return {
    page,
    invitationsPage: invitationsQuery.data,

    isPending: invitationsQuery.isPending,
    isError: invitationsQuery.isError,
    errorMessage: invitationsQuery.isError
      ? getApiErrorMessage(invitationsQuery.error)
      : null,
    isFetching: invitationsQuery.isFetching,
    refetch: invitationsQuery.refetch,

    selectedInvitationId,
    invitationDetails: invitationDetailsQuery.data,
    isDetailsPending: invitationDetailsQuery.isPending,
    isDetailsError: invitationDetailsQuery.isError,

    invitationToRevoke,
    isRevoking: revokeMutation.isPending,
    revokeErrorMessage: revokeMutation.isError
      ? getApiErrorMessage(revokeMutation.error)
      : null,

    successMessage,
    clearSuccessMessage: () => setSuccessMessage(null),

    openInvitationDetails,
    closeInvitationDetails,
    openRevokeDialog,
    closeRevokeDialog,
    confirmRevoke,
    changePage,
  };
}
