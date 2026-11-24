/**
 * ============================================================
 * TASK QUERY KEYS
 * ============================================================
 *
 * Centralized React Query cache keys for the Tasks feature.
 *
 * Benefits:
 * - Prevents inconsistent query keys across components.
 * - Keeps task cache isolated by project.
 * - Supports targeted cache invalidation.
 * - Makes it easy to invalidate all tasks for a project.
 * - Keeps task comments separate from task data.
 */

export const taskQueryKeys = {
  all: ["tasks"] as const,

  project: (projectId: string) =>
    [...taskQueryKeys.all, "project", projectId] as const,

  lists: (projectId: string) =>
    [...taskQueryKeys.project(projectId), "list"] as const,

  list: (projectId: string, page: number, size: number) =>
    [...taskQueryKeys.lists(projectId), { page, size }] as const,

  details: (projectId: string) =>
    [...taskQueryKeys.project(projectId), "detail"] as const,

  detail: (projectId: string, taskId: string) =>
    [...taskQueryKeys.details(projectId), taskId] as const,

  comments: (projectId: string) =>
    [...taskQueryKeys.project(projectId), "comments"] as const,

  taskComments: (projectId: string, taskId: string) =>
    [...taskQueryKeys.comments(projectId), taskId] as const,

  commentList: (
    projectId: string,
    taskId: string,
    page: number,
    size: number,
  ) =>
    [
      ...taskQueryKeys.taskComments(projectId, taskId),
      "list",
      { page, size },
    ] as const,
};
