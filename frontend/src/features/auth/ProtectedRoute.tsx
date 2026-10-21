import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { Box, CircularProgress } from "@mui/material";
import { useAuth } from "./AuthContext";

export default function ProtectedRoute({ children }: { children: ReactNode }) {
  const { status, isAuthenticated } = useAuth();

  if (status === "loading") {
    return (
      <Box
        sx={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "100vh",
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}
