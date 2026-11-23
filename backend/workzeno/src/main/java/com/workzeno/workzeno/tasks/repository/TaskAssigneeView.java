package com.workzeno.workzeno.tasks.repository;

import java.util.UUID;

/**
 * A lightweight projection containing the information needed
 * to display one task assignment.
 *
 * Spring Data JPA maps the query aliases to these getter names.
 */
public interface TaskAssigneeView {

    UUID getTaskId();

    UUID getUserId();

    String getEmail();
}