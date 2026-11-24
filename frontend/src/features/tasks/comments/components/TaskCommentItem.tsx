import { useState } from "react";

import { Avatar, Box, Button, Divider, Stack, Typography } from "@mui/material";

import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutlined";

import { useAuth } from "../../../auth/AuthContext";

import type { TaskComment } from "../types/task-comment.types";

import TaskCommentForm from "./TaskCommentForm";

interface TaskCommentItemProps {
  comment: TaskComment;
  updating: boolean;
  deleting: boolean;
  onUpdate: (content: string) => Promise<void>;
  onDelete: () => void;
}

function formatCommentDate(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(date);
}

export default function TaskCommentItem({
  comment,
  updating,
  deleting,
  onUpdate,
  onDelete,
}: TaskCommentItemProps) {
  const { user } = useAuth();

  const [editing, setEditing] = useState(false);

  // Only the author can see the edit and delete actions.
  // The backend independently enforces this authorization.
  const isAuthor = user?.id === comment.authorId;

  const wasEdited =
    new Date(comment.updatedAt).getTime() >
    new Date(comment.createdAt).getTime();

  const authorInitial = comment.authorEmail.charAt(0).toUpperCase() || "?";

  async function handleUpdate(content: string) {
    await onUpdate(content);
    setEditing(false);
  }

  return (
    <Stack spacing={1.5}>
      <Stack direction="row" spacing={1.5} sx={{ alignItems: "flex-start" }}>
        <Avatar
          sx={{
            width: 36,
            height: 36,
            fontSize: 14,
          }}
        >
          {authorInitial}
        </Avatar>

        <Box sx={{ flex: 1, minWidth: 0 }}>
          <Stack
            direction={{ xs: "column", sm: "row" }}
            spacing={{ xs: 0.25, sm: 1 }}
            sx={{
              alignItems: { xs: "flex-start", sm: "center" },
            }}
          >
            <Typography
              variant="subtitle2"
              sx={{
                fontWeight: 700,
                overflowWrap: "anywhere",
              }}
            >
              {comment.authorEmail}
            </Typography>

            <Typography variant="caption" color="text.secondary">
              {formatCommentDate(comment.createdAt)}
            </Typography>
          </Stack>

          {editing ? (
            <Box sx={{ mt: 1 }}>
              <TaskCommentForm
                key={`${comment.id}-${comment.content}`}
                initialContent={comment.content}
                submitLabel="Save changes"
                submitting={updating}
                onSubmit={handleUpdate}
                onCancel={() => setEditing(false)}
              />{" "}
            </Box>
          ) : (
            <Typography
              variant="body2"
              sx={{
                mt: 0.75,
                whiteSpace: "pre-wrap",
                overflowWrap: "anywhere",
              }}
            >
              {comment.content}
            </Typography>
          )}

          {!editing && (
            <Box
              sx={{
                mt: 1,
                display: "flex",
                alignItems: "center",
                gap: 1,
              }}
            >
              {wasEdited && (
                <Typography variant="caption" color="text.secondary">
                  Edited
                </Typography>
              )}

              {isAuthor && (
                <>
                  <Button
                    size="small"
                    color="inherit"
                    startIcon={<EditOutlinedIcon />}
                    onClick={() => setEditing(true)}
                    disabled={updating || deleting}
                  >
                    Edit
                  </Button>

                  <Button
                    size="small"
                    color="error"
                    startIcon={<DeleteOutlineIcon />}
                    onClick={onDelete}
                    disabled={updating || deleting}
                  >
                    Delete
                  </Button>
                </>
              )}
            </Box>
          )}
        </Box>
      </Stack>

      <Divider />
    </Stack>
  );
}
