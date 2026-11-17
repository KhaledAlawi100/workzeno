import { Chip, Stack, Typography } from "@mui/material";

import type { Project } from "../../project.types";

interface ProjectDetailsHeaderProps {
  project: Project;
}

export default function ProjectDetailsHeader({
  project,
}: ProjectDetailsHeaderProps) {
  const isActive = project.status === "ACTIVE";

  return (
    <Stack spacing={1}>
      <Typography
        variant="h4"
        component="h1"
        sx={{
          fontWeight: 700,
          overflowWrap: "anywhere",
        }}
      >
        {project.name}
      </Typography>

      <Chip
        label={isActive ? "Active" : "Archived"}
        color={isActive ? "success" : "default"}
        size="small"
        sx={{ alignSelf: "flex-start" }}
      />
    </Stack>
  );
}
