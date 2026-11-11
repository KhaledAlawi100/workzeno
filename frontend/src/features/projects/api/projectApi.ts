import { apiClient, type ApiResponse } from "../../../lib/api";

import type {
  CreateProjectRequest,
  Page,
  Project,
  ProjectSummary,
  UpdateProjectRequest,
} from "../project.types";

export async function getProjects(
  page = 0,
  size = 20,
  signal?: AbortSignal,
): Promise<Page<ProjectSummary>> {
  const response = await apiClient.get<ApiResponse<Page<ProjectSummary>>>(
    "/projects",
    {
      params: {
        page,
        size,
      },
      signal,
    },
  );

  return response.data.data;
}

export async function createProject(
  request: CreateProjectRequest,
): Promise<Project> {
  const response = await apiClient.post<ApiResponse<Project>>(
    "/projects",
    request,
  );

  return response.data.data;
}


export async function getProjectById(
  projectId: string,
  signal?: AbortSignal,
): Promise<Project> {
  const response = await apiClient.get<ApiResponse<Project>>(
    `/projects/${encodeURIComponent(projectId)}`,
    {
      signal,
    },
  );

  return response.data.data;
}

export async function updateProject(
  projectId: string,
  request: UpdateProjectRequest,
): Promise<Project> {
  const response = await apiClient.patch<ApiResponse<Project>>(
    `/projects/${encodeURIComponent(projectId)}`,
    request,
  );

  return response.data.data;
}


export async function archiveProject(projectId: string): Promise<Project> {
  const response = await apiClient.patch<ApiResponse<Project>>(
    `/projects/${encodeURIComponent(projectId)}/archive`,
  );

  return response.data.data;
}


export async function restoreProject(projectId: string): Promise<Project> {
  const response = await apiClient.patch<ApiResponse<Project>>(
    `/projects/${encodeURIComponent(projectId)}/restore`,
  );

  return response.data.data;
}
