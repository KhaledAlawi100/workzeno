import { Grid, Stack, Typography } from "@mui/material";

import type { ProjectMember } from "../../membership.types";

import ProjectMemberCard from "../ProjectMemberCard";
import ProjectMembersEmptyState from "./ProjectMembersEmptyState";

interface ProjectMembersListProps {
  members: ProjectMember[];
}

export default function ProjectMembersList({
  members,
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
              {/* Each card receives only its own member data. */}
              <ProjectMemberCard member={member} />
            </Grid>
          ))}
        </Grid>
      )}
    </Stack>
  );
}
