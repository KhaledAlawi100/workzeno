import { afterEach, describe, expect, it, vi } from "vitest";
import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import App from "./App";
import { getApiHealth } from "./features/health/healthApi";

vi.mock("./features/health/healthApi", () => ({
  getApiHealth: vi.fn(),
}));

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

describe("Workzeno health check", () => {
  it("shows connected when the API responds with UP", async () => {
    vi.mocked(getApiHealth).mockResolvedValue({ status: "UP" });

    const user = userEvent.setup();
    render(<App />);

    await user.click(
      screen.getByRole("button", { name: "Check Backend Health" }),
    );

    expect(await screen.findByText("Backend connected")).toBeInTheDocument();
  });

  it("shows a safe message when the API request fails", async () => {
    vi.mocked(getApiHealth).mockRejectedValue(
      new Error("Internal server error with sensitive details"),
    );

    const user = userEvent.setup();
    render(<App />);

    await user.click(screen.getByRole("button", { name: "Check Backend Health" }));

    expect(
      await screen.findByText(
        "Could not reach the backend. Check that it is running.",
      ),
    ).toBeInTheDocument();

    expect(screen.queryByText(/sensitive details/)).not.toBeInTheDocument();
  });
});
