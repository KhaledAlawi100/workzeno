import { apiClient, type ApiResponse } from "../../../lib/api";

import type {
  MarkAllNotificationsAsReadResponse,
  NotificationPage,
  NotificationResponse,
  UnreadNotificationCountResponse,
} from "../notification.types";

export async function getNotifications(
  page = 0,
  size = 20,
  signal?: AbortSignal,
): Promise<NotificationPage> {
  const response = await apiClient.get<ApiResponse<NotificationPage>>(
    "/notifications",
    {
      params: {
        page,
        size,
      },
      signal,
    },
  );

  return response.data.data;
}

export async function getUnreadNotificationCount(
  signal?: AbortSignal,
): Promise<UnreadNotificationCountResponse> {
  const response = await apiClient.get<
    ApiResponse<UnreadNotificationCountResponse>
  >("/notifications/unread-count", {
    signal,
  });

  return response.data.data;
}

export async function markNotificationAsRead(
  notificationId: string,
): Promise<NotificationResponse> {
  const response = await apiClient.patch<ApiResponse<NotificationResponse>>(
    `/notifications/${encodeURIComponent(notificationId)}/read`,
  );

  return response.data.data;
}

export async function markAllNotificationsAsRead(): Promise<MarkAllNotificationsAsReadResponse> {
  const response = await apiClient.patch<
    ApiResponse<MarkAllNotificationsAsReadResponse>
  >("/notifications/read-all");

  return response.data.data;
}
