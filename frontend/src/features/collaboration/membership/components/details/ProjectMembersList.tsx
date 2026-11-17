import { Grid, Stack, Typography } from "@mui/material";

import type {
  ProjectMember,
  ProjectMembershipRole,
} from "../../membership.types";

import ProjectMemberCard from "../ProjectMemberCard";
import ProjectMembersEmptyState from "./ProjectMembersEmptyState";

interface ProjectMembersListProps {
  members: ProjectMember[];

  isOwner: boolean;
  isAdmin: boolean;
  canManageMembers: boolean;

  isRolePending: (userId: string) => boolean;
  isRemovePending: (userId: string) => boolean;

  onChangeRole: (userId: string, role: ProjectMembershipRole) => Promise<void>;

  onRemoveMember: (userId: string) => Promise<void>;
}

export default function ProjectMembersList({
  members,
  isOwner,
  isAdmin,
  canManageMembers,
  isRolePending,
  isRemovePending,
  onChangeRole,
  onRemoveMember,
}: ProjectMembersListProps) {
  return (
    <Stack spacing={1.5}>
      <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
        Members
      </Typography>

      {members.length === 0 ? (
        <ProjectMembersEmptyState />
      ) : (
        <Grid container spacing={2}>
          {members.map((member) => (
            <Grid key={member.userId} size={{ xs: 12, sm: 6 }}>
              <ProjectMemberCard
                member={member}
                isOwner={isOwner}
                isAdmin={isAdmin}
                canManageMembers={canManageMembers}
                isRolePending={isRolePending(member.userId)}
                isRemovePending={isRemovePending(member.userId)}
                onChangeRole={(role) => onChangeRole(member.userId, role)}
                onRemoveMember={() => onRemoveMember(member.userId)}
              />
            </Grid>
          ))}
        </Grid>
      )}
    </Stack>
  );
}
