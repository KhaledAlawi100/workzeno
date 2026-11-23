
package com.workzeno.workzeno.collaboration.membership.controller;

import com.workzeno.workzeno.collaboration.membership.dto.*;
import com.workzeno.workzeno.collaboration.membership.service.ProjectMembershipService;
import com.workzeno.workzeno.shared.api.ApiResponse;
import com.workzeno.workzeno.shared.api.ApiResponseFactory;

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

import java.util.List;
import java.util.UUID;

@Validated
@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectId}")
@RequiredArgsConstructor
public class ProjectMembershipController {

    private final ProjectMembershipService membershipService;
    private final ApiResponseFactory responseFactory;

    /**
     * Adds a user to the project.
     */
    @PostMapping("/members")
    public ResponseEntity<ApiResponse<Void>> addMember(
            @PathVariable UUID projectId,
            @Valid @RequestBody AddProjectMemberRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info(
                "Add project member request received. projectId={}",
                projectId
        );

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

    /**
     * Changes the role of an existing project member.
     */
    @PatchMapping("/members/{userId}")
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

        membershipService.changeMemberRole(
                projectId,
                userId,
                request
        );

        ApiResponse<Void> body = responseFactory.success(
                "Project member role updated successfully",
                null,
                httpRequest
        );

        return ResponseEntity.ok(body);
    }

    /**
     * Removes a user from the project.
     */
    @DeleteMapping("/members/{userId}")
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

    /**
     * Retrieves only actual project memberships.
     */
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<Page<ProjectMemberResponse>>>
    getProjectMembers(
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

    /**
     * Retrieves the project owner and paginated project members.
     *
     * The owner is returned separately because the owner
     * is not a ProjectMembership record.
     */
    @GetMapping("/team")
    public ResponseEntity<ApiResponse<ProjectTeamResponse>> getProjectTeam(
            @PathVariable UUID projectId,

            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size,

            HttpServletRequest httpRequest
    ) {
        log.info(
                "Project team request received. projectId={}, page={}, size={}",
                projectId,
                page,
                size
        );

        ProjectTeamResponse result =
                membershipService.getProjectTeam(
                        projectId,
                        PageRequest.of(page, size)
                );

        ApiResponse<ProjectTeamResponse> body =
                responseFactory.success(
                        "Project team retrieved successfully",
                        result,
                        httpRequest
                );

        return ResponseEntity.ok(body);
    }



    @GetMapping("/my-membership")
    public ResponseEntity<ApiResponse<MyProjectMembershipResponse>>
    getMyProjectMembership(
            @PathVariable UUID projectId,
            HttpServletRequest httpRequest
    ) {
        log.info(
                "Current user project membership request received. projectId={}",
                projectId
        );

        MyProjectMembershipResponse result =
                membershipService.getMyProjectMembership(projectId);

        ApiResponse<MyProjectMembershipResponse> body =
                responseFactory.success(
                        "Your project membership retrieved successfully",
                        result,
                        httpRequest
                );

        return ResponseEntity.ok(body);
    }

    /**
     * Searches project members for task-assignment suggestions.
     *
     * Example:
     * GET /api/v1/projects/{projectId}/members/search?q=ali&limit=10
     */
    @GetMapping("/members/search")
    public ResponseEntity<ApiResponse<List<ProjectMemberResponse>>>
    searchProjectMembers(
            @PathVariable UUID projectId,

            @RequestParam("q")
            @jakarta.validation.constraints.Size(
                    min = 2,
                    max = 254,
                    message = "Search must contain between 2 and 254 characters"
            )
            String query,

            @RequestParam(defaultValue = "10")
            @Min(1) @Max(20)
            int limit,

            HttpServletRequest httpRequest
    ) {

        List<ProjectMemberResponse> result =
                membershipService.searchProjectMembers(
                        projectId,
                        query,
                        limit
                );

        return ResponseEntity.ok(
                responseFactory.success(
                        "Project member suggestions retrieved successfully",
                        result,
                        httpRequest
                )
        );
    }

}
