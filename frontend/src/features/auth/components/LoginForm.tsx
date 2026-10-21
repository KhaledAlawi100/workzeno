import { useState } from "react";
import {
  Alert,
  Box,
  Button,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { getApiErrorMessage } from "../../../lib/api";
import { loginSchema, type LoginFormValues } from "../authSchemas";
import { useAuth } from "../AuthContext";
import { useNavigate } from "react-router-dom";

export default function LoginForm() {
  const [successMessage, setSuccessMessage] = useState("");
  const navigate = useNavigate();
  const { login } = useAuth();
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  async function onSubmit(values: LoginFormValues) {
    setSuccessMessage("");

    try {
      await login(values);
      navigate("/dashboard", { replace: true });
    } catch (error) {
      setError("root", {
        message: getApiErrorMessage(error),
      });
    }
  }
  return (
    <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
      <Stack spacing={2}>
        <Typography variant="h5">Sign in to Workzeno</Typography>

        {errors.root && <Alert severity="error">{errors.root.message}</Alert>}

        {successMessage && <Alert severity="success">{successMessage}</Alert>}

        <TextField
          label="Email"
          type="email"
          autoComplete="email"
          fullWidth
          {...register("email")}
          error={Boolean(errors.email)}
          helperText={errors.email?.message}
        />

        <TextField
          label="Password"
          type="password"
          autoComplete="current-password"
          fullWidth
          {...register("password")}
          error={Boolean(errors.password)}
          helperText={errors.password?.message}
        />

        <Button
          type="submit"
          variant="contained"
          disabled={isSubmitting}
          fullWidth
        >
          {isSubmitting ? "Signing in..." : "Sign in"}
        </Button>
      </Stack>
    </Box>
  );
}
