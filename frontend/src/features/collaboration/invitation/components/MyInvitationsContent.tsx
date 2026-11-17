import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Pagination,
  Skeleton,
  Stack,
  Typography,
} from "@mui/material";

import MailOutlineIcon from "@mui/icons-material/MailOutlined";
import RefreshIcon from "@mui/icons-material/Refresh";

import { getApiErrorMessage } from "../../../../lib/api";

import PendingInvitationCard from "./PendingInvitationCard";

import type { MyInvitationsPage } from "../invitation.types";

interface MyInvitationsContentProps {
  data: MyInvitationsPage | undefined;
  isPending: boolean;
  isError: boolean;
  error: unknown;
  isFetching: boolean;
  page: number;
  isActionPending: boolean;
  onRetry: () => void;
  onPageChange: (page: number) => void;
  onAccept: (invitationId: string) => void;
  onDecline: (invitationId: string) => void;
}

export default function MyInvitationsContent({
  data,
  isPending,
  isError,
  error,
  isFetching,
  page,
  isActionPending,
  onRetry,
  onPageChange,
  onAccept,
  onDecline,
}: MyInvitationsContentProps) {
  if (isPending) {
    return (
      <Stack spacing={2}>
        {Array.from({ length: 3 }).map((_, index) => (
          <Skeleton key={index} variant="rounded" height={190} />
        ))}
      </Stack>
    );
  }

  if (isError) {
    return (
      <Alert
        severity="error"
        action={
          <Button
            color="inherit"
            size="small"
            startIcon={<RefreshIcon />}
            onClick={onRetry}
          >
            Retry
          </Button>
        }
      >
        {getApiErrorMessage(error)}
      </Alert>
    );
  }

  const invitations = data?.content ?? [];

  if (invitations.length === 0) {
    return (
      <Card variant="outlined">
        <CardContent>
          <Box
            sx={{
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              gap: 1,
              py: 4,
            }}
          >
            <MailOutlineIcon color="disabled" sx={{ fontSize: 44 }} />

            <Typography variant="h6">Your inbox is empty</Typography>

            <Typography color="text.secondary" sx={{ textAlign: "center" }}>
              You don't have any pending project invitations.
            </Typography>
          </Box>
        </CardContent>
      </Card>
    );
  }

  return (
    <Stack spacing={2}>
      {invitations.map((invitation) => (
        <PendingInvitationCard
          key={invitation.invitationId}
          invitation={invitation}
          disabled={isActionPending}
          onAccept={() => onAccept(invitation.invitationId)}
          onDecline={() => onDecline(invitation.invitationId)}
        />
      ))}

      {data && data.totalPages > 1 && (
        <Box sx={{ display: "flex", justifyContent: "center", pt: 1 }}>
          <Pagination
            count={data.totalPages}
            page={page + 1}
            onChange={(_, selectedPage) => onPageChange(selectedPage - 1)}
            color="primary"
            shape="rounded"
            showFirstButton
            showLastButton
            disabled={isActionPending || isFetching}
          />
        </Box>
      )}
    </Stack>
  );
}
