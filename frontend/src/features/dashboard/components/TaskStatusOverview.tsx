import {
  Box,
  Card,
  CardContent,
  LinearProgress,
  Stack,
  Typography,
} from "@mui/material";

import type { TaskMetrics } from "../dashboard.types";

interface TaskStatusOverviewProps {
  tasks: TaskMetrics;
}

export default function TaskStatusOverview({ tasks }: TaskStatusOverviewProps) {
  const statuses = [
    { label: "To do", value: tasks.todo, color: "#64748B" },
    {
      label: "In progress",
      value: tasks.inProgress,
      color: "#3659D9",
    },
    {
      label: "In review",
      value: tasks.inReview,
      color: "#C17A16",
    },
    { label: "Done", value: tasks.done, color: "#16845B" },
  ];

  return (
    <Card>
      <CardContent sx={{ p: 2.5 }}>
        <Stack spacing={2.5}>
          <Box>
            <Typography variant="h6" sx={{ fontWeight: 700 }}>
              Task overview
            </Typography>

            <Typography variant="body2" color="text.secondary">
              Distribution of tasks across their statuses.
            </Typography>
          </Box>

          {statuses.map((status) => {
            const percentage =
              tasks.total > 0 ? (status.value / tasks.total) * 100 : 0;

            return (
              <Stack key={status.label} spacing={1}>
                <Stack
                  direction="row"
                  sx={{
                    justifyContent: "space-between",
                    alignItems: "center",
                  }}
                >
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    {status.label}
                  </Typography>

                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ fontWeight: 600 }}
                  >
                    {status.value.toLocaleString()}
                  </Typography>
                </Stack>

                <LinearProgress
                  variant="determinate"
                  value={percentage}
                  sx={{
                    height: 8,
                    borderRadius: 5,
                    backgroundColor: "#EEF1F6",
                    "& .MuiLinearProgress-bar": {
                      backgroundColor: status.color,
                      borderRadius: 5,
                    },
                  }}
                />
              </Stack>
            );
          })}
        </Stack>
      </CardContent>
    </Card>
  );
}
