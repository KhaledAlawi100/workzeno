import { useState } from "react";
import type { MouseEvent } from "react";
import { useNavigate } from "react-router-dom";

import {
  Alert,
  Badge,
  Box,
  Button,
  Divider,
  IconButton,
  Menu,
  Stack,
  Typography,
} from "@mui/material";

import NotificationsOutlinedIcon from "@mui/icons-material/NotificationsOutlined";
import DoneAllOutlinedIcon from "@mui/icons-material/DoneAllOutlined";

import { getApiErrorMessage } from "../../../lib/api";

import { useNotifications } from "../hooks/useNotifications";
import { useUnreadNotificationCount } from "../hooks/useUnreadNotificationCount";
import { useMarkNotificationAsRead } from "../hooks/useMarkNotificationAsRead";
import { useMarkAllNotificationsAsRead } from "../hooks/useMarkAllNotificationsAsRead";

import NotificationList from "./NotificationList";
import NotificationLoading from "./NotificationLoading";
import NotificationEmptyState from "./NotificationEmptyState";

import type { NotificationResponse } from "../notification.types";

const PREVIEW_SIZE = 5;

function isInternalTargetUrl(targetUrl: string | null): targetUrl is string {
  return (
    targetUrl !== null &&
    targetUrl.startsWith("/") &&
    !targetUrl.startsWith("//")
  );
}

export default function NotificationMenu() {
  const navigate = useNavigate();

  const [anchorEl, setAnchorEl] = useState<HTMLElement | null>(null);

  const isOpen = Boolean(anchorEl);

  const notificationsQuery = useNotifications(0, PREVIEW_SIZE);
  const unreadCountQuery = useUnreadNotificationCount();

  const markAsReadMutation = useMarkNotificationAsRead();
  const markAllAsReadMutation = useMarkAllNotificationsAsRead();

  const unreadCount = unreadCountQuery.data?.unreadCount ?? 0;

  function handleOpen(event: MouseEvent<HTMLElement>) {
    setAnchorEl(event.currentTarget);
  }

  function handleClose() {
    setAnchorEl(null);
  }

  async function handleNotificationClick(notification: NotificationResponse) {
    if (!notification.read) {
      try {
        await markAsReadMutation.mutateAsync(notification.id);
      } catch {
        return;
      }
    }

    handleClose();

    if (isInternalTargetUrl(notification.targetUrl)) {
      navigate(notification.targetUrl);
    }
  }

  function handleMarkAllAsRead() {
    markAllAsReadMutation.mutate();
  }

  function handleViewAll() {
    handleClose();
    navigate("/notifications");
  }

  const mutationError = markAsReadMutation.error ?? markAllAsReadMutation.error;

  return (
    <>
      <IconButton
        aria-label={
          unreadCount > 0
            ? `Notifications, ${unreadCount} unread`
            : "Notifications"
        }
        aria-haspopup="true"
        aria-expanded={isOpen}
        onClick={handleOpen}
        color="inherit"
        sx={{ color: "text.secondary" }}
      >
        <Badge
          badgeContent={unreadCount}
          color="error"
          max={99}
          overlap="circular"
        >
          <NotificationsOutlinedIcon />
        </Badge>
      </IconButton>

      <Menu
        anchorEl={anchorEl}
        open={isOpen}
        onClose={handleClose}
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
              width: { xs: "calc(100vw - 32px)", sm: 380 },
              maxWidth: "calc(100vw - 32px)",
              maxHeight: 520,
              overflowY: "auto",
              borderRadius: 2,
            },
          },
        }}
      >
        <Stack spacing={1}>
          <Box
            sx={{
              px: 2,
              pt: 1.5,
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              gap: 1,
            }}
          >
            <Box>
              <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                Notifications
              </Typography>

              <Typography variant="caption" color="text.secondary">
                {unreadCountQuery.isPending
                  ? "Loading unread count..."
                  : `${unreadCount} unread`}
              </Typography>
            </Box>

            <Button
              size="small"
              startIcon={<DoneAllOutlinedIcon />}
              onClick={handleMarkAllAsRead}
              disabled={unreadCount === 0 || markAllAsReadMutation.isPending}
            >
              Mark all read
            </Button>
          </Box>

          <Divider />

          {notificationsQuery.isError && (
            <Alert severity="error" sx={{ mx: 2 }}>
              Could not load notifications.
            </Alert>
          )}

          {mutationError && (
            <Alert severity="error" sx={{ mx: 2 }}>
              {getApiErrorMessage(mutationError)}
            </Alert>
          )}

          {notificationsQuery.isPending && <NotificationLoading count={3} />}

          {notificationsQuery.data && !notificationsQuery.isError && (
            <>
              {notificationsQuery.data.empty ? (
                <NotificationEmptyState
                  title="No notifications yet"
                  description="Updates about your projects and tasks will appear here."
                />
              ) : (
                <NotificationList
                  notifications={notificationsQuery.data.content}
                  onNotificationClick={(notification) => {
                    void handleNotificationClick(notification);
                  }}
                  pendingNotificationId={
                    markAsReadMutation.isPending
                      ? markAsReadMutation.variables
                      : null
                  }
                />
              )}
            </>
          )}

          <Divider />

          <Box sx={{ px: 2, pb: 1 }}>
            <Button
              fullWidth
              onClick={handleViewAll}
              disabled={notificationsQuery.isError}
            >
              View all notifications
            </Button>
          </Box>
        </Stack>
      </Menu>
    </>
  );
}
