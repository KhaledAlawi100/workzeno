import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import {
  createTaskComment,
  deleteTaskComment,
  getTaskComments,
  updateTaskComment,
} from "../api/taskCommentApi";

import { taskQueryKeys } from "../../task.queryKeys";

import type { CreateTaskCommentRequest } from "../types/task-comment.types";

export const COMMENT_PAGE_SIZE = 20;

export function useTaskComments(
  projectId: string,
  taskId: string,
  page = 0,
  size = COMMENT_PAGE_SIZE,
) {
  return useQuery({
    queryKey: taskQueryKeys.commentList(projectId, taskId, page, size),
    queryFn: () => getTaskComments(projectId, taskId, page, size),
    enabled: Boolean(projectId && taskId),
  });
}

function invalidateTaskComments(
  queryClient: ReturnType<typeof useQueryClient>,
  projectId: string,
  taskId: string,
) {
  return queryClient.invalidateQueries({
    queryKey: taskQueryKeys.taskComments(projectId, taskId),
  });
}

export function useCreateTaskComment(projectId: string, taskId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateTaskCommentRequest) =>
      createTaskComment(projectId, taskId, request),

    onSuccess: () => invalidateTaskComments(queryClient, projectId, taskId),
  });
}

export function useUpdateTaskComment(projectId: string, taskId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({
      commentId,
      request,
    }: {
      commentId: string;
      request: CreateTaskCommentRequest;
    }) => updateTaskComment(projectId, taskId, commentId, request),

    onSuccess: () => invalidateTaskComments(queryClient, projectId, taskId),
  });
}

export function useDeleteTaskComment(projectId: string, taskId: string) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (commentId: string) =>
      deleteTaskComment(projectId, taskId, commentId),

    onSuccess: () => invalidateTaskComments(queryClient, projectId, taskId),
  });
}
