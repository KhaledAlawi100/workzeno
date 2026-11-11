import { useMutation, useQueryClient } from "@tanstack/react-query";

import { archiveProject, restoreProject } from "../api/projectApi";

interface ProjectStatusVariables {
  projectId: string;
}

export function useProjectStatus() {
  const queryClient = useQueryClient();

  const updateProjectCache = async (
    updatedProject: Awaited<ReturnType<typeof archiveProject>>,
  ) => {
    // Update the individual project's cached details immediately.
    queryClient.setQueryData(["project", updatedProject.id], updatedProject);

    // Refresh all cached projects-list queries.
    await queryClient.invalidateQueries({
      queryKey: ["projects"],
    });
  };

  const archiveMutation = useMutation({
    mutationFn: ({ projectId }: ProjectStatusVariables) =>
      archiveProject(projectId),

    onSuccess: updateProjectCache,
  });

  const restoreMutation = useMutation({
    mutationFn: ({ projectId }: ProjectStatusVariables) =>
      restoreProject(projectId),

    onSuccess: updateProjectCache,
  });

  return {
    archiveMutation,
    restoreMutation,
  };
}
