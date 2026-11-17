import { Link, useParams } from "react-router-dom";
import axios from "axios";
import {
  Alert,
  Button,
  Container,
  Skeleton,
  Snackbar,
  Stack,
  Typography,
} from "@mui/material";

import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import RefreshIcon from "@mui/icons-material/Refresh";
import { useProject } from "../hooks/useProject";
import ProjectFormDialog from "../components/ProjectFormDialog";
import ArchiveProjectDialog from "../components/ArchiveProjectDialog";
import ProjectDetailsHeader from "../components/details/ProjectDetailsHeader";
import ProjectDetailsActions from "../components/details/ProjectDetailsActions";
import ProjectDescription from "../components/details/ProjectDescription";
import ProjectInformation from "../components/details/ProjectInformation";
import { getApiErrorMessage } from "../../../lib/api";
import { useAuth } from "../../auth/AuthContext";
import { useProjectDetailsPermissions } from "../hooks/useProjectDetailsPermissions";
import { useProjectDetailsActions } from "../hooks/useProjectDetailsActions";

function ProjectDetailsLoading() {
  return (
    <Stack spacing={3}>
      <Skeleton variant="text" width={120} height={25} />
      <Skeleton variant="text" width="45%" height={45} />
      <Skeleton variant="rounded" width="100%" height={140} />
      <Skeleton variant="rounded" width="100%" height={100} />
    </Stack>
  );
}

export default function ProjectDetailsPage() {
  const { projectId } = useParams<{ projectId: string }>();

  const { user } = useAuth();

  const {
    data: project,
    isPending,
    isError,
    error,
    refetch,
    isFetching,
  } = useProject(projectId);

  const permissions = useProjectDetailsPermissions(
    project,
    projectId,
    user?.id,
  );

  const actions = useProjectDetailsActions(project);

  if (!projectId) {
    return (
      <Container maxWidth="md" sx={{ py: 5 }}>
        <Alert severity="error">
          Invalid project URL. Please select a valid project.
        </Alert>

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
      <Container maxWidth="md" sx={{ py: 5 }}>
        <ProjectDetailsLoading />
      </Container>
    );
  }

  if (isError) {
    const isNotFound =
      axios.isAxiosError(error) && error.response?.status === 404;

    return (
      <Container maxWidth="md" sx={{ py: 5 }}>
        <Stack spacing={2}>
          <Button
            component={Link}
            to="/projects"
            startIcon={<ArrowBackIcon />}
            sx={{ alignSelf: "flex-start" }}
          >
            Back to projects
          </Button>

          {/* Show a specific message if the project was not found. */}
          {isNotFound ? (
            <Alert severity="warning">
              <Typography sx={{ fontWeight: 600 }}>
                Project not found
              </Typography>
              This project may have been deleted or you may not have permission
              to access it.
            </Alert>
          ) : (
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
              {getApiErrorMessage(error)}
            </Alert>
          )}
        </Stack>
      </Container>
    );
  }

  // Defensive check in case the query has no project data.
  if (!project) {
    return (
      <Container maxWidth="md" sx={{ py: 5 }}>
        <Alert severity="warning">Project information is unavailable.</Alert>
      </Container>
    );
  }

  const isActive = project.status === "ACTIVE";

  return (
    <Container maxWidth="md" sx={{ py: 5 }}>
      <Stack spacing={3}>
        {/* Back navigation */}
        <Button
          component={Link}
          to="/projects"
          startIcon={<ArrowBackIcon />}
          sx={{ alignSelf: "flex-start" }}
        >
          Back to projects
        </Button>

        {/* Project heading and available actions */}
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={2}
          sx={{
            justifyContent: "space-between",
            alignItems: { xs: "flex-start", sm: "center" },
          }}
        >
          <ProjectDetailsHeader project={project} />

          <ProjectDetailsActions
            projectId={project.id}
            isActive={isActive}
            shouldLoadMembership={permissions.shouldLoadMembership}
            isMembershipPending={permissions.isMembershipPending}
            isMembershipError={permissions.isMembershipError}
            refetchMembership={permissions.refetchMembership}
            canShowManagementActions={permissions.canShowManagementActions}
            canEditProject={permissions.canEditProject}
            canManageProjectStatus={permissions.canManageProjectStatus}
            isActionPending={actions.isActionPending}
            isRestoring={actions.isRestoring}
            onEdit={actions.handleOpenEditDialog}
            onArchive={actions.handleOpenArchiveDialog}
            onRestore={actions.handleRestoreProject}
          />
        </Stack>

        {/* Show an indicator during background refreshes. */}
        {isFetching && (
          <Typography variant="caption" color="text.secondary">
            Refreshing project details...
          </Typography>
        )}

        {/* Project description */}
        <ProjectDescription description={project.description} />

        {/* Project metadata */}
        <ProjectInformation project={project} />
      </Stack>

      {/* Edit project dialog */}
      <ProjectFormDialog
        open={actions.editDialogOpen}
        mode="edit"
        project={project}
        onClose={actions.handleCloseEditDialog}
        onSubmit={actions.handleUpdateProject}
      />

      {/* Archive confirmation dialog */}
      <ArchiveProjectDialog
        open={actions.archiveDialogOpen}
        projectName={project.name}
        isPending={actions.isArchiving}
        onClose={actions.handleCloseArchiveDialog}
        onConfirm={actions.handleArchiveProject}
      />

      {/* Success and error notifications */}
      <Snackbar
        open={actions.notification !== null}
        autoHideDuration={5000}
        onClose={actions.handleCloseNotification}
        anchorOrigin={{
          vertical: "bottom",
          horizontal: "right",
        }}
      >
        {actions.notification ? (
          <Alert
            severity={actions.notification.severity}
            variant="filled"
            onClose={actions.handleCloseNotification}
            sx={{ width: "100%" }}
          >
            {actions.notification.message}
          </Alert>
        ) : undefined}
      </Snackbar>
    </Container>
  );
}
