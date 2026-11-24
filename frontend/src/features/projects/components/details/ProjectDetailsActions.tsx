import { Link } from "react-router-dom";

import { Alert, Button, Stack, Typography } from "@mui/material";

import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import ArchiveOutlinedIcon from "@mui/icons-material/ArchiveOutlined";
import RestoreOutlinedIcon from "@mui/icons-material/RestoreOutlined";
import GroupsOutlinedIcon from "@mui/icons-material/GroupsOutlined";
import TaskAltOutlinedIcon from "@mui/icons-material/TaskAltOutlined";

interface ProjectDetailsActionsProps {
  projectId: string;
  isActive: boolean;

  shouldLoadMembership: boolean;
  isMembershipPending: boolean;
  isMembershipError: boolean;
  refetchMembership: () => unknown;

  canShowManagementActions: boolean;
  canEditProject: boolean;
  canManageProjectStatus: boolean;

  isActionPending: boolean;
  isRestoring: boolean;

  onEdit: () => void;
  onArchive: () => void;
  onRestore: () => void;
}

export default function ProjectDetailsActions({
  projectId,
  isActive,
  shouldLoadMembership,
  isMembershipPending,
  isMembershipError,
  refetchMembership,
  canShowManagementActions,
  canEditProject,
  canManageProjectStatus,
  isActionPending,
  isRestoring,
  onEdit,
  onArchive,
  onRestore,
}: ProjectDetailsActionsProps) {
  return (
    <Stack
      direction="column"
      spacing={1}
      sx={{
        width: { xs: "100%", sm: "auto" },
      }}
    >
      {/* Every project viewer can access the team page. */}
      <Button
        component={Link}
        to={`/projects/${encodeURIComponent(projectId)}/members`}
        variant="outlined"
        startIcon={<GroupsOutlinedIcon />}
        fullWidth
      >
        View team
      </Button>

      {/* Every project viewer can access the project's task board. */}
      <Button
        component={Link}
        to={`/projects/${encodeURIComponent(projectId)}/tasks`}
        variant="outlined"
        startIcon={<TaskAltOutlinedIcon />}
        fullWidth
      >
        View tasks
      </Button>

      {/* Show feedback while the current user's role is being checked. */}
      {shouldLoadMembership && isMembershipPending && (
        <Typography
          variant="body2"
          color="text.secondary"
          sx={{ alignSelf: "center" }}
        >
          Checking project permissions...
        </Typography>
      )}

      {/* Fail closed if membership cannot be verified. */}
      {shouldLoadMembership && isMembershipError && (
        <Alert
          severity="warning"
          action={
            <Button
              color="inherit"
              size="small"
              onClick={() => {
                void refetchMembership();
              }}
            >
              Retry
            </Button>
          }
        >
          Unable to verify your project permissions. Management actions are
          temporarily unavailable.
        </Alert>
      )}

      {/* Display management actions only after permissions are verified. */}
      {canShowManagementActions && (
        <>
          {/* Owners and admins can edit project details. */}
          {canEditProject && (
            <Button
              variant="contained"
              startIcon={<EditOutlinedIcon />}
              onClick={onEdit}
              disabled={isActionPending}
            >
              Edit project
            </Button>
          )}

          {/* Only the owner can archive or restore the project. */}
          {canManageProjectStatus &&
            (isActive ? (
              <Button
                variant="outlined"
                color="warning"
                startIcon={<ArchiveOutlinedIcon />}
                onClick={onArchive}
                disabled={isActionPending}
              >
                Archive project
              </Button>
            ) : (
              <Button
                variant="contained"
                color="success"
                startIcon={<RestoreOutlinedIcon />}
                onClick={onRestore}
                disabled={isActionPending}
              >
                {isRestoring ? "Restoring..." : "Restore project"}
              </Button>
            ))}
        </>
      )}
    </Stack>
  );
}
