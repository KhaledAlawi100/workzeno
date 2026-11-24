import { Link } from "react-router-dom";

import { Box, Button, Stack, Typography } from "@mui/material";
import AddOutlinedIcon from "@mui/icons-material/AddOutlined";
import ArrowBackOutlinedIcon from "@mui/icons-material/ArrowBackOutlined";

interface TaskPageHeaderProps {
  projectId: string;
  canCreateTasks: boolean;
  onCreateTask: () => void;
}

export default function TaskPageHeader({
  projectId,
  canCreateTasks,
  onCreateTask,
}: TaskPageHeaderProps) {
  return (
    <Stack spacing={2}>
      <Button
        component={Link}
        to={`/projects/${encodeURIComponent(projectId)}`}
        startIcon={<ArrowBackOutlinedIcon />}
        sx={{ alignSelf: "flex-start" }}
      >
        Back to project
      </Button>

      <Stack
        direction={{ xs: "column", sm: "row" }}
        spacing={2}
        sx={{
          alignItems: { xs: "flex-start", sm: "center" },
          justifyContent: "space-between",
        }}
      >
        <Box>
          <Typography variant="h4" component="h1" sx={{ fontWeight: 750 }}>
            Tasks
          </Typography>
          <Typography color="text.secondary" sx={{ mt: 0.5 }}>
            Organize and track work across this project.
          </Typography>
        </Box>

        {canCreateTasks && (
          <Button
            variant="contained"
            startIcon={<AddOutlinedIcon />}
            onClick={onCreateTask}
          >
            Create task
          </Button>
        )}
      </Stack>
    </Stack>
  );
}
