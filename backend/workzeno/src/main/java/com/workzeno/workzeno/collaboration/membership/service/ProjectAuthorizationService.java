package com.workzeno.workzeno.collaboration.membership.service;

import com.workzeno.workzeno.collaboration.membership.api.ProjectAuthorization;
import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembership;
import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembershipRole;
import com.workzeno.workzeno.collaboration.membership.repository.ProjectMembershipRepository;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.shared.exception.ApiException;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectAuthorizationService implements ProjectAuthorization {


    private static final String EXCEPTION_TITLE = "Forbidden";


    private final ProjectMembershipRepository membershipRepository;

    public void requireOwner(Project project, UUID userId) {
        if (!isOwner(project, userId)) {
            throw forbidden("Only the project owner can perform this action");
        }
    }

    public void requireOwnerOrAdmin(Project project, UUID userId) {
        if (isOwner(project, userId))
            return;

        ProjectMembershipRole role = findRole(project, userId)
                .orElseThrow(() -> forbidden("You do not have permission to perform this action"));

        if (role != ProjectMembershipRole.ADMIN) {
            throw forbidden("Only project owners and admins can perform this action");
        }
    }

    private boolean isOwner(Project project, UUID userId) {
        return Objects.equals(project.getOwner().getId(), userId);
    }


    private Optional<ProjectMembershipRole> findRole(Project project, UUID userId) {
        return membershipRepository
                .findByProjectIdAndUserId(project.getId(), userId)
                .map(ProjectMembership::getRole);
    }

    private static ApiException forbidden(String message) {
        return new ApiException(HttpStatus.FORBIDDEN, EXCEPTION_TITLE, message);
    }
}