import type { Project } from "../../projects/project.types";
import type { ProjectMembershipRole } from "../../collaboration/membership/membership.types";

export interface TaskPermissions {
  canViewTasks: boolean;
  canCreateTasks: boolean;
  canEditTasks: boolean;
  canAssignTasks: boolean;
  canChangeTaskStatus: boolean;
  canManageComments: boolean;
  canDeleteTasks: boolean;
}

export function getTaskPermissions(
  project: Project | undefined,
  currentUserId: string | undefined,
  membershipRole: ProjectMembershipRole | undefined,
): TaskPermissions {
  const isOwner = Boolean(
    project && currentUserId && project.ownerId === currentUserId,
  );

  const isAdmin = membershipRole === "ADMIN";
  const isProjectMember = isOwner || isAdmin || membershipRole === "MEMBER";

  return {
    canViewTasks: isProjectMember,
    canCreateTasks: isProjectMember,
    canEditTasks: isProjectMember,
    canAssignTasks: isProjectMember,
    canChangeTaskStatus: isProjectMember,
    canManageComments: isProjectMember,

    // Only the owner and admins can delete tasks.
    canDeleteTasks: isOwner || isAdmin,
  };
}
