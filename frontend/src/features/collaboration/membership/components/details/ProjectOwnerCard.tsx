import {
  Avatar,
  Card,
  CardContent,
  Chip,
  Stack,
  Typography,
} from "@mui/material";

import WorkspacePremiumOutlinedIcon from "@mui/icons-material/WorkspacePremiumOutlined";

import type { ProjectOwner } from "../../membership.types";

interface ProjectOwnerCardProps {
  owner: ProjectOwner;
}

export default function ProjectOwnerCard({ owner }: ProjectOwnerCardProps) {
  const avatarLetter = owner.email.charAt(0).toUpperCase() || "?";

  return (
    <Stack spacing={1.5}>
      <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
        Project owner
      </Typography>

      <Card variant="outlined">
        <CardContent sx={{ p: 2.5, "&:last-child": { pb: 2.5 } }}>
          <Stack
            direction="row"
            spacing={2}
            sx={{
              alignItems: "center",
              justifyContent: "space-between",
            }}
          >
            <Stack
              direction="row"
              spacing={1.5}
              sx={{
                minWidth: 0,
                flex: 1,
                alignItems: "center",
              }}
            >
              <Avatar
                sx={{
                  width: 46,
                  height: 46,
                  bgcolor: "warning.light",
                  color: "warning.dark",
                }}
              >
                {avatarLetter}
              </Avatar>

              <Stack spacing={0.5} sx={{ minWidth: 0 }}>
                <Typography
                  variant="subtitle2"
                  sx={{
                    fontWeight: 700,
                    overflowWrap: "anywhere",
                  }}
                >
                  {owner.email}
                </Typography>

                <Typography variant="caption" color="text.secondary">
                  Project owner
                </Typography>
              </Stack>
            </Stack>

            <Chip
              icon={<WorkspacePremiumOutlinedIcon />}
              label="Owner"
              color="warning"
              size="small"
            />
          </Stack>
        </CardContent>
      </Card>
    </Stack>
  );
}
