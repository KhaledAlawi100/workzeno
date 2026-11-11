import { Link } from "react-router-dom";
import { Box, Button, Typography } from "@mui/material";

export default function NotFoundPage() {
  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        textAlign: "center",
        px: 2,
        gap: 2,
      }}
    >
      <Typography variant="h2" component="h1">
        404
      </Typography>

      <Typography variant="h5">Page not found</Typography>

      <Typography color="text.secondary">
        The page you're looking for doesn't exist or may have moved.
      </Typography>

      <Button component={Link} to="/" variant="contained">
        Back to home
      </Button>
    </Box>
  );
}
