package com.workzeno.workzeno.tasks.comment.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Information returned to the frontend for a task comment.
 */
public record TaskCommentResponse(

        UUID id,

        UUID taskId,

        UUID authorId,

        String authorEmail,

        String content,

        Instant createdAt,

        Instant updatedAt

) {
}