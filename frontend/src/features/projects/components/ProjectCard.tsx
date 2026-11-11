import {
  Box,
  Card,
  CardActionArea,
  CardContent,
  Chip,
  Stack,
  Typography,
} from "@mui/material";

import { Link } from "react-router-dom";

import type { ProjectSummary } from "../project.types";

interface ProjectCardProps {
  project: ProjectSummary;
}

export default function ProjectCard({ project }: ProjectCardProps) {
  const isActive = project.status === "ACTIVE";

  return (
    <Card
      variant="outlined"
      sx={{
        height: "100%",
        borderRadius: 2,
        transition: "box-shadow 0.2s ease, transform 0.2s ease",
        "&:hover": {
          boxShadow: 3,
          transform: "translateY(-2px)",
        },
      }}
    >
      <CardActionArea
        component={Link}
        to={`/projects/${project.id}`}
        sx={{ height: "100%" }}
      >
        <CardContent>
          <Stack spacing={2}>
            <Box
              sx={{
                display: "flex",
                alignItems: "flex-start",
                justifyContent: "space-between",
                gap: 2,
              }}
            >
              <Typography
                variant="h6"
                component="h2"
                sx={{
                  fontWeight: 600,
                  overflowWrap: "anywhere",
                }}
              >
                {project.name}
              </Typography>

              <Chip
                label={isActive ? "Active" : "Archived"}
                color={isActive ? "success" : "default"}
                size="small"
              />
            </Box>

            <Typography
              variant="body2"
              color="text.secondary"
              sx={{
                minHeight: 40,
                overflowWrap: "anywhere",
              }}
            >
              {project.description || "No description provided."}
            </Typography>

            <Typography variant="caption" color="text.secondary">
              Last updated: {new Date(project.updatedAt).toLocaleDateString()}
            </Typography>
          </Stack>
        </CardContent>
      </CardActionArea>
    </Card>
  );
}
