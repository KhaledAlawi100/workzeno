import { Link } from "react-router-dom";

import {
  Box,
  Card,
  CardContent,
  Chip,
  Divider,
  Stack,
  Typography,
} from "@mui/material";

import ArrowForwardOutlinedIcon from "@mui/icons-material/ArrowForwardOutlined";
import AssignmentOutlinedIcon from "@mui/icons-material/AssignmentOutlined";

import type {
  TaskPriority,
  TaskStatus,
  TaskSummary,
} from "../../tasks/types/task.types";

interface MyTasksProps {
  tasks: TaskSummary[];
}

const statusLabels: Record<TaskStatus, string> = {
  TODO: "To do",
  IN_PROGRESS: "In progress",
  IN_REVIEW: "In review",
  DONE: "Done",
};

const priorityColors: Record<
  TaskPriority,
  "default" | "info" | "warning" | "error"
> = {
  LOW: "default",
  MEDIUM: "info",
  HIGH: "warning",
  URGENT: "error",
};

function formatDueDate(value: string | null): string {
  if (!value) return "No due date";

  const date = new Date(`${value}T00:00:00`);

  if (Number.isNaN(date.getTime())) return "Invalid due date";

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
  }).format(date);
}

export default function MyTasks({ tasks }: MyTasksProps) {
  return (
    <Card>
      <CardContent sx={{ p: 2.5 }}>
        <Stack spacing={2}>
          <Stack
            direction="row"
            sx={{ justifyContent: "space-between", alignItems: "center" }}
          >
            <Box>
              <Typography variant="h6" sx={{ fontWeight: 700 }}>
                My tasks
              </Typography>

              <Typography variant="body2" color="text.secondary">
                Open tasks assigned to you.
              </Typography>
            </Box>

            <Chip
              size="small"
              label={`${tasks.length} ${tasks.length === 1 ? "task" : "tasks"}`}
              variant="outlined"
            />
          </Stack>

          {tasks.length === 0 ? (
            <Stack
              spacing={1}
              sx={{ alignItems: "center", textAlign: "center", py: 5 }}
            >
              <AssignmentOutlinedIcon
                sx={{ fontSize: 42, color: "text.disabled" }}
              />

              <Typography sx={{ fontWeight: 600 }}>
                You're all caught up!
              </Typography>

              <Typography variant="body2" color="text.secondary">
                You don't have any open tasks assigned to you.
              </Typography>
            </Stack>
          ) : (
            <Stack divider={<Divider flexItem />} spacing={0}>
              {tasks.map((task) => (
                <Stack
                  key={task.id}
                  direction={{ xs: "column", sm: "row" }}
                  spacing={1.5}
                  sx={{
                    alignItems: { xs: "flex-start", sm: "center" },
                    justifyContent: "space-between",
                    py: 2,
                  }}
                >
                  <Box sx={{ minWidth: 0, flex: 1 }}>
                    <Typography
                      sx={{
                        fontWeight: 650,
                        overflowWrap: "anywhere",
                      }}
                    >
                      {task.title}
                    </Typography>

                    <Typography
                      variant="caption"
                      color="text.secondary"
                      sx={{ display: "block", mt: 0.5 }}
                    >
                      Due: {formatDueDate(task.dueDate)}
                    </Typography>

                    <Stack
                      direction="row"
                      spacing={1}
                      useFlexGap
                      sx={{ mt: 1, flexWrap: "wrap" }}
                    >
                      <Chip
                        size="small"
                        label={statusLabels[task.status]}
                        variant="outlined"
                      />

                      <Chip
                        size="small"
                        label={task.priority}
                        color={priorityColors[task.priority]}
                      />
                    </Stack>
                  </Box>

                  <Chip
                    component={Link}
                    clickable
                    to={`/projects/${encodeURIComponent(task.projectId)}/tasks`}
                    label="Open project"
                    icon={<ArrowForwardOutlinedIcon />}
                    variant="outlined"
                    color="primary"
                    sx={{
                      flexShrink: 0,
                      textDecoration: "none",
                    }}
                  />
                </Stack>
              ))}
            </Stack>
          )}
        </Stack>
      </CardContent>
    </Card>
  );
}
