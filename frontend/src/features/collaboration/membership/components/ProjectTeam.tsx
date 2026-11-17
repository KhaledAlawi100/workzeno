import { useState } from "react";
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
  const [page, setPage] = useState(0);
  const requestedPage = page;

  const {
    data: team,
    isPending,
    isError,
    error,
    refetch,
    isFetching,
  } = useProjectTeam(projectId, requestedPage, PAGE_SIZE);

  const {
    data: myMembership,
    isPending: isMembershipPending,
    isError: isMembershipError,
  } = useMyProjectMembership(projectId, !isOwner);

  const {
    notification,
    changeRoleMutation,
    removeMemberMutation,
    handleChangeRole,
    handleRemoveMember,
    handleCloseNotification,
  } = useProjectTeamActions(projectId);

  const isAdmin = myMembership?.role === "ADMIN";

  function handlePageChange(selectedPage: number) {
    setPage(selectedPage - 1);
  }

  const canVerifyPermissions =
    isOwner || (!isMembershipPending && !isMembershipError);

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

        {/* Membership permission status. */}
        {!isOwner && isMembershipPending && (
          <Typography variant="caption" color="text.secondary">
            Checking your project permissions...
          </Typography>
        )}

        {!isOwner && isMembershipError && (
          <Alert severity="warning">
            Your membership role could not be verified. Member management
            actions are unavailable until your permissions can be checked.
          </Alert>
        )}

        <ProjectOwnerCard owner={owner} />

        <ProjectMembersList
          members={members.content}
          isOwner={isOwner}
          isAdmin={isAdmin}
          canManageMembers={canManageMembers && canVerifyPermissions}
          isRolePending={(userId) =>
            changeRoleMutation.isPending &&
            changeRoleMutation.variables?.userId === userId
          }
          isRemovePending={(userId) =>
            removeMemberMutation.isPending &&
            removeMemberMutation.variables === userId
          }
          onChangeRole={handleChangeRole}
          onRemoveMember={handleRemoveMember}
        />

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
