import { useEffect } from "react";
import { useLocation } from "react-router-dom";

export function useLandingPageHashScroll(): void {
  const location = useLocation();

  useEffect(() => {
    // No hash means there is no specific section to scroll to.
    if (!location.hash) {
      return;
    }

    // Convert "#features" into "features".
    const sectionId = decodeURIComponent(location.hash.substring(1));

    // Wait until the browser has rendered the landing page.
    const animationFrameId = requestAnimationFrame(() => {
      const section = document.getElementById(sectionId);

      if (section) {
        section.scrollIntoView({
          behavior: "smooth",
          block: "start",
        });
      }
    });

    // Cancel the scheduled animation if navigation changes.
    return () => cancelAnimationFrame(animationFrameId);
  }, [location.pathname, location.hash, location.key]);
}
