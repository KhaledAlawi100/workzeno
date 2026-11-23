package com.workzeno.workzeno.projects.service;

import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.projects.domain.ProjectStatus;
import com.workzeno.workzeno.shared.exception.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
/**
 * Centralizes rules governing whether a project can be modified.
 *
 * Other features should use this policy instead of implementing
 * their own archived-project checks.
 */
@Component
public class ProjectModificationPolicy {

    /**
     * Ensures that the project is active before a modification.
     *
     * @param project the project being modified
     * @param resource the resource being modified, such as Tasks or Comments
     * @throws ApiException if the project is archived
     */
    public void requireActive(Project project, String resource) {
        if (project.getStatus() != ProjectStatus.ACTIVE) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Project is archived",
                    resource + " cannot be modified while the project is archived"
            );
        }
    }
}