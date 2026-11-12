
package com.workzeno.workzeno.collaboration.membership.api;

import com.workzeno.workzeno.projects.domain.Project;

import java.util.UUID;

/**
 * Public authorization API exposed by Membership Management.
 *
 * Other modules can request authorization checks without
 * depending on the concrete authorization implementation.
 */
public interface ProjectAuthorization {

    void requireOwner(Project project, UUID userId);

    void requireOwnerOrAdmin(Project project, UUID userId);
}

