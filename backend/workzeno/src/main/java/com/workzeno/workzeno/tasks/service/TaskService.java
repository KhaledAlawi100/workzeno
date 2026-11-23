package com.workzeno.workzeno.tasks.service;

import com.workzeno.workzeno.collaboration.membership.api.ProjectAuthorization;
import com.workzeno.workzeno.collaboration.membership.domain.ProjectMembership;
import com.workzeno.workzeno.collaboration.membership.repository.ProjectMembershipRepository;
import com.workzeno.workzeno.identity.security.service.CurrentUserProvider;
import com.workzeno.workzeno.identity.user.domain.EmailAddress;
import com.workzeno.workzeno.identity.user.domain.User;
import com.workzeno.workzeno.identity.user.repository.UserRepository;
import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.projects.service.ProjectAccessService;
import com.workzeno.workzeno.projects.service.ProjectModificationPolicy;
import com.workzeno.workzeno.shared.exception.ApiException;
import com.workzeno.workzeno.tasks.domain.Task;
import com.workzeno.workzeno.tasks.domain.TaskAssignee;
import com.workzeno.workzeno.tasks.domain.TaskPriority;
import com.workzeno.workzeno.tasks.dto.*;
import com.workzeno.workzeno.tasks.mapper.TaskMapper;
import com.workzeno.workzeno.tasks.repository.TaskAssigneeRepository;
import com.workzeno.workzeno.tasks.repository.TaskAssigneeView;
import com.workzeno.workzeno.tasks.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final UserRepository userRepository;
    private final ProjectMembershipRepository membershipRepository;
    private final ProjectAccessService projectAccessService;
    private final CurrentUserProvider currentUserProvider;
    private final TaskMapper taskMapper;
    private final ProjectAuthorization authorizationService;
    private final ProjectModificationPolicy  projectModificationPolicy;

    // ============================================================
    // CREATE TASK
    // ============================================================

    @Transactional
    public TaskResponse createTask(
            UUID projectId,
            CreateTaskRequest request
    ) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        projectModificationPolicy.requireActive(project,"Tasks");

        TaskPriority priority = request.priority() == null
                ? TaskPriority.MEDIUM
                : request.priority();

        List<ProjectMembership> memberships = resolveAssigneeMemberships(
                projectId,
                request.assigneeEmails()
        );

        Task task = new Task(
                project,
                request.title(),
                request.description(),
                priority,
                request.dueDate()
        );


        Task savedTask = taskRepository.save(task);

        if (!memberships.isEmpty()) {

            List<TaskAssignee> assignments = memberships.stream()
                    .map(membership -> new TaskAssignee(
                            savedTask,
                            membership
                    ))
                    .toList();

            taskAssigneeRepository.saveAll(assignments);
        }

        Map<UUID, List<TaskAssigneeResponse>> assigneesByTaskId =
                loadAssigneesByTaskIds(List.of(savedTask.getId()));

        return toTaskResponse(savedTask, assigneesByTaskId);
    }


// ============================================================
// GET PROJECT TASKS
// ============================================================

    public Page<TaskSummaryResponse> getProjectTasks(
            UUID projectId,
            Pageable pageable
    ) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        Page<Task> taskPage = taskRepository.findAllByProject_Id(
                projectId,
                pageable
        );

        return taskPage.map(taskMapper::toSummaryResponse);
    }

    // ============================================================
    // GET ONE TASK
    // ============================================================

    public TaskResponse getTask(
            UUID projectId,
            UUID taskId
    ) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        // Preserve the existing project authorization check.
        projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        Task task = taskRepository.findByIdAndProject_Id(
                taskId,
                projectId
        ).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "Task not found",
                "Task does not exist in this project"
        ));

        // Two queries: one task query and one assignment query.
        Map<UUID, List<TaskAssigneeResponse>> assigneesByTaskId =
                loadAssigneesByTaskIds(List.of(task.getId()));

        return toTaskResponse(task, assigneesByTaskId);
    }

    // ============================================================
