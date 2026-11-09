package com.workzeno.workzeno.collaboration.mapper;

import com.workzeno.workzeno.collaboration.domain.ProjectMembership;
import com.workzeno.workzeno.collaboration.dto.ProjectMemberResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMembershipMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "role", target = "role")
    ProjectMemberResponse toProjectMemberResponse(
            ProjectMembership membership
    );
}