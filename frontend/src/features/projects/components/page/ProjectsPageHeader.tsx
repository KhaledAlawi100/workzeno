import { Button, Stack, Typography } from "@mui/material";

import AddIcon from "@mui/icons-material/Add";

interface ProjectsPageHeaderProps {
  onCreateProject: () => void;
}

export default function ProjectsPageHeader({
  onCreateProject,
}: ProjectsPageHeaderProps) {
  return (
    <Stack
      direction={{ xs: "column", sm: "row" }}
      spacing={2}
      sx={{
        justifyContent: "space-between",
        alignItems: { xs: "stretch", sm: "center" },
      }}
    >
      <Stack spacing={1}>
        <Typography variant="h4" component="h1" sx={{ fontWeight: 700 }}>
          Projects
        </Typography>

        <Typography color="text.secondary">
          View and manage your projects.
        </Typography>
      </Stack>

      <Button
        variant="contained"
        startIcon={<AddIcon />}
        onClick={onCreateProject}
      >
        New project
      </Button>
    </Stack>
  );
}
