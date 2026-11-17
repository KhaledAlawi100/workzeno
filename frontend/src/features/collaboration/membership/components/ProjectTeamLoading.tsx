import { Grid, Skeleton, Stack } from "@mui/material";

interface ProjectTeamLoadingProps {
  pageSize?: number;
}

export default function ProjectTeamLoading({
  pageSize = 6,
}: ProjectTeamLoadingProps) {
  return (
    <Stack spacing={3}>
      <Skeleton variant="rounded" height={105} />

      <Grid container spacing={2}>
        {Array.from({ length: pageSize }).map((_, index) => (
          <Grid key={index} size={{ xs: 12, sm: 6 }}>
            <Skeleton variant="rounded" height={100} />
          </Grid>
        ))}
      </Grid>
    </Stack>
  );
}
