package com.workzeno.workzeno.projects.service;

import com.workzeno.workzeno.identity.security.CurrentUserProvider;
import com.workzeno.workzeno.identity.user.User;
import com.workzeno.workzeno.identity.user.UserRepository;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.projects.dto.CreateProjectRequest;
import com.workzeno.workzeno.projects.dto.ProjectResponse;
import com.workzeno.workzeno.projects.dto.ProjectSummaryResponse;
import com.workzeno.workzeno.projects.dto.UpdateProjectRequest;
import com.workzeno.workzeno.projects.mapper.ProjectMapper;
import com.workzeno.workzeno.projects.repository.ProjectRepository;
import com.workzeno.workzeno.shared.exception.ApiException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        UUID userId = currentUserProvider.getCurrentUserId();

        User owner = loadAuthenticatedUser(userId);

        Project project = new Project(request.name(), request.description(), owner);

        return projectMapper.toResponse(projectRepository.save(project));
    }

    public Page<ProjectSummaryResponse> getMyProjects(Pageable pageable) {
        UUID userId = currentUserProvider.getCurrentUserId();

        return projectRepository
                .findAccessibleProjects(userId, pageable)
                .map(projectMapper::toSummaryResponse);
    }

    public ProjectResponse getProjectById(UUID projectId) {
        UUID userId = currentUserProvider.getCurrentUserId();

        Project project = loadAccessibleProject(projectId, userId);

        return projectMapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request) {
        UUID userId = currentUserProvider.getCurrentUserId();

        Project project = loadAccessibleProject(projectId, userId);

        checkProjectOwnership(project, userId);

        project.updateDetails(request.name(), request.description());

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse archiveProject(UUID projectId) {
        UUID userId = currentUserProvider.getCurrentUserId();

        Project project = loadAccessibleProject(projectId, userId);

        checkProjectOwnership(project, userId);

        project.archive();

        return projectMapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse restoreProject(UUID projectId) {
        UUID userId = currentUserProvider.getCurrentUserId();

        Project project = loadAccessibleProject(projectId, userId);
        checkProjectOwnership(project, userId);

        project.restore();

        return projectMapper.toResponse(project);
    }


    private User loadAuthenticatedUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        "Authenticated user could not be found"
                ));
    }

    private Project loadAccessibleProject(UUID projectId, UUID userId) {
        return projectRepository
                .findAccessibleProjectById(projectId, userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Project not found",
                        "Project does not exist or you do not have access to it"
                ));
    }

    private void checkProjectOwnership(Project project, UUID userId) {
        if (!Objects.equals(project.getOwner().getId(), userId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Forbidden",
                    "Only the project owner can perform this action"
            );
        }
    }
}
