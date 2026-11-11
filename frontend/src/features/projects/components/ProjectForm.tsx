import { Box, Button, Stack, TextField } from "@mui/material";

import { zodResolver } from "@hookform/resolvers/zod";
import { useEffect } from "react";
import { useForm } from "react-hook-form";

import {
  createProjectSchema,
  type CreateProjectFormValues,
} from "../projectSchemas";

interface ProjectFormProps {
  initialValues?: CreateProjectFormValues;
  mode: "create" | "edit";
  onSubmit: (values: CreateProjectFormValues) => Promise<void> | void;
  onCancel: () => void;
}

export default function ProjectForm({
  initialValues,
  mode,
  onSubmit,
  onCancel,
}: ProjectFormProps) {
  
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<CreateProjectFormValues>({
    resolver: zodResolver(createProjectSchema),
    defaultValues: initialValues ?? {
      name: "",
      description: "",
    },
  });

  // Update form values when switching between projects or modes.
  useEffect(() => {
    reset(
      initialValues ?? {
        name: "",
        description: "",
      },
    );
  }, [initialValues, reset]);

  return (
    <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
      <Stack spacing={2.5}>
        <TextField
          label="Project name"
          placeholder="e.g. Website redesign"
          fullWidth
          required
          {...register("name")}
          error={Boolean(errors.name)}
          helperText={errors.name?.message ?? "Enter a project name."}
          slotProps={{ htmlInput: { maxLength: 120 } }}
        />

        <TextField
          label="Description"
          placeholder="What is this project about?"
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
                ? "Create project"
                : "Save changes"}
          </Button>
        </Stack>
      </Stack>
    </Box>
  );
}
