
package com.workzeno.workzeno.collaboration.membership.dto;

import org.springframework.data.domain.Page;

public record ProjectTeamResponse(
        ProjectOwnerResponse owner,
        Page<ProjectMemberResponse> members
) {
}
