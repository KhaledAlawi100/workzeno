import { useState } from "react";
import { Link, useParams } from "react-router-dom";

import {
  Alert,
  Button,
  Container,
  Snackbar,
  Skeleton,
  Stack,
  Typography,
} from "@mui/material";

import MailOutlineIcon from "@mui/icons-material/MailOutlined";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import GroupsOutlinedIcon from "@mui/icons-material/GroupsOutlined";
import RefreshIcon from "@mui/icons-material/Refresh";
import PersonAddAltOutlinedIcon from "@mui/icons-material/PersonAddAltOutlined";

import { useAuth } from "../../../auth/AuthContext";
import { useProject } from "../../../projects/hooks/useProject";

import { useMyProjectMembership } from "../hooks/useMyProjectMembership";
import CreateInvitationDialog from "../../invitation/components/CreateInvitationDialog";

import ProjectTeam from "../components/ProjectTeam";

import type { CreateInvitationResponse } from "../../invitation/invitation.types";

function ProjectMembersPageLoading() {
  return (
    <Stack spacing={3}>
      <Skeleton variant="text" width={140} height={25} />
      <Skeleton variant="text" width="45%" height={45} />
      <Skeleton variant="rounded" height={110} />
      <Skeleton variant="rounded" height={200} />
    </Stack>
  );
}

export default function ProjectMembersPage() {
  const { projectId } = useParams<{ projectId: string }>();

  const { user } = useAuth();

  const { data: project, isPending, isError, refetch } = useProject(projectId);

  const [invitationDialogOpen, setInvitationDialogOpen] = useState(false);

  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // The project owner is not represented by a membership record.
  const isOwner = Boolean(project && user && project.ownerId === user.id);

  // Only non-owners need to retrieve their membership role.
  const {
    data: myMembership,
    isPending: isMembershipPending,
    isError: isMembershipError,
  } = useMyProjectMembership(projectId, Boolean(project && user && !isOwner));

  const isAdmin = myMembership?.role === "ADMIN";

  // Owners can invite both roles; admins can invite members only.
  const canCreateInvitation =
    isOwner || (!isMembershipPending && !isMembershipError && isAdmin);

  function handleInvitationCreated(invitation: CreateInvitationResponse) {
    setInvitationDialogOpen(false);

    setSuccessMessage(`Invitation sent to ${invitation.inviteeEmail}.`);
  }

  if (!projectId) {
    return (
      <Container maxWidth="md" sx={{ py: 5 }}>
        <Alert severity="error">Invalid project URL.</Alert>

        <Button
          component={Link}
          to="/projects"
          startIcon={<ArrowBackIcon />}
          sx={{ mt: 2 }}
        >
          Back to projects
        </Button>
      </Container>
    );
  }

  if (isPending) {
    return (
      <Container maxWidth="lg" sx={{ py: 5 }}>
        <ProjectMembersPageLoading />
      </Container>
    );
  }

  if (isError || !project) {
    return (
      <Container maxWidth="md" sx={{ py: 5 }}>
        <Stack spacing={2}>
          <Button
            component={Link}
            to={`/projects/${encodeURIComponent(projectId)}`}
            startIcon={<ArrowBackIcon />}
            sx={{ alignSelf: "flex-start" }}
          >
            Back to project
          </Button>

          <Alert
            severity="error"
            action={
              <Button
                color="inherit"
                size="small"
                startIcon={<RefreshIcon />}
                onClick={() => {
                  void refetch();
                }}
              >
                Retry
              </Button>
            }
          >
            Unable to load this project. Please try again.
          </Alert>
        </Stack>
      </Container>
    );
  }

  if (!user) {
    return (
      <Container maxWidth="md" sx={{ py: 5 }}>
        <Alert severity="warning">
          Your user information is unavailable. Please sign in again.
        </Alert>
      </Container>
    );
  }

  return (
    <>
      <Container maxWidth="lg" sx={{ py: 5 }}>
        <Stack spacing={3}>
          {/* Back to the selected project's details. */}
          <Button
            component={Link}
            to={`/projects/${encodeURIComponent(project.id)}`}
            startIcon={<ArrowBackIcon />}
            sx={{ alignSelf: "flex-start" }}
          >
            Back to project
          </Button>

          {/* Page heading and invitation actions. */}
          <Stack
            direction={{ xs: "column", sm: "row" }}
            spacing={2}
            sx={{
              justifyContent: "space-between",
              alignItems: { xs: "flex-start", sm: "center" },
            }}
          >
            <Stack spacing={1}>
              <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
                <GroupsOutlinedIcon color="primary" />

                <Typography
                  variant="h4"
                  component="h1"
                  sx={{
                    fontWeight: 700,
                    overflowWrap: "anywhere",
                  }}
                >
                  Project team
                </Typography>
              </Stack>

              <Typography color="text.secondary">
                Manage and view the people collaborating on{" "}
                <strong>{project.name}</strong>.
              </Typography>
            </Stack>

            {/* Only owners and admins can see invitation actions. */}
            {canCreateInvitation && (
              <Stack
                direction={{ xs: "column", sm: "row" }}
                spacing={1}
                sx={{ width: { xs: "100%", sm: "auto" } }}
              >
                {/* Navigate to the independent invitation management page. */}
                <Button
                  variant="outlined"
                  startIcon={<MailOutlineIcon />}
                  component={Link}
                  to={`/projects/${encodeURIComponent(project.id)}/invitations`}
                >
                  Manage invitations
                </Button>

                {/* Open the invitation creation dialog. */}
                <Button
                  variant="contained"
                  startIcon={<PersonAddAltOutlinedIcon />}
                  onClick={() => setInvitationDialogOpen(true)}
                >
                  Invite member
                </Button>
              </Stack>
            )}
          </Stack>

          {/* Owner and paginated members. */}
          <ProjectTeam
            projectId={project.id}
            isOwner={isOwner}
            canManageMembers={project.status === "ACTIVE"}
          />
        </Stack>
      </Container>

      {/* Create a project invitation. */}
      <CreateInvitationDialog
        open={invitationDialogOpen}
        projectId={project.id}
        canInviteAdmin={isOwner}
        onClose={() => setInvitationDialogOpen(false)}
        onCreated={handleInvitationCreated}
      />

      {/* Feedback after a successful invitation. */}
      <Snackbar
        open={successMessage !== null}
        autoHideDuration={5000}
        onClose={() => setSuccessMessage(null)}
        message={successMessage}
        sx={{
          "& .MuiSnackbarContent-root": {
            backgroundColor: "green",
          },
        }}
        anchorOrigin={{
          vertical: "bottom",
          horizontal: "right",
        }}
      />
    </>
  );
}
