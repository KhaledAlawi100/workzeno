import { useNavigate } from "react-router-dom";
import { Box, Button, Stack, Typography } from "@mui/material";
import { useAuth } from "../features/auth/AuthContext";

export default function DashboardPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout(): Promise<void> {
    await logout();
    navigate("/login", { replace: true });
  }

  return (
    <Box sx={{ p: 4 }}>
      <Stack spacing={2} sx={{ alignItems: "flex-start" }}>
        <Typography variant="h4">Workzeno Dashboard</Typography>

        <Typography>Welcome, {user?.email}</Typography>

        <Typography>Role: {user?.role}</Typography>

        <Button variant="contained" onClick={handleLogout}>
          Sign out
        </Button>
      </Stack>
    </Box>
  );
}
