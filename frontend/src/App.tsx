import { useState } from "react";

import { getApiHealth } from "./features/health/healthApi";
type CheckState = "idle" | "checking" | "connected" | "failed";

export default function App() {
  const [state, setState] = useState<CheckState>("idle");

  async function checkBackend() {
    setState("checking");

    try {
      const result = await getApiHealth();

      if (result.status !== "UP") {
        throw new Error(
          `Backend health check failed with status: ${result.status}`,
        );
      }

      setState("connected");
    } catch (error) {
      console.error("Error checking backend health:", error);
      setState("failed");
    }
  }

  return (
    <main style={{ padding: "2rem", fontFamily: "Arial, sans-serif" }}>
      <h1>Workzeno Frontend</h1>
      <p>Backend connectivity check</p>
      <button
        onClick={checkBackend}
        disabled={state === "checking"}
        style={{
          padding: "0.5rem 1rem",
          fontSize: "1rem",
          cursor: state === "checking" ? "not-allowed" : "pointer",
        }}
      >
        {state === "checking" ? "Checking..." : "Check Backend Health"}
      </button>

      <p role="status" aria-live="polite">
        {state === "idle" && "Not checked yet"}
        {state === "checking" && "Contacting backend..."}
        {state === "connected" && "Backend connected"}
        {state === "failed" &&
          "Could not reach the backend. Check that it is running."}
      </p>
    </main>
  );
}
