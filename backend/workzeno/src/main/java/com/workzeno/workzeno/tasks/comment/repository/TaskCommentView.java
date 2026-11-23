package com.workzeno.workzeno.tasks.comment.repository;

import java.time.Instant;
import java.util.UUID;

/**
 * Lightweight read-only projection for task comments.
 *
 * The repository retrieves exactly the fields required by the API.
 * This prevents lazy-loading queries when converting comments
 * into response DTOs.
 */
public interface TaskCommentView {

    UUID getId();

    UUID getTaskId();

    UUID getAuthorId();

    String getAuthorEmail();

    String getContent();

    Instant getCreatedAt();

    Instant getUpdatedAt();
}