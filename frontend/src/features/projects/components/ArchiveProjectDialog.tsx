import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from "@mui/material";

interface ArchiveProjectDialogProps {
  open: boolean;
  projectName: string;
  isPending: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

export default function ArchiveProjectDialog({
  open,
  projectName,
  isPending,
  onClose,
  onConfirm,
}: ArchiveProjectDialogProps) {
  return (
    <Dialog
      open={open}
      onClose={isPending ? undefined : onClose}
      maxWidth="xs"
      fullWidth
    >
      <DialogTitle>Archive project?</DialogTitle>

      <DialogContent>
        <DialogContentText>
          Are you sure you want to archive "{projectName}"? You can restore this
          project later.
        </DialogContentText>
      </DialogContent>

      <DialogActions sx={{ p: 2 }}>
        <Button onClick={onClose} color="inherit" disabled={isPending}>
          Cancel
        </Button>

        <Button
          onClick={onConfirm}
          color="warning"
          variant="contained"
          disabled={isPending}
        >
          {isPending ? "Archiving..." : "Archive project"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
