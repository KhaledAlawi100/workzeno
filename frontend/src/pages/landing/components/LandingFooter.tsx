import {
  Box,
  Container,
  Divider,
  Link as MuiLink,
  Stack,
  Typography,
} from "@mui/material";

import { Link as RouterLink } from "react-router-dom";

import Brand from "./Brand";

export default function LandingFooter() {
  return (
    <Box
      component="footer"
      sx={{
        bgcolor: "#fff",
        borderTop: "1px solid",
        borderColor: "divider",
      }}
    >
      <Container maxWidth="lg">
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={2}
          sx={{
            py: 3.5,
            alignItems: { xs: "flex-start", sm: "center" },
            justifyContent: "space-between",
          }}
        >
          <Stack spacing={1}>
            <Brand />

            <Typography variant="caption" color="text.secondary">
              Manage projects. Work better together.
            </Typography>
          </Stack>

          <Stack direction="row" spacing={2.5}>
            <MuiLink
              component={RouterLink}
              to="/login"
              underline="hover"
              color="text.secondary"
              variant="body2"
            >
              Sign in
            </MuiLink>

            <MuiLink
              component={RouterLink}
              to="/register"
              underline="hover"
              color="text.secondary"
              variant="body2"
            >
              Get started
            </MuiLink>

            <MuiLink
              href="/#features"
              underline="hover"
              color="text.secondary"
              variant="body2"
            >
              Features
            </MuiLink>
          </Stack>
        </Stack>

        <Divider />

        <Typography
          variant="caption"
          color="text.secondary"
          sx={{ display: "block", py: 2.5, textAlign: "center" }}
        >
          © {new Date().getFullYear()} Workzeno. All rights reserved.
        </Typography>
      </Container>
    </Box>
  );
}
