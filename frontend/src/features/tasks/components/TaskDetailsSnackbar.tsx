import { Alert, Snackbar } from "@mui/material";

interface TaskDetailsSnackbarProps {
  message: string | null;
  onClose: () => void;
}

export default function TaskDetailsSnackbar({
  message,
  onClose,
}: TaskDetailsSnackbarProps) {
  return (
    <Snackbar
      open={Boolean(message)}
      autoHideDuration={4000}
      onClose={(_event, reason) => {
        if (reason !== "clickaway") {
          onClose();
        }
      }}
      anchorOrigin={{
        vertical: "bottom",
        horizontal: "center",
      }}
    >
      <Alert
        severity="success"
        variant="filled"
        onClose={onClose}
        sx={{ width: "100%" }}
      >
        {message}
      </Alert>
    </Snackbar>
  );
}
