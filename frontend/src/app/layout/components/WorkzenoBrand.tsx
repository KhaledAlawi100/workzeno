import { Box, Typography } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";

export default function WorkzenoBrand() {
  return (
    <RouterLink
      to="/"
      aria-label="Workzeno home"
      style={{
        textDecoration: "none",
        flexShrink: 0,
        color: "inherit",
      }}
    >
      <Box
        sx={{
          display: "flex",
          alignItems: "center",
          gap: 1.25,
        }}
      >
        <Box
          sx={{
            width: 36,
            height: 36,
            display: "grid",
            placeItems: "center",
            borderRadius: 2,
            bgcolor: "primary.main",
            color: "#fff",
            fontSize: 19,
            fontWeight: 800,
            boxShadow: "0 4px 12px rgba(54,89,217,0.2)",
          }}
        >
          W
        </Box>

        <Typography
          variant="h6"
          sx={{
            fontWeight: 800,
            letterSpacing: "-0.04em",
          }}
        >
          Workzeno
        </Typography>
      </Box>
    </RouterLink>
  );
}
