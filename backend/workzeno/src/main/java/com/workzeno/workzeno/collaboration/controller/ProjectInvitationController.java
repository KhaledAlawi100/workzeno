
package com.workzeno.workzeno.collaboration.controller;

import com.workzeno.workzeno.collaboration.dto.invitation.AcceptInvitationResponse;
import com.workzeno.workzeno.collaboration.dto.invitation.CreateInvitationRequest;
import com.workzeno.workzeno.collaboration.dto.invitation.CreateInvitationResponse;
import com.workzeno.workzeno.collaboration.dto.invitation.MyInvitationResponse;
import com.workzeno.workzeno.collaboration.service.ProjectInvitationService;
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

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Validated
public class ProjectInvitationController {

    private final ProjectInvitationService invitationService;
    private final ApiResponseFactory responseFactory;

    // =========================================================
    // CREATE INVITATION
    // =========================================================

    @PostMapping("/projects/{projectId}/invitations")
    public ResponseEntity<ApiResponse<CreateInvitationResponse>>
    createInvitation(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateInvitationRequest request,
            HttpServletRequest httpRequest
    ) {
        log.info(
                "Create project invitation request received. projectId={}",
                projectId
        );

        CreateInvitationResponse result =
                invitationService.createInvitation(
                        projectId,
                        request
                );

        ApiResponse<CreateInvitationResponse> body =
                responseFactory.success(
                        "Invitation created successfully",
                        result,
                        httpRequest
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(body);
    }


    // =========================================================
    // GET MY PENDING INVITATIONS
    // =========================================================

    @GetMapping("/invitations/me")
    public ResponseEntity<ApiResponse<Page<MyInvitationResponse>>>
    getMyPendingInvitations(
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
                "Pending invitations request received. page={}, size={}",
                page,
                size
        );

        Page<MyInvitationResponse> result =
                invitationService.getMyPendingInvitations(
                        PageRequest.of(page, size)
                );

        ApiResponse<Page<MyInvitationResponse>> body =
                responseFactory.success(
                        "Pending invitations retrieved successfully",
                        result,
                        httpRequest
                );

        return ResponseEntity.ok(body);
    }


    // =========================================================
    // ACCEPT INVITATION
    // =========================================================

    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<ApiResponse<AcceptInvitationResponse>>
    acceptInvitation(
            @PathVariable UUID invitationId,
            HttpServletRequest httpRequest
    ) {
        log.info(
                "Invitation acceptance request received. invitationId={}",
                invitationId
        );

        AcceptInvitationResponse result =
                invitationService.acceptInvitation(invitationId);

        ApiResponse<AcceptInvitationResponse> body =
                responseFactory.success(
                        "Invitation accepted successfully",
                        result,
                        httpRequest
                );

        return ResponseEntity.ok(body);
    }

    // =========================================================
    // DECLINE INVITATION (INTENDED RECIPIENT)
    // =========================================================

    @PostMapping("/invitations/{invitationId}/decline")
    public ResponseEntity<ApiResponse<Void>> declineInvitation(
            @PathVariable UUID invitationId,
            HttpServletRequest httpRequest
    ) {
        log.info(
                "Invitation decline request received. invitationId={}",
                invitationId
        );

        invitationService.declineInvitation(invitationId);

        ApiResponse<Void> body = responseFactory.success(
                "Invitation declined successfully",
                null,
                httpRequest
        );

        return ResponseEntity.ok(body);
    }


    // =========================================================
    // REVOKE INVITATION (PROJECT OWNER OR ADMIN)
    // =========================================================

    @PostMapping("/projects/{projectId}/invitations/{invitationId}/revoke")
    public ResponseEntity<ApiResponse<Void>> revokeInvitation(
            @PathVariable UUID projectId,
            @PathVariable UUID invitationId,
            HttpServletRequest httpRequest
    ) {
        log.info(
                "Invitation revocation request received. projectId={}, invitationId={}",
                projectId,
                invitationId
        );

        invitationService.revokeInvitation(projectId, invitationId);

        ApiResponse<Void> body = responseFactory.success(
                "Invitation revoked successfully",
                null,
                httpRequest
        );

        return ResponseEntity.ok(body);
    }

}
