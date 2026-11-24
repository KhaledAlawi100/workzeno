import { useState } from "react";

import { Button, Stack, TextField } from "@mui/material";

interface TaskCommentFormProps {
  initialContent?: string;
  submitLabel: string;
  submitting?: boolean;
  onSubmit: (content: string) => Promise<void>;
  onCancel?: () => void;
}

const MAX_COMMENT_LENGTH = 2000;

export default function TaskCommentForm({
  initialContent = "",
  submitLabel,
  submitting = false,
  onSubmit,
  onCancel,
}: TaskCommentFormProps) {
  // Initialize the editor with the supplied content.
  // React uses this value when the component first mounts.
  const [content, setContent] = useState(initialContent);

  // Keep validation errors local to this form.
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit() {
    const normalizedContent = content.trim();

    if (!normalizedContent) {
      setError("Comment cannot be empty.");
      return;
    }

    if (normalizedContent.length > MAX_COMMENT_LENGTH) {
      setError("Comment cannot exceed 2000 characters.");
      return;
    }

    setError(null);

    try {
      await onSubmit(normalizedContent);

      // Clear the content after successful creation.
      // Edit mode has an onCancel callback, so it keeps its content.
      if (!onCancel) {
        setContent("");
      }
    } catch {
      // The parent component displays the API error.
    }
  }

  return (
    <Stack spacing={1.5}>
      <TextField
        value={content}
        onChange={(event) => {
          setContent(event.target.value);
          setError(null);
        }}
        placeholder="Write a comment..."
        multiline
        minRows={3}
        maxRows={8}
        fullWidth
        disabled={submitting}
        error={Boolean(error)}
        helperText={
          error ?? `${content.length}/${MAX_COMMENT_LENGTH} characters`
        }
        slotProps={{
          htmlInput: {
            maxLength: MAX_COMMENT_LENGTH,
          },
        }}
      />

      <Stack direction="row" spacing={1} sx={{ justifyContent: "flex-end" }}>
        {onCancel && (
          <Button color="inherit" onClick={onCancel} disabled={submitting}>
            Cancel
          </Button>
        )}

        <Button
          variant="contained"
          onClick={() => void handleSubmit()}
          disabled={submitting || !content.trim()}
        >
          {submitting ? "Saving..." : submitLabel}
        </Button>
      </Stack>
    </Stack>
  );
}
