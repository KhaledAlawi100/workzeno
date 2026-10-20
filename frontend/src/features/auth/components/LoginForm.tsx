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
import { loginUser } from "../authApi";
import { getApiErrorMessage } from "../../../lib/api";
import { loginSchema, type LoginFormValues } from "../authSchemas";

export default function LoginForm() {
  const [successMessage, setSuccessMessage] = useState("");

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
      await loginUser(values);

      // Day 9 will introduce the authentication context and token lifecycle.
      // Do not persist tokens in localStorage as part of this step.
      setSuccessMessage(
        "Login successful. Authentication session handling comes next.",
      );
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
