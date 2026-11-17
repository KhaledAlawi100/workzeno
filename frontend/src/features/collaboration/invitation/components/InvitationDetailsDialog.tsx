import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Stack,
  Typography,
  Chip,
  Divider,
  Alert,
  Skeleton,
} from "@mui/material";

import type {
  ProjectInvitationResponse,
  ProjectInvitationStatus,
} from "../invitation.types";

import { formatMemberDate } from "../../membership/utils/formatMemberDate";

interface InvitationDetailsDialogProps {
  open: boolean;
  invitation: ProjectInvitationResponse | undefined;
  isLoading: boolean;
  isError: boolean;
  onClose: () => void;
}

const statusColors: Record<
  ProjectInvitationStatus,
  "default" | "success" | "error" | "warning" | "info"
> = {
  PENDING: "warning",
  ACCEPTED: "success",
  DECLINED: "error",
  REVOKED: "default",
  EXPIRED: "info",
};

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <Stack spacing={0.5}>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>

      <Typography variant="body2" sx={{ overflowWrap: "anywhere" }}>
        {value}
      </Typography>
    </Stack>
  );
}

export default function InvitationDetailsDialog({
  open,
  invitation,
  isLoading,
  isError,
  onClose,
}: InvitationDetailsDialogProps) {
  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Invitation details</DialogTitle>

      <DialogContent dividers>
        {isLoading && (
          <Stack spacing={2}>
            <Skeleton variant="text" width="60%" />
            <Skeleton variant="rounded" height={50} />
            <Skeleton variant="rounded" height={50} />
            <Skeleton variant="rounded" height={50} />
          </Stack>
        )}

        {isError && (
          <Alert severity="error">
            Unable to load invitation details. Please close this dialog and try
            again.
          </Alert>
        )}

        {!isLoading && !isError && invitation && (
          <Stack spacing={2.5}>
            <Stack
              direction="row"
              spacing={1}
              sx={{ alignItems: "center", flexWrap: "wrap" }}
            >
              <Chip
                label={invitation.status}
                color={statusColors[invitation.status]}
                size="small"
              />

              <Chip label={invitation.role} variant="outlined" size="small" />
            </Stack>

            <DetailRow label="Invitee" value={invitation.inviteeEmail} />

            <DetailRow label="Invited by" value={invitation.invitedByEmail} />

            <Divider />

            <DetailRow
              label="Date sent"
              value={formatMemberDate(invitation.createdAt)}
            />

            <DetailRow
              label="Expiration date"
              value={formatMemberDate(invitation.expiresAt)}
            />

            <Divider />

            <DetailRow
              label="Accepted at"
              value={
                invitation.acceptedAt
                  ? formatMemberDate(invitation.acceptedAt)
                  : "Not accepted"
              }
            />

            <DetailRow
              label="Declined at"
              value={
                invitation.declinedAt
                  ? formatMemberDate(invitation.declinedAt)
                  : "Not declined"
              }
            />

            <DetailRow
              label="Revoked at"
              value={
                invitation.revokedAt
                  ? formatMemberDate(invitation.revokedAt)
                  : "Not revoked"
              }
            />

            <DetailRow
              label="Revoked by"
              value={invitation.revokedByEmail ?? "Not revoked"}
            />
          </Stack>
        )}
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2 }}>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
}
