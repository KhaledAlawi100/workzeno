import { Grid, Skeleton, Stack } from "@mui/material";

const PAGE_SIZE = 6;

export default function ProjectsLoadingState() {
  return (
    <Grid container spacing={3}>
      {Array.from({ length: PAGE_SIZE }).map((_, index) => (
        <Grid key={index} size={{ xs: 12, sm: 6, md: 4 }}>
          <Stack spacing={2}>
            <Skeleton variant="rounded" height={150} />
            <Skeleton variant="text" width="70%" height={30} />
            <Skeleton variant="text" width="100%" />
            <Skeleton variant="text" width="45%" />
          </Stack>
        </Grid>
      ))}
    </Grid>
  );
}
