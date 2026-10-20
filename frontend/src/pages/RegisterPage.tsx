import { Link as RouterLink } from "react-router-dom";
import { Box, Link, Paper, Typography } from "@mui/material";
import RegisterForm from "../features/auth/components/RegisterForm";

export default function RegisterPage() {
  return (
    <Box sx={{ maxWidth: 440, mx: "auto", mt: 6,px: 2 }}>
      <Paper elevation={2} sx={{ p: 4 }}>
        <RegisterForm />

        <Typography sx={{ mt: 3, textAlign: "center" }}>
          Already have an account ?{" "}
          <Link component={RouterLink} to="/login">
            Sign in
          </Link>
        </Typography>
      </Paper>
    </Box>
  );
}
