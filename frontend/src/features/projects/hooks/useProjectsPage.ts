import { useEffect, useState } from "react";

import { useProjects } from "./useProjects";
import { useCreateProject } from "./useCreateProject";

import { getApiErrorMessage } from "../../../lib/api";

import type { CreateProjectFormValues } from "../projectSchemas";

const PAGE_SIZE = 6;
const SEARCH_DEBOUNCE_MS = 350;
const MAX_SEARCH_LENGTH = 120;

export interface ProjectsNotification {
  open: boolean;
  severity: "success" | "error";
  message: string;
}

export function useProjectsPage() {
  const [page, setPage] = useState(0);

  // Keep the input responsive while the API uses the debounced value.
  const [searchInput, setSearchInput] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");

  const [dialogOpen, setDialogOpen] = useState(false);

  const [notification, setNotification] = useState<ProjectsNotification>({
    open: false,
    severity: "success",
    message: "",
  });

  // Debounce search requests to avoid calling the API on every keystroke.
  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      setDebouncedSearch(searchInput.trim());
    }, SEARCH_DEBOUNCE_MS);

    return () => {
      window.clearTimeout(timeoutId);
    };
  }, [searchInput]);

  // Fetch projects using the selected page and debounced search.
  const { data, isPending, isError, error, refetch, isFetching } = useProjects({
    page,
    size: PAGE_SIZE,
    search: debouncedSearch,
  });

  const createProjectMutation = useCreateProject();

  function handleSearchChange(value: string) {
    setSearchInput(value.slice(0, MAX_SEARCH_LENGTH));

    // Every new search starts from the first page.
    setPage(0);
  }

  function handleClearSearch() {
    setSearchInput("");
    setDebouncedSearch("");
    setPage(0);
  }

  function handlePageChange(selectedPage: number) {
    // MUI Pagination uses one-based page numbers.
    // The backend uses zero-based page numbers.
    setPage(selectedPage - 1);
  }

  function handleOpenDialog() {
    setDialogOpen(true);
  }

  function handleCloseDialog() {
    setDialogOpen(false);
  }

  async function handleCreateProject(
    values: CreateProjectFormValues,
  ): Promise<void> {
    try {
      await createProjectMutation.mutateAsync({
        name: values.name,
        description: values.description || null,
      });

      // Show the first page after creating a project.
      setPage(0);

      setDialogOpen(false);

      setNotification({
        open: true,
        severity: "success",
        message: "Project created successfully.",
      });
    } catch (error) {
      setNotification({
        open: true,
        severity: "error",
        message: getApiErrorMessage(error),
      });
    }
  }

  function handleCloseNotification() {
    setNotification((previous) => ({
      ...previous,
      open: false,
    }));
  }

  return {
    // Search state
    searchInput,
    debouncedSearch,

    // Dialog state
    dialogOpen,

    // Notification state
    notification,

    // Project query
    data,
    isPending,
    isError,
    error,
    refetch,
    isFetching,

    // Actions
    handleSearchChange,
    handleClearSearch,
    handlePageChange,
    handleOpenDialog,
    handleCloseDialog,
    handleCreateProject,
    handleCloseNotification,
  };
}
