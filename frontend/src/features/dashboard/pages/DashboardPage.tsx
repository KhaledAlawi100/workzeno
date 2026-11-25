import {
  Alert,
  Button,
  Container,
  Grid,
  Stack,
  Typography,
} from "@mui/material";

import RefreshOutlinedIcon from "@mui/icons-material/RefreshOutlined";

import { useAuth } from "../../auth//AuthContext";
import { getApiErrorMessage } from "../../../lib/api";

import { useDashboard } from "../hooks/useDashboard";

import DashboardHeader from "../components/DashboardHeader";
import DashboardMetrics from "../components/DashboardMetrics";
import TaskStatusOverview from "../components/TaskStatusOverview";
import MyTasks from "../components/MyTasks";
import DashboardLoading from "../components/DashboardLoading";

export default function DashboardPage() {
  const { user } = useAuth();
  const dashboardQuery = useDashboard();

  if (dashboardQuery.isPending) {
    return (
      <Container maxWidth="xl" sx={{ py: 4 }}>
        <DashboardLoading />
      </Container>
    );
  }

  if (dashboardQuery.isError) {
    return (
      <Container maxWidth="xl" sx={{ py: 4 }}>
        <Stack spacing={2}>
          <DashboardHeader email={user?.email} />

          <Alert
            severity="error"
            action={
              <Button
                color="inherit"
                size="small"
                startIcon={<RefreshOutlinedIcon />}
                onClick={() => void dashboardQuery.refetch()}
              >
                Retry
              </Button>
            }
          >
            {getApiErrorMessage(dashboardQuery.error)}
          </Alert>
        </Stack>
      </Container>
    );
  }

  const dashboard = dashboardQuery.data;

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Stack spacing={3}>
        <DashboardHeader email={user?.email} />

        {dashboardQuery.isFetching && (
          <Typography variant="caption" color="text.secondary">
            Refreshing dashboard...
          </Typography>
        )}

        <DashboardMetrics
          projects={dashboard.projects}
          tasks={dashboard.tasks}
        />

        <Grid container spacing={2}>
          <Grid size={{ xs: 12, md: 5 }}>
            <TaskStatusOverview tasks={dashboard.tasks} />
          </Grid>

          <Grid size={{ xs: 12, md: 7 }}>
            <MyTasks tasks={dashboard.myTasks} />
          </Grid>
        </Grid>
      </Stack>
    </Container>
  );
}
