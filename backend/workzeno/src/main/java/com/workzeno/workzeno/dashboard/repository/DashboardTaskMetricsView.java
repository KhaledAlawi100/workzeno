
package com.workzeno.workzeno.dashboard.repository;

public interface DashboardTaskMetricsView {

    Long getTotal();

    Long getTodo();

    Long getInProgress();

    Long getInReview();

    Long getDone();

    Long getOverdue();

}
