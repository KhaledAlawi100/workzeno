package com.workzeno.workzeno.projects.service;

import com.workzeno.workzeno.identity.security.service.CurrentUserProvider;
import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.identity.user.repository.UserRepository;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.projects.dto.CreateProjectRequest;
import com.workzeno.workzeno.projects.dto.ProjectResponse;
import com.workzeno.workzeno.projects.dto.ProjectSummaryResponse;
import com.workzeno.workzeno.projects.dto.UpdateProjectRequest;
import com.workzeno.workzeno.projects.mapper.ProjectMapper;
import com.workzeno.workzeno.projects.repository.ProjectRepository;
import com.workzeno.workzeno.shared.exception.ApiException;
import com.workzeno.workzeno.collaboration.membership.api.ProjectAuthorization;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final CurrentUserProvider currentUserProvider;
    private final ProjectAccessService projectAccessService;
    private final ProjectAuthorization projectAuthorizationService;

    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request) {
        UUID userId = currentUserProvider.getCurrentUserId();

        User owner = loadAuthenticatedUser(userId);

        Project project = new Project(request.name(), request.description(), owner);

        return projectMapper.toResponse(projectRepository.save(project));
    }

    public Page<ProjectSummaryResponse> getMyProjects(String search,Pageable pageable) {
        UUID userId = currentUserProvider.getCurrentUserId();

        String normalizedSearch = search == null
                ? ""
                : search.trim();

        return projectRepository
                .findAccessibleProjects(userId, normalizedSearch, pageable)
                .map(projectMapper::toSummaryResponse);
    }

    public ProjectResponse getProjectById(UUID projectId) {
        UUID userId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(projectId, userId);;

        return projectMapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(UUID projectId, UpdateProjectRequest request) {
        UUID userId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(projectId, userId);

        projectAuthorizationService.requireOwnerOrAdmin(project, userId);

        project.updateDetails(request.name(), request.description());

        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse archiveProject(UUID projectId) {
        UUID userId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(projectId, userId);

        projectAuthorizationService.requireOwner(project, userId);

        project.archive();

        return projectMapper.toResponse(project);
    }

    @Transactional
    public ProjectResponse restoreProject(UUID projectId) {
        UUID userId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(projectId, userId);
        projectAuthorizationService.requireOwner(project, userId);

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


}
