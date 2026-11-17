import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
} from "@mui/material";

interface RemoveMemberDialogProps {
  open: boolean;
  memberEmail: string;
  isPending?: boolean;
  onClose: () => void;
  onConfirm: () => Promise<void>;
}

export default function RemoveMemberDialog({
  open,
  memberEmail,
  isPending = false,
  onClose,
  onConfirm,
}: RemoveMemberDialogProps) {
  return (
    <Dialog
      open={open}
      onClose={isPending ? undefined : onClose}
      fullWidth
      maxWidth="xs"
    >
      <DialogTitle>Remove project member?</DialogTitle>

      <DialogContent>
        <Alert severity="warning" sx={{ mb: 2 }}>
          This person will immediately lose access to this project.
        </Alert>

        <Typography variant="body2" color="text.secondary">
          Are you sure you want to remove <strong>{memberEmail}</strong> from
          this project?
        </Typography>
      </DialogContent>

      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button color="inherit" onClick={onClose} disabled={isPending}>
          Cancel
        </Button>

        <Button
          color="error"
          variant="contained"
          onClick={onConfirm}
          disabled={isPending}
        >
          {isPending ? "Removing..." : "Remove member"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
