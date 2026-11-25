import { useQuery } from "@tanstack/react-query";

import { getUnreadNotificationCount } from "../api/notificationApi";
import { notificationQueryKeys } from "../notification.queryKeys";

export function useUnreadNotificationCount() {
  return useQuery({
    queryKey: notificationQueryKeys.unreadCount(),

    queryFn: ({ signal }) => getUnreadNotificationCount(signal),

    refetchInterval: 30_000,
  });
}
