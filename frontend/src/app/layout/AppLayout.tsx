import { Box } from "@mui/material";
import { Outlet } from "react-router-dom";

import AppNavbar from "./components/AppNavbar";

export default function AppLayout() {
  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
        bgcolor: "background.default",
      }}
    >
      <AppNavbar variant="app" />

      {/* Protected page content is rendered below the shared navbar. */}
      <Box component="main" sx={{ flex: 1 }}>
        <Outlet />
      </Box>
    </Box>
  );
}
