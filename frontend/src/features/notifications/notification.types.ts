import type { Page } from "../../types/page.types";

export type NotificationType =
  | "TASK_ASSIGNED"
  | "PROJECT_INVITATION"
  | "INVITATION_ACCEPTED";

export interface NotificationResponse {
  id: string;
  type: NotificationType;
  title: string;
  message: string;
  targetUrl: string | null;
  referenceType: string | null;
  referenceId: string | null;
  actorEmail: string | null;
  readAt: string | null;
  createdAt: string;
  read: boolean;
}

export interface UnreadNotificationCountResponse {
  unreadCount: number;
}

export interface MarkAllNotificationsAsReadResponse {
  updatedCount: number;
}

export type NotificationPage = Page<NotificationResponse>;
