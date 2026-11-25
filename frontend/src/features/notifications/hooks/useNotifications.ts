import { useQuery } from "@tanstack/react-query";

import { getNotifications } from "../api/notificationApi";
import { notificationQueryKeys } from "../notification.queryKeys";

export function useNotifications(page = 0, size = 20) {
  return useQuery({
    queryKey: notificationQueryKeys.list(page, size),

    queryFn: ({ signal }) => getNotifications(page, size, signal),
  });
}
