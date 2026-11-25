import { useState } from "react";
import { useNavigate } from "react-router-dom";

import {
  Alert,
  Box,
  Button,
  Container,
  Pagination,
  Paper,
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

import NotificationList from "../components/NotificationList";
import NotificationLoading from "../components/NotificationLoading";
import NotificationEmptyState from "../components/NotificationEmptyState";

const PAGE_SIZE = 20;

function isInternalTargetUrl(targetUrl: string | null): targetUrl is string {
  return (
    targetUrl !== null &&
    targetUrl.startsWith("/") &&
    !targetUrl.startsWith("//")
  );
}

export default function NotificationsPage() {
  const navigate = useNavigate();

  const [page, setPage] = useState(0);

  const notificationsQuery = useNotifications(page, PAGE_SIZE);
  const unreadCountQuery = useUnreadNotificationCount();

  const markAsReadMutation = useMarkNotificationAsRead();
  const markAllAsReadMutation = useMarkAllNotificationsAsRead();

  const unreadCount = unreadCountQuery.data?.unreadCount ?? 0;

  async function handleNotificationClick(
    notification: NonNullable<
      typeof notificationsQuery.data
    >["content"][number],
  ) {
    markAsReadMutation.reset();

    if (!notification.read) {
      try {
        await markAsReadMutation.mutateAsync(notification.id);
      } catch {
        // Keep the user on the page if marking the notification fails.
        return;
      }
    }

    if (isInternalTargetUrl(notification.targetUrl)) {
      navigate(notification.targetUrl);
    }
  }

  function handleMarkAllAsRead() {
    markAllAsReadMutation.reset();
    markAllAsReadMutation.mutate();
  }

  const actionError = markAsReadMutation.error ?? markAllAsReadMutation.error;

  return (
    <Container maxWidth="md" sx={{ py: 5 }}>
      <Stack spacing={3}>
        {/* Page heading */}
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={2}
          sx={{
            alignItems: { xs: "flex-start", sm: "center" },
            justifyContent: "space-between",
          }}
        >
          <Stack direction="row" spacing={1.5} sx={{ alignItems: "center" }}>
            <NotificationsOutlinedIcon color="primary" sx={{ fontSize: 34 }} />

            <Box>
              <Typography variant="h4" component="h1">
                Notifications
              </Typography>

              <Typography color="text.secondary">
                Stay up to date with your projects and tasks.
              </Typography>
            </Box>
          </Stack>

          <Stack
            direction="row"
            spacing={1}
            sx={{ alignItems: "center", flexWrap: "wrap" }}
          >
            <Typography variant="body2" color="text.secondary">
              {unreadCountQuery.isPending
                ? "Loading unread count..."
                : `${unreadCount} unread`}
            </Typography>

            <Button
              variant="outlined"
              size="small"
              startIcon={<DoneAllOutlinedIcon />}
              onClick={handleMarkAllAsRead}
              disabled={unreadCount === 0 || markAllAsReadMutation.isPending}
            >
              Mark all as read
            </Button>
          </Stack>
        </Stack>

        {/* Notifications loading error */}
        {notificationsQuery.isError && (
          <Alert
            severity="error"
            action={
              <Button
                color="inherit"
                size="small"
                onClick={() => {
                  void notificationsQuery.refetch();
                }}
              >
                Retry
              </Button>
            }
          >
            {getApiErrorMessage(notificationsQuery.error)}
          </Alert>
        )}

        {/* Mutation errors */}
        {actionError && (
          <Alert severity="error">{getApiErrorMessage(actionError)}</Alert>
        )}

        {/* Unread count error */}
        {unreadCountQuery.isError && (
          <Alert severity="warning">
            Could not load your unread notification count.
          </Alert>
        )}

        {/* Notifications content */}
        {notificationsQuery.isPending && <NotificationLoading />}

        {notificationsQuery.data && !notificationsQuery.isError && (
          <Stack spacing={2}>
            {notificationsQuery.isFetching && (
              <Typography variant="caption" color="text.secondary">
                Updating notifications...
              </Typography>
            )}

            <Paper variant="outlined" sx={{ overflow: "hidden" }}>
              {notificationsQuery.data.empty ? (
                <NotificationEmptyState />
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
            </Paper>

            {/* Server-side pagination */}
            {notificationsQuery.data.totalPages > 1 && (
              <Box sx={{ display: "flex", justifyContent: "center", pt: 1 }}>
                <Pagination
                  count={notificationsQuery.data.totalPages}
                  page={page + 1}
                  onChange={(_, selectedPage) => {
                    setPage(selectedPage - 1);
                  }}
                  color="primary"
                  shape="rounded"
                  showFirstButton
                  showLastButton
                  disabled={notificationsQuery.isFetching}
                />
              </Box>
            )}

            <Typography
              variant="body2"
              color="text.secondary"
              sx={{ textAlign: "center" }}
            >
              {notificationsQuery.data.totalElements}{" "}
              {notificationsQuery.data.totalElements === 1
                ? "notification"
                : "notifications"}{" "}
              in total
            </Typography>
          </Stack>
        )}
      </Stack>
    </Container>
  );
}
