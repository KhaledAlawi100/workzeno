package com.workzeno.workzeno.tasks.repository;

import com.workzeno.workzeno.tasks.domain.Task;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    Page<Task> findAllByProject_Id(
            UUID projectId,
            Pageable pageable
    );

    Optional<Task> findByIdAndProject_Id(
            UUID taskId,
            UUID projectId
    );

}