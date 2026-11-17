import { useMyProjectMembership } from "../../collaboration/membership/hooks/useMyProjectMembership";
import type { Project } from "../project.types";

export function useProjectDetailsPermissions(
  project: Project | undefined,
  projectId: string | undefined,
  currentUserId: string | undefined,
) {
  // The owner is stored directly on the Project.
  // The owner does not have a ProjectMembership record.
  const isOwner = Boolean(
    project && currentUserId && project.ownerId === currentUserId,
  );

  // Only non-owners need to retrieve their membership role.
  const shouldLoadMembership = Boolean(project && currentUserId && !isOwner);

  const {
    data: myMembership,
    isPending: isMembershipPending,
    isError: isMembershipError,
    refetch: refetchMembership,
  } = useMyProjectMembership(projectId, shouldLoadMembership);

  // Only owners and ADMIN members can edit project details.
  const isAdmin = myMembership?.role === "ADMIN";
  const canEditProject = isOwner || isAdmin;

  // Only the owner can archive or restore the project.
  const canManageProjectStatus = isOwner;

  // Do not show management actions until membership is verified.
  const canShowManagementActions =
    !shouldLoadMembership || (!isMembershipPending && !isMembershipError);

  return {
    isOwner,
    isAdmin,
    shouldLoadMembership,
    isMembershipPending,
    isMembershipError,
    refetchMembership,
    canEditProject,
    canManageProjectStatus,
    canShowManagementActions,
  };
}
