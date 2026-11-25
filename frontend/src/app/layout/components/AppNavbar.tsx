
import { useState } from "react";

import {
  AppBar,
  Box,
  Divider,
  IconButton,
  Toolbar,
} from "@mui/material";

import MenuIcon from "@mui/icons-material/Menu";

import { useNavigate } from "react-router-dom";

import { useAuth } from "../../../features/auth/AuthContext";
import NotificationMenu from "../../../features/notifications/components/NotificationMenu";

import type { NavbarVariant } from "./navbar.types";

import WorkzenoBrand from "./WorkzenoBrand";
import PublicNavigation from "./PublicNavigation";
import AuthenticatedNavigation from "./AuthenticatedNavigation";
import AccountMenu from "./AccountMenu";
import MobileNavigation from "./MobileNavigation";

type AppNavbarProps = {
  variant: NavbarVariant;
};

export default function AppNavbar({ variant }: AppNavbarProps) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [mobileDrawerOpen, setMobileDrawerOpen] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const isPublic = variant === "public";

  function closeMobileDrawer() {
    setMobileDrawerOpen(false);
  }

  async function handleLogout(): Promise<void> {
    if (isLoggingOut) {
      return;
    }

    setIsLoggingOut(true);
    closeMobileDrawer();

    try {
      await logout();

      // Replace the protected page in browser history after logout.
      navigate("/login", { replace: true });
    } finally {
      setIsLoggingOut(false);
    }
  }

  return (
    <>
      <AppBar
        position="sticky"
        color="inherit"
        elevation={0}
        sx={{
          bgcolor: "rgba(255,255,255,0.94)",
          backdropFilter: "blur(12px)",
          borderBottom: "1px solid",
          borderColor: "divider",
          zIndex: (theme) => theme.zIndex.appBar,
        }}
      >
        <Toolbar
          disableGutters
          sx={{
            minHeight: "72px !important",
            px: { xs: 2, sm: 3 },
          }}
        >
          <Box
            sx={{
              width: "100%",
              maxWidth: "1200px",
              mx: "auto",
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              gap: 2,
            }}
          >
            <WorkzenoBrand />

            {/* Desktop navigation */}
            <Box
              sx={{
                display: { xs: "none", md: "flex" },
                alignItems: "center",
                gap: 2,
              }}
            >
              {isPublic ? (
                <PublicNavigation />
              ) : (
                <>
                  <AuthenticatedNavigation />

                  <NotificationMenu />

                  <Divider orientation="vertical" flexItem />

                  <AccountMenu
                    user={user}
                    isLoggingOut={isLoggingOut}
                    onLogout={() => void handleLogout()}
                  />
                </>
              )}
            </Box>

            {/* Mobile menu button */}
            <IconButton
              aria-label="Open navigation menu"
              onClick={() => setMobileDrawerOpen(true)}
              sx={{ display: { xs: "inline-flex", md: "none" } }}
            >
              <MenuIcon />
            </IconButton>
          </Box>
        </Toolbar>
      </AppBar>

      {/* Responsive mobile navigation */}
      <MobileNavigation
        variant={variant}
        user={user}
        open={mobileDrawerOpen}
        isLoggingOut={isLoggingOut}
        onClose={closeMobileDrawer}
        onLogout={() => void handleLogout()}
      />
    </>
  );
}
