import { apiClient, type ApiResponse } from "../../../../lib/api";

import type { Page } from "../../../../types/page.types";
import type {
  AcceptInvitationResponse,
  CreateInvitationRequest,
  CreateInvitationResponse,
  MyInvitationResponse,
  ProjectInvitationResponse,
  ProjectInvitationSummaryResponse,
} from "../invitation.types";


export async function createInvitation(
  projectId: string,
  request: CreateInvitationRequest,
): Promise<CreateInvitationResponse> {
  const response = await apiClient.post<ApiResponse<CreateInvitationResponse>>(
    `/projects/${encodeURIComponent(projectId)}/invitations`,
    request,
  );

  return response.data.data;
}


export async function getProjectInvitations(
  projectId: string,
  page = 0,
  size = 20,
  signal?: AbortSignal,
): Promise<Page<ProjectInvitationSummaryResponse>> {
  const response = await apiClient.get<
    ApiResponse<Page<ProjectInvitationSummaryResponse>>
  >(`/projects/${encodeURIComponent(projectId)}/invitations`, {
    params: {
      page,
      size,
    },
    signal,
  });

  return response.data.data;
}

/**
 * Retrieves the full details of one project invitation.
 */
export async function getProjectInvitation(
  projectId: string,
  invitationId: string,
  signal?: AbortSignal,
): Promise<ProjectInvitationResponse> {
  const response = await apiClient.get<ApiResponse<ProjectInvitationResponse>>(
    `/projects/${encodeURIComponent(projectId)}/invitations/${encodeURIComponent(invitationId)}`,
    {
      signal,
    },
  );

  return response.data.data;
}

export async function revokeInvitation(
  projectId: string,
  invitationId: string,
): Promise<void> {
  const response = await apiClient.post<ApiResponse<void>>(
    `/projects/${encodeURIComponent(projectId)}/invitations/${encodeURIComponent(invitationId)}/revoke`,
  );

  return response.data.data;
}

export async function getMyInvitations(
  page = 0,
  size = 20,
  signal?: AbortSignal,
): Promise<Page<MyInvitationResponse>> {
  const response = await apiClient.get<ApiResponse<Page<MyInvitationResponse>>>(
    "/invitations/me",
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

export async function acceptInvitation(
  invitationId: string,
): Promise<AcceptInvitationResponse> {
  const response = await apiClient.post<ApiResponse<AcceptInvitationResponse>>(
    `/invitations/${encodeURIComponent(invitationId)}/accept`,
  );

  return response.data.data;
}

export async function declineInvitation(invitationId: string): Promise<void> {
  const response = await apiClient.post<ApiResponse<void>>(
    `/invitations/${encodeURIComponent(invitationId)}/decline`,
  );

  return response.data.data;
}
