package com.workzeno.workzeno.collaboration.membership.domain;

import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.projects.domain.Project;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "project_memberships",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_project_memberships_project_user",
                        columnNames = {"project_id", "user_id"}
                )
        }
)
@Getter
public class ProjectMembership {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "project_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_project_memberships_project")
    )
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_project_memberships_user")
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ProjectMembershipRole role = ProjectMembershipRole.MEMBER;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    protected ProjectMembership() {
        // Required by JPA.
    }

    public ProjectMembership(
            Project project,
            User user,
            ProjectMembershipRole role
    ) {
        this.project = project;
        this.user = user;
        this.role = role;
    }

    @PrePersist
    protected void onCreate() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }

    public void changeRole(ProjectMembershipRole role) {
        this.role = role;
    }
}