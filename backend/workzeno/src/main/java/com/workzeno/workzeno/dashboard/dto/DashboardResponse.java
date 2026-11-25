package com.workzeno.workzeno.dashboard.dto;

import com.workzeno.workzeno.tasks.dto.TaskSummaryResponse;

import java.util.List;

public record DashboardResponse(
        ProjectMetricsResponse projects,
        TaskMetricsResponse tasks,
        List<TaskSummaryResponse> myTasks
) {
}

