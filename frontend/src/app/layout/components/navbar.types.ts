import type { ReactNode } from "react";

export type NavigationItem = {
  label: string;
  to: string;
  icon?: ReactNode;
};

export type NavbarVariant = "public" | "app";

export type NavigationComponentProps = {
  onNavigate?: () => void;
};
