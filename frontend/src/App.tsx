import { BrowserRouter, Link, Route, Routes } from "react-router-dom";
import { Box, Button, Stack, Typography } from "@mui/material";
import { useState } from "react";
import { getApiHealth } from "./features/health/healthApi";
import RegisterPage from "./pages/RegisterPage";
import LoginPage from "./pages/LoginPage";

type CheckState = "idle" | "checking" | "connected" | "failed";

function HealthPage() {
  const [state, setState] = useState<CheckState>("idle");

  async function checkBackend() {
    setState("checking");

    try {
      const result = await getApiHealth();

      if (result.status !== "UP") {
        throw new Error("Backend health check failed");
      }

      setState("connected");
    } catch (error) {
      console.error("Error checking backend health:", error);
      setState("failed");
    }
  }

  return (
    <Box sx={{ p: 4 }}>
      <Typography variant="h4" gutterBottom>
        Workzeno
      </Typography>

      <Typography sx={{ mb: 2 }}>Backend connectivity check</Typography>

      <Stack direction="row" spacing={2} sx={{ mb: 2 }}>
        <Button
          variant="contained"
          onClick={checkBackend}
          disabled={state === "checking"}
        >
          {state === "checking" ? "Checking..." : "Check Backend Health"}
        </Button>

        <Button component={Link} to="/register">
          Register
        </Button>

        <Button component={Link} to="/login">
          Sign in
        </Button>
      </Stack>

      <Typography role="status" aria-live="polite">
        {state === "idle" && "Not checked yet"}
        {state === "checking" && "Contacting backend..."}
        {state === "connected" && "Backend connected"}
        {state === "failed" &&
          "Could not reach the backend. Check that it is running."}
      </Typography>
    </Box>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<HealthPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/login" element={<LoginPage />} />
      </Routes>
    </BrowserRouter>
  );
}
