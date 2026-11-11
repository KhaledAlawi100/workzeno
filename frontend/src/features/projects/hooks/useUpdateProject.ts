import { useMutation, useQueryClient } from "@tanstack/react-query";
import { updateProject } from "../api/projectApi";
import type { UpdateProjectRequest } from "../project.types";

interface UpdateProjectVariables {
  projectId: string;
  request: UpdateProjectRequest;
}

export function useUpdateProject() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ projectId, request }: UpdateProjectVariables) =>
      updateProject(projectId, request),

    onSuccess: async (updatedProject) => {
      // Refresh the individual project's cached details.
      queryClient.setQueryData(["project", updatedProject.id], updatedProject);

      // Refresh every cached projects-list page.
      await queryClient.invalidateQueries({
        queryKey: ["projects"],
      });
    },
  });
}
