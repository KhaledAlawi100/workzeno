import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
} from "@mui/material";

interface RevokeInvitationDialogProps {
  open: boolean;
  inviteeEmail: string;
  isPending?: boolean;
  errorMessage?: string | null;
  onClose: () => void;
  onConfirm: () => Promise<void>;
}

export default function RevokeInvitationDialog({
  open,
  inviteeEmail,
  isPending = false,
  errorMessage,
  onClose,
  onConfirm,
}: RevokeInvitationDialogProps) {
  return (
    <Dialog
      open={open}
      onClose={isPending ? undefined : onClose}
      fullWidth
      maxWidth="xs"
    >
      <DialogTitle>Revoke invitation?</DialogTitle>

      <DialogContent>
        <Alert severity="warning" sx={{ mb: 2 }}>
          The recipient will no longer be able to use this invitation to join
          the project.
        </Alert>

        <Typography variant="body2" color="text.secondary">
          Are you sure you want to revoke the invitation sent to{" "}
          <strong>{inviteeEmail}</strong>?
        </Typography>

        {errorMessage && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {errorMessage}
          </Alert>
        )}
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
          {isPending ? "Revoking..." : "Revoke invitation"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
