import type { TaskSummary } from "../tasks/types/task.types";

export interface ProjectMetrics {
  total: number;
  active: number;
  archived: number;
}

export interface TaskMetrics {
  total: number;
  todo: number;
  inProgress: number;
  inReview: number;
  done: number;
  overdue: number;
  assignedToMe: number;
}

export interface DashboardData {
  projects: ProjectMetrics;
  tasks: TaskMetrics;
  myTasks: TaskSummary[];
}
