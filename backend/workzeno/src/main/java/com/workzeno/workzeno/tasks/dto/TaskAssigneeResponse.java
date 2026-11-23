package com.workzeno.workzeno.tasks.dto;

import java.util.UUID;

/**
 * Represents one user assigned to a task.
 *
 * userId identifies the actual user, not the assignment row.
 */
public record TaskAssigneeResponse(

        UUID userId,

        String email

) {
}