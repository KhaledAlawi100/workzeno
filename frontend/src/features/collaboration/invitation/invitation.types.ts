import type { Page } from "../../../types/page.types";


export type InvitationRole = "ADMIN" | "MEMBER";


export type ProjectInvitationStatus =
  | "PENDING"
  | "ACCEPTED"
  | "DECLINED"
  | "REVOKED"
  | "EXPIRED";


  export interface CreateInvitationRequest {
  inviteeEmail: string;
  role: InvitationRole;
}



export interface CreateInvitationResponse {
  invitationId: string;
  inviteeEmail: string;
  role: InvitationRole;
  expiresAt: string;
}


export interface ProjectInvitationSummaryResponse {
  invitationId: string;
  inviteeEmail: string;
  role: InvitationRole;
  status: ProjectInvitationStatus;
  invitedByEmail: string;
  createdAt: string;
  expiresAt: string;
}


export interface ProjectInvitationResponse extends ProjectInvitationSummaryResponse {
  acceptedAt: string | null;
  declinedAt: string | null;
  revokedAt: string | null;
  revokedByEmail: string | null;
}


export interface MyInvitationResponse {
  invitationId: string;
  projectId: string;
  projectName: string;
  invitedByEmail: string;
  role: InvitationRole;
  createdAt: string;
  expiresAt: string;
}


export interface AcceptInvitationResponse {
  invitationId: string;
  projectId: string;
  projectName: string;
  role: InvitationRole;
  acceptedAt: string;
}

/**
 * Helpful aliases for the paginated backend responses.
 */
export type ProjectInvitationsPage = Page<ProjectInvitationSummaryResponse>;

export type MyInvitationsPage = Page<MyInvitationResponse>;
