import { createTheme } from "@mui/material/styles";

export const theme = createTheme({
  palette: {
    mode: "light",

    primary: {
      main: "#3659D9",
      light: "#6680E8",
      dark: "#2442B5",
      contrastText: "#FFFFFF",
    },

    secondary: {
      main: "#64748B",
      light: "#94A3B8",
      dark: "#475569",
      contrastText: "#FFFFFF",
    },

    background: {
      default: "#F6F8FC",
      paper: "#FFFFFF",
    },

    text: {
      primary: "#172033",
      secondary: "#64748B",
    },

    success: {
      main: "#16845B",
      light: "#DCFCE7",
      dark: "#166534",
    },

    warning: {
      main: "#C17A16",
      light: "#FEF3C7",
      dark: "#92400E",
    },

    error: {
      main: "#D14343",
      light: "#FEE2E2",
      dark: "#991B1B",
    },

    divider: "#E8ECF3",
  },

  typography: {
    fontFamily: ["Inter", "Roboto", "Arial", "sans-serif"].join(","),

    h1: {
      fontWeight: 700,
      letterSpacing: "-0.04em",
    },

    h2: {
      fontWeight: 700,
      letterSpacing: "-0.035em",
    },

    h3: {
      fontWeight: 700,
      letterSpacing: "-0.03em",
    },

    h4: {
      fontWeight: 700,
      letterSpacing: "-0.025em",
    },

    h5: {
      fontWeight: 600,
      letterSpacing: "-0.02em",
    },

    h6: {
      fontWeight: 600,
    },

    subtitle1: {
      fontWeight: 600,
    },

    button: {
      fontWeight: 600,
      textTransform: "none",
    },
  },

  shape: {
    borderRadius: 12,
  },

  spacing: 8,

  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          minWidth: 320,
          backgroundColor: "#F6F8FC",
        },

        "*": {
          boxSizing: "border-box",
        },
      },
    },

    MuiButton: {
      defaultProps: {
        disableElevation: true,
      },

      styleOverrides: {
        root: {
          minHeight: 40,
          borderRadius: 10,
          paddingInline: 18,
        },

        contained: {
          boxShadow: "none",

          "&:hover": {
            boxShadow: "none",
          },
        },
      },
    },

    MuiCard: {
      styleOverrides: {
        root: {
          border: "1px solid #E8ECF3",
          borderRadius: 16,
          boxShadow: "0 2px 8px rgba(23, 32, 51, 0.04)",
        },
      },
    },

    MuiPaper: {
      styleOverrides: {
        rounded: {
          borderRadius: 16,
        },
      },
    },

    MuiTextField: {
      defaultProps: {
        size: "small",
        fullWidth: true,
      },
    },

    MuiOutlinedInput: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          backgroundColor: "#FFFFFF",
        },
      },
    },

    MuiChip: {
      styleOverrides: {
        root: {
          borderRadius: 8,
          fontWeight: 600,
        },
      },
    },

    MuiPaginationItem: {
      styleOverrides: {
        root: {
          borderRadius: 8,
        },
      },
    },
  },
});
