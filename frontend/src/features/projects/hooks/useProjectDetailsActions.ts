import { useState } from "react";

import { getApiErrorMessage } from "../../../lib/api";

import { useUpdateProject } from "./useUpdateProject";
import { useProjectStatus } from "./useProjectStatus";

import type { Project } from "../project.types";
import type { CreateProjectFormValues } from "../projectSchemas";

interface Notification {
  message: string;
  severity: "success" | "error";
}

export function useProjectDetailsActions(project: Project | undefined) {
  // Controls whether the edit dialog is visible.
  const [editDialogOpen, setEditDialogOpen] = useState(false);

  // Controls whether the archive confirmation dialog is visible.
  const [archiveDialogOpen, setArchiveDialogOpen] = useState(false);

  // Stores success and error messages for the Snackbar.
  const [notification, setNotification] = useState<Notification | null>(null);

  // Project mutations.
  const updateProjectMutation = useUpdateProject();
  const { archiveMutation, restoreMutation } = useProjectStatus();

  // Prevent multiple project actions from running simultaneously.
  const isActionPending =
    updateProjectMutation.isPending ||
    archiveMutation.isPending ||
    restoreMutation.isPending;

  function handleOpenEditDialog() {
    if (!isActionPending) {
      setEditDialogOpen(true);
    }
  }

  function handleCloseEditDialog() {
    if (!isActionPending) {
      setEditDialogOpen(false);
    }
  }

  async function handleUpdateProject(
    values: CreateProjectFormValues,
  ): Promise<void> {
    if (!project) {
      return;
    }

    try {
      await updateProjectMutation.mutateAsync({
        projectId: project.id,
        request: {
          name: values.name,
          description: values.description || null,
        },
      });

      setEditDialogOpen(false);

      setNotification({
        message: "Project updated successfully.",
        severity: "success",
      });
    } catch (error) {
      setNotification({
        message: getApiErrorMessage(error),
        severity: "error",
      });
    }
  }

  function handleOpenArchiveDialog() {
    if (!isActionPending) {
      setArchiveDialogOpen(true);
    }
  }

  function handleCloseArchiveDialog() {
    if (!isActionPending) {
      setArchiveDialogOpen(false);
    }
  }

  async function handleArchiveProject(): Promise<void> {
    if (!project) {
      return;
    }

    try {
      await archiveMutation.mutateAsync({
        projectId: project.id,
      });

      setArchiveDialogOpen(false);

      setNotification({
        message: "Project archived successfully.",
        severity: "success",
      });
    } catch (error) {
      setNotification({
        message: getApiErrorMessage(error),
        severity: "error",
      });
    }
  }

  async function handleRestoreProject(): Promise<void> {
    if (!project) {
      return;
    }

    try {
      await restoreMutation.mutateAsync({
        projectId: project.id,
      });

      setNotification({
        message: "Project restored successfully.",
        severity: "success",
      });
    } catch (error) {
      setNotification({
        message: getApiErrorMessage(error),
        severity: "error",
      });
    }
  }

  function handleCloseNotification() {
    setNotification(null);
  }

  return {
    editDialogOpen,
    archiveDialogOpen,
    notification,
    isActionPending,
    isArchiving: archiveMutation.isPending,

    isRestoring: restoreMutation.isPending,

    handleOpenEditDialog,
    handleCloseEditDialog,
    handleUpdateProject,

    handleOpenArchiveDialog,
    handleCloseArchiveDialog,
    handleArchiveProject,
    handleRestoreProject,

    handleCloseNotification,
  };
}
