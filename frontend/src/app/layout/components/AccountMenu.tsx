import { useState } from "react";
import type { MouseEvent } from "react";

import {
  Avatar,
  Box,
  Button,
  Chip,
  Divider,
  Menu,
  MenuItem,
  Tooltip,
  Typography,
} from "@mui/material";

import AccountCircleOutlinedIcon from "@mui/icons-material/AccountCircleOutlined";
import LogoutOutlinedIcon from "@mui/icons-material/LogoutOutlined";

import type { UserProfile } from "../../../features/auth/api/authApi";

type AccountMenuProps = {
  user: UserProfile | null;
  isLoggingOut: boolean;
  onLogout: () => void;
};

export default function AccountMenu({
  user,
  isLoggingOut,
  onLogout,
}: AccountMenuProps) {
  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);

  const isOpen = Boolean(anchorEl);

  function openMenu(event: MouseEvent<HTMLElement>) {
    setAnchorEl(event.currentTarget);
  }

  function closeMenu() {
    setAnchorEl(null);
  }

  function handleLogout() {
    closeMenu();
    onLogout();
  }

  return (
    <>
      <Tooltip title="Account settings">
        <Button
          onClick={openMenu}
          color="inherit"
          startIcon={
            <Avatar
              sx={{
                width: 32,
                height: 32,
                bgcolor: "primary.main",
                fontSize: 13,
              }}
            >
              {user?.email?.charAt(0).toUpperCase() ?? "U"}
            </Avatar>
          }
          endIcon={<AccountCircleOutlinedIcon />}
          sx={{
            color: "text.primary",
            textTransform: "none",
            textAlign: "left",
          }}
        >
          <Box sx={{ display: "flex", flexDirection: "column" }}>
            <Typography
              variant="body2"
              sx={{ maxWidth: 150, fontWeight: 700 }}
              noWrap
            >
              {user?.email ?? "My account"}
            </Typography>

            <Typography
              variant="caption"
              color="text.secondary"
              sx={{ textAlign: "left" }}
            >
              {user?.role ?? "User"}
            </Typography>
          </Box>
        </Button>
      </Tooltip>

      <Menu
        anchorEl={anchorEl}
        open={isOpen}
        onClose={closeMenu}
        anchorOrigin={{
          vertical: "bottom",
          horizontal: "right",
        }}
        transformOrigin={{
          vertical: "top",
          horizontal: "right",
        }}
        slotProps={{
          paper: {
            sx: {
              mt: 1,
              minWidth: 240,
              borderRadius: 2,
            },
          },
        }}
      >
        <Box sx={{ px: 2, py: 1.5 }}>
          <Typography variant="body2" sx={{ fontWeight: 700 }} noWrap>
            {user?.email ?? "My account"}
          </Typography>

          <Chip
            label={user?.role ?? "User"}
            size="small"
            color="primary"
            variant="outlined"
            sx={{ mt: 1 }}
          />
        </Box>

        <Divider />

        <MenuItem onClick={handleLogout} disabled={isLoggingOut}>
          <LogoutOutlinedIcon fontSize="small" sx={{ mr: 1.5 }} />

          {isLoggingOut ? "Signing out..." : "Logout"}
        </MenuItem>
      </Menu>
    </>
  );
}
