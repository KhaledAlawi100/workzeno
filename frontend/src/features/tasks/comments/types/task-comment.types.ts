
export interface CreateTaskCommentRequest {
  content: string;
}

// ============================================================
// 2. TASK COMMENT RESPONSE
// ============================================================

export interface TaskComment {
  id: string;
  taskId: string;
  authorId: string;
  authorEmail: string;
  content: string;
  createdAt: string;
  updatedAt: string;
}
