import { Button, Stack } from "@mui/material";

import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import DeleteOutlineIcon from "@mui/icons-material/DeleteOutlined";

interface TaskDetailsActionsProps {
  canEdit: boolean;
  canDelete: boolean;
  deleting: boolean;
  onEdit: () => void;
  onDelete: () => void;
  onDone: () => void;
}

export default function TaskDetailsActions({
  canEdit,
  canDelete,
  deleting,
  onEdit,
  onDelete,
  onDone,
}: TaskDetailsActionsProps) {
  return (
    <>
      {canDelete && (
        <Button
          color="error"
          startIcon={<DeleteOutlineIcon />}
          onClick={onDelete}
          disabled={deleting}
        >
          Delete
        </Button>
      )}

      <Stack direction="row" spacing={1} sx={{ ml: "auto" }}>
        {canEdit && (
          <Button startIcon={<EditOutlinedIcon />} onClick={onEdit}>
            Edit
          </Button>
        )}

        {/* Done closes the dialog; changes have already been saved. */}
        <Button variant="contained" onClick={onDone}>
          Done
        </Button>
      </Stack>
    </>
  );
}
