import { Button, Stack } from "@mui/material";
import { NavLink, useLocation } from "react-router-dom";

import { appLinks } from "./navigation.config";

export default function AuthenticatedNavigation() {
  const location = useLocation();

  return (
    <Stack direction="row" spacing={0.5} sx={{ alignItems: "center" }}>
      {appLinks.map((item) => {
        const isActive =
          item.to === "/projects"
            ? location.pathname === "/projects" ||
              location.pathname.startsWith("/projects/")
            : location.pathname === item.to;

        return (
          <Button
            key={item.to}
            component={NavLink}
            to={item.to}
            startIcon={item.icon}
            color="inherit"
            sx={{
              px: 1.5,
              color: isActive ? "primary.main" : "text.secondary",
              bgcolor: isActive ? "rgba(54,89,217,0.08)" : "transparent",
              fontWeight: isActive ? 700 : 600,
              "&:hover": {
                bgcolor: "rgba(54,89,217,0.08)",
                color: "primary.main",
              },
            }}
          >
            {item.label}
          </Button>
        );
      })}
    </Stack>
  );
}
