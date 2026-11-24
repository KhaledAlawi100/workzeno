import { useState } from "react";

import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Stack,
  Typography,
} from "@mui/material";

import CloseIcon from "@mui/icons-material/Close";
import TaskComments from "../comments/components/TaskComments";

import { getApiErrorMessage } from "../../../lib/api";

import {
  useDeleteTask,
  useTask,
  useUpdateTaskStatus,
} from "../hooks/taskHooks";

import type { TaskStatus } from "../types/task.types";

import TaskAssigneeManager from "./TaskAssigneeManager";
import TaskDetailsContent from "./TaskDetailsContent";
import TaskDetailsActions from "./TaskDetailsActions";
import DeleteTaskDialog from "./DeleteTaskDialog";
import TaskDetailsSnackbar from "./TaskDetailsSnackbar";

interface TaskDetailsDialogProps {
  open: boolean;
  projectId: string;
  taskId: string | null;
  canEdit: boolean;
  canChangeStatus: boolean;
  canDelete: boolean;
  onClose: () => void;
  onEdit: () => void;
}

export default function TaskDetailsDialog({
  open,
  projectId,
  taskId,
  canEdit,
  canChangeStatus,
  canDelete,
  onClose,
  onEdit,
}: TaskDetailsDialogProps) {
  // Controls the delete confirmation dialog.
  const [deleteConfirmation, setDeleteConfirmation] = useState(false);

  // Stores errors from status updates and task deletion.
  const [actionError, setActionError] = useState<string | null>(null);

  // Stores success messages from immediate-save operations.
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Fetches the complete task, including its assignees.
  const {
    data: task,
    isPending,
    isError,
    error,
    refetch,
  } = useTask(projectId, taskId ?? "");

  const updateStatus = useUpdateTaskStatus(projectId);
  const deleteTask = useDeleteTask(projectId);

  /**
   * Updates the task status immediately.
   * The success notification is shown only after the API succeeds.
   */
  async function handleStatusChange(status: TaskStatus) {
    if (!task) return;

    setActionError(null);

    try {
      await updateStatus.mutateAsync({
        taskId: task.id,
        request: { status },
      });

      setSuccessMessage("Task status updated successfully.");
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    }
  }

  /**
   * Deletes the task after the user confirms the action.
   */
  async function handleDelete() {
    if (!task) return;

    setActionError(null);

    try {
      await deleteTask.mutateAsync(task.id);

      setDeleteConfirmation(false);
      onClose();
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    }
  }

  return (
    <>
      <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
        {/* Dialog header */}
        <DialogTitle
          sx={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            gap: 2,
          }}
        >
          Task details
          <IconButton
            aria-label="Close task details"
            onClick={onClose}
            edge="end"
          >
            <CloseIcon />
          </IconButton>
        </DialogTitle>

        {/* Dialog content */}
        <DialogContent dividers>
          <Stack spacing={2.5}>
            {actionError && <Alert severity="error">{actionError}</Alert>}

            {isPending && (
              <Typography color="text.secondary">
                Loading task details...
              </Typography>
            )}

            {isError && (
              <Alert
                severity="error"
                action={
                  <Button
                    color="inherit"
                    size="small"
                    onClick={() => void refetch()}
                  >
                    Retry
                  </Button>
                }
              >
                {getApiErrorMessage(error)}
              </Alert>
            )}

            {task && (
              <>
                {/* Task information and status */}
                <TaskDetailsContent
                  task={task}
                  canChangeStatus={canChangeStatus}
                  statusUpdating={updateStatus.isPending}
                  onStatusChange={handleStatusChange}
                />

                {/* Assignment is available independently of edit permissions. */}
                <TaskAssigneeManager
                  projectId={projectId}
                  taskId={task.id}
                  assignees={task.assignees}
                  onSuccess={setSuccessMessage}
                />
                {/* Task comments and collaboration */}
                <TaskComments projectId={projectId} taskId={task.id} />
              </>
            )}
          </Stack>
        </DialogContent>

        {/* Footer actions */}
        <DialogActions sx={{ px: 3, py: 2 }}>
          <TaskDetailsActions
            canEdit={canEdit && Boolean(task)}
            canDelete={canDelete && Boolean(task)}
            deleting={deleteTask.isPending}
            onEdit={onEdit}
            onDelete={() => setDeleteConfirmation(true)}
            onDone={onClose}
          />
        </DialogActions>
      </Dialog>

      {/* Separate confirmation dialog for destructive actions. */}
      <DeleteTaskDialog
        open={deleteConfirmation}
        deleting={deleteTask.isPending}
        onCancel={() => setDeleteConfirmation(false)}
        onConfirm={() => void handleDelete()}
      />

      {/* Shared success notification for immediate-save operations. */}
      <TaskDetailsSnackbar
        message={successMessage}
        onClose={() => setSuccessMessage(null)}
      />
    </>
  );
}
