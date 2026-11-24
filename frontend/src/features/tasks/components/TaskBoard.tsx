import { Box } from "@mui/material";

import type { TaskStatus, TaskSummary } from "../types/task.types";

import { TaskBoardProvider } from "../context/TaskBoardContext";
import TaskColumn from "./TaskColumn";

interface TaskBoardProps {
  tasks: TaskSummary[];
  canChangeStatus: boolean;
  onTaskClick: (taskId: string) => void;
  onTaskStatusChange: (taskId: string, status: TaskStatus) => void;
}

const columns: ReadonlyArray<{
  title: string;
  status: TaskStatus;
}> = [
  { title: "To do", status: "TODO" },
  { title: "In progress", status: "IN_PROGRESS" },
  { title: "In review", status: "IN_REVIEW" },
  { title: "Done", status: "DONE" },
];

export default function TaskBoard({
  tasks,
  canChangeStatus,
  onTaskClick,
  onTaskStatusChange,
}: TaskBoardProps) {
  return (
    <TaskBoardProvider
      canDrag={canChangeStatus}
      onTaskClick={onTaskClick}
      onTaskStatusChange={onTaskStatusChange}
    >
      <Box
        sx={{
          display: "flex",
          gap: 2,
          overflowX: "auto",
          alignItems: "stretch",
          pb: 1,
        }}
      >
        {columns.map((column) => (
          <TaskColumn
            key={column.status}
            title={column.title}
            status={column.status}
            tasks={tasks.filter((task) => task.status === column.status)}
          />
        ))}
      </Box>
    </TaskBoardProvider>
  );
}
