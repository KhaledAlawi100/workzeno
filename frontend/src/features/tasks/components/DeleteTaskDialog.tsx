import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
} from "@mui/material";

interface DeleteTaskDialogProps {
  open: boolean;
  deleting: boolean;
  onCancel: () => void;
  onConfirm: () => void;
}

export default function DeleteTaskDialog({
  open,
  deleting,
  onCancel,
  onConfirm,
}: DeleteTaskDialogProps) {
  return (
    <Dialog open={open} onClose={onCancel}>
      <DialogTitle>Delete task?</DialogTitle>

      <DialogContent>
        <Typography color="text.secondary">
          This action cannot be undone. The task will be permanently deleted
          from this project.
        </Typography>
      </DialogContent>

      <DialogActions>
        <Button onClick={onCancel} disabled={deleting}>
          Cancel
        </Button>

        <Button
          color="error"
          variant="contained"
          onClick={onConfirm}
          disabled={deleting}
        >
          {deleting ? "Deleting..." : "Delete task"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
