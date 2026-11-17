import { Paper, Stack, Typography } from "@mui/material";

interface ProjectDescriptionProps {
  description: string | null;
}

export default function ProjectDescription({
  description,
}: ProjectDescriptionProps) {
  return (
    <Paper variant="outlined" sx={{ p: 3, borderRadius: 2 }}>
      <Stack spacing={2}>
        <Typography variant="h6" component="h2">
          Description
        </Typography>

        <Typography
          color={description ? "text.primary" : "text.secondary"}
          sx={{
            whiteSpace: "pre-wrap",
            overflowWrap: "anywhere",
          }}
        >
          {description || "No description provided."}
        </Typography>
      </Stack>
    </Paper>
  );
}
