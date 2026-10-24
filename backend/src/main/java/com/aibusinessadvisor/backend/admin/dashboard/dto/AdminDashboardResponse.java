package com.aibusinessadvisor.backend.admin.dashboard.dto;


public record AdminDashboardResponse(

        long totalUsers,
        long activeUsers,
        long adminUsers,
        long analystUsers,

        long totalAssessments,
        long riskAssessments,
        long fraudAssessments,
        long flaggedAssessments

) {
}