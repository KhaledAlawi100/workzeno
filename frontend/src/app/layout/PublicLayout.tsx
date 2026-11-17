import { Box, CircularProgress } from "@mui/material";
import { Outlet, Navigate } from "react-router-dom";

import AppNavbar from "./components/AppNavbar";
import { useAuth } from "../../features/auth/AuthContext";

  

export default function PublicLayout() {
  const { status } = useAuth();

  if (status === "loading") {
    return (
      <Box
        sx={{
          minHeight: "100vh",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  // Authenticated users should use the application dashboard.
  if (status === "authenticated") {
    return <Navigate to="/dashboard" replace />;
  }

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
        bgcolor: "background.default",
      }}
    >
      <AppNavbar variant="public" />

      {/* The selected public route is rendered here. */}
      <Box component="main" sx={{ flex: 1 }}>
        <Outlet />
      </Box>
    </Box>
  );
}
