import { Box, Card, Grid, Skeleton, Stack } from "@mui/material";

export default function DashboardLoading() {
  return (
    <Stack spacing={3}>
      <Box>
        <Skeleton variant="text" width={220} height={44} />
        <Skeleton variant="text" width={340} height={24} />
      </Box>

      <Grid container spacing={2}>
        {Array.from({ length: 6 }).map((_, index) => (
          <Grid key={index} size={{ xs: 12, sm: 6, lg: 4 }}>
            <Card>
              <Stack spacing={2} sx={{ p: 2.5 }}>
                <Skeleton width="65%" />
                <Skeleton width="35%" height={42} />
                <Skeleton width="75%" />
              </Stack>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Grid container spacing={2}>
        <Grid size={{ xs: 12, md: 5 }}>
          <Card sx={{ p: 2.5 }}>
            <Skeleton width="50%" height={32} />
            {Array.from({ length: 4 }).map((_, index) => (
              <Skeleton key={index} height={36} sx={{ mt: 1.5 }} />
            ))}
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 7 }}>
          <Card sx={{ p: 2.5 }}>
            <Skeleton width="40%" height={32} />
            {Array.from({ length: 4 }).map((_, index) => (
              <Skeleton key={index} height={55} sx={{ mt: 1.5 }} />
            ))}
          </Card>
        </Grid>
      </Grid>
    </Stack>
  );
}
