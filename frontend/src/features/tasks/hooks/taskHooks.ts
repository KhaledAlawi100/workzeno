
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import {
    assignTask,
  createTask,
  deleteTask,
  getProjectTasks,
  getTaskById,
  unassignTask,
  updateTask,
  updateTaskStatus,
} from "../api/taskApi";

import type { Page } from "../../../types/page.types";

import type { Task, TaskSummary } from "../types/task.types";

import { taskQueryKeys } from "../task.queryKeys";

import type {
    AssignTaskRequest,
  CreateTaskRequest,
  UpdateTaskRequest,
  UpdateTaskStatusRequest,
} from "../types/task.types";

// ============================================================
// 1. GET PROJECT TASKS
// ============================================================

export const useProjectTasks = (projectId: string, page = 0, size = 20) => {
  return useQuery({
    queryKey: taskQueryKeys.list(projectId, page, size),

    enabled: Boolean(projectId),

    queryFn: () => getProjectTasks(projectId, page, size),
  });
};

// ============================================================
// 2. GET INDIVIDUAL TASK
// ============================================================

export const useTask = (projectId: string, taskId: string) => {
  return useQuery({
    queryKey: taskQueryKeys.detail(projectId, taskId),

    // Prevent requests when required identifiers are missing.
    enabled: Boolean(projectId && taskId),

    queryFn: () => getTaskById(projectId, taskId),
  });
};

// ============================================================
// 3. CREATE TASK
// ============================================================

export const useCreateTask = (projectId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateTaskRequest) => createTask(projectId, request),

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: taskQueryKeys.lists(projectId),
      });
    },
  });
};

// ============================================================
// 4. UPDATE TASK
// ============================================================

export const useUpdateTask = (projectId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      taskId,
      request,
    }: {
      taskId: string;
      request: UpdateTaskRequest;
    }) => updateTask(projectId, taskId, request),

    onSuccess: async (_data, variables) => {
      // Refresh all paginated task lists for this project.
      await queryClient.invalidateQueries({
        queryKey: taskQueryKeys.lists(projectId),
      });

      // Refresh the updated task's detail cache.
      await queryClient.invalidateQueries({
        queryKey: taskQueryKeys.detail(projectId, variables.taskId),
      });
    },
  });
};

// ============================================================
// 5. DELETE TASK
// ============================================================

export const useDeleteTask = (projectId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (taskId: string) => deleteTask(projectId, taskId),

    onSuccess: async (_data, taskId) => {
      // Refresh project task lists.
      await queryClient.invalidateQueries({
        queryKey: taskQueryKeys.lists(projectId),
      });

      // The deleted task should no longer exist in the cache.
      queryClient.removeQueries({
        queryKey: taskQueryKeys.detail(projectId, taskId),
        exact: true,
      });
    },
  });
};

// ============================================================
// 6. UPDATE TASK STATUS
// ============================================================

export const useUpdateTaskStatus = (projectId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      taskId,
      request,
    }: {
      taskId: string;
      request: UpdateTaskStatusRequest;
    }) => updateTaskStatus(projectId, taskId, request),

    onMutate: async ({ taskId, request }) => {
      const listQueryKey = taskQueryKeys.lists(projectId);
      const detailQueryKey = taskQueryKeys.detail(projectId, taskId);

      await Promise.all([
        queryClient.cancelQueries({ queryKey: listQueryKey }),
        queryClient.cancelQueries({ queryKey: detailQueryKey }),
      ]);

      const previousLists = queryClient.getQueriesData<Page<TaskSummary>>({
        queryKey: listQueryKey,
      });

      const previousDetail = queryClient.getQueryData<Task>(detailQueryKey);

      queryClient.setQueriesData<Page<TaskSummary>>(
        { queryKey: listQueryKey },
        (oldPage) => {
          if (!oldPage) return oldPage;

          return {
            ...oldPage,
            content: oldPage.content.map((task) =>
              task.id === taskId ? { ...task, status: request.status } : task,
            ),
          };
        },
      );

      // Keep the individual task details cache in sync.
      queryClient.setQueryData<Task>(detailQueryKey, (oldTask) =>
        oldTask ? { ...oldTask, status: request.status } : oldTask,
      );

      return {
        previousLists,
        previousDetail,
        detailQueryKey,
      };
    },

    // Restore the old data if the backend rejects the update.
    onError: (_error, _variables, context) => {
      if (!context) return;

      context.previousLists.forEach(([queryKey, previousData]) => {
        queryClient.setQueryData(queryKey, previousData);
      });

      queryClient.setQueryData(context.detailQueryKey, context.previousDetail);
    },

    // Always refresh relevant queries after success or failure.
    onSettled: async (_data, _error, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: taskQueryKeys.lists(projectId),
        }),
        queryClient.invalidateQueries({
          queryKey: taskQueryKeys.detail(projectId, variables.taskId),
        }),
      ]);
    },
  });
};

/**
 * Assigns a user to a task.
 *
 * After assignment, refresh the project task lists and
 * the affected task detail query.
 */
export const useAssignTask = (projectId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      taskId,
      request,
    }: {
      taskId: string;
      request: AssignTaskRequest;
    }) => assignTask(projectId, taskId, request),

    onSuccess: async (_data, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: taskQueryKeys.lists(projectId),
        }),
        queryClient.invalidateQueries({
          queryKey: taskQueryKeys.detail(
            projectId,
            variables.taskId,
          ),
        }),
      ]);
    },
  });
};

/**
 * Removes a user from a task.
 *
 * We invalidate task lists and task details because the
 * assignee data has changed.
 */
export const useUnassignTask = (projectId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      taskId,
      userId,
    }: {
      taskId: string;
      userId: string;
    }) => unassignTask(projectId, taskId, userId),

    onSuccess: async (_data, variables) => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: taskQueryKeys.lists(projectId),
        }),
        queryClient.invalidateQueries({
          queryKey: taskQueryKeys.detail(
            projectId,
            variables.taskId,
          ),
        }),
      ]);
    },
  });
};

