import { Box, Paper, Stack, Typography } from "@mui/material";

import { useAuth } from "../features/auth/AuthContext";

export default function DashboardPage() {
  const { user } = useAuth();

  return (
    <Box sx={{ p: { xs: 2, sm: 3, md: 4 } }}>
      <Stack spacing={3}>
        <Box>
          <Typography variant="h4" component="h1">
            Workzeno Dashboard
          </Typography>

          <Typography color="text.secondary" sx={{ mt: 1 }}>
            Welcome back, {user?.email.split("@")[0]}.
          </Typography>
        </Box>

        <Paper variant="outlined" sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            Your workspace
          </Typography>

          <Typography color="text.secondary">
            Manage your projects and collaborate with your team from the
            navigation above.
          </Typography>
        </Paper>
      </Stack>
    </Box>
  );
}
