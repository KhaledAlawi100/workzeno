import { useAuth } from "../../auth/AuthContext";
import { useMyProjectMembership } from "../../collaboration/membership/hooks/useMyProjectMembership";
import { useProject } from "../../projects/hooks/useProject";

import { getTaskPermissions } from "../permissions/taskPermissions";

/**
 * Loads the project and the current user's membership,
 * then derives the permissions for task actions.
 */
export function useTaskPermissions(projectId: string | undefined) {
  const { user, status } = useAuth();

  const {
    data: project,
    isPending: isProjectPending,
    isError: isProjectError,
  } = useProject(projectId);

  const isOwner = Boolean(project && user?.id && project.ownerId === user.id);

  // Owners have no membership record, so do not request one.
  const shouldLoadMembership = Boolean(project && user?.id && !isOwner);

  const {
    data: membership,
    isPending: isMembershipPending,
    isError: isMembershipError,
    refetch: refetchMembership,
  } = useMyProjectMembership(projectId, shouldLoadMembership);

  const permissions = getTaskPermissions(project, user?.id, membership?.role);

  const isLoading =
    status === "loading" ||
    isProjectPending ||
    (shouldLoadMembership && isMembershipPending);

  const isError = isProjectError || (shouldLoadMembership && isMembershipError);

  return {
    project,
    membership,
    permissions,
    isLoading,
    isError,
    refetchMembership,
  };
}
