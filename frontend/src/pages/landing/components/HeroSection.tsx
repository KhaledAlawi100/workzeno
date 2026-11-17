import { Box, Button, Chip, Container, Stack, Typography } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";

import ProductPreview from "./ProductPreview";

export default function HeroSection() {
  return (
    <Box
      sx={{
        background:
          "radial-gradient(ellipse at 78% 22%, rgba(102,128,232,0.10), transparent 35%), linear-gradient(180deg, #F8FAFF 0%, #FFFFFF 88%)",
      }}
    >
      <Container maxWidth="lg">
        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", md: "1.02fr 0.98fr" },
            alignItems: "center",
            gap: { xs: 7, md: 5 },
            py: { xs: 8, sm: 10, md: 13 },
          }}
        >
          {/* Hero text and actions */}
          <Box sx={{ maxWidth: 590 }}>
            <Chip
              label="A clearer way to work together"
              size="small"
              sx={{
                mb: 3,
                bgcolor: "#E9EEFF",
                color: "primary.dark",
                fontWeight: 700,
                borderRadius: 2,
              }}
            />

            <Typography
              component="h1"
              sx={{
                fontSize: { xs: "2.7rem", sm: "3.5rem", md: "4.15rem" },
                lineHeight: 1.08,
                letterSpacing: "-0.055em",
                fontWeight: 900,
                color: "#172033",
                maxWidth: 620,
              }}
            >
              Manage projects.
              <Box
                component="span"
                sx={{ display: "block", color: "primary.main" }}
              >
                Work better together.
              </Box>
            </Typography>

            <Typography
              color="text.secondary"
              sx={{
                mt: 3,
                maxWidth: 510,
                fontSize: { xs: 16, sm: 18 },
                lineHeight: 1.8,
              }}
            >
              Organize your projects, collaborate with your team, and manage
              your work from one place.
            </Typography>

            <Stack
              direction={{ xs: "column", sm: "row" }}
              spacing={1.5}
              sx={{ mt: 4, maxWidth: { xs: 320, sm: "none" } }}
            >
              <Button
                component={RouterLink}
                to="/register"
                variant="contained"
                size="large"
                sx={{
                  minHeight: 50,
                  px: 3.5,
                  boxShadow: "0 9px 22px rgba(54,89,217,0.22)",
                }}
              >
                Get started
              </Button>

              <Button
                component={RouterLink}
                to="/login"
                variant="outlined"
                color="inherit"
                size="large"
                sx={{
                  minHeight: 50,
                  px: 3.5,
                  borderColor: "#D9DFEA",
                  color: "text.primary",
                  bgcolor: "#fff",
                }}
              >
                Sign in
              </Button>
            </Stack>

            {/* Short product summary */}
            <Box
              sx={{
                mt: 3.5,
                display: "flex",
                flexDirection: "row",
                alignItems: "center",
                gap: 1,
              }}
            >
              <Box
                sx={{
                  width: 7,
                  height: 7,
                  borderRadius: "50%",
                  bgcolor: "success.main",
                }}
              />

              <Typography variant="caption" color="text.secondary">
                Projects, people, and permissions in one workspace.
              </Typography>
            </Box>
          </Box>

          {/* Product illustration */}
          <Box sx={{ py: { xs: 1, md: 2 }, px: { xs: 1, sm: 2 } }}>
            <ProductPreview />
          </Box>
        </Box>
      </Container>
    </Box>
  );
}