// UPDATE TASK
// ============================================================

    @Transactional
    public TaskResponse updateTask(
            UUID projectId,
            UUID taskId,
            UpdateTaskRequest request
    ) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        // Check that the current user can access this project.
        Project project = projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        // Archived projects cannot be modified.
        projectModificationPolicy.requireActive(project,"Tasks");

        // Find the task inside this project only.
        Task task = loadTask(projectId, taskId);

        /*
         * Update the task's ordinary fields.
         *
         * Your existing updateDetails method:
         * - normalizes the title;
         * - normalizes the description;
         * - updates priority;
         * - updates or clears the due date.
         */
        task.updateDetails(
                request.title(),
                request.description(),
                request.priority(),
                request.dueDate()
        );

        // Update the workflow status separately.
        task.changeStatus(request.status());

        // Load assignments in one bulk query for the response.
        Map<UUID, List<TaskAssigneeResponse>> assigneesByTaskId =
                loadAssigneesByTaskIds(List.of(task.getId()));

        return toTaskResponse(task, assigneesByTaskId);
    }


    // ============================================================
// DELETE TASK
// ============================================================

    @Transactional
    public void deleteTask(UUID projectId, UUID taskId) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        projectModificationPolicy.requireActive(project,"Tasks");

        authorizationService.requireOwnerOrAdmin(
                project,
                currentUserId
        );

        Task task = loadTask(projectId, taskId);

        taskRepository.delete(task);
    }

    // ============================================================
// ASSIGN MEMBER TO TASK
// ============================================================

    @Transactional
    public TaskResponse assignMember(
            UUID projectId,
            UUID taskId,
            AssignTaskRequest request
    ) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        projectModificationPolicy.requireActive(project,"Tasks");

        Task task = loadTask(projectId, taskId);

        List<ProjectMembership> memberships = resolveAssigneeMemberships(
                projectId,
                List.of(request.email())
        );

        ProjectMembership membership = memberships.getFirst();

        UUID userId = membership.getUser().getId();

        if (taskAssigneeRepository.existsByTaskIdAndUserId(
                taskId,
                userId
        )) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Assignment already exists",
                    "This member is already assigned to the task"
            );
        }

        TaskAssignee assignment = new TaskAssignee(
                task,
                membership
        );

        taskAssigneeRepository.save(assignment);

        Map<UUID, List<TaskAssigneeResponse>> assigneesByTaskId =
                loadAssigneesByTaskIds(List.of(taskId));

        return toTaskResponse(task, assigneesByTaskId);
    }

    // ============================================================
