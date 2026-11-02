package com.workzeno.workzeno.collaboration.service;
import com.workzeno.workzeno.collaboration.domain.ProjectMembership;
import com.workzeno.workzeno.collaboration.domain.ProjectMembershipRole;
import com.workzeno.workzeno.collaboration.dto.AddProjectMemberRequest;
import com.workzeno.workzeno.collaboration.dto.UpdateProjectMemberRoleRequest;
import com.workzeno.workzeno.collaboration.repository.ProjectMembershipRepository;
import com.workzeno.workzeno.identity.security.service.CurrentUserProvider;
import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.identity.user.repository.UserRepository;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.projects.domain.ProjectStatus;
import com.workzeno.workzeno.projects.service.ProjectAccessService;
import com.workzeno.workzeno.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectMembershipService {

    private final ProjectMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final ProjectAuthorizationService authorizationService;
    private final ProjectAccessService projectAccessService;

    @Transactional
    public void addMember(UUID projectId, AddProjectMemberRequest request) {
        UUID currentUserId = getCurrentUserId();
        Project project = loadActiveProject(projectId, currentUserId);

        ensureRoleIsProvided(request.role());

        authorizeMemberAddition(project, currentUserId, request.role());

        ensureOwnerIsNotTarget(project, request.userId());


        if (membershipRepository.existsByProjectIdAndUserId(projectId, request.userId())) {
            throw membershipAlreadyExists();
        }

        User user = loadUser(request.userId());

        ProjectMembership membership = new ProjectMembership(
                project,
                user,
                request.role()
        );

        try {
            //Save and flush the membership to ensure that any constraint violations are caught immediately
            membershipRepository.saveAndFlush(membership);

        } catch (DataIntegrityViolationException exception) {

            if (isDuplicateMembershipViolation(exception)) {
                throw membershipAlreadyExists();
            }

            throw exception;
        }
    }

    @Transactional
    public void changeMemberRole(
            UUID projectId,
            UUID targetUserId,
            UpdateProjectMemberRoleRequest request
    ) {
        UUID currentUserId = getCurrentUserId();
        Project project = loadActiveProject(projectId, currentUserId);

        authorizationService.requireOwner(project, currentUserId);
        ensureOwnerIsNotTarget(project, targetUserId);

        ProjectMembership membership =
                loadMembership(projectId, targetUserId);

        ensureRoleIsProvided(request.role());
        membership.changeRole(request.role());
    }

    @Transactional
    public void removeMember(UUID projectId, UUID targetUserId) {
        UUID currentUserId = getCurrentUserId();
        Project project = loadActiveProject(projectId, currentUserId);

        authorizationService.requireOwnerOrAdmin(project, currentUserId);
        ensureOwnerIsNotTarget(project, targetUserId);

        ProjectMembership membership =
                loadMembership(projectId, targetUserId);

        ensureAdminCanOnlyBeRemovedByOwner(
                project,
                membership,
                currentUserId
        );

        membershipRepository.delete(membership);
    }

    // --------------------------------------------------
    // Shared helper methods
    // --------------------------------------------------

    private UUID getCurrentUserId() {
        return currentUserProvider.getCurrentUserId();
    }

    private Project loadActiveProject(UUID projectId, UUID currentUserId) {
        Project project = projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        ensureProjectIsActive(project);

        return project;
    }

    private void authorizeMemberAddition(
            Project project,
            UUID currentUserId,
            ProjectMembershipRole role
    ) {
        if (role == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid membership role",
                    "A membership role is required"
            );
        }

        // If the role of the member being added is ADMIN, only the project owner can add them.
        if (role == ProjectMembershipRole.ADMIN) {
            authorizationService.requireOwner(project, currentUserId);
            return;
        }

        authorizationService.requireOwnerOrAdmin(project, currentUserId);
    }

    private void ensureOwnerIsNotTarget(Project project, UUID targetUserId) {
        if (Objects.equals(project.getOwner().getId(), targetUserId)) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid membership",
                    "The project owner cannot be managed as a member"
            );
        }
    }

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User not found",
                        "The requested user does not exist"
                ));
    }

    private ProjectMembership loadMembership(
            UUID projectId,
            UUID targetUserId
    ) {
        return membershipRepository
                .findByProjectIdAndUserId(projectId, targetUserId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Membership not found",
                        "The requested user is not a member of this project"
                ));
    }

    private void ensureAdminCanOnlyBeRemovedByOwner(
            Project project,
            ProjectMembership membership,
            UUID currentUserId
    ) {
        boolean targetIsAdmin =
                membership.getRole() == ProjectMembershipRole.ADMIN;

        boolean currentUserIsOwner =
                Objects.equals(project.getOwner().getId(), currentUserId);

        if (targetIsAdmin && !currentUserIsOwner) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Forbidden",
                    "Only the project owner can remove an admin"
            );
        }
    }

    private void ensureProjectIsActive(Project project) {
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Project is archived",
                    "Membership cannot be changed while the project is archived"
            );
        }
    }

    private ApiException membershipAlreadyExists() {
        return new ApiException(
                HttpStatus.CONFLICT,
                "Membership conflict",
                "This user is already a member of the project"
        );
    }

    private boolean isDuplicateMembershipViolation(Throwable exception) {
        Throwable current = exception;

        while (current != null) {

            if (current instanceof ConstraintViolationException violation) {
                return "uq_project_memberships_project_user"
                        .equalsIgnoreCase(violation.getConstraintName());
            }

            current = current.getCause();
        }

        return false;
    }

    private void ensureRoleIsProvided(ProjectMembershipRole role) {
        if (role == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid membership role",
                    "A membership role is required"
            );
        }
    }

}
