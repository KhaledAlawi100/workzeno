import { lazy, Suspense } from "react";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import { Box, CircularProgress, Typography } from "@mui/material";

import ProtectedRoute from "./features/auth/ProtectedRoute";

// Lazy-loaded pages
const HealthPage = lazy(() => import("./pages/HealthPage"));
const NotFoundPage = lazy(() => import("./pages/NotFoundPage"));

const RegisterPage = lazy(() => import("./pages/RegisterPage"));
const LoginPage = lazy(() => import("./pages/LoginPage"));
const DashboardPage = lazy(() => import("./pages/DashboardPage"));

const ProjectsPage = lazy(
  () => import("./features/projects/pages/ProjectsPage"),
);

const ProjectDetailsPage = lazy(
  () => import("./features/projects/pages/ProjectDetailsPage"),
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
          <Route path="/" element={<HealthPage />} />

          <Route path="/register" element={<RegisterPage />} />
          <Route path="/login" element={<LoginPage />} />

          <Route
            path="/dashboard"
            element={
              <ProtectedRoute>
                <DashboardPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/projects"
            element={
              <ProtectedRoute>
                <ProjectsPage />
              </ProtectedRoute>
            }
          />

          <Route
            path="/projects/:projectId"
            element={
              <ProtectedRoute>
                <ProjectDetailsPage />
              </ProtectedRoute>
            }
          />

          
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </Suspense>
    </BrowserRouter>
  );
}
