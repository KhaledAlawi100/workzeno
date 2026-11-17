import type { Page } from "../../../types/page.types";

export type ProjectMembershipRole = "ADMIN" | "MEMBER";

export interface ProjectMember {
  userId: string;
  email: string;
  role: ProjectMembershipRole;
  joinedAt: string;
}

export interface ProjectOwner {
  userId: string;
  email: string;
}

/**
 * Response returned by the GET /projects/{projectId}/team endpoint.
 *
 * The owner is separate from memberships.
 * Members are returned as one page, not as a complete array.
 */
export interface ProjectTeamResponse {
  owner: ProjectOwner;
  members: Page<ProjectMember>;
}

export interface UpdateMemberRoleRequest {
  role: ProjectMembershipRole;
}

/**
 * Represents the authenticated user's membership in a project.
 *
 * The owner is not a membership, so ownership is checked separately.
 */
export interface MyProjectMembership {
  projectId: string;
  role: ProjectMembershipRole;
}
