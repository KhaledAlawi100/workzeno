import { apiClient, type ApiResponse } from "../../../../lib/api";

import type { Page } from "../../../../types/page.types";

import type {
  MyProjectMembership,
  ProjectMember,
  ProjectTeamResponse,
  UpdateMemberRoleRequest,
} from "../membership.types";

/**
 * Retrieves one page of project team information.
 *
 * The response contains:
 * - The project owner.
 * - A paginated list of project members.
 *
 * The AbortSignal allows React Query to cancel the request.
 */
export async function getProjectTeam(
  projectId: string,
  page = 0,
  size = 100,
  signal?: AbortSignal,
): Promise<ProjectTeamResponse> {
  const response = await apiClient.get<ApiResponse<ProjectTeamResponse>>(
    `/projects/${encodeURIComponent(projectId)}/team`,
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

/**
 * Retrieves one page of project members.
 *
 * Unlike getProjectTeam(), this endpoint returns only
 * the paginated membership records.
 */
export async function getProjectMembers(
  projectId: string,
  page = 0,
  size = 20,
  signal?: AbortSignal,
): Promise<Page<ProjectMember>> {
  const response = await apiClient.get<ApiResponse<Page<ProjectMember>>>(
    `/projects/${encodeURIComponent(projectId)}/members`,
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

export async function updateMemberRole(
  projectId: string,
  userId: string,
  request: UpdateMemberRoleRequest,
): Promise<void> {
  const response = await apiClient.patch<ApiResponse<void>>(
    `/projects/${encodeURIComponent(projectId)}/members/${encodeURIComponent(userId)}`,
    request,
  );

  return response.data.data;
}

export async function removeProjectMember(
  projectId: string,
  userId: string,
): Promise<void> {
  const response = await apiClient.delete<ApiResponse<void>>(
    `/projects/${encodeURIComponent(projectId)}/members/${encodeURIComponent(userId)}`,
  );

  return response.data.data;
}

export async function getMyProjectMembership(
  projectId: string,
  signal?: AbortSignal,
): Promise<MyProjectMembership> {
  const response = await apiClient.get<ApiResponse<MyProjectMembership>>(
    `/projects/${encodeURIComponent(projectId)}/my-membership`,
    {
      signal,
    },
  );

  return response.data.data;
}

export async function searchProjectMembers(
  projectId: string,
  query: string,
  limit = 10,
  signal?: AbortSignal,
): Promise<ProjectMember[]> {
  const response = await apiClient.get<ApiResponse<ProjectMember[]>>(
    `/projects/${encodeURIComponent(projectId)}/members/search`,
    {
      params: {
        q: query,
        limit,
      },
      signal,
    },
  );

  return response.data.data;
}
