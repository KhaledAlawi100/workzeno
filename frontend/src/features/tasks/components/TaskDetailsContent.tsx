import {
  Chip,
  Divider,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from "@mui/material";

import type { Task, TaskStatus } from "../types/task.types";

interface TaskDetailsContentProps {
  task: Task;
  canChangeStatus: boolean;
  statusUpdating: boolean;
  onStatusChange: (status: TaskStatus) => void;
}

const statusOptions: Array<{
  value: TaskStatus;
  label: string;
}> = [
  { value: "TODO", label: "To do" },
  { value: "IN_PROGRESS", label: "In progress" },
  { value: "IN_REVIEW", label: "In review" },
  { value: "DONE", label: "Done" },
];

const priorityColors = {
  LOW: "default",
  MEDIUM: "info",
  HIGH: "warning",
  URGENT: "error",
} as const;

/**
 * Formats the due date for display.
 */
function formatDate(value: string | null): string {
  if (!value) return "Not set";

  return new Intl.DateTimeFormat(undefined, {
    day: "numeric",
    month: "short",
    year: "numeric",
  }).format(new Date(`${value}T00:00:00`));
}

export default function TaskDetailsContent({
  task,
  canChangeStatus,
  statusUpdating,
  onStatusChange,
}: TaskDetailsContentProps) {
  return (
    <Stack spacing={2.5}>
      {/* Priority and current status */}
      <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: "wrap" }}>
        <Chip
          label={task.priority}
          color={priorityColors[task.priority]}
          variant="outlined"
        />

        <Chip label={task.status.replace("_", " ")} variant="outlined" />
      </Stack>

      {/* Task title */}
      <Typography variant="h5" sx={{ fontWeight: 700 }}>
        {task.title}
      </Typography>

      {/* Task description */}
      <Typography
        variant="body1"
        color={task.description ? "text.primary" : "text.secondary"}
        sx={{
          whiteSpace: "pre-wrap",
          overflowWrap: "anywhere",
        }}
      >
        {task.description || "No description provided."}
      </Typography>

      <Divider />

      {/* Task due date */}
      <Stack spacing={1}>
        <Typography variant="subtitle2">Due date</Typography>

        <Typography color="text.secondary">
          {formatDate(task.dueDate)}
        </Typography>
      </Stack>

      {/* Status changes are saved immediately. */}
      {canChangeStatus && (
        <>
          <Divider />

          <TextField
            select
            label="Task status"
            value={task.status}
            fullWidth
            disabled={statusUpdating}
            onChange={(event) =>
              onStatusChange(event.target.value as TaskStatus)
            }
          >
            {statusOptions.map((option) => (
              <MenuItem key={option.value} value={option.value}>
                {option.label}
              </MenuItem>
            ))}
          </TextField>
        </>
      )}
    </Stack>
  );
}
