package com.workzeno.workzeno.collaboration.invitation.mapper;

import com.workzeno.workzeno.collaboration.invitation.domain.ProjectInvitation;
import com.workzeno.workzeno.collaboration.invitation.dto.AcceptInvitationResponse;
import com.workzeno.workzeno.collaboration.invitation.dto.CreateInvitationResponse;
import com.workzeno.workzeno.collaboration.invitation.dto.MyInvitationResponse;
import com.workzeno.workzeno.collaboration.invitation.dto.ProjectInvitationResponse;
import com.workzeno.workzeno.collaboration.invitation.dto.ProjectInvitationSummaryResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectInvitationMapper {

    // =========================================================
    // MY INVITATIONS
    // =========================================================

    @Mapping(source = "id", target = "invitationId")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.name", target = "projectName")
    @Mapping(source = "invitedBy.email", target = "invitedByEmail")
    MyInvitationResponse toMyInvitationResponse(
            ProjectInvitation invitation
    );

    // =========================================================
    // CREATE INVITATION
    // =========================================================

    CreateInvitationResponse toCreateInvitationResponse(
            ProjectInvitation invitation
    );

    // =========================================================
    // ACCEPT INVITATION
    // =========================================================

    @Mapping(target = "invitationId", source = "id")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "acceptedAt", source = "acceptedAt")
    AcceptInvitationResponse toAcceptInvitationResponse(
            ProjectInvitation invitation
    );

    // =========================================================
    // PROJECT INVITATION SUMMARY
    // =========================================================

    /**
     * Maps an invitation to the compact representation used by
     * the project invitation listing endpoint.
     *
     * Lifecycle audit fields are intentionally excluded.
     */
    @Mapping(source = "id", target = "invitationId")
    @Mapping(source = "invitedBy.email", target = "invitedByEmail")
    ProjectInvitationSummaryResponse toProjectInvitationSummaryResponse(
            ProjectInvitation invitation
    );

    // =========================================================
    // PROJECT INVITATION DETAILS
    // =========================================================

    /**
     * Maps an invitation to its complete representation.
     *
     * Includes lifecycle timestamps and revocation information.
     */
    @Mapping(source = "id", target = "invitationId")
    @Mapping(source = "invitedBy.email", target = "invitedByEmail")
    @Mapping(source = "revokedBy.email", target = "revokedByEmail")
    ProjectInvitationResponse toProjectInvitationResponse(
            ProjectInvitation invitation
    );
}