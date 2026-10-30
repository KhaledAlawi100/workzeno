package com.workzeno.workzeno.projects.mapper;

import com.workzeno.workzeno.projects.domain.Project;
import com.workzeno.workzeno.projects.dto.ProjectResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "ownerId", source = "owner.id")
    ProjectResponse toResponse(Project project);
}