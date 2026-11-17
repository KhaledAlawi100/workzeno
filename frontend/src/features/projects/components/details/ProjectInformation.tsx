import { Box, Chip, Divider, Paper, Stack, Typography } from "@mui/material";

import type { Project } from "../../project.types";

interface ProjectInformationProps {
  project: Project;
}

export default function ProjectInformation({
  project,
}: ProjectInformationProps) {
  const isActive = project.status === "ACTIVE";

  return (
    <Paper variant="outlined" sx={{ p: 3, borderRadius: 2 }}>
      <Stack spacing={2}>
        <Typography variant="h6" component="h2">
          Project information
        </Typography>

        <Divider />

        {/* Display the current project status. */}
        <Box>
          <Typography variant="caption" color="text.secondary">
            Project status
          </Typography>

          <Box sx={{ mt: 0.5 }}>
            <Chip
              label={isActive ? "Active" : "Archived"}
              color={isActive ? "success" : "default"}
              size="small"
            />
          </Box>
        </Box>

        {/* Display the original creation date. */}
        <Box>
          <Typography variant="caption" color="text.secondary">
            Creation date
          </Typography>

          <Typography variant="body2">
            {new Date(project.createdAt).toLocaleString()}
          </Typography>
        </Box>

        {/* Display the most recent update date. */}
        <Box>
          <Typography variant="caption" color="text.secondary">
            Last updated
          </Typography>

          <Typography variant="body2">
            {new Date(project.updatedAt).toLocaleString()}
          </Typography>
        </Box>
      </Stack>
    </Paper>
  );
}
