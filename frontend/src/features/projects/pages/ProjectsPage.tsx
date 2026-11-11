import { useState } from "react";

import {
  Alert,
  Box,
  Button,
  Container,
  Grid,
  Pagination,
  Skeleton,
  Snackbar,
  Stack,
  Typography,
} from "@mui/material";

import AddIcon from "@mui/icons-material/Add";

import ProjectsList from "../components/ProjectsList";
import ProjectFormDialog from "../components/ProjectFormDialog";

import { useProjects } from "../hooks/useProjects";
import { useCreateProject } from "../hooks/useCreateProject";

import { getApiErrorMessage } from "../../../lib/api";

import type { CreateProjectFormValues } from "../projectSchemas";

const PAGE_SIZE = 6;

function ProjectsLoadingState() {
  return (
    <Grid container spacing={3}>
      {Array.from({ length: PAGE_SIZE }).map((_, index) => (
        <Grid key={index} size={{ xs: 12, sm: 6, md: 4 }}>
          <Stack spacing={2}>
            <Skeleton variant="rounded" height={150} />
            <Skeleton variant="text" width="70%" height={30} />
            <Skeleton variant="text" width="100%" />
            <Skeleton variant="text" width="45%" />
          </Stack>
        </Grid>
      ))}
    </Grid>
  );
}

export default function ProjectsPage() {
  const [page, setPage] = useState(0);

  const [dialogOpen, setDialogOpen] = useState(false);

  const [notification, setNotification] = useState<{
    open: boolean;
    severity: "success" | "error";
    message: string;
  }>({
    open: false,
    severity: "success",
    message: "",
  });

  const { data, isPending, isError, error, refetch, isFetching } = useProjects({
    page,
    size: PAGE_SIZE,
  });

  const createProjectMutation = useCreateProject();

  function handleOpenDialog() {
    setDialogOpen(true);
  }

  function handleCloseDialog() {
    setDialogOpen(false);
  }

  async function handleCreateProject(
    values: CreateProjectFormValues,
  ): Promise<void> {
    try {
     
      await createProjectMutation.mutateAsync({
        name: values.name,
        description: values.description || null,
      });

      // Return to the first page after creating a project.
      setPage(0);

      setDialogOpen(false);

      setNotification({
        open: true,
        severity: "success",
        message: "Project created successfully.",
      });
    } catch (error) {
      setNotification({
        open: true,
        severity: "error",
        message: getApiErrorMessage(error),
      });
    }
  }

  function handleCloseNotification() {
    setNotification((previous) => ({
      ...previous,
      open: false,
    }));
  }

  return (
    <Container maxWidth="lg" sx={{ py: 5 }}>
      <Stack spacing={3}>
        {/* Page header */}
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={2}
          sx={{
            justifyContent: "space-between",
            alignItems: { xs: "stretch", sm: "center" },
          }}
        >
          <Stack spacing={1}>
            <Typography variant="h4" component="h1" sx={{ fontWeight: 700 }}>
              Projects
            </Typography>

            <Typography color="text.secondary">
              View and manage your projects.
            </Typography>
          </Stack>

          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenDialog}
          >
            New project
          </Button>
        </Stack>

        {/* Loading state */}
        {isPending && <ProjectsLoadingState />}

        {/* Page loading error */}
        {isError && (
          <Alert
            severity="error"
            action={
              <Button color="inherit" size="small" onClick={() => refetch()}>
                Retry
              </Button>
            }
          >
            {error instanceof Error
              ? error.message
              : "Could not load your projects. Please try again."}
          </Alert>
        )}

        {/* Projects list */}
        {data && !isError && (
          <Stack spacing={3}>
            {isFetching && (
              <Typography variant="caption" color="text.secondary">
                Updating projects...
              </Typography>
            )}

            <ProjectsList projects={data.content} />

            {data.totalPages > 1 && (
              <Box
                sx={{
                  display: "flex",
                  justifyContent: "center",
                  pt: 2,
                }}
              >
                <Pagination
                  count={data.totalPages}
                  page={page + 1}
                  onChange={(_, selectedPage) => {
                    setPage(selectedPage - 1);
                  }}
                  color="primary"
                  shape="rounded"
                  showFirstButton
                  showLastButton
                  disabled={isFetching}
                />
              </Box>
            )}

            <Typography
              variant="body2"
              color="text.secondary"
              sx={{ textAlign: "center" }}
            >
              {data.totalElements}{" "}
              {data.totalElements === 1 ? "project" : "projects"} in total
            </Typography>
          </Stack>
        )}
      </Stack>

      {/* Create project modal */}
      <ProjectFormDialog
        open={dialogOpen}
        mode="create"
        onClose={handleCloseDialog}
        onSubmit={handleCreateProject}
      />

      {/* Mutation success/error notification */}
      <Snackbar
        open={notification.open}
        autoHideDuration={5000}
        onClose={handleCloseNotification}
        anchorOrigin={{
          vertical: "bottom",
          horizontal: "right",
        }}
      >
        <Alert
          onClose={handleCloseNotification}
          severity={notification.severity}
          variant="filled"
          sx={{ width: "100%" }}
        >
          {notification.message}
        </Alert>
      </Snackbar>
    </Container>
  );
}
