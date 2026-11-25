import { Box, Divider, Skeleton, Stack } from "@mui/material";

interface NotificationLoadingProps {
  count?: number;
}

export default function NotificationLoading({
  count = 4,
}: NotificationLoadingProps) {
  return (
    <Stack>
      {Array.from({ length: count }).map((_, index) => (
        <Box key={index} sx={{ px: 2, py: 1.75 }}>
          <Stack direction="row" spacing={1.5}>
            <Skeleton variant="circular" width={28} height={28} />

            <Stack spacing={0.75} sx={{ flex: 1 }}>
              <Skeleton variant="text" width="55%" height={22} />
              <Skeleton variant="text" width="90%" height={18} />
              <Skeleton variant="text" width="35%" height={16} />
            </Stack>
          </Stack>
        </Box>
      ))}
      <Divider />
    </Stack>
  );
}
