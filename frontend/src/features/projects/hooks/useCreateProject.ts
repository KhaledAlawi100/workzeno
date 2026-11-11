import { useMutation, useQueryClient } from "@tanstack/react-query";

import { createProject } from "../api/projectApi";
import type { CreateProjectRequest } from "../project.types";

export function useCreateProject() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (request: CreateProjectRequest) => createProject(request),

    onSuccess: async () => {
      // Refresh every cached projects page after creation.
      await queryClient.invalidateQueries({
        queryKey: ["projects"],
      });
    },
  });
}
