import { createContext, useContext, useState, type ReactNode } from "react";

import type { TaskStatus } from "../types/task.types";

interface TaskBoardContextValue {
  canDrag: boolean;
  draggedTaskId: string | null;

  startDragging: (taskId: string) => void;
  endDragging: () => void;
  dropTask: (status: TaskStatus) => void;
  openTask: (taskId: string) => void;
}

const TaskBoardContext = createContext<TaskBoardContextValue | undefined>(
  undefined,
);

interface TaskBoardProviderProps {
  canDrag: boolean;
  onTaskClick: (taskId: string) => void;
  onTaskStatusChange: (taskId: string, status: TaskStatus) => void;
  children: ReactNode;
}

export function TaskBoardProvider({
  canDrag,
  onTaskClick,
  onTaskStatusChange,
  children,
}: TaskBoardProviderProps) {
  
    const [draggedTaskId, setDraggedTaskId] = useState<string | null>(null);

  function startDragging(taskId: string) {
    if (!canDrag) return;
    setDraggedTaskId(taskId);
  }

  function endDragging() {
    setDraggedTaskId(null);
  }

  function dropTask(status: TaskStatus) {
    if (!canDrag || !draggedTaskId) return;

    onTaskStatusChange(draggedTaskId, status);
    setDraggedTaskId(null);
  }

  function openTask(taskId: string) {
    onTaskClick(taskId);
  }

  const value: TaskBoardContextValue = {
    canDrag,
    draggedTaskId,
    startDragging,
    endDragging,
    dropTask,
    openTask,
  };

  return (
    <TaskBoardContext.Provider value={value}>
      {children}
    </TaskBoardContext.Provider>
  );
}


// eslint-disable-next-line react-refresh/only-export-components
export function useTaskBoard() {
  const context = useContext(TaskBoardContext);

  if (!context) {
    throw new Error("useTaskBoard must be used inside TaskBoardProvider");
  }

  return context;
}
