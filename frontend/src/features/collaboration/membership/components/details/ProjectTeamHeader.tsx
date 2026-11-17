import { Chip, Stack, Typography } from "@mui/material";

import GroupsOutlinedIcon from "@mui/icons-material/GroupsOutlined";

interface ProjectTeamHeaderProps {
  totalMembers: number;
}

export default function ProjectTeamHeader({
  totalMembers,
}: ProjectTeamHeaderProps) {
  return (
    <Stack
      direction={{ xs: "column", sm: "row" }}
      spacing={1}
      sx={{
        alignItems: { xs: "flex-start", sm: "center" },
        justifyContent: "space-between",
      }}
    >
      <Stack spacing={0.5}>
        <Typography variant="h6" component="h2">
          Project members
        </Typography>

        <Typography variant="body2" color="text.secondary">
          People who have access to this project.
        </Typography>
      </Stack>

      <Chip
        icon={<GroupsOutlinedIcon />}
        label={`${totalMembers} ${totalMembers === 1 ? "member" : "members"}`}
        color="primary"
        variant="outlined"
      />
    </Stack>
  );
}
