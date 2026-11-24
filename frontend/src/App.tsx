import { lazy, Suspense } from "react";
import { BrowserRouter, Route, Routes } from "react-router-dom";

import { Box, CircularProgress, Typography } from "@mui/material";

import ProtectedRoute from "./features/auth/ProtectedRoute";

import PublicLayout from "./app/layout/PublicLayout";
import AppLayout from "./app/layout/AppLayout";

// Public pages
const LandingPage = lazy(() => import("./pages/landing/LandingPage"));
const HealthPage = lazy(() => import("./pages/HealthPage"));
const LoginPage = lazy(() => import("./pages/LoginPage"));
const RegisterPage = lazy(() => import("./pages/RegisterPage"));
const NotFoundPage = lazy(() => import("./pages/NotFoundPage"));

// Protected pages
const DashboardPage = lazy(() => import("./pages/DashboardPage"));

const ProjectsPage = lazy(
  () => import("./features/projects/pages/ProjectsPage"),
);

const ProjectDetailsPage = lazy(
  () => import("./features/projects/pages/ProjectDetailsPage"),
);

const ProjectTasksPage = lazy(
  () => import("./features/tasks/pages/ProjectTasksPage"),
);

const ProjectMembersPage = lazy(
  () => import("./features/collaboration/membership/pages/ProjectMembersPage"),
);

const ProjectInvitationsPage = lazy(
  () =>
    import("./features/collaboration/invitation/pages/ProjectInvitationsPage"),
);

const MyInvitationsPage = lazy(
  () => import("./features/collaboration/invitation/pages/MyInvitationsPage"),
);

function PageLoadingFallback() {
  return (
    <Box
      role="status"
      aria-label="Loading page"
      sx={{
        minHeight: "50vh",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        gap: 2,
      }}
    >
      <CircularProgress />
      <Typography color="text.secondary">Loading page...</Typography>
    </Box>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Suspense fallback={<PageLoadingFallback />}>
        <Routes>
          {/* All public pages share the public navigation. */}
          <Route element={<PublicLayout />}>
            <Route path="/" element={<LandingPage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/health" element={<HealthPage />} />
          </Route>

          {/* All protected pages share the authenticated navigation. */}
          <Route
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/projects" element={<ProjectsPage />} />

            <Route
              path="/projects/:projectId"
              element={<ProjectDetailsPage />}
            />

            {/* Project task board */}
            <Route
              path="/projects/:projectId/tasks"
              element={<ProjectTasksPage />}
            />

            <Route
              path="/projects/:projectId/members"
              element={<ProjectMembersPage />}
            />

            <Route
              path="/projects/:projectId/invitations"
              element={<ProjectInvitationsPage />}
            />

            <Route path="/invitations" element={<MyInvitationsPage />} />
          </Route>

          {/* Unknown routes */}
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}
