
package com.workzeno.workzeno.collaboration.membership.api;

import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembershipRole;
import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.projects.domain.Project;

import java.util.UUID;

/**
 * Public API exposed by the Membership Management module.
 *
 * Other modules use this interface instead of accessing
 * ProjectMembershipRepository directly.
 */
public interface MembershipManagement {

    /**
     * Checks whether a user already belongs to a project.
     */
    boolean isMember(UUID projectId, UUID userId);

    /**
     * Creates membership when an invitation is accepted.
     *
     * The invitation module has already validated the invitation.
     * Membership Management remains responsible for enforcing
     * membership-specific rules and persisting the membership.
     */
    void addMemberFromInvitation(
            Project project,
            User user,
            ProjectMembershipRole role
    );
}
