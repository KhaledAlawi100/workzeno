import { Link } from "react-router-dom";

import { Alert, Button, Container, Stack } from "@mui/material";
import ArrowBackOutlinedIcon from "@mui/icons-material/ArrowBackOutlined";
import RefreshOutlinedIcon from "@mui/icons-material/Refresh";

interface TaskPageErrorProps {
  projectId?: string;
  message: string;
  onRetry?: () => void;
}

export default function TaskPageError({
  projectId,
  message,
  onRetry,
}: TaskPageErrorProps) {
  if (!projectId) {
    return (
      <Container maxWidth="lg" sx={{ py: 4 }}>
        <Alert severity="error">{message}</Alert>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Stack spacing={2}>
        <Button
          component={Link}
          to={`/projects/${encodeURIComponent(projectId)}`}
          startIcon={<ArrowBackOutlinedIcon />}
          sx={{ alignSelf: "flex-start" }}
        >
          Back to project
        </Button>

        <Alert
          severity="error"
          action={
            onRetry ? (
              <Button
                color="inherit"
                size="small"
                startIcon={<RefreshOutlinedIcon />}
                onClick={onRetry}
              >
                Retry
              </Button>
            ) : undefined
          }
        >
          {message}
        </Alert>
      </Stack>
    </Container>
  );
}
