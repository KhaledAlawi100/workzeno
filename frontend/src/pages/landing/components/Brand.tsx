import { Box, Stack, Typography } from "@mui/material";

type BrandProps = {
  light?: boolean;
};

export default function Brand({ light = false }: BrandProps) {
  return (
    <Stack direction="row" spacing={1.25} sx={{ alignItems: "center" }}>
      <Box
        aria-hidden="true"
        sx={{
          width: 36,
          height: 36,
          display: "grid",
          placeItems: "center",
          borderRadius: 2,
          bgcolor: light ? "rgba(255,255,255,0.14)" : "primary.main",
          color: "#fff",
          fontWeight: 800,
          fontSize: 19,
          boxShadow: light ? "none" : "0 5px 14px rgba(54,89,217,0.22)",
        }}
      >
        W
      </Box>

      <Typography
        variant="h6"
        sx={{
          fontWeight: 800,
          letterSpacing: "-0.04em",
          color: light ? "#fff" : "text.primary",
        }}
      >
        Workzeno
      </Typography>
    </Stack>
  );
}
