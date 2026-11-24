import { useState } from "react";

import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Pagination,
  Stack,
  Typography,
} from "@mui/material";

import ChatBubbleOutlineIcon from "@mui/icons-material/ChatBubbleOutlined";

import { getApiErrorMessage } from "../../../../lib/api";

import {
  COMMENT_PAGE_SIZE,
  useCreateTaskComment,
  useDeleteTaskComment,
  useTaskComments,
  useUpdateTaskComment,
} from "../hooks/taskCommentHooks";

import TaskCommentForm from "./TaskCommentForm";
import TaskCommentItem from "./TaskCommentItem";
import DeleteCommentDialog from "./DeleteCommentDialog";

interface TaskCommentsProps {
  projectId: string;
  taskId: string;
}

export default function TaskComments({ projectId, taskId }: TaskCommentsProps) {
  const [page, setPage] = useState(0);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const [commentToDelete, setCommentToDelete] = useState<string | null>(null);

  const commentsQuery = useTaskComments(
    projectId,
    taskId,
    page,
    COMMENT_PAGE_SIZE,
  );

  const createMutation = useCreateTaskComment(projectId, taskId);
  const updateMutation = useUpdateTaskComment(projectId, taskId);
  const deleteMutation = useDeleteTaskComment(projectId, taskId);

  const commentPage = commentsQuery.data;
  const comments = commentPage?.content ?? [];

  async function handleCreate(content: string) {
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      await createMutation.mutateAsync({ content });

      setPage(0);
      setSuccessMessage("Comment added successfully.");
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
      throw error;
    }
  }

  async function handleUpdate(commentId: string, content: string) {
    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      await updateMutation.mutateAsync({
        commentId,
        request: { content },
      });

      setSuccessMessage("Comment updated successfully.");
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
      throw error;
    }
  }

  async function handleDelete() {
    if (!commentToDelete) return;

    setErrorMessage(null);
    setSuccessMessage(null);

    try {
      await deleteMutation.mutateAsync(commentToDelete);

      setCommentToDelete(null);
      setSuccessMessage("Comment deleted successfully.");

      if (comments.length === 1 && page > 0) {
        setPage((currentPage) => currentPage - 1);
      }
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
    }
  }

  return (
    <Stack spacing={2.5} sx={{ mt: 3 }}>
      <Stack direction="row" spacing={1} sx={{ alignItems: "center" }}>
        <ChatBubbleOutlineIcon color="action" />

        <Typography variant="h6">
          Comments
          {commentPage && ` (${commentPage.totalElements})`}
        </Typography>
      </Stack>

      {errorMessage && (
        <Alert severity="error" onClose={() => setErrorMessage(null)}>
          {errorMessage}
        </Alert>
      )}

      {successMessage && (
        <Alert severity="success" onClose={() => setSuccessMessage(null)}>
          {successMessage}
        </Alert>
      )}

      <TaskCommentForm
        submitLabel="Post comment"
        submitting={createMutation.isPending}
        onSubmit={handleCreate}
      />

      {commentsQuery.isPending ? (
        <Box sx={{ display: "flex", justifyContent: "center", py: 3 }}>
          <CircularProgress size={28} />
        </Box>
      ) : commentsQuery.isError ? (
        <Alert
          severity="error"
          action={
            <Button
              color="inherit"
              size="small"
              onClick={() => void commentsQuery.refetch()}
            >
              Retry
            </Button>
          }
        >
          Could not load comments.
        </Alert>
      ) : comments.length === 0 ? (
        <Typography color="text.secondary" variant="body2" sx={{ py: 2 }}>
          No comments yet. Be the first to comment.
        </Typography>
      ) : (
        <Stack spacing={2}>
          {comments.map((comment) => (
            <TaskCommentItem
              key={comment.id}
              comment={comment}
              updating={
                updateMutation.isPending &&
                updateMutation.variables?.commentId === comment.id
              }
              deleting={
                deleteMutation.isPending &&
                deleteMutation.variables === comment.id
              }
              onUpdate={(content) => handleUpdate(comment.id, content)}
              onDelete={() => setCommentToDelete(comment.id)}
            />
          ))}
        </Stack>
      )}

      {commentPage && commentPage.totalPages > 1 && (
        <Box sx={{ display: "flex", justifyContent: "center" }}>
          <Pagination
            count={commentPage.totalPages}
            page={page + 1}
            onChange={(_, selectedPage) => setPage(selectedPage - 1)}
            color="primary"
            disabled={commentsQuery.isFetching}
          />
        </Box>
      )}

      <DeleteCommentDialog
        open={Boolean(commentToDelete)}
        deleting={deleteMutation.isPending}
        onCancel={() => setCommentToDelete(null)}
        onConfirm={() => void handleDelete()}
      />
    </Stack>
  );
}
