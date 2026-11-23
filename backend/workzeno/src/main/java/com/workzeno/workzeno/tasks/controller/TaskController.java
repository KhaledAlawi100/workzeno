package com.workzeno.workzeno.tasks.controller;

import com.workzeno.workzeno.shared.api.ApiResponse;
import com.workzeno.workzeno.shared.api.ApiResponseFactory;
import com.workzeno.workzeno.tasks.dto.*;
import com.workzeno.workzeno.tasks.service.TaskService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
@RequiredArgsConstructor
@Validated
public class TaskController {

    private final TaskService taskService;

    private final ApiResponseFactory responseFactory;


    // ============================================================
    // CREATE TASK
    // ============================================================

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest request,
            HttpServletRequest httpRequest
    ) {

        log.info("Creating task in project {}", projectId);

        TaskResponse response = taskService.createTask(
                projectId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                responseFactory.success(
                        "Task created successfully",
                        response,
                        httpRequest
                )
        );
    }


// ============================================================
// GET PROJECT TASKS
// ============================================================

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskSummaryResponse>>> getProjectTasks(
            @PathVariable UUID projectId,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size,

            HttpServletRequest httpRequest
    ) {

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt")
                .and(Sort.by(Sort.Direction.DESC, "id"));

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                sort
        );

        Page<TaskSummaryResponse> response = taskService.getProjectTasks(
                projectId,
                pageRequest
        );

        return ResponseEntity.ok(
                responseFactory.success(
                        "Project tasks retrieved successfully",
                        response,
                        httpRequest
                )
        );
    }

    // ============================================================
    // GET ONE TASK
    // ============================================================

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            HttpServletRequest httpRequest
    ) {

        TaskResponse response = taskService.getTask(
                projectId,
                taskId
        );

        return ResponseEntity.ok(
                responseFactory.success(
                        "Task retrieved successfully",
                        response,
                        httpRequest
                )
        );
    }


    // ============================================================
    // UPDATE TASK
    // ============================================================

    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            HttpServletRequest httpRequest
    ) {

        log.info(
                "Updating task {} in project {}",
                taskId,
                projectId
        );

        TaskResponse response = taskService.updateTask(
                projectId,
                taskId,
                request
        );

        return ResponseEntity.ok(
                responseFactory.success(
                        "Task updated successfully",
                        response,
                        httpRequest
                )
        );
    }


    // ============================================================
    // DELETE TASK
    // ============================================================

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId
    ) {

        log.info(
                "Deleting task {} from project {}",
                taskId,
                projectId
        );

        taskService.deleteTask(projectId, taskId);

        // HTTP 204 means the deletion succeeded and has no response body.
        return ResponseEntity.noContent().build();
    }


    // ============================================================
    // ASSIGN MEMBER TO TASK
    // ============================================================

    @PostMapping("/{taskId}/assignees")
    public ResponseEntity<ApiResponse<TaskResponse>> assignMember(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody AssignTaskRequest request,
            HttpServletRequest httpRequest
    ) {

        TaskResponse response = taskService.assignMember(
                projectId,
                taskId,
                request
        );

        return ResponseEntity.ok(
                responseFactory.success(
                        "Member assigned to task successfully",
                        response,
                        httpRequest
                )
        );
    }


    // ============================================================
    // UNASSIGN MEMBER FROM TASK
    // ============================================================

    @DeleteMapping("/{taskId}/assignees/{userId}")
    public ResponseEntity<ApiResponse<TaskResponse>> unassignMember(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PathVariable UUID userId,
            HttpServletRequest httpRequest
    ) {

        TaskResponse response = taskService.unassignMember(
                projectId,
                taskId,
                userId
        );

        return ResponseEntity.ok(
                responseFactory.success(
                        "Member unassigned from task successfully",
                        response,
                        httpRequest
                )
        );
    }

    // ============================================================
// UPDATE TASK STATUS
// ============================================================

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            HttpServletRequest httpRequest
    ) {

        TaskResponse response = taskService.updateTaskStatus(
                projectId,
                taskId,
                request
        );

        return ResponseEntity.ok(
                responseFactory.success(
                        "Task status updated successfully",
                        response,
                        httpRequest
                )
        );
    }
}