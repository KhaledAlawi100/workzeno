import { Box, Grid, Paper, Typography } from "@mui/material";
import FolderOpenOutlinedIcon from "@mui/icons-material/FolderOpenOutlined";

import type { ProjectSummary } from "../project.types";
import ProjectCard from "./ProjectCard";

interface ProjectsListProps {
  projects: ProjectSummary[];
}

export default function ProjectsList({ projects }: ProjectsListProps) {
  if (projects.length === 0) {
    return (
      <Paper
        variant="outlined"
        sx={{
          py: 7,
          px: 3,
          borderRadius: 2,
          textAlign: "center",
        }}
      >
        <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
            gap: 2,
          }}
        >
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              width: 64,
              height: 64,
              borderRadius: "50%",
              bgcolor: "action.hover",
            }}
          >
            <FolderOpenOutlinedIcon
              sx={{ fontSize: 34, color: "text.secondary" }}
            />
          </Box>

          <Typography variant="h6" component="h2">
            No projects yet
          </Typography>

          <Typography color="text.secondary" variant="body2">
            Your projects will appear here when you create one.
          </Typography>
        </Box>
      </Paper>
    );
  }

  return (
    <Grid container spacing={3}>
      {projects.map((project) => (
        <Grid key={project.id} size={{ xs: 12, sm: 6, md: 4 }}>
          <ProjectCard project={project} />
        </Grid>
      ))}
    </Grid>
  );
}
