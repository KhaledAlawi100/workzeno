import { Button, Divider, Stack } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";

import { publicLinks } from "./navigation.config";

export default function PublicNavigation() {
  return (
    <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
      {publicLinks.map((item) => (
        <Button
          key={item.label}
          component={RouterLink}
          to={item.to}
          color="inherit"
          sx={{
            color: "text.secondary",
            fontWeight: 600,
            "&:hover": {
              color: "primary.main",
              bgcolor: "action.hover",
            },
          }}
        >
          {item.label}
        </Button>
      ))}

      <Divider orientation="vertical" flexItem sx={{ mx: 1 }} />

      <Button
        component={RouterLink}
        to="/login"
        color="inherit"
        sx={{ fontWeight: 700 }}
      >
        Sign in
      </Button>

      <Button component={RouterLink} to="/register" variant="contained">
        Get started
      </Button>
    </Stack>
  );
}
