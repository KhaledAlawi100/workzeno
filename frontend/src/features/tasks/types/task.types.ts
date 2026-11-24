export type TaskStatus = "TODO" | "IN_PROGRESS" | "IN_REVIEW" | "DONE";

export type TaskPriority = "LOW" | "MEDIUM" | "HIGH" | "URGENT";

export interface TaskAssignee {
  userId: string;
  email: string;
}

export interface Task {
  id: string;
  projectId: string;
  assignees: TaskAssignee[];
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface TaskSummary {
  id: string;
  projectId: string;
  title: string;
  description: string | null;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string | null;
  priority?: TaskPriority | null;
  dueDate?: string | null;
  assigneeEmails?: string[];
}

export interface UpdateTaskRequest {
  title: string;
  description?: string | null;
  priority: TaskPriority;
  status: TaskStatus;
  dueDate?: string | null;
}

export interface UpdateTaskStatusRequest {
  status: TaskStatus;
}

export interface AssignTaskRequest {
  email: string;
}
