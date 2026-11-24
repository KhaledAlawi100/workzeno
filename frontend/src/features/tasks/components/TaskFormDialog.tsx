import { useMemo } from "react";

import { Dialog, DialogContent, DialogTitle, IconButton } from "@mui/material";

import CloseIcon from "@mui/icons-material/Close";

import type { Task } from "../types/task.types";
import type { TaskFormValues } from "../taskSchemas";

import TaskForm from "./TaskForm";

interface TaskFormDialogProps {
  open: boolean;
  mode: "create" | "edit";
  task?: Task | null;
  onClose: () => void;
  onSubmit: (values: TaskFormValues) => Promise<void>;
}

export default function TaskFormDialog({
  open,
  mode,
  task,
  onClose,
  onSubmit,
}: TaskFormDialogProps) {
  const initialValues = useMemo<TaskFormValues>(
    () => ({
      title: task?.title ?? "",
      description: task?.description ?? "",
      priority: task?.priority ?? "MEDIUM",
      dueDate: task?.dueDate ?? "",
    }),
    [task],
  );

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle
        sx={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
        }}
      >
        {mode === "create" ? "Create task" : "Edit task"}

        <IconButton aria-label="Close dialog" onClick={onClose} edge="end">
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent dividers>
        {open && (
          <TaskForm
            mode={mode}
            initialValues={initialValues}
            onSubmit={onSubmit}
            onCancel={onClose}
          />
        )}
      </DialogContent>
    </Dialog>
  );
}
