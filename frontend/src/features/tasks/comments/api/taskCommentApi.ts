
import {
  apiClient,
  type ApiResponse,
} from "../../../../lib/api";

import type { Page } from "../../../../types/page.types";

import type {
  CreateTaskCommentRequest,
  TaskComment,
} from "../types/task-comment.types";

const getTaskCommentsUrl = (
  projectId: string,
  taskId: string,
): string =>
  `/projects/${encodeURIComponent(projectId)}/tasks/${encodeURIComponent(taskId)}/comments`;


  export async function createTaskComment(
  projectId: string,
  taskId: string,
  request: CreateTaskCommentRequest,
): Promise<TaskComment> {
  const response = await apiClient.post<ApiResponse<TaskComment>>(
    getTaskCommentsUrl(projectId, taskId),
    request,
  );

  return response.data.data;
}

export async function getTaskComments(
  projectId: string,
  taskId: string,
  page = 0,
  size = 20,
): Promise<Page<TaskComment>> {
  const response = await apiClient.get<
    ApiResponse<Page<TaskComment>>
  >(getTaskCommentsUrl(projectId, taskId), {
    params: { page, size },
  });

  return response.data.data;
}


export async function updateTaskComment(
  projectId: string,
  taskId: string,
  commentId: string,
  request: CreateTaskCommentRequest,
): Promise<TaskComment> {
  const response = await apiClient.put<ApiResponse<TaskComment>>(
    `${getTaskCommentsUrl(projectId, taskId)}/${encodeURIComponent(commentId)}`,
    request,
  );

  return response.data.data;
}

export async function deleteTaskComment(
  projectId: string,
  taskId: string,
  commentId: string,
): Promise<void> {
  await apiClient.delete(
    `${getTaskCommentsUrl(projectId, taskId)}/${encodeURIComponent(commentId)}`,
  );
}
