import { Skeleton, Stack } from "@mui/material";

export default function InvitationListLoading() {
  return (
    <Stack spacing={2}>
      {Array.from({ length: 3 }).map((_, index) => (
        <Skeleton key={index} variant="rounded" height={150} />
      ))}
    </Stack>
  );
}
