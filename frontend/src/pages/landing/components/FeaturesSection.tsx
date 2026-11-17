import { Box, Container, Paper, Stack, Typography } from "@mui/material";

import { features } from "../data/features";

export default function FeaturesSection() {
  return (
    <Box
      id="features"
      component="section"
      sx={{
        py: { xs: 8, md: 11 },
        bgcolor: "#fff",
        borderTop: "1px solid",
        borderBottom: "1px solid",
        borderColor: "divider",
        scrollMarginTop: "88px",
      }}
    >
      <Container maxWidth="lg">
        {/* Section heading */}
        <Box sx={{ maxWidth: 650, mx: "auto", textAlign: "center", mb: 6 }}>
          <Typography
            variant="overline"
            color="primary.main"
            sx={{ fontWeight: 800, letterSpacing: "0.12em" }}
          >
            CORE FEATURES
          </Typography>

          <Typography
            component="h2"
            variant="h3"
            sx={{
              mt: 1,
              fontSize: { xs: "2rem", sm: "2.6rem" },
              color: "text.primary",
            }}
          >
            Everything starts with clarity.
          </Typography>

          <Typography color="text.secondary" sx={{ mt: 2, lineHeight: 1.8 }}>
            Workzeno brings the essentials of project collaboration into one
            focused place, so your team can spend less time managing tools and
            more time doing meaningful work.
          </Typography>
        </Box>

        {/* Feature cards */}
        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", md: "repeat(3, 1fr)" },
            gap: 2.5,
          }}
        >
          {features.map((feature) => (
            <Paper
              key={feature.number}
              variant="outlined"
              sx={{
                p: 3.5,
                borderRadius: 3,
                borderColor: "divider",
                boxShadow: "none",
                transition: "transform 180ms ease, box-shadow 180ms ease",
                "&:hover": {
                  transform: "translateY(-4px)",
                  boxShadow: "0 14px 35px rgba(23,32,51,0.08)",
                },
              }}
            >
              <Stack
                direction="row"
                sx={{
                  mb: 3,
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <Box
                  sx={{
                    width: 48,
                    height: 48,
                    display: "grid",
                    placeItems: "center",
                    borderRadius: 2.5,
                    bgcolor: "#EEF2FF",
                    color: "primary.main",
                    fontSize: 25,
                    fontWeight: 700,
                  }}
                >
                  {feature.symbol}
                </Box>

                <Typography
                  variant="caption"
                  color="text.disabled"
                  sx={{ fontWeight: 700 }}
                >
                  {feature.number}
                </Typography>
              </Stack>

              <Typography variant="h6" sx={{ mb: 1 }}>
                {feature.title}
              </Typography>

              <Typography
                color="text.secondary"
                variant="body2"
                sx={{ lineHeight: 1.8 }}
              >
                {feature.description}
              </Typography>
            </Paper>
          ))}
        </Box>
      </Container>
    </Box>
  );
}
