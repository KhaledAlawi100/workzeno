import { Link, useParams } from "react-router-dom";

import {
  Alert,
  Button,
  Container,
  Skeleton,
  Stack,
  Typography,
} from "@mui/material";

import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import MailOutlineIcon from "@mui/icons-material/MailOutlined";
import RefreshIcon from "@mui/icons-material/Refresh";

import { useAuth } from "../../../auth/AuthContext";
import { useProject } from "../../../projects/hooks/useProject";
import { useMyProjectMembership } from "../../membership/hooks/useMyProjectMembership";

import ProjectInvitationList from "../components/ProjectInvitationList";

function PageLoading() {
  return (
    <Stack spacing={3}>
      <Skeleton variant="text" width={150} height={30} />
      <Skeleton variant="text" width="45%" height={45} />
      <Skeleton variant="rounded" height={150} />
      <Skeleton variant="rounded" height={150} />
    </Stack>
  );
}

export default function ProjectInvitationsPage() {
  const { projectId } = useParams<{ projectId: string }>();
  const { user } = useAuth();

  const { data: project, isPending, isError, refetch } = useProject(projectId);

  // The owner is separate from project memberships.
  const isOwner = Boolean(project && user && project.ownerId === user.id);

  // Only non-owners need to retrieve their membership role.
  const {
    data: myMembership,
    isPending: isMembershipPending,
    isError: isMembershipError,
  } = useMyProjectMembership(projectId, Boolean(project && user && !isOwner));

  const isAdmin = myMembership?.role === "ADMIN";

  const canManageInvitations =
    isOwner || (!isMembershipPending && !isMembershipError && isAdmin);

  if (!projectId) {
    return (
      <Container maxWidth="md" sx={{ py: 5 }}>
        <Alert severity="error">Invalid project URL.</Alert>
      </Container>
    );
  }

  if (isPending) {
    return (
      <Container maxWidth="lg" sx={{ py: 5 }}>
        <PageLoading />
      </Container>
    );
  }

  if (isError || !project) {
    return (
      <Container maxWidth="md" sx={{ py: 5 }}>
        <Stack spacing={2}>
          <Button
            component={Link}
            to={`/projects/${encodeURIComponent(projectId)}/members`}
            startIcon={<ArrowBackIcon />}
            sx={{ alignSelf: "flex-start" }}
          >
            Back to team
          </Button>

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
            Unable to load this project.
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

  // Do not render invitation management while membership permissions
  // are loading or if the current user is not an owner or admin.
  if (isMembershipPending && !isOwner) {
    return (
      <Container maxWidth="lg" sx={{ py: 5 }}>
        <PageLoading />
      </Container>
    );
  }

  if (!canManageInvitations) {
    return (
      <Container maxWidth="md" sx={{ py: 5 }}>
        <Stack spacing={2}>
          <Button
            component={Link}
            to={`/projects/${encodeURIComponent(project.id)}/members`}
            startIcon={<ArrowBackIcon />}
            sx={{ alignSelf: "flex-start" }}
          >
            Back to team
          </Button>

          <Alert severity="error">
            You do not have permission to manage this project's invitations.
          </Alert>
        </Stack>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 5 }}>
      <Stack spacing={3}>
        <Button
          component={Link}
          to={`/projects/${encodeURIComponent(project.id)}/members`}
          startIcon={<ArrowBackIcon />}
          sx={{ alignSelf: "flex-start" }}
        >
          Back to team
        </Button>

        <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
          <MailOutlineIcon color="primary" />

          <Typography
            variant="h4"
            component="h1"
            sx={{ fontWeight: 700, overflowWrap: "anywhere" }}
          >
            Project invitations
          </Typography>
        </Stack>

        <Typography color="text.secondary">
          View and manage invitations for <strong>{project.name}</strong>.
        </Typography>

        <ProjectInvitationList
          projectId={project.id}
          canRevokeInvitations={canManageInvitations}
        />
      </Stack>
    </Container>
  );
}
