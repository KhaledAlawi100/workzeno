import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  type ReactNode,
} from "react";

import type { ProjectMembershipRole } from "../membership.types";

interface ProjectTeamContextValue {
  // Permission information for the current user.
  isOwner: boolean;
  isAdmin: boolean;
  canManageMembers: boolean;

  // Check whether an operation is currently running for a member.
  isRolePending: (userId: string) => boolean;
  isRemovePending: (userId: string) => boolean;

  // Actions for managing project members.
  changeRole: (userId: string, role: ProjectMembershipRole) => Promise<void>;

  removeMember: (userId: string) => Promise<void>;
}

interface ProjectTeamProviderProps {
  children: ReactNode;
  isOwner: boolean;
  isAdmin: boolean;
  canManageMembers: boolean;
  isRolePending: (userId: string) => boolean;
  isRemovePending: (userId: string) => boolean;
  onChangeRole: (userId: string, role: ProjectMembershipRole) => Promise<void>;
  onRemoveMember: (userId: string) => Promise<void>;
}

const ProjectTeamContext = createContext<ProjectTeamContextValue | undefined>(
  undefined,
);

export function ProjectTeamProvider({
  children,
  isOwner,
  isAdmin,
  canManageMembers,
  isRolePending,
  isRemovePending,
  onChangeRole,
  onRemoveMember,
}: ProjectTeamProviderProps) {
  // Keep the context value stable unless one of its dependencies changes.
  const changeRole = useCallback(
    (userId: string, role: ProjectMembershipRole) => {
      return onChangeRole(userId, role);
    },
    [onChangeRole],
  );

  const removeMember = useCallback(
    (userId: string) => {
      return onRemoveMember(userId);
    },
    [onRemoveMember],
  );

  const value = useMemo<ProjectTeamContextValue>(
    () => ({
      isOwner,
      isAdmin,
      canManageMembers,
      isRolePending,
      isRemovePending,
      changeRole,
      removeMember,
    }),
    [
      isOwner,
      isAdmin,
      canManageMembers,
      isRolePending,
      isRemovePending,
      changeRole,
      removeMember,
    ],
  );

  return (
    <ProjectTeamContext.Provider value={value}>
      {children}
    </ProjectTeamContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useProjectTeamContext() {
  const context = useContext(ProjectTeamContext);

  if (!context) {
    throw new Error(
      "useProjectTeamContext must be used inside ProjectTeamProvider",
    );
  }

  return context;
}
