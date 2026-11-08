package com.workzeno.workzeno.collaboration.mapper;

import com.workzeno.workzeno.collaboration.domain.ProjectInvitation;
import com.workzeno.workzeno.collaboration.dto.invitation.AcceptInvitationResponse;
import com.workzeno.workzeno.collaboration.dto.invitation.CreateInvitationResponse;
import com.workzeno.workzeno.collaboration.dto.invitation.MyInvitationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectInvitationMapper {

    @Mapping(source = "id", target = "invitationId")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.name", target = "projectName")
    @Mapping(source = "invitedBy.email", target = "invitedByEmail")
    MyInvitationResponse toMyInvitationResponse(
            ProjectInvitation invitation
    );


    @Mapping(target = "token", source = "rawToken")
    CreateInvitationResponse toCreateInvitationResponse(
            ProjectInvitation invitation,
            String rawToken
    );

    @Mapping(target = "invitationId", source = "id")
    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "acceptedAt", source = "acceptedAt")
    AcceptInvitationResponse toAcceptInvitationResponse(
            ProjectInvitation invitation
    );






}
