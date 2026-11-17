import { useState } from "react";

import { getApiErrorMessage } from "../../../../lib/api";

import { useChangeMemberRole } from "./useChangeMemberRole";
import { useRemoveMember } from "./useRemoveMember";

import type { ProjectMembershipRole } from "../membership.types";

interface Notification {
  message: string;
  severity: "success" | "error";
}

export function useProjectTeamActions(projectId: string) {
  const changeRoleMutation = useChangeMemberRole(projectId);
  const removeMemberMutation = useRemoveMember(projectId);

  const [notification, setNotification] = useState<Notification | null>(null);

  async function handleChangeRole(
    userId: string,
    role: ProjectMembershipRole,
  ): Promise<void> {
    try {
      await changeRoleMutation.mutateAsync({ userId, role });

      setNotification({
        message: "Member role updated successfully.",
        severity: "success",
      });
    } catch (error) {
      setNotification({
        message: getApiErrorMessage(error),
        severity: "error",
      });

      throw error;
    }
  }

  async function handleRemoveMember(userId: string): Promise<void> {
    try {
      await removeMemberMutation.mutateAsync(userId);

      setNotification({
        message: "Member removed from the project successfully.",
        severity: "success",
      });
    } catch (error) {
      setNotification({
        message: getApiErrorMessage(error),
        severity: "error",
      });

      throw error;
    }
  }

  function handleCloseNotification() {
    setNotification(null);
  }

  return {
    notification,
    changeRoleMutation,
    removeMemberMutation,
    handleChangeRole,
    handleRemoveMember,
    handleCloseNotification,
  };
}
