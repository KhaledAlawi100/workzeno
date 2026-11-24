import { useState } from "react";

import { getApiErrorMessage } from "../../../lib/api";

import {
  useCreateTask,
  useProjectTasks,
  useTask,
  useUpdateTask,
  useUpdateTaskStatus,
} from "./taskHooks";
import { useTaskPermissions } from "./useTaskPermissions";

import type { TaskFormValues } from "../taskSchemas";
import type {
  CreateTaskRequest,
  TaskStatus,
  UpdateTaskRequest,
} from "../types/task.types";

const PAGE_SIZE = 20;

/**
 * Coordinates the data and interactions used by ProjectTasksPage.
 * API behavior, caching, optimistic updates, and rollback remain in taskHooks.
 */
export function useProjectTasksPage(projectId: string | undefined) {
  const [page, setPage] = useState(0);
  const [formOpen, setFormOpen] = useState(false);
  const [selectedTaskId, setSelectedTaskId] = useState<string | null>(null);
  const [editingTask, setEditingTask] = useState(false);
  const [pageError, setPageError] = useState<string | null>(null);

  const safeProjectId = projectId ?? "";

  // Fetch the selected page of tasks and the current user's permissions.
  const tasksQuery = useProjectTasks(safeProjectId, page, PAGE_SIZE);
  const permissionsQuery = useTaskPermissions(projectId);

  // Fetch full task data only while a task is selected.
  const selectedTaskQuery = useTask(safeProjectId, selectedTaskId ?? "");

  const createMutation = useCreateTask(safeProjectId);
  const updateMutation = useUpdateTask(safeProjectId);
  const updateStatusMutation = useUpdateTaskStatus(safeProjectId);

  async function handleCreate(values: TaskFormValues) {
    setPageError(null);

    const request: CreateTaskRequest = {
      title: values.title,
      description: values.description || null,
      priority: values.priority,
      dueDate: values.dueDate || null,
    };

    try {
      await createMutation.mutateAsync(request);
      setFormOpen(false);

      // Newly created tasks should be visible from the first page.
      setPage(0);
    } catch (error) {
      setPageError(getApiErrorMessage(error));
      // Re-throw so the form can also handle the submission failure.
      throw error;
    }
  }

  async function handleUpdate(values: TaskFormValues) {
    const selectedTask = selectedTaskQuery.data;
    if (!selectedTaskId || !selectedTask) return;

    setPageError(null);

    const request: UpdateTaskRequest = {
      title: values.title,
      description: values.description || null,
      priority: values.priority,

      // Preserve the current status when editing task information.
      status: selectedTask.status,

      dueDate: values.dueDate || null,
    };

    try {
      await updateMutation.mutateAsync({ taskId: selectedTaskId, request });
      setEditingTask(false);
    } catch (error) {
      setPageError(getApiErrorMessage(error));
      throw error;
    }
  }

  /**
   * The mutation hook handles optimistic cache updates, backend persistence,
   * rollback on failure, and query invalidation.
   */
  async function handleTaskStatusChange(taskId: string, status: TaskStatus) {
    if (!permissionsQuery.permissions.canChangeTaskStatus) return;

    setPageError(null);

    try {
      await updateStatusMutation.mutateAsync({ taskId, request: { status } });
    } catch (error) {
      // The mutation hook already rolls back; this handler displays the error.
      setPageError(getApiErrorMessage(error));
    }
  }

  function openCreateDialog() {
    setPageError(null);
    setFormOpen(true);
  }

  function openTaskDetails(taskId: string) {
    setSelectedTaskId(taskId);
    setEditingTask(false);
  }

  function closeTaskDetails() {
    setSelectedTaskId(null);
    setEditingTask(false);
  }

  return {
    page,
    setPage,
    formOpen,
    setFormOpen,
    selectedTaskId,
    editingTask,
    setEditingTask,
    pageError,
    setPageError,
    tasksQuery,
    permissionsQuery,
    selectedTask: selectedTaskQuery.data,
    handleCreate,
    handleUpdate,
    handleTaskStatusChange,
    openCreateDialog,
    openTaskDetails,
    closeTaskDetails,
  };
}
