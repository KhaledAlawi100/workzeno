import { useEffect, useState } from "react";

import { Autocomplete, CircularProgress, TextField } from "@mui/material";

import type { ProjectMember } from "../membership.types";

import { useProjectMemberSearch } from "../hooks/useProjectMemberSearch";

interface ProjectMemberSearchProps {
  projectId: string;

  // Members already selected by the parent are excluded from suggestions.
  excludedUserIds?: string[];

  // The parent receives a real member returned by the backend.
  onSelect: (member: ProjectMember) => void;

  disabled?: boolean;
}

export default function ProjectMemberSearch({
  projectId,
  excludedUserIds = [],
  onSelect,
  disabled = false,
}: ProjectMemberSearchProps) {
  const [inputValue, setInputValue] = useState("");
  const [searchQuery, setSearchQuery] = useState("");

  // Debounce requests so we do not call the backend on every keystroke.
  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      setSearchQuery(inputValue.trim());
    }, 300);

    return () => window.clearTimeout(timeoutId);
  }, [inputValue]);

  const {
    data: members = [],
    isFetching,
    isError,
  } = useProjectMemberSearch(projectId, searchQuery, !disabled);

  // Do not suggest members who are already assigned to this task.
  const availableMembers = members.filter(
    (member) => !excludedUserIds.includes(member.userId),
  );

  return (
    <Autocomplete<ProjectMember, false, false, false>
      options={availableMembers}
      value={null}
      inputValue={inputValue}
      disabled={disabled}
      loading={isFetching}
      filterOptions={(options) => options}
      getOptionLabel={(option) => option.email}
      isOptionEqualToValue={(option, value) => option.userId === value.userId}
      noOptionsText={
        inputValue.trim().length < 2
          ? "Type at least 2 characters"
          : isError
            ? "Unable to load members"
            : "No project members found"
      }
      onInputChange={(_event, newInputValue, reason) => {
        // Ignore the input reset caused by selecting an option.
        // The selection handler clears the input explicitly.
        if (reason === "input" || reason === "clear") {
          setInputValue(newInputValue);
        }
      }}
      onChange={(_event, selectedMember) => {
        if (!selectedMember) return;

        // Only a selected backend result reaches the parent.
        onSelect(selectedMember);

        // Prepare the component for the next search.
        setInputValue("");
        setSearchQuery("");
      }}
      renderInput={(params) => (
        <TextField
          {...params}
          label="Search project members"
          placeholder="Enter member email..."
          helperText="Search by email. Only existing project members can be selected."
          error={isError}
          slotProps={{
            ...params.slotProps,
            input: {
              ...params.slotProps?.input,
              endAdornment: (
                <>
                  {isFetching && <CircularProgress color="inherit" size={18} />}

                  {params.slotProps?.input?.endAdornment}
                </>
              ),
            },
          }}
        />
      )}
    />
  );
}
