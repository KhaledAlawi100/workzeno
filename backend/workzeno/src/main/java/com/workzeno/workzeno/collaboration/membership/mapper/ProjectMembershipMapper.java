
package com.workzeno.workzeno.collaboration.membership.mapper;

import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembership;
import com.workzeno.workzeno.collaboration.membership.dto.ProjectMemberResponse;
import com.workzeno.workzeno.collaboration.membership.dto.ProjectOwnerResponse;
import com.workzeno.workzeno.identity.user.domain.User;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMembershipMapper {

    /**
     * Converts an actual project membership into its API response.
     */
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "role", target = "role")
    ProjectMemberResponse toProjectMemberResponse(
            ProjectMembership membership
    );

    /**
     * Converts the project's owner into the owner API response.
     *
     * User.id is mapped to ProjectOwnerResponse.userId.
     */
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "email", target = "email")
    ProjectOwnerResponse toProjectOwnerResponse(User owner);
}
