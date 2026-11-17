/**
 * Centralized React Query keys for the collaboration feature.
 *
 * Keeping keys in one place prevents spelling differences
 * and makes cache invalidation predictable.
 */
export const collaborationQueryKeys = {
  
  projectTeam: (projectId: string) => ["project-team", projectId] as const,

 
  projectMembers: (projectId: string) =>
    ["project-members", projectId] as const,

  projectMembersPage: (projectId: string, page: number, size: number) =>
    ["project-members", projectId, { page, size }] as const,

  
  projectInvitations: (projectId: string) =>
    ["project-invitations", projectId] as const,

  projectInvitationsPage: (projectId: string, page: number, size: number) =>
    ["project-invitations", projectId, { page, size }] as const,

  
  projectInvitation: (projectId: string, invitationId: string) =>
    ["project-invitation", projectId, invitationId] as const,

  
  myInvitations: () => ["my-invitations"] as const,

  myInvitationsPage: (page: number, size: number) =>
    ["my-invitations", { page, size }] as const,
};
