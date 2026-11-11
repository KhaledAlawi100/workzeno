import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import axios from "axios";

import {
  Alert,
  Box,
  Button,
  Chip,
  Container,
  Divider,
  Paper,
  Skeleton,
  Snackbar,
  Stack,
  Typography,
} from "@mui/material";

import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import RefreshIcon from "@mui/icons-material/Refresh";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import ArchiveOutlinedIcon from "@mui/icons-material/ArchiveOutlined";
import RestoreOutlinedIcon from "@mui/icons-material/RestoreOutlined";

import { useProject } from "../hooks/useProject";
import { useUpdateProject } from "../hooks/useUpdateProject";
import { useProjectStatus } from "../hooks/useProjectStatus";

import ProjectFormDialog from "../components/ProjectFormDialog";
import ArchiveProjectDialog from "../components/ArchiveProjectDialog";

import type { CreateProjectFormValues } from "../projectSchemas";

import { getApiErrorMessage } from "../../../lib/api";

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

  // Fetch the selected project's details.
  const {
    data: project,
    isPending,
    isError,
    error,
    refetch,
    isFetching,
  } = useProject(projectId);

  // Dialog states.
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [archiveDialogOpen, setArchiveDialogOpen] = useState(false);

  // Success and error notification state.
  const [notification, setNotification] = useState<{
    message: string;
    severity: "success" | "error";
  } | null>(null);

  // Project mutations.
  const updateProjectMutation = useUpdateProject();

  const { archiveMutation, restoreMutation } = useProjectStatus();

  // Prevent conflicting project actions.
  const isActionPending =
    updateProjectMutation.isPending ||
    archiveMutation.isPending ||
    restoreMutation.isPending;

  // Close edit dialog.
  function handleCloseEditDialog() {
    if (isActionPending) {
      return;
    }

    setEditDialogOpen(false);
  }

  // Update project.
  async function handleUpdateProject(
    values: CreateProjectFormValues,
  ): Promise<void> {
    if (!project) {
      return;
    }

    try {
      await updateProjectMutation.mutateAsync({
        projectId: project.id,
        request: {
          name: values.name,
          description: values.description || null,
        },
      });

      setEditDialogOpen(false);

      setNotification({
        message: "Project updated successfully.",
        severity: "success",
      });
    } catch (error) {
      setNotification({
        message: getApiErrorMessage(error),
        severity: "error",
      });
    }
  }

  // Close archive confirmation dialog.
  function handleCloseArchiveDialog() {
    if (isActionPending) {
      return;
    }

    setArchiveDialogOpen(false);
  }

  // Archive project.
  async function handleArchiveProject() {
    if (!project) {
      return;
    }

    try {
      await archiveMutation.mutateAsync({
        projectId: project.id,
      });

      setArchiveDialogOpen(false);

      setNotification({
        message: "Project archived successfully.",
        severity: "success",
      });
    } catch (error) {
      setNotification({
        message: getApiErrorMessage(error),
        severity: "error",
      });
    }
  }

  // Restore project.
  async function handleRestoreProject() {
    if (!project) {
      return;
    }

    try {
      await restoreMutation.mutateAsync({
        projectId: project.id,
      });

      setNotification({
        message: "Project restored successfully.",
        severity: "success",
      });
    } catch (error) {
      setNotification({
        message: getApiErrorMessage(error),
        severity: "error",
      });
    }
  }

  // Invalid or missing route parameter.
  if (!projectId) {
    return (
      <Container maxWidth="md" sx={{ py: 5 }}>
        <Alert severity="error">
          Invalid project URL. A project ID is required.
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

  // Initial loading state.
  if (isPending) {
    return (
      <Container maxWidth="md" sx={{ py: 5 }}>
        <ProjectDetailsLoading />
      </Container>
    );
  }

  // Error and not-found states.
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
                  onClick={() => refetch()}
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

        {/* Project heading and actions */}
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={2}
          sx={{
            justifyContent: "space-between",
            alignItems: { xs: "flex-start", sm: "center" },
          }}
        >
          <Stack spacing={1}>
            <Typography
              variant="h4"
              component="h1"
              sx={{
                fontWeight: 700,
                overflowWrap: "anywhere",
              }}
            >
              {project.name}
            </Typography>

            <Chip
              label={isActive ? "Active" : "Archived"}
              color={isActive ? "success" : "default"}
              size="small"
              sx={{ alignSelf: "flex-start" }}
            />
          </Stack>

          {/* Edit, archive, and restore actions */}
          <Stack
            direction={{ xs: "column", sm: "row" }}
            spacing={1}
            sx={{
              width: { xs: "100%", sm: "auto" },
            }}
          >
            <Button
              variant="contained"
              startIcon={<EditOutlinedIcon />}
              onClick={() => setEditDialogOpen(true)}
              disabled={isActionPending}
            >
              Edit project
            </Button>

            {isActive ? (
              <Button
                variant="outlined"
                color="warning"
                startIcon={<ArchiveOutlinedIcon />}
                onClick={() => setArchiveDialogOpen(true)}
                disabled={isActionPending}
              >
                Archive project
              </Button>
            ) : (
              <Button
                variant="contained"
                color="success"
                startIcon={<RestoreOutlinedIcon />}
                onClick={handleRestoreProject}
                disabled={isActionPending}
              >
                {restoreMutation.isPending ? "Restoring..." : "Restore project"}
              </Button>
            )}
          </Stack>
        </Stack>

        {/* Background refresh indicator */}
        {isFetching && (
          <Typography variant="caption" color="text.secondary">
            Refreshing project details...
          </Typography>
        )}

        {/* Project description */}
        <Paper variant="outlined" sx={{ p: 3, borderRadius: 2 }}>
          <Stack spacing={2}>
            <Typography variant="h6" component="h2">
              Description
            </Typography>

            <Typography
              color={project.description ? "text.primary" : "text.secondary"}
              sx={{
                whiteSpace: "pre-wrap",
                overflowWrap: "anywhere",
              }}
            >
              {project.description || "No description provided."}
            </Typography>
          </Stack>
        </Paper>

        {/* Project information */}
        <Paper variant="outlined" sx={{ p: 3, borderRadius: 2 }}>
          <Stack spacing={2}>
            <Typography variant="h6" component="h2">
              Project information
            </Typography>

            <Divider />

            <Box>
              <Typography variant="caption" color="text.secondary">
                Project ID
              </Typography>

              <Typography variant="body2" sx={{ overflowWrap: "anywhere" }}>
                {project.id}
              </Typography>
            </Box>

            <Box>
              <Typography variant="caption" color="text.secondary">
                Owner ID
              </Typography>

              <Typography variant="body2" sx={{ overflowWrap: "anywhere" }}>
                {project.ownerId}
              </Typography>
            </Box>

            <Box>
              <Typography variant="caption" color="text.secondary">
                Status
              </Typography>

              <Typography variant="body2">{project.status}</Typography>
            </Box>

            <Box>
              <Typography variant="caption" color="text.secondary">
                Created at
              </Typography>

              <Typography variant="body2">
                {new Date(project.createdAt).toLocaleString()}
              </Typography>
            </Box>

            <Box>
              <Typography variant="caption" color="text.secondary">
                Last updated
              </Typography>

              <Typography variant="body2">
                {new Date(project.updatedAt).toLocaleString()}
              </Typography>
            </Box>
          </Stack>
        </Paper>

        {/* Project members placeholder */}
        <Paper variant="outlined" sx={{ p: 3, borderRadius: 2 }}>
          <Stack spacing={1}>
            <Typography variant="h6" component="h2">
              Project members
            </Typography>

            <Typography color="text.secondary" variant="body2">
              Project member management will be available in a future step.
            </Typography>
          </Stack>
        </Paper>
      </Stack>

      {/* Edit project dialog */}
      <ProjectFormDialog
        open={editDialogOpen}
        mode="edit"
        project={project}
        onClose={handleCloseEditDialog}
        onSubmit={handleUpdateProject}
      />

      {/* Archive confirmation dialog */}
      <ArchiveProjectDialog
        open={archiveDialogOpen}
        projectName={project.name}
        isPending={archiveMutation.isPending}
        onClose={handleCloseArchiveDialog}
        onConfirm={handleArchiveProject}
      />

      {/* Success and error notifications */}
      <Snackbar
        open={notification !== null}
        autoHideDuration={5000}
        onClose={() => setNotification(null)}
        anchorOrigin={{
          vertical: "bottom",
          horizontal: "right",
        }}
      >
        {notification ? (
          <Alert
            severity={notification.severity}
            variant="filled"
            onClose={() => setNotification(null)}
            sx={{ width: "100%" }}
          >
            {notification.message}
          </Alert>
        ) : undefined}
      </Snackbar>
    </Container>
  );
}
