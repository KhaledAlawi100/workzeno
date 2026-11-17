import { useEffect } from "react";
import { Controller, useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";

import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormHelperText,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
} from "@mui/material";

import { getApiErrorMessage } from "../../../../lib/api";

import { useCreateInvitation } from "../hooks/useCreateInvitation";

import type {
  CreateInvitationResponse,
  InvitationRole,
} from "../invitation.types";

// Validate the invitation form before sending a request.
const createInvitationSchema = z.object({
  inviteeEmail: z
    .string()
    .trim()
    .min(1, "Email address is required")
    .email("Please enter a valid email address"),

  role: z.enum(["MEMBER", "ADMIN"]),
});

type CreateInvitationFormValues = z.infer<typeof createInvitationSchema>;

interface CreateInvitationDialogProps {
  open: boolean;
  projectId: string;

  canInviteAdmin: boolean;

  onClose: () => void;
  onCreated: (invitation: CreateInvitationResponse) => void;
}

export default function CreateInvitationDialog({
  open,
  projectId,
  canInviteAdmin,
  onClose,
  onCreated,
}: CreateInvitationDialogProps) {
  const createInvitationMutation = useCreateInvitation(projectId);
  const resetCreateInvitation = createInvitationMutation.reset;
  const {
    register,
    handleSubmit,
    control,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<CreateInvitationFormValues>({
    resolver: zodResolver(createInvitationSchema),
    defaultValues: {
      inviteeEmail: "",
      role: "MEMBER",
    },
  });

  // Reset the form and previous API error whenever the dialog opens.
  useEffect(() => {
    if (open) {
      reset({
        inviteeEmail: "",
        role: "MEMBER",
      });

      resetCreateInvitation();
    }
  }, [open, reset, resetCreateInvitation]);
  async function handleFormSubmit(
    values: CreateInvitationFormValues,
  ): Promise<void> {
    // Prevent an ADMIN invitation if the current user is not the owner.
    const role: InvitationRole = canInviteAdmin ? values.role : "MEMBER";

    try {
      const invitation = await createInvitationMutation.mutateAsync({
        inviteeEmail: values.inviteeEmail,
        role,
      });

      // Let the parent close the dialog and show a success message.
      onCreated(invitation);
    } catch {
      // The mutation error is displayed inside the dialog.
    }
  }

  const isPending = createInvitationMutation.isPending || isSubmitting;

  return (
    <Dialog
      open={open}
      onClose={isPending ? undefined : onClose}
      fullWidth
      maxWidth="sm"
    >
      <DialogTitle>Invite a team member</DialogTitle>

      <DialogContent dividers>
        <Stack
          component="form"
          id="create-invitation-form"
          spacing={2.5}
          onSubmit={handleSubmit(handleFormSubmit)}
          sx={{ pt: 1 }}
        >
          <TextField
            label="Workzeno account email"
            placeholder="person@example.com"
            type="email"
            fullWidth
            required
            {...register("inviteeEmail")}
            error={Boolean(errors.inviteeEmail)}
            helperText={
              errors.inviteeEmail?.message ??
              "Enter the email address of the person you want to invite."
            }
            disabled={isPending}
          />

          <Controller
            name="role"
            control={control}
            render={({ field }) => (
              <FormControl
                fullWidth
                error={Boolean(errors.role)}
                disabled={isPending}
              >
                <InputLabel id="invitation-role-label">Project role</InputLabel>

                <Select
                  {...field}
                  labelId="invitation-role-label"
                  label="Project role"
                >
                  <MenuItem value="MEMBER">Member</MenuItem>

                  {canInviteAdmin && <MenuItem value="ADMIN">Admin</MenuItem>}
                </Select>

                <FormHelperText>
                  {errors.role?.message ??
                    (canInviteAdmin
                      ? "Choose the role for this invitation."
                      : "You can invite members only.")}
                </FormHelperText>
              </FormControl>
            )}
          />

          {createInvitationMutation.isError && (
            <Alert severity="error">
              {getApiErrorMessage(createInvitationMutation.error)}
            </Alert>
          )}
        </Stack>
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2 }}>
        <Button color="inherit" onClick={onClose} disabled={isPending}>
          Cancel
        </Button>

        <Button
          type="submit"
          form="create-invitation-form"
          variant="contained"
          disabled={isPending}
        >
          {isPending ? "Sending..." : "Send invitation"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
