package com.workzeno.workzeno.tasks.domain;

import com.workzeno.workzeno.projects.domain.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A unit of work that belongs to one project.
 *
 * A task can be assigned to zero or more project members.
 * The task_assignees table stores those assignments.
 */
@Entity
@Table(name = "tasks")
@Getter
public class Task {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // A task must always belong to a project.
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_tasks_project")
    )
    private Project project;



    @NotBlank
    @Size(max = 200)
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status = TaskStatus.TODO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Task() {
        // Required by JPA. Use the public constructor in application code.
    }

    public Task(Project project, String title, String description) {
        this(project, title, description, TaskPriority.MEDIUM, null);
    }

    public Task(
            Project project,
            String title,
            String description,
            TaskPriority priority,
            LocalDate dueDate
    ) {
        this.project = requireProject(project);
        this.title = normalizeTitle(title);
        this.description = normalizeDescription(description);
        this.priority = requirePriority(priority);
        this.status = TaskStatus.TODO;
        this.dueDate = dueDate;
    }

    /** Update only the supplied fields; null description means clear it. */
    public void updateDetails(String title, String description, TaskPriority priority, LocalDate dueDate) {
        if (title != null) {
            this.title = normalizeTitle(title);
        }
        this.description = normalizeDescription(description);
        if (priority != null) {
            this.priority = priority;
        }
        this.dueDate = dueDate;
    }

    public void changeStatus(TaskStatus status) {
        this.status = requireStatus(status);
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

    private static Project requireProject(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("A task must belong to a project");
        }
        return project;
    }

    private static String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title must not be blank");
        }
        String normalized = title.trim();
        if (normalized.length() > 200) {
            throw new IllegalArgumentException("Task title must not exceed 200 characters");
        }
        return normalized;
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String normalized = description.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        if (normalized.length() > 2000) {
            throw new IllegalArgumentException("Task description must not exceed 2000 characters");
        }
        return normalized;
    }

    private static TaskPriority requirePriority(TaskPriority priority) {
        if (priority == null) {
            throw new IllegalArgumentException("Task priority is required");
        }
        return priority;
    }

    private static TaskStatus requireStatus(TaskStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Task status is required");
        }
        return status;
    }
}
