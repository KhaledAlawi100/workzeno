export const notificationQueryKeys = {
  all: ["notifications"] as const,

  lists: () => [...notificationQueryKeys.all, "list"] as const,

  list: (page: number, size: number) =>
    [...notificationQueryKeys.lists(), { page, size }] as const,

  unreadCount: () => [...notificationQueryKeys.all, "unread-count"] as const,
};
