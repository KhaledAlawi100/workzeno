import { useEffect } from "react";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { Box, Button, Stack, TextField } from "@mui/material";

import { taskFormSchema, type TaskFormValues } from "../taskSchemas";

import type { TaskPriority } from "../types/task.types";

import TaskPrioritySelect from "./TaskPrioritySelect";

interface TaskFormProps {
  mode: "create" | "edit";
  initialValues?: TaskFormValues;
  onSubmit: (values: TaskFormValues) => Promise<void>;
  onCancel: () => void;
}

const emptyValues: TaskFormValues = {
  title: "",
  description: "",
  priority: "MEDIUM",
  dueDate: "",
};

/**
 * Returns today's date in YYYY-MM-DD format using local time.
 *
 * We avoid toISOString() here because it converts to UTC,
 * which can produce the wrong calendar date in some time zones.
 */
function getTodayDate(): string {
  const today = new Date();

  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, "0");
  const day = String(today.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

export default function TaskForm({
  mode,
  initialValues,
  onSubmit,
  onCancel,
}: TaskFormProps) {
  // Calculate the earliest allowed due date.
  const today = getTodayDate();

  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors, isSubmitting },
  } = useForm<TaskFormValues>({
    resolver: zodResolver(taskFormSchema),
    defaultValues: initialValues ?? emptyValues,
  });

  // Reset the form when the selected task or form mode changes.
  useEffect(() => {
    reset(initialValues ?? emptyValues);
  }, [initialValues, reset]);

  return (
    <Box component="form" noValidate onSubmit={handleSubmit(onSubmit)}>
      <Stack spacing={2.5}>
        <TextField
          label="Task title"
          placeholder="e.g. Implement authentication"
          required
          fullWidth
          {...register("title")}
          error={Boolean(errors.title)}
          helperText={errors.title?.message ?? "Maximum 200 characters."}
          slotProps={{ htmlInput: { maxLength: 200 } }}
        />

        <TextField
          label="Description"
          placeholder="Describe what needs to be done..."
          fullWidth
          multiline
          minRows={4}
          {...register("description")}
          error={Boolean(errors.description)}
          helperText={
            errors.description?.message ?? "Optional. Maximum 2000 characters."
          }
          slotProps={{ htmlInput: { maxLength: 2000 } }}
        />

        <Controller
          name="priority"
          control={control}
          render={({ field }) => (
            <TaskPrioritySelect
              value={field.value as TaskPriority}
              onChange={field.onChange}
              error={errors.priority?.message}
              disabled={isSubmitting}
            />
          )}
        />

        {/* 
          The min attribute prevents selecting a date before today.
          Zod also validates the date in case the user types manually.
        */}
        <TextField
          label="Due date"
          type="date"
          fullWidth
          disabled={isSubmitting}
          slotProps={{
            inputLabel: { shrink: true },
            htmlInput: {
              min: today,
            },
          }}
          {...register("dueDate")}
          error={Boolean(errors.dueDate)}
          helperText={
            errors.dueDate?.message ?? "Optional. Today or a future date."
          }
        />

        <Stack direction="row" spacing={2} sx={{ justifyContent: "flex-end" }}>
          <Button
            variant="outlined"
            color="inherit"
            onClick={onCancel}
            disabled={isSubmitting}
          >
            Cancel
          </Button>

          <Button type="submit" variant="contained" disabled={isSubmitting}>
            {isSubmitting
              ? "Saving..."
              : mode === "create"
                ? "Create task"
                : "Save changes"}
          </Button>
        </Stack>
      </Stack>
    </Box>
  );
}
