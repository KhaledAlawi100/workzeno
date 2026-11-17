import {
  Alert,
  Box,
  Button,
  Container,
  Pagination,
  Snackbar,
  Stack,
  Typography,
} from "@mui/material";

import ProjectsList from "../components/ProjectsList";
import ProjectFormDialog from "../components/ProjectFormDialog";

import ProjectsPageHeader from "../components/page/ProjectsPageHeader";
import ProjectsSearch from "../components/page/ProjectsSearch";
import ProjectsLoadingState from "../components/page/ProjectsLoadingState";

import { useProjectsPage } from "../hooks/useProjectsPage";


export default function ProjectsPage() {
  const {
    searchInput,
    debouncedSearch,
    dialogOpen,
    notification,

    data,
    isPending,
    isError,
    error,
    refetch,
    isFetching,

    handleSearchChange,
    handleClearSearch,
    handlePageChange,
    handleOpenDialog,
    handleCloseDialog,
    handleCreateProject,
    handleCloseNotification,
  } = useProjectsPage();

  return (
    <Container maxWidth="lg" sx={{ py: 5 }}>
      <Stack spacing={3}>
        <ProjectsPageHeader onCreateProject={handleOpenDialog} />

        <ProjectsSearch
          value={searchInput}
          onChange={handleSearchChange}
          onClear={handleClearSearch}
        />

        {/* Initial loading state */}
        {isPending && <ProjectsLoadingState />}

        {/* Projects loading error */}
        {isError && (
          <Alert
            severity="error"
            action={
              <Button
                color="inherit"
                size="small"
                onClick={() => {
                  void refetch();
                }}
              >
                Retry
              </Button>
            }
          >
            {error instanceof Error
              ? error.message
              : "Could not load your projects. Please try again."}
          </Alert>
        )}

        {/* Successfully loaded projects */}
        {data && !isError && (
          <Stack spacing={3}>
            {isFetching && (
              <Typography variant="caption" color="text.secondary">
                Updating projects...
              </Typography>
            )}

            <ProjectsList
              projects={data.content}
              searchTerm={debouncedSearch}
            />

            {/* Server-side pagination */}
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
                  page={data.totalPages > 0 ? undefined : 1}
                  onChange={(_, selectedPage) => {
                    handlePageChange(selectedPage);
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

      {/* Create project dialog */}
      <ProjectFormDialog
        open={dialogOpen}
        mode="create"
        onClose={handleCloseDialog}
        onSubmit={handleCreateProject}
      />

      {/* Create success/error notification */}
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
