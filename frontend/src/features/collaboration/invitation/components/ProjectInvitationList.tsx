import {
  Alert,
  Box,
  Button,
  Pagination,
  Snackbar,
  Stack,
  Typography,
} from "@mui/material";

import RefreshIcon from "@mui/icons-material/Refresh";

import InvitationDetailsDialog from "./InvitationDetailsDialog";
import RevokeInvitationDialog from "./RevokeInvitationDialog";
import ProjectInvitationCard from "./ProjectInvitationCard";
import InvitationListLoading from "./InvitationListLoading";

import { useProjectInvitationList } from "../hooks/useProjectInvitationList";

interface ProjectInvitationListProps {
  projectId: string;
  canRevokeInvitations: boolean;
}

export default function ProjectInvitationList({
  projectId,
  canRevokeInvitations,
}: ProjectInvitationListProps) {
  const invitationList = useProjectInvitationList(projectId);

  const {
    invitationsPage,
    isPending,
    isError,
    errorMessage,
    isFetching,
    refetch,
    page,
  } = invitationList;

  // Decide which list content should be displayed.
  function renderInvitationContent() {
    if (isPending) {
      return <InvitationListLoading />;
    }

    if (isError) {
      return (
        <Alert
          severity="error"
          action={
            <Button
              color="inherit"
              size="small"
              startIcon={<RefreshIcon />}
              onClick={() => void refetch()}
            >
              Retry
            </Button>
          }
        >
          {errorMessage}
        </Alert>
      );
    }

    if (!invitationsPage || invitationsPage.content.length === 0) {
      return (
        <Alert severity="info">
          No project invitations have been created yet.
        </Alert>
      );
    }

    return (
      <Stack spacing={2}>
        <Stack
          direction="row"
          sx={{
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
            Invitations ({invitationsPage.totalElements})
          </Typography>

          {isFetching && (
            <Typography variant="caption" color="text.secondary">
              Updating...
            </Typography>
          )}
        </Stack>

        {invitationsPage.content.map((invitation) => (
          <ProjectInvitationCard
            key={invitation.invitationId}
            invitation={invitation}
            canRevokeInvitations={canRevokeInvitations}
            onViewDetails={() =>
              invitationList.openInvitationDetails(invitation.invitationId)
            }
            onRevoke={() => invitationList.openRevokeDialog(invitation)}
          />
        ))}

        {invitationsPage.totalPages > 1 && (
          <Box sx={{ display: "flex", justifyContent: "center", pt: 1 }}>
            <Pagination
              count={invitationsPage.totalPages}
              page={page + 1}
              onChange={(_, selectedPage) =>
                invitationList.changePage(selectedPage)
              }
              color="primary"
              shape="rounded"
              showFirstButton
              showLastButton
              disabled={isFetching}
            />
          </Box>
        )}
      </Stack>
    );
  }

  return (
    <>
      {renderInvitationContent()}

      {/* Invitation details are loaded only when the user requests them. */}
      <InvitationDetailsDialog
        open={Boolean(invitationList.selectedInvitationId)}
        invitation={invitationList.invitationDetails}
        isLoading={invitationList.isDetailsPending}
        isError={invitationList.isDetailsError}
        onClose={invitationList.closeInvitationDetails}
      />

      <RevokeInvitationDialog
        open={invitationList.invitationToRevoke !== null}
        inviteeEmail={invitationList.invitationToRevoke?.inviteeEmail ?? ""}
        isPending={invitationList.isRevoking}
        errorMessage={invitationList.revokeErrorMessage}
        onClose={invitationList.closeRevokeDialog}
        onConfirm={invitationList.confirmRevoke}
      />

      <Snackbar
        open={invitationList.successMessage !== null}
        autoHideDuration={5000}
        onClose={invitationList.clearSuccessMessage}
        message={invitationList.successMessage}
        anchorOrigin={{
          vertical: "bottom",
          horizontal: "right",
        }}
      />
    </>
  );
}
