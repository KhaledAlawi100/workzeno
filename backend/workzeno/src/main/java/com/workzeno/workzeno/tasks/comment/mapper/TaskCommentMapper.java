package com.workzeno.workzeno.tasks.comment.mapper;

import com.workzeno.workzeno.tasks.comment.domain.TaskComment;
import com.workzeno.workzeno.tasks.comment.dto.TaskCommentResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Converts comment entities into API responses.
 */
@Mapper(componentModel = "spring")
public interface TaskCommentMapper {

    @Mapping(target = "taskId", source = "task.id")
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorEmail", source = "author.email")
    TaskCommentResponse toResponse(TaskComment comment);
}