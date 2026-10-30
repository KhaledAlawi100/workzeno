package com.workzeno.workzeno.projects.controller;

import com.workzeno.workzeno.projects.dto.CreateProjectRequest;
import com.workzeno.workzeno.projects.dto.ProjectResponse;
import com.workzeno.workzeno.projects.service.ProjectService;
import com.workzeno.workzeno.shared.api.ApiResponse;
import com.workzeno.workzeno.shared.api.ApiResponseFactory;

import com.workzeno.workzeno.shared.openapi.CreateProjectOperation;
import com.workzeno.workzeno.shared.openapi.GetProjectsOperation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ApiResponse<Page<ProjectResponse>>> getMyProjects(
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page index must not be negative")
            int page,

            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "Page size must be at least 1")
            @Max(value = 100, message = "Page size must not exceed 100")
            int size,

            HttpServletRequest httpRequest
    ) {
        log.info(
                "Project list request received. page={}, size={}",
                page,
                size
        );


        Page<ProjectResponse> result = projectService.getMyProjects(
                PageRequest.of(page, size)
        );

        ApiResponse<Page<ProjectResponse>> body = responseFactory.success(
                "Projects retrieved successfully",
                result,
                httpRequest
        );

        return ResponseEntity
                .ok()
                .body(body);
    }

}