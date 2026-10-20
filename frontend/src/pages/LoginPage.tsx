import { Link as RouterLink } from "react-router-dom";
import { Box, Link, Paper, Typography } from "@mui/material";
import LoginForm from "../features/auth/components/LoginForm";

export default function LoginPage() {
  return (
    <Box sx={{ maxWidth: 440, mx: "auto", mt: 6, px: 2 }}>
      <Paper elevation={2} sx={{ p: 4 }}>
        <LoginForm />

        <Typography sx={{ mt: 3, textAlign: "center" }}>
          Need an account?{" "}
          <Link component={RouterLink} to="/register">
            Register
          </Link>
        </Typography>
      </Paper>
    </Box>
  );
}
