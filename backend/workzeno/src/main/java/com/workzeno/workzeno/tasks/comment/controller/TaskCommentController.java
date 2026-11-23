package com.workzeno.workzeno.tasks.comment.controller;

import com.workzeno.workzeno.shared.api.ApiResponse;
import com.workzeno.workzeno.shared.api.ApiResponseFactory;
import com.workzeno.workzeno.tasks.comment.dto.CreateTaskCommentRequest;
import com.workzeno.workzeno.tasks.comment.dto.TaskCommentResponse;
import com.workzeno.workzeno.tasks.comment.service.TaskCommentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks/{taskId}/comments")
@RequiredArgsConstructor
@Validated
public class TaskCommentController {

    private final TaskCommentService commentService;

    private final ApiResponseFactory responseFactory;


    @PostMapping
    public ResponseEntity<ApiResponse<TaskCommentResponse>> addComment(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateTaskCommentRequest request,
            HttpServletRequest httpRequest
    ) {

        log.info(
                "Adding comment to task {} in project {}",
                taskId,
                projectId
        );

        TaskCommentResponse response = commentService.addComment(
                projectId,
                taskId,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                responseFactory.success(
                        "Comment added successfully",
                        response,
                        httpRequest
                )
        );
    }



    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskCommentResponse>>>
    getTaskComments(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size,

            HttpServletRequest httpRequest
    ) {

        // Create a bounded page request.
        // The repository query defines chronological ordering.
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<TaskCommentResponse> response =
                commentService.getTaskComments(
                        projectId,
                        taskId,
                        pageRequest
                );

        return ResponseEntity.ok(
                responseFactory.success(
                        "Task comments retrieved successfully",
                        response,
                        httpRequest
                )
        );
    }

    // ============================================================
// UPDATE COMMENT
// ============================================================

    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<TaskCommentResponse>> updateComment(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PathVariable UUID commentId,
            @Valid @RequestBody CreateTaskCommentRequest request,
            HttpServletRequest httpRequest
    ) {

        TaskCommentResponse response = commentService.updateComment(
                projectId,
                taskId,
                commentId,
                request
        );

        return ResponseEntity.ok(
                responseFactory.success(
                        "Comment updated successfully",
                        response,
                        httpRequest
                )
        );
    }

    // ============================================================
// DELETE COMMENT
// ============================================================

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId,
            @PathVariable UUID commentId
    ) {

        commentService.deleteComment(
                projectId,
                taskId,
                commentId
        );

        return ResponseEntity.noContent().build();
    }





}