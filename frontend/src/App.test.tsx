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

describe("Workzeno application", () => {
  it("renders the health check page", async () => {
    render(<App />);

    expect(
      await screen.findByRole("heading", { name: "Workzeno" }),
    ).toBeTruthy();

    expect(
      screen.getByRole("button", { name: "Check Backend Health" }),
    ).toBeTruthy();
  });

  it("shows a successful backend connection", async () => {
    vi.mocked(getApiHealth).mockResolvedValue({ status: "UP" });

    const user = userEvent.setup();
    render(<App />);

    await user.click(
      screen.getByRole("button", { name: "Check Backend Health" }),
    );

    expect(await screen.findByText("Backend connected")).toBeTruthy();
  });

  it("shows an error message when the backend is unavailable", async () => {
    vi.mocked(getApiHealth).mockRejectedValue(
      new Error("Internal server error"),
    );

    const user = userEvent.setup();
    render(<App />);

    await user.click(
      screen.getByRole("button", { name: "Check Backend Health" }),
    );

    expect(
      await screen.findByText(
        "Could not reach the backend. Check that it is running.",
      ),
    ).toBeTruthy();
  });
});
