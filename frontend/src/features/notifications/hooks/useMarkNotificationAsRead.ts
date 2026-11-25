import { useMutation, useQueryClient } from "@tanstack/react-query";

import { markNotificationAsRead } from "../api/notificationApi";
import { notificationQueryKeys } from "../notification.queryKeys";

export function useMarkNotificationAsRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (notificationId: string) =>
      markNotificationAsRead(notificationId),

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
