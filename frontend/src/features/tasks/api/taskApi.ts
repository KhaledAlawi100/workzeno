import { apiClient, type ApiResponse } from "../../../lib/api";
import type { Page } from "../../../types/page.types";

import type {
  AssignTaskRequest,
  CreateTaskRequest,
  Task,
  TaskSummary,
  UpdateTaskRequest,
  UpdateTaskStatusRequest,
} from "../types/task.types";

const getTasksUrl = (projectId: string) =>
  `/projects/${encodeURIComponent(projectId)}/tasks`;

const getTaskUrl = (projectId: string, taskId: string) =>
  `${getTasksUrl(projectId)}/${encodeURIComponent(taskId)}`;

export async function createTask(
  projectId: string,
  request: CreateTaskRequest,
): Promise<Task> {
  const response = await apiClient.post<ApiResponse<Task>>(
    getTasksUrl(projectId),
    request,
  );

  return response.data.data;
}

export async function getProjectTasks(
  projectId: string,
  page = 0,
  size = 20,
): Promise<Page<TaskSummary>> {
  const response = await apiClient.get<ApiResponse<Page<TaskSummary>>>(
    getTasksUrl(projectId),
    { params: { page, size } },
  );

  return response.data.data;
}

export async function getTaskById(
  projectId: string,
  taskId: string,
): Promise<Task> {
  const response = await apiClient.get<ApiResponse<Task>>(
    getTaskUrl(projectId, taskId),
  );

  return response.data.data;
}

export async function updateTask(
  projectId: string,
  taskId: string,
  request: UpdateTaskRequest,
): Promise<Task> {
  const response = await apiClient.put<ApiResponse<Task>>(
    getTaskUrl(projectId, taskId),
    request,
  );

  return response.data.data;
}

export async function deleteTask(
  projectId: string,
  taskId: string,
): Promise<void> {
  await apiClient.delete(getTaskUrl(projectId, taskId));
}

export async function assignTask(
  projectId: string,
  taskId: string,
  request: AssignTaskRequest,
): Promise<Task> {
  const response = await apiClient.post<ApiResponse<Task>>(
    `${getTaskUrl(projectId, taskId)}/assignees`,
    request,
  );

  return response.data.data;
}

export async function unassignTask(
  projectId: string,
  taskId: string,
  userId: string,
): Promise<Task> {
  const response = await apiClient.delete<ApiResponse<Task>>(
    `${getTaskUrl(projectId, taskId)}/assignees/${encodeURIComponent(userId)}`,
  );

  return response.data.data;
}

export async function updateTaskStatus(
  projectId: string,
  taskId: string,
  request: UpdateTaskStatusRequest,
): Promise<Task> {
  const response = await apiClient.patch<ApiResponse<Task>>(
    `${getTaskUrl(projectId, taskId)}/status`,
    request,
  );

  return response.data.data;
}
