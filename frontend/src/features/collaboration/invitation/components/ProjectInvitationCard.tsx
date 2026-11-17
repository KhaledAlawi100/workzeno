import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Stack,
  Typography,
} from "@mui/material";

import VisibilityOutlinedIcon from "@mui/icons-material/VisibilityOutlined";
import BlockOutlinedIcon from "@mui/icons-material/BlockOutlined";

import { formatMemberDate } from "../../membership/utils/formatMemberDate";

import type {
  ProjectInvitationStatus,
  ProjectInvitationSummaryResponse,
} from "../invitation.types";

interface ProjectInvitationCardProps {
  invitation: ProjectInvitationSummaryResponse;
  canRevokeInvitations: boolean;
  onViewDetails: () => void;
  onRevoke: () => void;
}

// Keep status presentation in one place.
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

export default function ProjectInvitationCard({
  invitation,
  canRevokeInvitations,
  onViewDetails,
  onRevoke,
}: ProjectInvitationCardProps) {

    const canRevoke = canRevokeInvitations && invitation.status === "PENDING";

  return (
    <Card variant="outlined">
      <CardContent>
        <Stack spacing={2}>
          <Stack
            direction={{ xs: "column", sm: "row" }}
            spacing={1}
            sx={{
              justifyContent: "space-between",
              alignItems: { xs: "flex-start", sm: "center" },
            }}
          >
            <Box sx={{ minWidth: 0 }}>
              <Typography
                variant="subtitle1"
                sx={{ fontWeight: 700, overflowWrap: "anywhere" }}
              >
                {invitation.inviteeEmail}
              </Typography>

              <Typography variant="body2" color="text.secondary">
                Invited by {invitation.invitedByEmail}
              </Typography>
            </Box>

            <Stack direction="row" spacing={1} sx={{ flexWrap: "wrap" }}>
              <Chip
                label={invitation.status}
                color={statusColors[invitation.status]}
                size="small"
              />

              <Chip label={invitation.role} variant="outlined" size="small" />
            </Stack>
          </Stack>

          <Stack
            direction={{ xs: "column", sm: "row" }}
            spacing={{ xs: 1, sm: 4 }}
          >
            <Box>
              <Typography variant="caption" color="text.secondary">
                Date sent
              </Typography>

              <Typography variant="body2">
                {formatMemberDate(invitation.createdAt)}
              </Typography>
            </Box>

            <Box>
              <Typography variant="caption" color="text.secondary">
                Expires
              </Typography>

              <Typography variant="body2">
                {formatMemberDate(invitation.expiresAt)}
              </Typography>
            </Box>
          </Stack>

          <Stack
            direction="row"
            spacing={1}
            sx={{ justifyContent: "flex-end" }}
          >
            <Button
              size="small"
              startIcon={<VisibilityOutlinedIcon />}
              onClick={onViewDetails}
            >
              Details
            </Button>

            {canRevoke && (
              <Button
                size="small"
                color="error"
                startIcon={<BlockOutlinedIcon />}
                onClick={onRevoke}
              >
                Revoke
              </Button>
            )}
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  );
}
