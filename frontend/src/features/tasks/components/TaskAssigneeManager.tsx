import { useState } from "react";

import {
  Alert,
  Chip,
  CircularProgress,
  Stack,
  Typography,
} from "@mui/material";

import { getApiErrorMessage } from "../../../lib/api";

import ProjectMemberSearch from "../../collaboration/membership/components/ProjectMemberSearch";

import type { TaskAssignee } from "../types/task.types";

import { useAssignTask, useUnassignTask } from "../hooks/taskHooks";

interface TaskAssigneeManagerProps {
  projectId: string;
  taskId: string;
  assignees: TaskAssignee[];

  // Allows the parent dialog to display success notifications.
  onSuccess?: (message: string) => void;
}

export default function TaskAssigneeManager({
  projectId,
  taskId,
  assignees,
  onSuccess,
}: TaskAssigneeManagerProps) {
  const [actionError, setActionError] = useState<string | null>(null);

  // Existing mutation hooks handle the API requests and query invalidation.
  const assignTask = useAssignTask(projectId);
  const unassignTask = useUnassignTask(projectId);

  // Disable assignment controls while either operation is running.
  const isUpdating = assignTask.isPending || unassignTask.isPending;

  /**
   * Assigns only a member selected from the search results.
   *
   * ProjectMemberSearch returns a ProjectMember object.
   * The backend assignment endpoint expects that member's email.
   */
  async function handleAssign(memberEmail: string) {
    setActionError(null);

    try {
      await assignTask.mutateAsync({
        taskId,
        request: {
          email: memberEmail,
        },
      });

      // Notify the parent dialog after the API request succeeds.
      onSuccess?.("Assignee added successfully.");
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    }
  }

  /**
   * Removes an existing assignee using their user ID.
   */
  async function handleUnassign(userId: string) {
    setActionError(null);

    try {
      await unassignTask.mutateAsync({
        taskId,
        userId,
      });

      // Notify the parent dialog after the API request succeeds.
      onSuccess?.("Assignee removed successfully.");
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    }
  }

  return (
    <Stack spacing={2}>
      <Typography variant="subtitle2">Assignees</Typography>

      {/* Display assignment or removal errors */}
      {actionError && (
        <Alert severity="error" onClose={() => setActionError(null)}>
          {actionError}
        </Alert>
      )}

      {/* Search existing project members */}
      <ProjectMemberSearch
        projectId={projectId}
        excludedUserIds={assignees.map((assignee) => assignee.userId)}
        disabled={isUpdating}
        onSelect={(member) => {
          // Only the selected member's email is passed to the API.
          void handleAssign(member.email);
        }}
      />

      {/* Display current task assignees */}
      {assignees.length === 0 ? (
        <Typography variant="body2" color="text.secondary">
          No members assigned to this task yet.
        </Typography>
      ) : (
        <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: "wrap" }}>
          {assignees.map((assignee) => (
            <Chip
              key={assignee.userId}
              label={assignee.email}
              variant="outlined"
              onDelete={() => {
                void handleUnassign(assignee.userId);
              }}
              disabled={isUpdating}
              deleteIcon={
                isUpdating ? <CircularProgress size={14} /> : undefined
              }
            />
          ))}
        </Stack>
      )}

      {/* Show progress while updating assignees */}
      {isUpdating && (
        <Typography variant="caption" color="text.secondary">
          Updating task assignees...
        </Typography>
      )}
    </Stack>
  );
}