// UNASSIGN MEMBER FROM TASK
// ============================================================

    @Transactional
    public TaskResponse unassignMember(
            UUID projectId,
            UUID taskId,
            UUID userId
    ) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        projectModificationPolicy.requireActive(project,"Tasks");

        Task task = loadTask(projectId, taskId);

        int deletedRows = taskAssigneeRepository.deleteByTaskIdAndUserId(
                taskId,
                userId
        );

        if (deletedRows == 0) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Assignment not found",
                    "This user is not assigned to the task"
            );
        }

        Map<UUID, List<TaskAssigneeResponse>> assigneesByTaskId =
                loadAssigneesByTaskIds(List.of(taskId));

        return toTaskResponse(task, assigneesByTaskId);
    }



    @Transactional
    public TaskResponse updateTaskStatus(
            UUID projectId,
            UUID taskId,
            UpdateTaskStatusRequest request
    ) {

        UUID currentUserId = currentUserProvider.getCurrentUserId();

        Project project = projectAccessService.loadAccessibleProject(
                projectId,
                currentUserId
        );

        projectModificationPolicy.requireActive(project,"Tasks");

        Task task = loadTask(projectId, taskId);

        task.changeStatus(request.status());

        Map<UUID, List<TaskAssigneeResponse>> assigneesByTaskId =
                loadAssigneesByTaskIds(List.of(taskId));

        return toTaskResponse(task, assigneesByTaskId);
    }



    private Task loadTask(UUID projectId, UUID taskId) {

        return taskRepository.findByIdAndProject_Id(
                taskId,
                projectId
        ).orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "Task not found",
                "Task does not exist in this project"
        ));
    }

    // ============================================================
    // ASSIGNEE EMAIL RESOLUTION AND VALIDATION
    // ============================================================

    /**
     * <h6>Resolve emails to project memberships.<h6/>
     * <p>Policy:<p>
     * <p> 1-Every email must belong to an existing user.<p/>
     * <p> 2- Every user must be a member of this project.<p/>
     * <p>3- Duplicate emails are rejected after normalization.<p/>
     * <p>4- If any email fails validation, the entire request fails.<p/>
     *
     * <p>This method performs bulk queries instead of one query per email.<p/>
     */
    private List<ProjectMembership> resolveAssigneeMemberships(
            UUID projectId,
            List<String> suppliedEmails
    ) {

        if (suppliedEmails == null || suppliedEmails.isEmpty()) {
            return List.of();
        }

        List<String> normalizedEmails = suppliedEmails.stream()
                .map(EmailAddress::normalize)
                .toList();

        Set<String> uniqueEmails = new HashSet<>(normalizedEmails);

        if (uniqueEmails.size() != normalizedEmails.size()) {
            throw invalidAssignee(
                    "Duplicate assignee email addresses are not allowed"
            );
        }

        List<User> users = userRepository.findAllByEmailIn(
                uniqueEmails
        );

        Map<String, User> usersByEmail = users.stream()
                .collect(Collectors.toMap(
                        user -> EmailAddress.normalize(user.getEmail()),
                        Function.identity()
                ));

        List<String> unknownEmails = uniqueEmails.stream()
                .filter(email -> !usersByEmail.containsKey(email))
                .toList();

        if (!unknownEmails.isEmpty()) {
            throw invalidAssignee(
                    "One or more supplied email addresses do not belong to existing users"
            );
        }

        List<UUID> userIds = users.stream()
                .map(User::getId)
                .toList();

        List<ProjectMembership> memberships =
                membershipRepository.findAllByProjectIdAndUserIds(
                        projectId,
                        userIds
                );

        Set<UUID> memberIds = memberships.stream()
                .map(membership -> membership.getUser().getId())
                .collect(Collectors.toSet());

        boolean containsNonMember = userIds.stream()
                .anyMatch(userId -> !memberIds.contains(userId));

        if (containsNonMember) {
            throw invalidAssignee(
                    "Every assignee must be a member of this project"
            );
        }

        return memberships;
    }



    // ============================================================
    // BULK ASSIGNEE LOADING
    // ============================================================

    private Map<UUID, List<TaskAssigneeResponse>> loadAssigneesByTaskIds(
            Collection<UUID> taskIds
    ) {

        if (taskIds == null || taskIds.isEmpty()) {
            return Map.of();
        }

        List<TaskAssigneeView> rows =
                taskAssigneeRepository.findAssigneesByTaskIds(taskIds);

        Map<UUID, List<TaskAssigneeResponse>> result = new HashMap<>();

        for (TaskAssigneeView row : rows) {

            TaskAssigneeResponse assignee = new TaskAssigneeResponse(
                    row.getUserId(),
                    row.getEmail()
            );

            result.computeIfAbsent(
                    row.getTaskId(),
                    ignored -> new ArrayList<>()
            ).add(assignee);
        }

        return result;
    }


    /**
     * Combine the scalar task response from MapStruct with the
     * assignee list loaded by the bulk query.
     */
    private TaskResponse toTaskResponse(
            Task task,
            Map<UUID, List<TaskAssigneeResponse>> assigneesByTaskId
    ) {

        TaskResponse scalarResponse = taskMapper.toResponse(task);

        List<TaskAssigneeResponse> assignees =
                assigneesByTaskId.getOrDefault(
                        task.getId(),
                        List.of()
                );

        return new TaskResponse(
                scalarResponse.id(),
                scalarResponse.projectId(),
                assignees,
                scalarResponse.title(),
                scalarResponse.description(),
                scalarResponse.status(),
                scalarResponse.priority(),
                scalarResponse.dueDate(),
                scalarResponse.createdAt(),
                scalarResponse.updatedAt()
        );
    }

    private ApiException invalidAssignee(String detail) {
        return new ApiException(
                HttpStatus.BAD_REQUEST,
                "Invalid task assignee",
                detail
        );
    }

    }



