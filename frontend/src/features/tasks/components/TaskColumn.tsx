import { Badge, Box, Paper, Stack, Typography } from "@mui/material";

import type { TaskStatus, TaskSummary } from "../types/task.types";

import { useTaskBoard } from "../context/TaskBoardContext";
import TaskCard from "./TaskCard";

interface TaskColumnProps {
  title: string;
  status: TaskStatus;
  tasks: TaskSummary[];
}

export default function TaskColumn({ title, status, tasks }: TaskColumnProps) {
  const { canDrag, draggedTaskId, dropTask } = useTaskBoard();

  const isDropTarget = Boolean(
    draggedTaskId && tasks.every((task) => task.id !== draggedTaskId),
  );

  return (
    <Paper
      variant="outlined"
      onDragOver={(event) => {
        if (!canDrag || !draggedTaskId) return;

        event.preventDefault();
      }}
      onDrop={(event) => {
        if (!canDrag || !draggedTaskId) return;

        event.preventDefault();

        // The context handles the status update.
        dropTask(status);
      }}
      sx={{
        width: { xs: 280, sm: 300 },
        minWidth: { xs: 280, sm: 300 },
        p: 1.5,
        borderRadius: 2.5,
        bgcolor: isDropTarget ? "action.hover" : "background.default",
        borderColor: isDropTarget ? "primary.main" : "divider",
        transition: "background-color 150ms ease, border-color 150ms ease",
        alignSelf: "stretch",
      }}
    >
      <Stack spacing={1.5}>
        <Stack
          direction="row"
          sx={{
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
            {title}
          </Typography>

          <Badge
            badgeContent={tasks.length}
            color="default"
            sx={{
              "& .MuiBadge-badge": {
                position: "static",
                transform: "none",
              },
            }}
          />
        </Stack>

        <Stack spacing={1.25} sx={{ minHeight: 88 }}>
          {tasks.length === 0 ? (
            <Box
              sx={{
                minHeight: 88,
                display: "grid",
                placeItems: "center",
                border: 1,
                borderStyle: "dashed",
                borderColor: "divider",
                borderRadius: 2,
                px: 2,
              }}
            >
              <Typography variant="body2" color="text.secondary">
                {draggedTaskId ? "Drop task here" : "No tasks"}
              </Typography>
            </Box>
          ) : (
            tasks.map((task) => <TaskCard key={task.id} task={task} />)
          )}
        </Stack>
      </Stack>
    </Paper>
  );
}
