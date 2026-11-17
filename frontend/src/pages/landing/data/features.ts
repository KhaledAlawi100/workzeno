export type LandingFeature = {
  number: string;
  symbol: string;
  title: string;
  description: string;
};

export const features: LandingFeature[] = [
  {
    number: "01",
    symbol: "▦",
    title: "Projects",
    description:
      "Keep project information, descriptions, and status organized in one clear workspace.",
  },
  {
    number: "02",
    symbol: "◎",
    title: "Teams",
    description:
      "Bring people together around shared projects and make collaboration easier.",
  },
  {
    number: "03",
    symbol: "⌘",
    title: "Permissions",
    description:
      "Give team members the appropriate level of access with clear roles and permissions.",
  },
];
