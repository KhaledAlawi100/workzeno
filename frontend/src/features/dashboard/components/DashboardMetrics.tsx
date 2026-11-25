import { Box, Card, CardContent, Grid, Stack, Typography } from "@mui/material";

import FolderOutlinedIcon from "@mui/icons-material/FolderOutlined";
import FolderOpenOutlinedIcon from "@mui/icons-material/FolderOpenOutlined";
import ArchiveOutlinedIcon from "@mui/icons-material/ArchiveOutlined";
import TaskAltOutlinedIcon from "@mui/icons-material/TaskAltOutlined";
import AssignmentIndOutlinedIcon from "@mui/icons-material/AssignmentIndOutlined";
import WarningAmberOutlinedIcon from "@mui/icons-material/WarningAmberOutlined";

import type { ProjectMetrics, TaskMetrics } from "../dashboard.types";

interface DashboardMetricsProps {
  projects: ProjectMetrics;
  tasks: TaskMetrics;
}

interface MetricCardProps {
  label: string;
  value: number;
  icon: React.ReactNode;
  description: string;
  color: string;
  background: string;
}

function MetricCard({
  label,
  value,
  icon,
  description,
  color,
  background,
}: MetricCardProps) {
  return (
    <Card sx={{ height: "100%" }}>
      <CardContent sx={{ p: 2.5 }}>
        <Stack spacing={2}>
          <Stack
            direction="row"
            sx={{ justifyContent: "space-between", alignItems: "center" }}
          >
            <Typography
              variant="body2"
              color="text.secondary"
              sx={{ fontWeight: 600 }}
            >
              {label}
            </Typography>

            <Stack
              sx={{
                width: 42,
                height: 42,
                borderRadius: 2.5,
                color,
                backgroundColor: background,
                alignItems: "center",
                justifyContent: "center",
              }}
            >
              {icon}
            </Stack>
          </Stack>

          <Box>
            <Typography variant="h4" sx={{ fontWeight: 750 }}>
              {value.toLocaleString()}
            </Typography>

            <Typography variant="caption" color="text.secondary">
              {description}
            </Typography>
          </Box>
        </Stack>
      </CardContent>
    </Card>
  );
}

export default function DashboardMetrics({
  projects,
  tasks,
}: DashboardMetricsProps) {
  const metrics: MetricCardProps[] = [
    {
      label: "Total projects",
      value: projects.total,
      icon: <FolderOutlinedIcon />,
      description: "Projects you can access",
      color: "#3659D9",
      background: "#E8EDFF",
    },
    {
      label: "Active projects",
      value: projects.active,
      icon: <FolderOpenOutlinedIcon />,
      description: "Currently active",
      color: "#16845B",
      background: "#DCFCE7",
    },
    {
      label: "Archived projects",
      value: projects.archived,
      icon: <ArchiveOutlinedIcon />,
      description: "Archived projects",
      color: "#64748B",
      background: "#F1F5F9",
    },
    {
      label: "Total tasks",
      value: tasks.total,
      icon: <TaskAltOutlinedIcon />,
      description: "Tasks in accessible projects",
      color: "#3659D9",
      background: "#E8EDFF",
    },
    {
      label: "Assigned to me",
      value: tasks.assignedToMe,
      icon: <AssignmentIndOutlinedIcon />,
      description: "Tasks assigned to you",
      color: "#7C3AED",
      background: "#F3E8FF",
    },
    {
      label: "Overdue tasks",
      value: tasks.overdue,
      icon: <WarningAmberOutlinedIcon />,
      description: "Uncompleted tasks past due",
      color: "#D14343",
      background: "#FEE2E2",
    },
  ];

  return (
    <Grid container spacing={2}>
      {metrics.map((metric) => (
        <Grid key={metric.label} size={{ xs: 12, sm: 6, lg: 4 }}>
          <MetricCard {...metric} />
        </Grid>
      ))}
    </Grid>
  );
}
