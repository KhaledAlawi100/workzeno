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

import type { ProjectMember } from "../membership.types";

import { useProjectTeamContext } from "../context/ProjectTeamContext";
import { formatMemberDate } from "../utils/formatMemberDate";

import MemberRoleMenu from "./MemberRoleMenu";
import RemoveMemberDialog from "./RemoveMemberDialog";

interface ProjectMemberCardProps {
  member: ProjectMember;
}

export default function ProjectMemberCard({ member }: ProjectMemberCardProps) {
  const [removeDialogOpen, setRemoveDialogOpen] = useState(false);

  const { isOwner, isAdmin, canManageMembers, isRemovePending, removeMember } =
    useProjectTeamContext();

  const isAdminMember = member.role === "ADMIN";

  // Only the owner can change another member's role.
  const canChangeRole = isOwner;

  // The owner can remove anyone; an admin can remove regular members only.
  const canRemoveMember = isOwner || (isAdmin && member.role === "MEMBER");

  const memberRemovalPending = isRemovePending(member.userId);

  // Avoid displaying management actions when permissions are unavailable.
  const showManagementMenu =
    canManageMembers && (canChangeRole || canRemoveMember);

  const avatarLetter = member.email?.charAt(0).toUpperCase() || "?";

  async function handleConfirmRemoval() {
    try {
      // Close the dialog only after the server confirms removal.
      await removeMember(member.userId);
      setRemoveDialogOpen(false);
    } catch {
      // Keep the dialog open. The provider's action hook displays the error.
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
        isPending={memberRemovalPending}
        onClose={() => setRemoveDialogOpen(false)}
        onConfirm={handleConfirmRemoval}
      />

      {/* The menu opens this card's own removal confirmation dialog. */}
      {/* MemberRoleMenu needs to receive this local callback. */}
    </>
  );
}
