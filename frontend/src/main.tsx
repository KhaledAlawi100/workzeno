import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import App from "./App";
import AppProviders from "./app/providers/AppProviders";
import { AuthProvider } from "./features/auth/AuthContext";

import "./index.css";

const rootElement = document.getElementById("root");

if (!rootElement) {
  throw new Error('Root element "#root" was not found. Check index.html.');
}

createRoot(rootElement).render(
  <StrictMode>
    <AppProviders>
      <AuthProvider>
        <App />
      </AuthProvider>
    </AppProviders>
  </StrictMode>,
);
