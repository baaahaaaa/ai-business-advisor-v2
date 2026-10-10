package com.aibusinessadvisor.backend.admin.dashboard.controller;

import com.aibusinessadvisor.backend.admin.dashboard.dto.AdminDashboardResponse;
import com.aibusinessadvisor.backend.admin.dashboard.service.AdminDashboardService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService
            adminDashboardService;


    public AdminDashboardController(
            AdminDashboardService
                    adminDashboardService
    ) {

        this.adminDashboardService =
                adminDashboardService;
    }


    @GetMapping
    public AdminDashboardResponse dashboard() {

        return adminDashboardService
                .getDashboard();
    }
}