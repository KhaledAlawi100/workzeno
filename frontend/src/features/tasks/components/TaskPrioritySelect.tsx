import {
  FormControl,
  FormHelperText,
  InputLabel,
  MenuItem,
  Select,
} from "@mui/material";

import type { TaskPriority } from "../types/task.types";

interface TaskPrioritySelectProps {
  value: TaskPriority;
  onChange: (value: TaskPriority) => void;
  error?: string;
  disabled?: boolean;
}

const priorities: Array<{
  value: TaskPriority;
  label: string;
}> = [
  { value: "LOW", label: "Low" },
  { value: "MEDIUM", label: "Medium" },
  { value: "HIGH", label: "High" },
  { value: "URGENT", label: "Urgent" },
];

export default function TaskPrioritySelect({
  value,
  onChange,
  error,
  disabled = false,
}: TaskPrioritySelectProps) {
  return (
    <FormControl fullWidth error={Boolean(error)} disabled={disabled}>
      <InputLabel id="task-priority-label">Priority</InputLabel>

      <Select
        labelId="task-priority-label"
        label="Priority"
        value={value}
        onChange={(event) => onChange(event.target.value as TaskPriority)}
      >
        {priorities.map((priority) => (
          <MenuItem key={priority.value} value={priority.value}>
            {priority.label}
          </MenuItem>
        ))}
      </Select>

      {error && <FormHelperText>{error}</FormHelperText>}
    </FormControl>
  );
}
