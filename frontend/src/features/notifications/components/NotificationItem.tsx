import {
  Box,
  ListItemButton,
  ListItemIcon,
  Stack,
  Typography,
} from "@mui/material";

import AssignmentOutlinedIcon from "@mui/icons-material/AssignmentOutlined";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutlined";
import MailOutlineIcon from "@mui/icons-material/MailOutlined";

import type { ReactNode } from "react";

import type { NotificationResponse } from "../notification.types";

interface NotificationItemProps {
  notification: NotificationResponse;
  onClick: (notification: NotificationResponse) => void;
  disabled?: boolean;
}

function getNotificationIcon(type: NotificationResponse["type"]): ReactNode {
  switch (type) {
    case "TASK_ASSIGNED":
      return <AssignmentOutlinedIcon color="primary" />;

    case "PROJECT_INVITATION":
      return <MailOutlineIcon color="primary" />;

    case "INVITATION_ACCEPTED":
      return <CheckCircleOutlineIcon color="success" />;

    default:
      return null;
  }
}

function formatNotificationDate(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "";
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export default function NotificationItem({
  notification,
  onClick,
  disabled = false,
}: NotificationItemProps) {
  return (
    <ListItemButton
      onClick={() => onClick(notification)}
      disabled={disabled}
      alignItems="flex-start"
      sx={{
        px: 2,
        py: 1.75,
        gap: 1,
        bgcolor: notification.read ? "background.paper" : "action.hover",
        "&:hover": {
          bgcolor: "action.selected",
        },
      }}
    >
      <ListItemIcon
        sx={{
          minWidth: 38,
          mt: 0.25,
        }}
      >
        {getNotificationIcon(notification.type)}
      </ListItemIcon>

      <Stack spacing={0.5} sx={{ flex: 1, minWidth: 0 }}>
        <Stack
          direction="row"
          spacing={1}
          sx={{
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <Typography
            variant="body2"
            sx={{
              fontWeight: notification.read ? 500 : 700,
              minWidth: 0,
            }}
          >
            {notification.title}
          </Typography>

          {!notification.read && (
            <Box
              aria-label="Unread notification"
              sx={{
                width: 9,
                height: 9,
                borderRadius: "50%",
                bgcolor: "primary.main",
                flexShrink: 0,
              }}
            />
          )}
        </Stack>

        <Typography
          variant="body2"
          color="text.secondary"
          sx={{
            display: "-webkit-box",
            WebkitBoxOrient: "vertical",
            WebkitLineClamp: 2,
            overflow: "hidden",
            overflowWrap: "anywhere",
          }}
        >
          {notification.message}
        </Typography>

        <Typography variant="caption" color="text.secondary">
          {formatNotificationDate(notification.createdAt)}
        </Typography>
      </Stack>
    </ListItemButton>
  );
}
