package com.workzeno.workzeno.projects.service;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.projects.repository.ProjectRepository;
import com.workzeno.workzeno.shared.exception.ApiException;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectAccessService {

    private final ProjectRepository projectRepository;

    public Project loadAccessibleProject(UUID projectId, UUID userId) {
        return projectRepository.findAccessibleProjectById(projectId, userId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Project not found",
                        "Project does not exist or you do not have access to it"
                ));
    }
}
