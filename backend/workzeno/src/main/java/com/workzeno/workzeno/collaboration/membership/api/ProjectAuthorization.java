
package com.workzeno.workzeno.collaboration.membership.api;

import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.shared.exception.ApiException;

import java.util.UUID;

/**
 * Public authorization API exposed by Membership Management.
 *
 * Other modules can request authorization checks without
 * depending on the concrete authorization implementation.
 */
public interface ProjectAuthorization {

    /**
     * Checks if the user is the owner of the project.
     *
     * @throws ApiException if the user is not the owner
     */
    void requireOwner(Project project, UUID userId);


    /**
     * Checks if the user is the owner or an admin of the project.
     *
     * @throws ApiException if the user is neither the owner nor an admin
     */
    void requireOwnerOrAdmin(Project project, UUID userId);
}

