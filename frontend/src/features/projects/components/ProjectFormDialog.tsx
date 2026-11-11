import { useMemo } from "react";

import { Dialog, DialogContent, DialogTitle, IconButton } from "@mui/material";

import CloseIcon from "@mui/icons-material/Close";

import ProjectForm from "./ProjectForm";

import type { ProjectSummary } from "../project.types";
import type { CreateProjectFormValues } from "../projectSchemas";

interface ProjectFormDialogProps {
  open: boolean;
  mode: "create" | "edit";
  project?: ProjectSummary | null;
  onClose: () => void;
  onSubmit: (values: CreateProjectFormValues) => Promise<void>;
}

export default function ProjectFormDialog({
  open,
  mode,
  project,
  onClose,
  onSubmit,
}: ProjectFormDialogProps) {
  
  const initialValues = useMemo(
    () => ({
      name: project?.name ?? "",
      description: project?.description ?? "",
    }),
    [project?.name, project?.description],
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
        {mode === "create" ? "Create project" : "Edit project"}

        <IconButton aria-label="Close dialog" onClick={onClose} edge="end">
          <CloseIcon />
        </IconButton>
      </DialogTitle>
      <DialogContent dividers>
        {open && (
          <ProjectForm
            mode={mode}
            initialValues={initialValues}
            onSubmit={onSubmit}
            onCancel={onClose}
          />
        )}
      </DialogContent>{" "}
    </Dialog>
  );
}
