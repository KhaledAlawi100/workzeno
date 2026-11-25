import { Box, Typography } from "@mui/material";

import NotificationsNoneOutlinedIcon from "@mui/icons-material/NotificationsNoneOutlined";

interface NotificationEmptyStateProps {
  title?: string;
  description?: string;
}

export default function NotificationEmptyState({
  title = "You're all caught up",
  description = "You don't have any notifications right now.",
}: NotificationEmptyStateProps) {
  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: 1.5,
        px: 3,
        py: 6,
        textAlign: "center",
      }}
    >
      <Box
        sx={{
          width: 64,
          height: 64,
          borderRadius: "50%",
          bgcolor: "action.hover",
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <NotificationsNoneOutlinedIcon
          sx={{ fontSize: 32, color: "text.secondary" }}
        />
      </Box>

      <Typography variant="h6" component="h2">
        {title}
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 320 }}>
        {description}
      </Typography>
    </Box>
  );
}
