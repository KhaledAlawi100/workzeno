import { Box, Skeleton, Stack } from "@mui/material";

/** Displays a skeleton of the four Kanban columns while data is loading. */
export default function TaskBoardLoading() {
  return (
    <Stack direction="row" spacing={2} sx={{ overflow: "hidden" }}>
      {[0, 1, 2, 3].map((column) => (
        <Box key={column} sx={{ minWidth: 280, flex: 1 }}>
          <Skeleton variant="rounded" height={38} sx={{ mb: 1.5 }} />
          <Stack spacing={1.25}>
            <Skeleton variant="rounded" height={125} />
            <Skeleton variant="rounded" height={105} />
            <Skeleton variant="rounded" height={90} />
          </Stack>
        </Box>
      ))}
    </Stack>
  );
}
