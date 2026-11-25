package com.workzeno.workzeno.dashboard.service;

import com.workzeno.workzeno.dashboard.dto.DashboardResponse;
import com.workzeno.workzeno.dashboard.mapper.DashboardMapper;
import com.workzeno.workzeno.dashboard.repository.DashboardProjectMetricsView;
import com.workzeno.workzeno.dashboard.repository.DashboardRepository;
import com.workzeno.workzeno.dashboard.repository.DashboardTaskMetricsView;
import com.workzeno.workzeno.identity.security.service.CurrentUserProvider;
import com.workzeno.workzeno.tasks.dto.TaskSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int MY_TASKS_LIMIT = 5;

    private final DashboardRepository dashboardRepository;
    private final DashboardMapper dashboardMapper;
    private final CurrentUserProvider currentUserProvider;
    private final Clock applicationClock;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {

        UUID userId = currentUserProvider.getCurrentUserId();

        // Use the configured Asia/Riyadh business timezone.
        LocalDate today = LocalDate.now(applicationClock);

        DashboardProjectMetricsView projectMetrics =
                dashboardRepository.getProjectMetrics(userId);

        DashboardTaskMetricsView taskMetrics =
                dashboardRepository.getTaskMetrics(userId, today);

        long assignedToMe =
                dashboardRepository.countTasksAssignedToUser(userId);

        List<TaskSummaryResponse> myTasks =
                dashboardRepository.findMyOpenTasks(
                                userId,
                                PageRequest.of(0, MY_TASKS_LIMIT)
                        )
                        .stream()
                        .map(dashboardMapper::toTaskSummaryResponse)
                        .toList();

        return new DashboardResponse(
                dashboardMapper.toProjectMetricsResponse(projectMetrics),
                dashboardMapper.toTaskMetricsResponse(
                        taskMetrics,
                        assignedToMe
                ),
                myTasks
        );
    }
}