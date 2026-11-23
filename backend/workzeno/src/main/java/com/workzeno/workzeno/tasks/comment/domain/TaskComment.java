package com.workzeno.workzeno.tasks.comment.domain;

import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.tasks.domain.Task;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;

import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * A comment written by a user on a task.
 *
 * Comments belong to tasks, not directly to projects.
 * The task determines which project the comment belongs to.
 */
@Entity
@Table(name = "task_comments")
@Getter
public class TaskComment {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "task_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_task_comments_task")
    )
    private Task task;

    // The user who created the comment.
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "author_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_task_comments_author")
    )
    private User author;

    @NotBlank
    @Size(max = 2000)
    @Column(name = "content", nullable = false, length = 2000)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected TaskComment() {
    }

    public TaskComment(Task task, User author, String content) {
        this.task = requireTask(task);
        this.author = requireAuthor(author);
        this.content = normalizeContent(content);
    }

    /**
     * Update the comment content.
     *
     * We keep the original author and creation timestamp unchanged.
     */
    public void updateContent(String content) {
        this.content = normalizeContent(content);
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    private static Task requireTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException(
                    "A comment must belong to a task"
            );
        }

        return task;
    }

    private static User requireAuthor(User author) {
        if (author == null) {
            throw new IllegalArgumentException(
                    "A comment must have an author"
            );
        }

        return author;
    }

    private static String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "Comment content must not be blank"
            );
        }

        String normalized = content.trim();

        if (normalized.length() > 2000) {
            throw new IllegalArgumentException(
                    "Comment content must not exceed 2000 characters"
            );
        }

        return normalized;
    }
}