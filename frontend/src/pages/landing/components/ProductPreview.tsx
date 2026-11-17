import {
  Avatar,
  Box,
  Chip,
  Divider,
  Paper,
  Stack,
  Typography,
} from "@mui/material";

const previewProjects = [
  {
    name: "Website redesign",
    detail: "Design and experience",
    color: "#DCFCE7",
    status: "Active",
  },
  {
    name: "Mobile application",
    detail: "Product development",
    color: "#DBEAFE",
    status: "Active",
  },
  {
    name: "Marketing launch",
    detail: "Campaign planning",
    color: "#F1F5F9",
    status: "Planning",
  },
];

const teamInitials = ["AM", "SK", "JL", "RT"];

const teamColors = ["#3659D9", "#16845B", "#A855F7", "#D97706"];


export default function ProductPreview() {
  return (
    <Box
      sx={{
        position: "relative",
        maxWidth: 570,
        mx: "auto",
        width: "100%",
        "&::before": {
          content: '""',
          position: "absolute",
          width: "78%",
          height: "78%",
          right: "-5%",
          top: "-5%",
          borderRadius: "50%",
          background:
            "radial-gradient(circle, rgba(102,128,232,0.20), transparent 70%)",
          filter: "blur(8px)",
          zIndex: 0,
        },
      }}
    >
      {/* Main dashboard preview */}
      <Paper
        elevation={0}
        sx={{
          position: "relative",
          zIndex: 1,
          p: { xs: 2, sm: 3 },
          border: "1px solid",
          borderColor: "divider",
          borderRadius: 4,
          bgcolor: "#fff",
          boxShadow: "0 28px 80px rgba(35, 52, 100, 0.13)",
          transform: { md: "rotate(1.5deg)" },
        }}
      >
        {/* Dashboard header */}
        <Stack
          direction="row"
          sx={{
            mb: 3,
            alignItems: "center",
            justifyContent: "space-between",
          }}
        >
          <Stack direction="row" spacing={1.25} sx={{ alignItems: "center" }}>
            <Box
              sx={{
                width: 30,
                height: 30,
                borderRadius: 2,
                bgcolor: "primary.main",
                color: "#fff",
                display: "grid",
                placeItems: "center",
                fontWeight: 800,
              }}
            >
              W
            </Box>

            <Typography sx={{ fontWeight: 750 }}>Workspace</Typography>
          </Stack>

          <Chip
            label="Your projects"
            size="small"
            sx={{ bgcolor: "#EEF2FF", color: "primary.dark" }}
          />
        </Stack>

        {/* Dashboard introduction */}
        <Typography variant="overline" color="text.secondary">
          OVERVIEW
        </Typography>

        <Typography variant="h5" sx={{ fontWeight: 750, mb: 0.5 }}>
          Good work starts together.
        </Typography>

        <Typography variant="body2" color="text.secondary" sx={{ mb: 2.5 }}>
          A quick look at your team's workspace.
        </Typography>

        {/* Dashboard statistics */}
        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
            gap: 1.5,
            mb: 2.5,
          }}
        >
          <Paper
            variant="outlined"
            sx={{ p: 1.75, borderRadius: 2.5, borderColor: "divider" }}
          >
            <Typography variant="caption" color="text.secondary">
              Projects
            </Typography>

            <Typography variant="h4" sx={{ fontWeight: 800 }}>
              12
            </Typography>

            <Typography variant="caption" color="success.main">
              Organized in one place
            </Typography>
          </Paper>

          <Paper
            variant="outlined"
            sx={{ p: 1.75, borderRadius: 2.5, borderColor: "divider" }}
          >
            <Typography variant="caption" color="text.secondary">
              Team members
            </Typography>

            <Typography variant="h4" sx={{ fontWeight: 800 }}>
              08
            </Typography>

            <Typography variant="caption" color="text.secondary">
              Working together
            </Typography>
          </Paper>
        </Box>

        {/* Recent projects heading */}
        <Stack
          direction="row"
          sx={{
            mb: 1.5,
            justifyContent: "space-between",
            alignItems: "center",
          }}
        >
          <Typography sx={{ fontWeight: 700 }}>Recent projects</Typography>

          <Typography
            variant="caption"
            color="primary.main"
            sx={{ fontWeight: 700 }}
          >
            Workspace
          </Typography>
        </Stack>

        {/* Recent projects list */}
        {previewProjects.map((project) => (
          <Stack
            key={project.name}
            direction="row"
            spacing={1}
            sx={{
              py: 1.4,
              borderTop: "1px solid",
              borderColor: "divider",
              alignItems: "center",
              justifyContent: "space-between",
            }}
          >
            <Stack direction="row" spacing={1.25} sx={{ alignItems: "center" }}>
              <Box
                sx={{
                  width: 34,
                  height: 34,
                  borderRadius: 2,
                  bgcolor: project.color,
                  flexShrink: 0,
                }}
              />

              <Box>
                <Typography variant="body2" sx={{ fontWeight: 700 }}>
                  {project.name}
                </Typography>

                <Typography variant="caption" color="text.secondary">
                  {project.detail}
                </Typography>
              </Box>
            </Stack>

            <Chip
              label={project.status}
              size="small"
              variant="outlined"
              sx={{ fontSize: 10, height: 23 }}
            />
          </Stack>
        ))}

        <Divider sx={{ my: 1.5 }} />

        {/* Team member avatars */}
        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
          <Stack direction="row" spacing={-0.75}>
            {teamInitials.map((initials, index) => (
              <Avatar
                key={initials}
                sx={{
                  width: 27,
                  height: 27,
                  fontSize: 10,
                  fontWeight: 700,
                  border: "2px solid #fff",
                  bgcolor: teamColors[index],
                }}
              >
                {initials}
              </Avatar>
            ))}
          </Stack>

          <Typography variant="caption" color="text.secondary">
            Better work, together.
          </Typography>
        </Box>
      </Paper>

      {/* Floating teamwork card */}
      <Paper
        elevation={0}
        sx={{
          position: "absolute",
          zIndex: 2,
          left: { xs: -5, sm: -24 },
          bottom: { xs: -20, sm: 25 },
          p: 1.5,
          border: "1px solid",
          borderColor: "divider",
          borderRadius: 3,
          boxShadow: "0 12px 35px rgba(35,52,100,0.12)",
          display: "flex",
          alignItems: "center",
          gap: 1.25,
          bgcolor: "#fff",
        }}
      >
        <Box
          sx={{
            width: 34,
            height: 34,
            display: "grid",
            placeItems: "center",
            borderRadius: 2,
            bgcolor: "#DCFCE7",
            color: "#16845B",
            fontSize: 18,
            fontWeight: 800,
          }}
        >
          ✓
        </Box>

        <Box>
          <Typography variant="caption" color="text.secondary">
            Teamwork
          </Typography>

          <Typography variant="body2" sx={{ fontWeight: 750 }}>
            All in sync
          </Typography>
        </Box>
      </Paper>
    </Box>
  );
}
