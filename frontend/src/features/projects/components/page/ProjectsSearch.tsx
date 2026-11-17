import { IconButton, InputAdornment, TextField } from "@mui/material";

import ClearIcon from "@mui/icons-material/Clear";
import SearchIcon from "@mui/icons-material/Search";

interface ProjectsSearchProps {
  value: string;
  onChange: (value: string) => void;
  onClear: () => void;
}

const MAX_SEARCH_LENGTH = 120;

export default function ProjectsSearch({
  value,
  onChange,
  onClear,
}: ProjectsSearchProps) {
  return (
    <TextField
      value={value}
      onChange={(event) => onChange(event.target.value)}
      placeholder="Search projects by name or description..."
      aria-label="Search projects by name or description"
      slotProps={{
        htmlInput: {
          maxLength: MAX_SEARCH_LENGTH,
        },
        input: {
          startAdornment: (
            <InputAdornment position="start">
              <SearchIcon color="action" />
            </InputAdornment>
          ),
          endAdornment: value ? (
            <InputAdornment position="end">
              <IconButton
                aria-label="Clear project search"
                onClick={onClear}
                edge="end"
                size="small"
              >
                <ClearIcon fontSize="small" />
              </IconButton>
            </InputAdornment>
          ) : undefined,
        },
      }}
      sx={{ maxWidth: 560 }}
    />
  );
}
