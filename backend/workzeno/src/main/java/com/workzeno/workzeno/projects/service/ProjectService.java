package com.workzeno.workzeno.projects.service;

import com.workzeno.workzeno.identity.security.CurrentUserProvider;
import com.workzeno.workzeno.identity.user.User;
import com.workzeno.workzeno.identity.user.UserRepository;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.projects.dto.CreateProjectRequest;
import com.workzeno.workzeno.projects.dto.ProjectResponse;
import com.workzeno.workzeno.projects.dto.ProjectSummaryResponse;
import com.workzeno.workzeno.projects.mapper.ProjectMapper;
import com.workzeno.workzeno.projects.repository.ProjectRepository;
import com.workzeno.workzeno.shared.exception.ApiException;

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


    private  User loadAuthenticatedUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "Unauthorized",
                        "Authenticated user could not be found"
                ));
    }

}