import {
  Button,
  Card,
  CardContent,
  Chip,
  Stack,
  Typography,
} from "@mui/material";

import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutlined";
import CloseOutlinedIcon from "@mui/icons-material/CloseOutlined";

import { formatMemberDate } from "../../membership/utils/formatMemberDate";

import type { MyInvitationResponse } from "../invitation.types";

interface PendingInvitationCardProps {
  invitation: MyInvitationResponse;
  disabled: boolean;
  onAccept: () => void;
  onDecline: () => void;
}

export default function PendingInvitationCard({
  invitation,
  disabled,
  onAccept,
  onDecline,
}: PendingInvitationCardProps) {
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
            <Typography variant="h6" component="h2" sx={{ fontWeight: 700 }}>
              {invitation.projectName}
            </Typography>

            <Chip
              label={invitation.role}
              color="primary"
              variant="outlined"
              size="small"
            />
          </Stack>

          <Typography variant="body2" color="text.secondary">
            Invited by {invitation.invitedByEmail}
          </Typography>

          <Stack direction={{ xs: "column", sm: "row" }} spacing={3}>
            <Stack spacing={0.5}>
              <Typography variant="caption" color="text.secondary">
                Received
              </Typography>

              <Typography variant="body2">
                {formatMemberDate(invitation.createdAt)}
              </Typography>
            </Stack>

            <Stack spacing={0.5}>
              <Typography variant="caption" color="text.secondary">
                Expires
              </Typography>

              <Typography variant="body2">
                {formatMemberDate(invitation.expiresAt)}
              </Typography>
            </Stack>
          </Stack>

          <Stack
            direction="row"
            spacing={1}
            sx={{ justifyContent: "flex-end" }}
          >
            <Button
              color="inherit"
              variant="outlined"
              startIcon={<CloseOutlinedIcon />}
              disabled={disabled}
              onClick={onDecline}
            >
              Decline
            </Button>

            <Button
              variant="contained"
              startIcon={<CheckCircleOutlineIcon />}
              disabled={disabled}
              onClick={onAccept}
            >
              Accept
            </Button>
          </Stack>
        </Stack>
      </CardContent>
    </Card>
  );
}
