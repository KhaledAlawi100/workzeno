package com.workzeno.workzeno.projects.controller;

import com.workzeno.workzeno.projects.dto.CreateProjectRequest;
import com.workzeno.workzeno.projects.dto.ProjectResponse;
import com.workzeno.workzeno.projects.dto.ProjectSummaryResponse;
import com.workzeno.workzeno.projects.dto.UpdateProjectRequest;
import com.workzeno.workzeno.projects.service.ProjectService;
import com.workzeno.workzeno.shared.api.ApiResponse;
import com.workzeno.workzeno.shared.api.ApiResponseFactory;

import com.workzeno.workzeno.shared.openapi.CreateProjectOperation;
import com.workzeno.workzeno.shared.openapi.GetProjectsOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Validated
public class ProjectController {

    private final ProjectService projectService;
    private final ApiResponseFactory responseFactory;

    @CreateProjectOperation
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Project creation request received");

        ProjectResponse result = projectService.createProject(
                request
        );

        ApiResponse<ProjectResponse> body = responseFactory.success(
                "Project created successfully",
                result,
                httpRequest
        );

        log.info("Project created successfully. projectId={}", result.id());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(body);
    }


    @GetProjectsOperation
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectSummaryResponse>>> getMyProjects(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page index must not be negative")
            int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size must not exceed 100")
            int size,
            @RequestParam(required = false)
            @Size(max = 120, message = "Search term must not exceed 120 characters")
            String search,

            HttpServletRequest httpRequest
    ) {
        log.info("Project list request received. page={}, size={}", page, size);

        Page<ProjectSummaryResponse> result = projectService.getMyProjects(
                search,
                PageRequest.of(page, size)
        );

        ApiResponse<Page<ProjectSummaryResponse>> body = responseFactory.success(
                "Projects retrieved successfully",
                result,
                httpRequest
        );

        return ResponseEntity
                .ok()
                .body(body);
    }


    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> getProjectById(
            @PathVariable UUID projectId,
            HttpServletRequest httpRequest
    ) {
        log.info("Project details request received. projectId={}", projectId);

        ProjectResponse result = projectService.getProjectById(projectId);

        ApiResponse<ProjectResponse> body = responseFactory.success(
                "Project retrieved successfully",
                result,
                httpRequest
        );

        return ResponseEntity.ok(body);
    }


    @PatchMapping("/{projectId}")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Project update request received. projectId={}", projectId);

        ProjectResponse result = projectService.updateProject(projectId, request);

        ApiResponse<ProjectResponse> body = responseFactory.success(
                "Project updated successfully",
                result,
                httpRequest
        );

        return ResponseEntity.ok(body);
    }

    @PatchMapping("/{projectId}/archive")
    public ResponseEntity<ApiResponse<ProjectResponse>> archiveProject(
            @PathVariable UUID projectId,
            HttpServletRequest httpRequest
    ) {
        log.info("Project archive request received. projectId={}", projectId);

        ProjectResponse result = projectService.archiveProject(projectId);

        ApiResponse<ProjectResponse> body = responseFactory.success(
                "Project archived successfully",
                result,
                httpRequest
        );

        return ResponseEntity.ok(body);
    }

    @PatchMapping("/{projectId}/restore")
    public ResponseEntity<ApiResponse<ProjectResponse>> restoreProject(
            @PathVariable UUID projectId,
            HttpServletRequest httpRequest
    ) {
        log.info("Project restore request received. projectId={}", projectId);

        ProjectResponse result = projectService.restoreProject(projectId);

        ApiResponse<ProjectResponse> body = responseFactory.success(
                "Project restored successfully",
                result,
                httpRequest
        );

        return ResponseEntity.ok(body);
    }




}