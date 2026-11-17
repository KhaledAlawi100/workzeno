import { useState } from "react";
import { Link } from "react-router-dom";

import {
  Alert,
  Box,
  Button,
  Container,
  Snackbar,
  Stack,
  Typography,
} from "@mui/material";

import MailOutlinedIcon from "@mui/icons-material/MailOutlined";

import { getApiErrorMessage } from "../../../../lib/api";

import { useMyInvitations } from "../hooks/useMyInvitations";
import { useAcceptInvitation } from "../hooks/useAcceptInvitation";
import { useDeclineInvitation } from "../hooks/useDeclineInvitation";

import MyInvitationsContent from "../components/MyInvitationsContent";

const PAGE_SIZE = 20;

interface AcceptedProject {
  id: string;
  name: string;
}

export default function MyInvitationsPage() {
  const [page, setPage] = useState(0);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [acceptedProject, setAcceptedProject] =
    useState<AcceptedProject | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const invitationsQuery = useMyInvitations(page, PAGE_SIZE);
  const acceptMutation = useAcceptInvitation();
  const declineMutation = useDeclineInvitation();

  const isActionPending = acceptMutation.isPending || declineMutation.isPending;

  async function handleAccept(invitationId: string) {
    setErrorMessage(null);

    try {
      const accepted = await acceptMutation.mutateAsync({ invitationId });

      setAcceptedProject({
        id: accepted.projectId,
        name: accepted.projectName,
      });

      setSuccessMessage(`You joined ${accepted.projectName} successfully.`);

      moveBackIfPageBecomesEmpty();
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    }
  }

  async function handleDecline(invitationId: string) {
    setErrorMessage(null);

    try {
      await declineMutation.mutateAsync({ invitationId });

      setAcceptedProject(null);
      setSuccessMessage("Invitation declined.");

      moveBackIfPageBecomesEmpty();
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    }
  }

  function moveBackIfPageBecomesEmpty() {
    // Avoid leaving the user on an empty page after the last item is removed.
    if (invitationsQuery.data?.content.length === 1 && page > 0) {
      setPage((currentPage) => currentPage - 1);
    }
  }

  function closeNotification() {
    setSuccessMessage(null);
    setAcceptedProject(null);
  }

  return (
    <Container maxWidth="md" sx={{ py: 5 }}>
      <Stack spacing={3}>
        <Stack direction="row" spacing={1.5} sx={{ alignItems: "center" }}>
          <MailOutlinedIcon color="primary" fontSize="large" />

          <Box>
            <Typography variant="h4" component="h1">
              My invitations
            </Typography>

            <Typography color="text.secondary">
              Review invitations to join Workzeno projects.
            </Typography>
          </Box>
        </Stack>

        {errorMessage && (
          <Alert severity="error" onClose={() => setErrorMessage(null)}>
            {errorMessage}
          </Alert>
        )}

        <MyInvitationsContent
          data={invitationsQuery.data}
          isPending={invitationsQuery.isPending}
          isError={invitationsQuery.isError}
          error={invitationsQuery.error}
          isFetching={invitationsQuery.isFetching}
          page={page}
          isActionPending={isActionPending}
          onRetry={() => void invitationsQuery.refetch()}
          onPageChange={setPage}
          onAccept={(invitationId) => void handleAccept(invitationId)}
          onDecline={(invitationId) => void handleDecline(invitationId)}
        />
      </Stack>

      <Snackbar
        open={successMessage !== null}
        autoHideDuration={6000}
        onClose={closeNotification}
        message={successMessage}
        action={
          acceptedProject ? (
            <Button
              component={Link}
              to={`/projects/${encodeURIComponent(acceptedProject.id)}`}
              color="inherit"
              size="small"
              onClick={closeNotification}
            >
              View project
            </Button>
          ) : undefined
        }
        anchorOrigin={{
          vertical: "bottom",
          horizontal: "right",
        }}
      />
    </Container>
  );
}
