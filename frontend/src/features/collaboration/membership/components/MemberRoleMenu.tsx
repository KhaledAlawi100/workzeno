import { useState } from "react";
import type { MouseEvent } from "react";
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  ListItemIcon,
  ListItemText,
  Menu,
  MenuItem,
  Typography,
} from "@mui/material";

import MoreVertIcon from "@mui/icons-material/MoreVert";
import AdminPanelSettingsOutlinedIcon from "@mui/icons-material/AdminPanelSettingsOutlined";
import PersonOutlineIcon from "@mui/icons-material/PersonOutlined";

import type { ProjectMember } from "../membership.types";

interface MemberRoleMenuProps {
  member: ProjectMember;
  canChangeRole: boolean;
  canRemoveMember: boolean;
  isRolePending?: boolean;
  isRemovePending?: boolean;

  onChangeRole?: (role: "ADMIN" | "MEMBER") => Promise<void>;
  onRemove?: () => void;
}

export default function MemberRoleMenu({
  member,
  canChangeRole,
  canRemoveMember,
  isRolePending = false,
  isRemovePending = false,
  onChangeRole,
  onRemove,
}: MemberRoleMenuProps) {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [roleDialogOpen, setRoleDialogOpen] = useState(false);

  const open = Boolean(anchorEl);

  // A MEMBER can be promoted; an ADMIN can be changed to MEMBER.
  const newRole = member.role === "MEMBER" ? "ADMIN" : "MEMBER";

  function handleOpen(event: MouseEvent<HTMLElement>) {
    setAnchorEl(event.currentTarget);
  }

  function handleClose() {
    setAnchorEl(null);
  }

  function handleSelectRoleChange() {
    handleClose();
    setRoleDialogOpen(true);
  }

  async function handleConfirmRoleChange() {
    if (!onChangeRole) {
      return;
    }

    try {
      await onChangeRole(newRole);
      setRoleDialogOpen(false);
    } catch {
      // Keep the dialog open when the API request fails.
      // The parent component displays the error notification.
    }
  }

  function handleRemove() {
    handleClose();
    onRemove?.();
  }

  // Do not render an empty menu when no actions are permitted.
  if (!canChangeRole && !canRemoveMember) {
    return null;
  }

  return (
    <>
      <IconButton
        aria-label={`Manage ${member.email}`}
        aria-haspopup="true"
        aria-expanded={open ? "true" : undefined}
        onClick={handleOpen}
        size="small"
        disabled={isRolePending || isRemovePending}
      >
        <MoreVertIcon />
      </IconButton>

      <Menu
        anchorEl={anchorEl}
        open={open}
        onClose={handleClose}
        anchorOrigin={{
          vertical: "bottom",
          horizontal: "right",
        }}
        transformOrigin={{
          vertical: "top",
          horizontal: "right",
        }}
      >
        {canChangeRole && (
          <MenuItem onClick={handleSelectRoleChange} disabled={isRolePending}>
            <ListItemIcon>
              {newRole === "ADMIN" ? (
                <AdminPanelSettingsOutlinedIcon fontSize="small" />
              ) : (
                <PersonOutlineIcon fontSize="small" />
              )}
            </ListItemIcon>

            <ListItemText>
              {newRole === "ADMIN" ? "Make admin" : "Change to member"}
            </ListItemText>
          </MenuItem>
        )}

        {canRemoveMember && (
          <MenuItem
            onClick={handleRemove}
            sx={{ color: "error.main" }}
            disabled={isRemovePending}
          >
            <ListItemText>Remove from project</ListItemText>
          </MenuItem>
        )}
      </Menu>

      {/* Confirm the role change before sending the PATCH request. */}
      <Dialog
        open={roleDialogOpen}
        onClose={isRolePending ? undefined : () => setRoleDialogOpen(false)}
        fullWidth
        maxWidth="xs"
      >
        <DialogTitle>Change member role?</DialogTitle>

        <DialogContent>
          <Alert severity="info" sx={{ mb: 2 }}>
            {newRole === "ADMIN"
              ? "An admin can manage project details and remove regular members."
              : "This person will lose admin permissions and become a regular member."}
          </Alert>

          <Typography variant="body2" color="text.secondary">
            Change <strong>{member.email}</strong> from{" "}
            <strong>{member.role}</strong> to <strong>{newRole}</strong>?
          </Typography>
        </DialogContent>

        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button
            color="inherit"
            onClick={() => setRoleDialogOpen(false)}
            disabled={isRolePending}
          >
            Cancel
          </Button>

          <Button
            variant="contained"
            onClick={handleConfirmRoleChange}
            disabled={isRolePending}
          >
            {isRolePending ? "Updating..." : "Confirm change"}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
