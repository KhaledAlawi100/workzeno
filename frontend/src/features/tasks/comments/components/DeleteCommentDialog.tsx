import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
} from "@mui/material";

interface DeleteCommentDialogProps {
  open: boolean;
  deleting: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}

export default function DeleteCommentDialog({
  open,
  deleting,
  onCancel,
  onConfirm,
}: DeleteCommentDialogProps) {
  return (
    <Dialog
      open={open}
      onClose={deleting ? undefined : onCancel}
      fullWidth
      maxWidth="xs"
    >
      <DialogTitle>Delete comment?</DialogTitle>

      <DialogContent>
        <Typography color="text.secondary">
          Are you sure you want to permanently delete this comment? This action
          cannot be undone.
        </Typography>
      </DialogContent>

      <DialogActions>
        <Button onClick={onCancel} disabled={deleting}>
          Cancel
        </Button>

        <Button
          variant="contained"
          color="error"
          onClick={onConfirm}
          disabled={deleting}
        >
          {deleting ? "Deleting..." : "Delete comment"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
