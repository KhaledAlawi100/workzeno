package com.workzeno.workzeno.tasks.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data supplied by the client when adding a task comment.
 *
 * The client must not provide authorId.
 * The backend obtains the author from the authenticated user.
 */
public record CreateTaskCommentRequest(

        @NotBlank(message = "Comment content is required")
        @Size(
                max = 2000,
                message = "Comment must not exceed 2000 characters"
        )
        String content

) {
}