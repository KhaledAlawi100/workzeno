
package com.workzeno.workzeno.collaboration.controller;

import com.workzeno.workzeno.collaboration.dto.AddProjectMemberRequest;
import com.workzeno.workzeno.collaboration.dto.ProjectMemberResponse;
import com.workzeno.workzeno.collaboration.dto.UpdateProjectMemberRoleRequest;
import com.workzeno.workzeno.collaboration.service.ProjectMembershipService;
import com.workzeno.workzeno.shared.api.ApiResponse;
import com.workzeno.workzeno.shared.api.ApiResponseFactory;

import com.workzeno.workzeno.shared.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMembershipController {

    private final ProjectMembershipService membershipService;
    private final ApiResponseFactory responseFactory;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody AddProjectMemberRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info("Add project member request received. projectId={}", projectId);

        membershipService.addMember(projectId, request);

        ApiResponse<Void> body = responseFactory.success(
                "Project member added successfully",
                null,
                httpRequest
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(body);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> changeMemberRole(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateProjectMemberRoleRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info(
                "Change project member role request received. projectId={}, userId={}",
                projectId,
                userId
        );

        membershipService.changeMemberRole(projectId, userId, request);

        ApiResponse<Void> body = responseFactory.success(
                "Project member role updated successfully",
                null,
                httpRequest
        );

        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId,
            HttpServletRequest httpRequest
    ) {
        log.info(
                "Remove project member request received. projectId={}, userId={}",
                projectId,
                userId
        );

        membershipService.removeMember(projectId, userId);

        ApiResponse<Void> body = responseFactory.success(
                "Project member removed successfully",
                null,
                httpRequest
        );

        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProjectMemberResponse>>> getProjectMembers(
            @PathVariable UUID projectId,

            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size,

            HttpServletRequest httpRequest
    ) {
        log.info(
                "Project members request received. projectId={}, page={}, size={}",
                projectId,
                page,
                size
        );

        Page<ProjectMemberResponse> result =
                membershipService.getProjectMembers(
                        projectId,
                        PageRequest.of(page, size)
                );

        ApiResponse<Page<ProjectMemberResponse>> body =
                responseFactory.success(
                        "Project members retrieved successfully",
                        result,
                        httpRequest
                );

        return ResponseEntity.ok(body);
    }
}
