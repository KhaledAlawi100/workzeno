import { Box } from "@mui/material";

import HeroSection from "./components/HeroSection";
import FeaturesSection from "./components/FeaturesSection";
import AboutSection from "./components/AboutSection";
import LandingFooter from "./components/LandingFooter";

import { useLandingPageHashScroll } from "./hooks/useLandingPageHashScroll";

export default function LandingPage() {
  // Handle navigation to #features and #about.
  useLandingPageHashScroll();

  return (
    <Box sx={{ minHeight: "100vh", bgcolor: "#fff" }}>
      {/* Main landing page content */}
      <Box component="main">
        <HeroSection />
        <FeaturesSection />
        <AboutSection />
      </Box>

      {/* Landing page footer */}
      <LandingFooter />
    </Box>
  );
}
