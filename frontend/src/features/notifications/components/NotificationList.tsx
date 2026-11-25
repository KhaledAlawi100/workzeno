import { Divider, List } from "@mui/material";

import NotificationItem from "./NotificationItem";

import type { NotificationResponse } from "../notification.types";

interface NotificationListProps {
  notifications: NotificationResponse[];
  onNotificationClick: (notification: NotificationResponse) => void;
  pendingNotificationId?: string | null;
}

export default function NotificationList({
  notifications,
  onNotificationClick,
  pendingNotificationId = null,
}: NotificationListProps) {
  return (
    <List disablePadding>
      {notifications.map((notification, index) => (
        <div key={notification.id}>
          <NotificationItem
            notification={notification}
            onClick={onNotificationClick}
            disabled={pendingNotificationId === notification.id}
          />

          {index < notifications.length - 1 && <Divider component="li" />}
        </div>
      ))}
    </List>
  );
}
