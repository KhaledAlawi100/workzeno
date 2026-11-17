import { useState } from "react";

import {
  Avatar,
  Card,
  CardContent,
  Chip,
  Stack,
  Typography,
} from "@mui/material";

import AdminPanelSettingsOutlinedIcon from "@mui/icons-material/AdminPanelSettingsOutlined";
import PersonOutlineIcon from "@mui/icons-material/PersonOutlined";

import type { ProjectMember, ProjectMembershipRole } from "../membership.types";

import { formatMemberDate } from "../utils/formatMemberDate";

import MemberRoleMenu from "./MemberRoleMenu";
import RemoveMemberDialog from "./RemoveMemberDialog";

interface ProjectMemberCardProps {
  member: ProjectMember;
  isOwner: boolean;
  isAdmin: boolean;
  canManageMembers: boolean;

  isRolePending?: boolean;
  isRemovePending?: boolean;

  onChangeRole?: (role: ProjectMembershipRole) => Promise<void>;
  onRemoveMember?: () => Promise<void>;
}

export default function ProjectMemberCard({
  member,
  isOwner,
  isAdmin,
  canManageMembers,
  isRolePending = false,
  isRemovePending = false,
  onChangeRole,
  onRemoveMember,
}: ProjectMemberCardProps) {
  const [removeDialogOpen, setRemoveDialogOpen] = useState(false);

  const isAdminMember = member.role === "ADMIN";

  // Only the owner can change another member's role.
  const canChangeRole = isOwner;

  // The owner can remove anyone; an admin can remove regular members only.
  const canRemoveMember = isOwner || (isAdmin && member.role === "MEMBER");

  // Also require a callback so the UI never displays a non-working action.
  const showManagementMenu =
    canManageMembers &&
    ((canChangeRole && Boolean(onChangeRole)) ||
      (canRemoveMember && Boolean(onRemoveMember)));

  const avatarLetter = member.email?.charAt(0).toUpperCase() || "?";

async function handleConfirmRemoval() {
  if (!onRemoveMember) {
    return;
  }

  try {
    // Close the dialog only after the server confirms removal.
    await onRemoveMember();
    setRemoveDialogOpen(false);
  } catch {
    // Keep the dialog open. The parent displays the error notification.
  }
}
  return (
    <>
      <Card
        variant="outlined"
        sx={{
          height: "100%",
          transition: "border-color 0.2s ease, box-shadow 0.2s ease",
          "&:hover": {
            borderColor: "primary.light",
            boxShadow: "0 4px 14px rgba(23, 32, 51, 0.06)",
          },
        }}
      >
        <CardContent sx={{ p: 2.5, "&:last-child": { pb: 2.5 } }}>
          <Stack
            direction="row"
            spacing={2}
            sx={{
              alignItems: "center",
              justifyContent: "space-between",
            }}
          >
            <Stack
              direction="row"
              spacing={1.5}
              sx={{ minWidth: 0, flex: 1, alignItems: "center" }}
            >
              <Avatar
                sx={{
                  width: 44,
                  height: 44,
                  bgcolor: isAdminMember ? "primary.light" : "secondary.light",
                  color: "white",
                  fontWeight: 700,
                }}
              >
                {avatarLetter}
              </Avatar>

              <Stack spacing={0.5} sx={{ minWidth: 0 }}>
                <Typography
                  variant="subtitle2"
                  sx={{
                    fontWeight: 700,
                    overflowWrap: "anywhere",
                  }}
                >
                  {member.email}
                </Typography>

                <Typography variant="caption" color="text.secondary">
                  Joined {formatMemberDate(member.joinedAt)}
                </Typography>
              </Stack>
            </Stack>

            <Stack
              direction="row"
              spacing={1}
              sx={{ flexShrink: 0, alignItems: "center" }}
            >
              <Chip
                size="small"
                icon={
                  isAdminMember ? (
                    <AdminPanelSettingsOutlinedIcon />
                  ) : (
                    <PersonOutlineIcon />
                  )
                }
                label={isAdminMember ? "Admin" : "Member"}
                color={isAdminMember ? "primary" : "default"}
                variant={isAdminMember ? "filled" : "outlined"}
              />

              {showManagementMenu && (
                <MemberRoleMenu
                  member={member}
                  canChangeRole={canChangeRole && Boolean(onChangeRole)}
                  canRemoveMember={canRemoveMember && Boolean(onRemoveMember)}
                  isRolePending={isRolePending}
                  isRemovePending={isRemovePending}
                  onChangeRole={onChangeRole}
                  onRemove={() => setRemoveDialogOpen(true)}
                />
              )}
            </Stack>
          </Stack>
        </CardContent>
      </Card>

      <RemoveMemberDialog
        open={removeDialogOpen}
        memberEmail={member.email}
        isPending={isRemovePending}
        onClose={() => setRemoveDialogOpen(false)}
        onConfirm={handleConfirmRemoval}
      />
    </>
  );
}
