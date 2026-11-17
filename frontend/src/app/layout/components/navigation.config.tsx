import DashboardOutlinedIcon from "@mui/icons-material/DashboardOutlined";
import FolderOutlinedIcon from "@mui/icons-material/FolderOutlined";
import MailOutlinedIcon from "@mui/icons-material/MailOutlined";

import type { NavigationItem } from "./navbar.types";

export const publicLinks: NavigationItem[] = [
  { label: "Home", to: "/" },
  { label: "Features", to: "/#features" },
  { label: "About", to: "/#about" },
];

export const appLinks: NavigationItem[] = [
  {
    label: "Dashboard",
    to: "/dashboard",
    icon: <DashboardOutlinedIcon fontSize="small" />,
  },
  {
    label: "Projects",
    to: "/projects",
    icon: <FolderOutlinedIcon fontSize="small" />,
  },
  {
    label: "My invitations",
    to: "/invitations",
    icon: <MailOutlinedIcon fontSize="small" />,
  },
];
