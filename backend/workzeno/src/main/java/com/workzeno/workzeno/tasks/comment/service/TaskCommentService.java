package com.workzeno.workzeno.tasks.comment.service;

import com.workzeno.workzeno.identity.security.service.CurrentUserProvider;
import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.identity.user.repository.UserRepository;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.projects.service.ProjectAccessService;
import com.workzeno.workzeno.projects.service.ProjectModificationPolicy;
import com.workzeno.workzeno.shared.exception.ApiException;
import com.workzeno.workzeno.tasks.comment.domain.TaskComment;
import com.workzeno.workzeno.tasks.comment.dto.CreateTaskCommentRequest;
import com.workzeno.workzeno.tasks.comment.dto.TaskCommentResponse;
import com.workzeno.workzeno.tasks.comment.mapper.TaskCommentMapper;
import com.workzeno.workzeno.tasks.comment.repository.TaskCommentRepository;
import com.workzeno.workzeno.tasks.domain.Task;
import com.workzeno.workzeno.tasks.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskCommentService {

    private final TaskCommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectAccessService projectAccessService;
    private final CurrentUserProvider currentUserProvider;
    private final TaskCommentMapper commentMapper;
    private final ProjectModificationPolicy projectModificationPolicy;


    // ============================================================
    // CREATE COMMENT
    // ============================================================

    @Transactional
    public TaskCommentResponse addComment(
            UUID projectId,
            UUID taskId,
            CreateTaskCommentRequest request
    ) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        projectModificationPolicy.requireActive(project, "Comment");

        Task task = loadTask(projectId, taskId);

        User author = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User not found",
                        "The authenticated user could not be found"
                ));

        TaskComment comment = new TaskComment(
                task,
                author,
                request.content()
        );

        TaskComment savedComment = commentRepository.save(comment);

        return commentMapper.toResponse(savedComment);
    }


    // ============================================================
    // GET TASK COMMENTS
    // ============================================================

    public Page<TaskCommentResponse> getTaskComments(
            UUID projectId,
            UUID taskId,
            Pageable pageable
    ) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        loadTask(projectId, taskId);

        return commentRepository
                .findCommentViewsByTaskId(taskId, pageable)
                .map(comment -> new TaskCommentResponse(
                        comment.getId(),
                        comment.getTaskId(),
                        comment.getAuthorId(),
                        comment.getAuthorEmail(),
                        comment.getContent(),
                        comment.getCreatedAt(),
                        comment.getUpdatedAt()
                ));
    }


    // ============================================================
    // UPDATE COMMENT
    // ============================================================

    @Transactional
    public TaskCommentResponse updateComment(
            UUID projectId,
            UUID taskId,
            UUID commentId,
            CreateTaskCommentRequest request
    ) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        projectModificationPolicy.requireActive(project, "Comments");

        loadTask(projectId, taskId);

        TaskComment comment = loadComment(taskId, commentId);

        requireCommentAuthor(comment, currentUserId);

        comment.updateContent(request.content());

        return commentMapper.toResponse(comment);
    }


    @Transactional
    public void deleteComment(
            UUID projectId,
            UUID taskId,
            UUID commentId
    ) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        projectModificationPolicy.requireActive(project, "Comments");

        loadTask(projectId, taskId);

        TaskComment comment = loadComment(taskId, commentId);

        requireCommentAuthor(comment, currentUserId);

        commentRepository.delete(comment);
    }


    // ============================================================
    // LOAD COMMENT
    // ============================================================

    private TaskComment loadComment(
            UUID taskId,
            UUID commentId
    ) {

        return commentRepository.findById(commentId)
                .filter(comment ->
                        comment.getTask().getId().equals(taskId)
                )
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Comment not found",
                        "Comment does not exist on this task"
                ));
    }


    // ============================================================
    // COMMENT AUTHOR VALIDATION
    // ============================================================

    private void requireCommentAuthor(
            TaskComment comment,
            UUID currentUserId
    ) {

        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Cannot modify comment",
                    "Only the comment author can edit or delete this comment"
            );
        }
    }


    // ============================================================
    // LOAD TASK
    // ============================================================

    private Task loadTask(
            UUID projectId,
            UUID taskId
    ) {

        return taskRepository.findByIdAndProject_Id(
                taskId,
                projectId
        ).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "Task not found",
                "Task does not exist in this project"
        ));
    }
}