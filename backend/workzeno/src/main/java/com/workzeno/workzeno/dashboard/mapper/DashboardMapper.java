package com.workzeno.workzeno.dashboard.mapper;

import com.workzeno.workzeno.dashboard.dto.ProjectMetricsResponse;
import com.workzeno.workzeno.dashboard.dto.TaskMetricsResponse;
import com.workzeno.workzeno.dashboard.repository.DashboardProjectMetricsView;
import com.workzeno.workzeno.dashboard.repository.DashboardTaskMetricsView;
import com.workzeno.workzeno.dashboard.repository.DashboardTaskSummaryView;
import com.workzeno.workzeno.tasks.dto.TaskSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DashboardMapper {

    @Mapping(target = "total", source = "total", defaultValue = "0L")
    @Mapping(target = "active", source = "active", defaultValue = "0L")
    @Mapping(target = "archived", source = "archived", defaultValue = "0L")
    ProjectMetricsResponse toProjectMetricsResponse(
            DashboardProjectMetricsView projection
    );

    @Mapping(target = "total", source = "projection.total", defaultValue = "0L")
    @Mapping(target = "todo", source = "projection.todo", defaultValue = "0L")
    @Mapping(target = "inProgress", source = "projection.inProgress", defaultValue = "0L")
    @Mapping(target = "inReview", source = "projection.inReview", defaultValue = "0L")
    @Mapping(target = "done", source = "projection.done", defaultValue = "0L")
    @Mapping(target = "overdue", source = "projection.overdue", defaultValue = "0L")
    @Mapping(target = "assignedToMe", source = "assignedToMe")
    TaskMetricsResponse toTaskMetricsResponse(
            DashboardTaskMetricsView projection,
            long assignedToMe
    );

    TaskSummaryResponse toTaskSummaryResponse(
            DashboardTaskSummaryView projection
    );
}