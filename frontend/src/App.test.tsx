import { afterEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import App from "./App";
import AppProviders from "./app/providers/AppProviders";
import { AuthProvider } from "./features/auth/AuthContext";

import { getApiHealth } from "./features/health/healthApi";

// Mock the backend health API so tests do not make real HTTP requests.
vi.mock("./features/health/healthApi", () => ({
  getApiHealth: vi.fn(),
}));

// Mock authentication API calls.
// The rejected refresh simulates a user who is not logged in.
vi.mock("./features/auth/api/authApi", () => ({
  getCurrentUser: vi.fn(),
  loginUser: vi.fn(),
  logoutUser: vi.fn(),
  refreshAccessToken: vi.fn().mockRejectedValue(new Error("No active session")),
}));

// Render the application with the same providers used in production.
function renderWorkzenoApp() {
  return render(
    <AppProviders>
      <AuthProvider>
        <App />
      </AuthProvider>
    </AppProviders>,
  );
}

afterEach(() => {
  cleanup();
  vi.clearAllMocks();

  // Keep tests independent by resetting the browser route.
  window.history.pushState({}, "", "/");
});

describe("Workzeno application", () => {
  it("renders the public landing page at the root route", async () => {
    window.history.pushState({}, "", "/");

    renderWorkzenoApp();

    expect(
      await screen.findByRole("heading", {
        name: /manage projects\.\s*work better together\./i,
      }),
    ).toBeTruthy();

    // The landing page intentionally has multiple Get started links.
    expect(
      screen.getAllByRole("link", { name: "Get started" }).length,
    ).toBeGreaterThan(0);

    expect(
      screen.getAllByRole("link", { name: "Sign in" }).length,
    ).toBeGreaterThan(0);

    expect(screen.getByRole("heading", { name: "Projects" })).toBeTruthy();

    expect(screen.getByRole("heading", { name: "Teams" })).toBeTruthy();

    expect(screen.getByRole("heading", { name: "Permissions" })).toBeTruthy();
  });

  it("keeps the backend health check available at /health", async () => {
    window.history.pushState({}, "", "/health");

    renderWorkzenoApp();

    // Select the page title, not the Workzeno logo in the header.
    expect(
      await screen.findByRole("heading", {
        level: 4,
        name: "Workzeno",
      }),
    ).toBeTruthy();

    expect(
      screen.getByRole("button", {
        name: "Check Backend Health",
      }),
    ).toBeTruthy();
  });

  it("shows a successful backend connection", async () => {
    vi.mocked(getApiHealth).mockResolvedValue({ status: "UP" });

    window.history.pushState({}, "", "/health");

    const user = userEvent.setup();

    renderWorkzenoApp();

    await user.click(
      await screen.findByRole("button", {
        name: "Check Backend Health",
      }),
    );

    expect(await screen.findByText("Backend connected")).toBeTruthy();
  });

  it("shows an error message when the backend is unavailable", async () => {
    vi.mocked(getApiHealth).mockRejectedValue(
      new Error("Internal server error"),
    );

    window.history.pushState({}, "", "/health");

    const user = userEvent.setup();

    renderWorkzenoApp();

    await user.click(
      await screen.findByRole("button", {
        name: "Check Backend Health",
      }),
    );

    expect(
      await screen.findByText(
        "Could not reach the backend. Check that it is running.",
      ),
    ).toBeTruthy();
  });
});
