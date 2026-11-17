import { Box, Typography } from "@mui/material";

import GroupsOutlinedIcon from "@mui/icons-material/GroupsOutlined";

export default function ProjectMembersEmptyState() {
  return (
    <Box
      sx={{
        py: 5,
        px: 2,
        textAlign: "center",
        border: 1,
        borderColor: "divider",
        borderRadius: 2,
        bgcolor: "background.paper",
      }}
    >
      <GroupsOutlinedIcon
        sx={{
          fontSize: 38,
          color: "text.secondary",
          mb: 1,
        }}
      />

      <Typography sx={{ fontWeight: 600 }}>No members yet</Typography>

      <Typography variant="body2" color="text.secondary">
        Invite people to collaborate on this project.
      </Typography>
    </Box>
  );
}
