package com.workzeno.workzeno.dashboard.controller;

import com.workzeno.workzeno.dashboard.dto.DashboardResponse;
import com.workzeno.workzeno.dashboard.service.DashboardService;
import com.workzeno.workzeno.shared.api.ApiResponse;
import com.workzeno.workzeno.shared.api.ApiResponseFactory;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final ApiResponseFactory responseFactory;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(
            HttpServletRequest httpRequest
    ) {

        log.info("Dashboard request received");

        DashboardResponse response = dashboardService.getDashboard();

        return ResponseEntity.ok(
                responseFactory.success(
                        "Dashboard retrieved successfully",
                        response,
                        httpRequest
                )
        );
    }
}