import {
  Avatar,
  Box,
  Button,
  Divider,
  Drawer,
  IconButton,
  List,
  ListItemButton,
  ListItemText,
  Stack,
  Typography,
} from "@mui/material";

import LogoutOutlinedIcon from "@mui/icons-material/LogoutOutlined";

import { Link as RouterLink, useLocation } from "react-router-dom";

import type { UserProfile } from "../../../features/auth/api/authApi";

import { appLinks, publicLinks } from "./navigation.config";
import type { NavbarVariant } from "./navbar.types";

import WorkzenoBrand from "./WorkzenoBrand";

type MobileNavigationProps = {
  variant: NavbarVariant;
  user: UserProfile | null;
  open: boolean;
  isLoggingOut: boolean;
  onClose: () => void;
  onLogout: () => void;
};

export default function MobileNavigation({
  variant,
  user,
  open,
  isLoggingOut,
  onClose,
  onLogout,
}: MobileNavigationProps) {
  const location = useLocation();

  const isPublic = variant === "public";

  
  function isLinkActive(path: string): boolean {
    if (path === "/projects") {
      return (
        location.pathname === "/projects" ||
        location.pathname.startsWith("/projects/")
      );
    }

    return location.pathname === path;
  }

  return (
    <Drawer
      anchor="right"
      open={open}
      onClose={onClose}
      sx={{
        "& .MuiDrawer-paper": {
          width: "min(320px, 85vw)",
          p: 2,
        },
      }}
    >
      <Stack spacing={2} sx={{ height: "100%" }}>
        {/* Drawer header */}
        <Box
          sx={{
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <WorkzenoBrand />

          <IconButton aria-label="Close navigation menu" onClick={onClose}>
            <Typography component="span" sx={{ fontSize: 22 }}>
              ×
            </Typography>
          </IconButton>
        </Box>

        <Divider />

        {isPublic ? (
          <>
            {/* Public navigation links */}
            <List disablePadding>
              {publicLinks.map((item) => (
                <ListItemButton
                  key={item.label}
                  component={RouterLink}
                  to={item.to}
                  onClick={onClose}
                  selected={item.to === "/" && location.pathname === "/"}
                  sx={{ borderRadius: 2 }}
                >
                  <ListItemText primary={item.label} />
                </ListItemButton>
              ))}
            </List>

            {/* Public authentication actions */}
            <Box sx={{ mt: "auto" }}>
              <Button
                component={RouterLink}
                to="/login"
                fullWidth
                color="inherit"
                onClick={onClose}
                sx={{ mb: 1 }}
              >
                Sign in
              </Button>

              <Button
                component={RouterLink}
                to="/register"
                fullWidth
                variant="contained"
                onClick={onClose}
              >
                Get started
              </Button>
            </Box>
          </>
        ) : (
          <>
            {/* Authenticated user information */}
            <Box sx={{ px: 1, py: 1 }}>
              <Box
                sx={{
                  display: "flex",
                  flexDirection: "row",
                  gap: 1.5,
                  alignItems: "center",
                }}
              >
                <Avatar sx={{ bgcolor: "primary.main" }}>
                  {user?.email?.charAt(0).toUpperCase() ?? "U"}
                </Avatar>

                <Box sx={{ minWidth: 0 }}>
                  <Typography variant="body2" noWrap sx={{ fontWeight: 700 }}>
                    {user?.email ?? "My account"}
                  </Typography>

                  <Typography variant="caption" color="text.secondary">
                    {user?.role ?? "User"}
                  </Typography>
                </Box>
              </Box>
            </Box>

            <Divider />

            {/* Authenticated navigation links */}
            <List disablePadding>
              {appLinks.map((item) => (
                <ListItemButton
                  key={item.to}
                  component={RouterLink}
                  to={item.to}
                  onClick={onClose}
                  selected={isLinkActive(item.to)}
                  sx={{ borderRadius: 2, mb: 0.5 }}
                >
                  {item.icon}

                  <ListItemText primary={item.label} sx={{ ml: 1.5 }} />
                </ListItemButton>
              ))}
            </List>

            <Divider />

            {/* Logout */}
            <List disablePadding>
              <ListItemButton
                onClick={onLogout}
                disabled={isLoggingOut}
                sx={{ borderRadius: 2 }}
              >
                <LogoutOutlinedIcon fontSize="small" />

                <ListItemText
                  primary={isLoggingOut ? "Signing out..." : "Logout"}
                  sx={{ ml: 1.5 }}
                />
              </ListItemButton>
            </List>
          </>
        )}
      </Stack>
    </Drawer>
  );
}
