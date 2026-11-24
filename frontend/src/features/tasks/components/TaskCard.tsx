import {
  Card,
  CardActionArea,
  CardContent,
  Chip,
  Stack,
  Typography,
} from "@mui/material";

import CalendarTodayOutlinedIcon from "@mui/icons-material/CalendarTodayOutlined";

import type { TaskSummary } from "../types/task.types";

import { useTaskBoard } from "../context/TaskBoardContext";

interface TaskCardProps {
  task: TaskSummary;
}

const priorityColor = {
  LOW: "default",
  MEDIUM: "info",
  HIGH: "warning",
  URGENT: "error",
} as const;

export default function TaskCard({ task }: TaskCardProps) {
 
  const { canDrag, startDragging, endDragging, openTask } = useTaskBoard();

  const formattedDueDate = task.dueDate
    ? new Intl.DateTimeFormat(undefined, {
        day: "numeric",
        month: "short",
        year: "numeric",
      }).format(new Date(`${task.dueDate}T00:00:00`))
    : null;

  return (
    <Card
      variant="outlined"
      draggable={canDrag}
      onDragStart={(event) => {
        event.dataTransfer.effectAllowed = "move";
        event.dataTransfer.setData("text/plain", task.id);

        startDragging(task.id);
      }}
      onDragEnd={endDragging}
      sx={{
        borderRadius: 2,
        bgcolor: "background.paper",
        cursor: canDrag ? "grab" : "default",
        transition: "border-color 150ms ease, box-shadow 150ms ease",
        "&:hover": {
          borderColor: "primary.main",
          boxShadow: 1,
        },
        "&:active": {
          cursor: canDrag ? "grabbing" : "default",
        },
      }}
    >
      <CardActionArea
        onClick={() => openTask(task.id)}
        aria-label={`View task: ${task.title}`}
      >
        <CardContent
          sx={{
            p: 2,
            "&:last-child": { pb: 2 },
          }}
        >
          <Stack spacing={1.25}>
            <Chip
              size="small"
              label={task.priority}
              color={priorityColor[task.priority]}
              variant="outlined"
              sx={{ alignSelf: "flex-start" }}
            />

            <Typography
              variant="subtitle2"
              sx={{
                fontWeight: 650,
                overflowWrap: "anywhere",
              }}
            >
              {task.title}
            </Typography>

            {task.description && (
              <Typography
                variant="body2"
                color="text.secondary"
                sx={{
                  display: "-webkit-box",
                  WebkitBoxOrient: "vertical",
                  WebkitLineClamp: 2,
                  overflow: "hidden",
                  overflowWrap: "anywhere",
                }}
              >
                {task.description}
              </Typography>
            )}

            {formattedDueDate && (
              <Stack
                direction="row"
                spacing={0.75}
                sx={{ alignItems: "center" }}
              >
                <CalendarTodayOutlinedIcon
                  sx={{ fontSize: 15, color: "text.secondary" }}
                />

                <Typography variant="caption" color="text.secondary">
                  Due {formattedDueDate}
                </Typography>
              </Stack>
            )}
          </Stack>
        </CardContent>
      </CardActionArea>
    </Card>
  );
}
