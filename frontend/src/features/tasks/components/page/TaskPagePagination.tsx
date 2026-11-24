import { Pagination, Stack } from "@mui/material";

interface TaskPagePaginationProps {
  totalPages: number;
  page: number;
  onChange: (page: number) => void;
}

export default function TaskPagePagination({
  totalPages,
  page,
  onChange,
}: TaskPagePaginationProps) {
  if (totalPages <= 1) return null;

  return (
    <Stack direction="row" sx={{ pt: 1, justifyContent: "center" }}>
      <Pagination
        count={totalPages}
        page={page + 1}
        onChange={(_event, selectedPage) => onChange(selectedPage - 1)}
        color="primary"
        shape="rounded"
      />
    </Stack>
  );
}
