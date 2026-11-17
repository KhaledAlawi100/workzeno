import { Box, Button, Container, Paper, Typography } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";

export default function AboutSection() {
  return (
    <Box
      id="about"
      component="section"
      sx={{
        py: { xs: 8, md: 11 },
        scrollMarginTop: "88px",
      }}
    >
      <Container maxWidth="md">
        <Paper
          elevation={0}
          sx={{
            p: { xs: 3, sm: 5, md: 6 },
            borderRadius: 4,
            color: "#fff",
            background:
              "linear-gradient(125deg, #243FAD 0%, #3659D9 55%, #647DE5 100%)",
            position: "relative",
            overflow: "hidden",
          }}
        >
          {/* Decorative background circle */}
          <Box
            aria-hidden="true"
            sx={{
              position: "absolute",
              width: 260,
              height: 260,
              border: "1px solid rgba(255,255,255,0.16)",
              borderRadius: "50%",
              right: -90,
              top: -120,
              boxShadow:
                "0 0 0 35px rgba(255,255,255,0.04), 0 0 0 70px rgba(255,255,255,0.03)",
            }}
          />

          <Box sx={{ position: "relative", maxWidth: 650 }}>
            <Typography
              variant="overline"
              sx={{ color: "rgba(255,255,255,0.75)", fontWeight: 800 }}
            >
              BUILT FOR COLLABORATION
            </Typography>

            <Typography
              component="h2"
              variant="h3"
              sx={{
                mt: 1,
                fontSize: { xs: "2rem", sm: "2.65rem" },
                lineHeight: 1.2,
              }}
            >
              Good projects are built together.
            </Typography>

            <Typography
              sx={{
                mt: 2,
                color: "rgba(255,255,255,0.82)",
                lineHeight: 1.9,
                maxWidth: 570,
              }}
            >
              Give your team a shared place to organize projects, invite
              collaborators, and work with clear responsibilities. Everyone
              knows where the work stands and how they can contribute.
            </Typography>

            <Button
              component={RouterLink}
              to="/register"
              variant="contained"
              size="large"
              sx={{
                mt: 3.5,
                bgcolor: "#fff",
                color: "primary.dark",
                "&:hover": { bgcolor: "#EEF2FF" },
              }}
            >
              Start with Workzeno
            </Button>
          </Box>
        </Paper>
      </Container>
    </Box>
  );
}
