import { Box, Typography } from "@mui/material";

interface DashboardHeaderProps {
  email?: string;
}

export default function DashboardHeader({ email }: DashboardHeaderProps) {
  const displayName = email?.split("@")[0] || "there";

  return (
    <Box>
      <Typography variant="h4" component="h1" sx={{ fontWeight: 750 }}>
        Dashboard
      </Typography>

      <Typography color="text.secondary" sx={{ mt: 0.75 }}>
        Welcome back, {displayName}. Here's an overview of your workspace.
      </Typography>
    </Box>
  );
}
