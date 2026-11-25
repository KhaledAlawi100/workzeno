import { useMutation, useQueryClient } from "@tanstack/react-query";

import { markAllNotificationsAsRead } from "../api/notificationApi";
import { notificationQueryKeys } from "../notification.queryKeys";

export function useMarkAllNotificationsAsRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: markAllNotificationsAsRead,

    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: notificationQueryKeys.lists(),
        }),

        queryClient.invalidateQueries({
          queryKey: notificationQueryKeys.unreadCount(),
        }),
      ]);
    },
  });
}
