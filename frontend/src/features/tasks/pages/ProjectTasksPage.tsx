import { useParams } from "react-router-dom";

import { Alert, Container, Skeleton, Stack, Typography } from "@mui/material";

import { getApiErrorMessage } from "../../../lib/api";

import TaskBoard from "../components/TaskBoard";
import TaskDetailsDialog from "../components/TaskDetailsDialog";
import TaskFormDialog from "../components/TaskFormDialog";

import TaskBoardLoading from "../components/page/TaskBoardLoading";
import TaskPageError from "../components/page/TaskPageError";
import TaskPageHeader from "../components/page/TaskPageHeader";
import TaskPagePagination from "../components/page/TaskPagePagination";

import { useProjectTasksPage } from "../hooks/useProjectTasksPage";

export default function ProjectTasksPage() {
  const { projectId } = useParams<{ projectId: string }>();

  const pageState = useProjectTasksPage(projectId);

  const {
    page,
    setPage,
    formOpen,
    setFormOpen,
    selectedTaskId,
    editingTask,
    setEditingTask,
    pageError,
    setPageError,
    tasksQuery,
    permissionsQuery,
    selectedTask,
    handleCreate,
    handleUpdate,
    handleTaskStatusChange,
    openCreateDialog,
    openTaskDetails,
    closeTaskDetails,
  } = pageState;

  if (!projectId) {
    return (
      <TaskPageError message="Invalid project URL. A project ID is required." />
    );
  }

  // Show loading UI while tasks and permissions are being fetched.
  if (tasksQuery.isPending || permissionsQuery.isLoading) {
    return (
      <Container maxWidth="xl" sx={{ py: 4 }}>
        <Stack spacing={3}>
          <Skeleton variant="text" width={180} height={28} />
          <Skeleton variant="text" width={260} height={42} />
          <TaskBoardLoading />
        </Stack>
      </Container>
    );
  }

  // Handle task-fetching failures.
  if (tasksQuery.isError) {
    return (
      <TaskPageError
        projectId={projectId}
        message={getApiErrorMessage(tasksQuery.error)}
        onRetry={() => void tasksQuery.refetch()}
      />
    );
  }

  // Ensure the current user can access project tasks.
  if (permissionsQuery.isError || !permissionsQuery.permissions.canViewTasks) {
    return (
      <TaskPageError
        projectId={projectId}
        message={
          permissionsQuery.isError
            ? "We couldn't verify your project membership."
            : "You do not have permission to view this project's tasks."
        }
        onRetry={
          permissionsQuery.isError
            ? () => void permissionsQuery.refetchMembership()
            : undefined
        }
      />
    );
  }

  const taskPage = tasksQuery.data;
  const tasks = taskPage.content;

  const hasNoTasks = taskPage.totalElements === 0;
  const hasNoTasksOnCurrentPage = !hasNoTasks && tasks.length === 0;

  // Keep the displayed page within the valid range after deletions/refreshes.
  const effectivePage =
    taskPage.totalPages > 0 ? Math.min(page, taskPage.totalPages - 1) : 0;

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Stack spacing={3}>
        <TaskPageHeader
          projectId={projectId}
          canCreateTasks={permissionsQuery.permissions.canCreateTasks}
          onCreateTask={openCreateDialog}
        />

        {pageError && (
          <Alert severity="error" onClose={() => setPageError(null)}>
            {pageError}
          </Alert>
        )}

        {tasksQuery.isFetching && (
          <Typography variant="caption" color="text.secondary">
            Refreshing tasks...
          </Typography>
        )}

        {hasNoTasks && (
          <Alert severity="info">
            This project doesn't have any tasks yet.
          </Alert>
        )}

        {hasNoTasksOnCurrentPage && (
          <Alert severity="info">
            There are no tasks on this page. Try selecting another page.
          </Alert>
        )}

        <TaskBoard
          tasks={tasks}
          canChangeStatus={permissionsQuery.permissions.canChangeTaskStatus}
          onTaskClick={openTaskDetails}
          onTaskStatusChange={handleTaskStatusChange}
        />

        <TaskPagePagination
          totalPages={taskPage.totalPages}
          page={effectivePage}
          onChange={setPage}
        />
      </Stack>

      {/* Create task dialog */}
      <TaskFormDialog
        open={formOpen}
        mode="create"
        onClose={() => setFormOpen(false)}
        onSubmit={handleCreate}
      />

      {/* Edit task dialog */}
      {editingTask && selectedTask && (
        <TaskFormDialog
          open
          mode="edit"
          task={selectedTask}
          onClose={() => setEditingTask(false)}
          onSubmit={handleUpdate}
        />
      )}

      {/* Task details dialog */}
      <TaskDetailsDialog
        open={Boolean(selectedTaskId) && !editingTask}
        projectId={projectId}
        taskId={selectedTaskId}
        canEdit={permissionsQuery.permissions.canEditTasks}
        canChangeStatus={permissionsQuery.permissions.canChangeTaskStatus}
        canDelete={permissionsQuery.permissions.canDeleteTasks}
        onClose={closeTaskDetails}
        onEdit={() => setEditingTask(true)}
      />
    </Container>
  );
}
