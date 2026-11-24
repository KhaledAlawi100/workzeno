import { useCallback, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Pagination,
  Snackbar,
  Stack,
  Typography,
} from "@mui/material";

import RefreshIcon from "@mui/icons-material/Refresh";
import { getApiErrorMessage } from "../../../../lib/api";

import { useProjectTeam } from "../hooks/useProjectTeam";
import { useMyProjectMembership } from "../hooks/useMyProjectMembership";
import { useProjectTeamActions } from "../hooks/useProjectTeamActions";

import { ProjectTeamProvider } from "../context/ProjectTeamContext";

import ProjectTeamHeader from "./details/ProjectTeamHeader";
import ProjectOwnerCard from "./details/ProjectOwnerCard";
import ProjectMembersList from "./details/ProjectMembersList";
import ProjectTeamLoading from "./ProjectTeamLoading";

const PAGE_SIZE = 6;

interface ProjectTeamProps {
  projectId: string;
  isOwner: boolean;
  canManageMembers: boolean;
}

export default function ProjectTeam({
  projectId,
  isOwner,
  canManageMembers,
}: ProjectTeamProps) {
  // MUI Pagination uses one-based page numbers.
  // The backend pagination uses zero-based page numbers.
  const [page, setPage] = useState(0);

  const requestedPage = page;

  // Fetch the project owner and the requested page of project members.
  const {
    data: team,
    isPending,
    isError,
    error,
    refetch,
    isFetching,
  } = useProjectTeam(projectId, requestedPage, PAGE_SIZE);

  // Fetch the current user's membership to determine their role.
  // Owners do not need this request because their permissions are already known.
  const {
    data: myMembership,
    isPending: isMembershipPending,
    isError: isMembershipError,
  } = useMyProjectMembership(projectId, !isOwner);

  // Keep all project-team mutations and their notifications in one hook.
  const {
    notification,
    changeRoleMutation,
    removeMemberMutation,
    handleChangeRole,
    handleRemoveMember,
    handleCloseNotification,
  } = useProjectTeamActions(projectId);

  // Determine whether the current user is an administrator.
  const isAdmin = myMembership?.role === "ADMIN";

  // Convert MUI's one-based page number to the backend's zero-based page number.
  function handlePageChange(selectedPage: number) {
    setPage(selectedPage - 1);
  }

  // Do not allow member-management actions until the current user's
  // membership permissions have been successfully verified.
  // The project owner is already trusted and does not need this check.
  const canVerifyPermissions =
    isOwner || (!isMembershipPending && !isMembershipError);

  // Check whether a role change is currently pending for a specific member.
  // useCallback keeps this function reference stable between renders
  // unless the mutation's pending state or variables change.
  const isRolePending = useCallback(
    (userId: string) =>
      changeRoleMutation.isPending &&
      changeRoleMutation.variables?.userId === userId,
    [changeRoleMutation.isPending, changeRoleMutation.variables?.userId],
  );

  // Check whether member removal is currently pending for a specific member.
  const isRemovePending = useCallback(
    (userId: string) =>
      removeMemberMutation.isPending &&
      removeMemberMutation.variables === userId,
    [removeMemberMutation.isPending, removeMemberMutation.variables],
  );

  if (isPending) {
    return <ProjectTeamLoading pageSize={PAGE_SIZE} />;
  }

  if (isError) {
    return (
      <Alert
        severity="error"
        action={
          <Button
            color="inherit"
            size="small"
            startIcon={<RefreshIcon />}
            onClick={() => {
              void refetch();
            }}
          >
            Retry
          </Button>
        }
      >
        {getApiErrorMessage(error)}
      </Alert>
    );
  }

  if (!team) {
    return (
      <Alert severity="warning">Project team information is unavailable.</Alert>
    );
  }

  const { owner, members } = team;

  return (
    <>
      <Stack spacing={3}>
        {/* Team title and total number of members. */}
        <ProjectTeamHeader totalMembers={members.totalElements} />

        {/* Background page refresh indicator. */}
        {isFetching && (
          <Typography variant="caption" color="text.secondary">
            Updating team information...
          </Typography>
        )}

        {/* Show a status message while checking the current user's role. */}
        {!isOwner && isMembershipPending && (
          <Typography variant="caption" color="text.secondary">
            Checking your project permissions...
          </Typography>
        )}

        {/* Prevent member-management actions when permissions cannot be verified. */}
        {!isOwner && isMembershipError && (
          <Alert severity="warning">
            Your membership role could not be verified. Member management
            actions are unavailable until your permissions can be checked.
          </Alert>
        )}

        {/* The owner card does not need the membership context. */}
        <ProjectOwnerCard owner={owner} />

        {/* 
          Provide shared membership state and actions to the member list
          and its descendants.

          ProjectMembersList, ProjectMemberCard, and MemberRoleMenu
          can now access these values through useProjectTeamContext().
        */}
        <ProjectTeamProvider
          isOwner={isOwner}
          isAdmin={isAdmin}
          canManageMembers={canManageMembers && canVerifyPermissions}
          isRolePending={isRolePending}
          isRemovePending={isRemovePending}
          onChangeRole={handleChangeRole}
          onRemoveMember={handleRemoveMember}
        >
          <ProjectMembersList members={members.content} />
        </ProjectTeamProvider>

        {/* Backend pagination: convert zero-based page to MUI's one-based page. */}
        {members.totalPages > 1 && (
          <Box
            sx={{
              display: "flex",
              justifyContent: "center",
              pt: 1,
            }}
          >
            <Pagination
              count={members.totalPages}
              page={page + 1}
              onChange={(_, selectedPage) => handlePageChange(selectedPage)}
              color="primary"
              shape="rounded"
              showFirstButton
              showLastButton
              disabled={isFetching}
            />
          </Box>
        )}

        {/* Total member count, independent of the current page. */}
        <Typography
          variant="body2"
          color="text.secondary"
          sx={{ textAlign: "center" }}
        >
          {members.totalElements}{" "}
          {members.totalElements === 1 ? "member" : "members"} in total
        </Typography>
      </Stack>

      {/* Feedback after role changes and member removals. */}
      <Snackbar
        open={notification !== null}
        autoHideDuration={5000}
        onClose={handleCloseNotification}
        anchorOrigin={{
          vertical: "bottom",
          horizontal: "right",
        }}
      >
        {notification ? (
          <Alert
            severity={notification.severity}
            variant="filled"
            onClose={handleCloseNotification}
            sx={{ width: "100%" }}
          >
            {notification.message}
          </Alert>
        ) : undefined}
      </Snackbar>
    </>
  );
}
