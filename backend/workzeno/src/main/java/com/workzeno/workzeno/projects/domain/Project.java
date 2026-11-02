package com.workzeno.workzeno.projects.domain;

import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.shared.exception.ApiException;
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
import jakarta.validation.constraints.Size;

import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
public class Project {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "owner_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_projects_owner")
    )
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Project() {
        // Required by JPA.
    }

    public Project(String name, String description, User owner) {
        this.name = normalizeName(name);
        this.description = normalizeDescription(description);
        this.owner = owner;
        this.status = ProjectStatus.ACTIVE;
    }

    public void updateDetails(String name, String description) {
        if (name != null) {
            if (name.isBlank()) {
                throw new ApiException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid project name",
                        "Project name must not be blank"
                );            }

            this.name = normalizeName(name);
        }

        if (description != null) {
            this.description = normalizeDescription(description);
        }
    }

    public void archive() {
        this.status = ProjectStatus.ARCHIVED;
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

    private static String normalizeName(String value) {
        return value == null ? null : value.trim();
    }

    private static String normalizeDescription(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public void restore() {
        this.status = ProjectStatus.ACTIVE;
    }

}