package com.workzeno.workzeno.tasks.mapper;

import com.workzeno.workzeno.tasks.domain.Task;
import com.workzeno.workzeno.tasks.dto.TaskResponse;
import com.workzeno.workzeno.tasks.dto.TaskSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {


    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "assignees", ignore = true)
    TaskResponse toResponse(Task task);

    @Mapping(target = "projectId", source = "project.id")
    TaskSummaryResponse toSummaryResponse(Task task);

}