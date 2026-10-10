package com.aibusinessadvisor.backend.admin.dashboard.service;

import com.aibusinessadvisor.backend.admin.dashboard.dto.AdminDashboardResponse;

import com.aibusinessadvisor.backend.assessment.model.AssessmentType;
import com.aibusinessadvisor.backend.assessment.repository.AssessmentRecordRepository;

import com.aibusinessadvisor.backend.user.model.UserRole;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AdminDashboardService {

    private final AppUserRepository
            appUserRepository;

    private final AssessmentRecordRepository
            assessmentRecordRepository;


    public AdminDashboardService(
            AppUserRepository appUserRepository,
            AssessmentRecordRepository
                    assessmentRecordRepository
    ) {

        this.appUserRepository =
                appUserRepository;

        this.assessmentRecordRepository =
                assessmentRecordRepository;
    }


    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {

        var users =
                appUserRepository
                        .findAllByOrderByCreatedAtDesc();

        var assessments =
                assessmentRecordRepository
                        .findAllByOrderByCreatedAtDesc();


        long activeUsers =
                users.stream()
                        .filter(
                                user ->
                                        user.isEnabled()
                        )
                        .count();


        long adminUsers =
                users.stream()
                        .filter(
                                user ->
                                        user.getRole()
                                                == UserRole.ADMIN
                        )
                        .count();


        long analystUsers =
                users.stream()
                        .filter(
                                user ->
                                        user.getRole()
                                                == UserRole.ANALYST
                        )
                        .count();


        long riskAssessments =
                assessments.stream()
                        .filter(
                                assessment ->
                                        assessment
                                                .getAssessmentType()
                                                == AssessmentType.RISK
                        )
                        .count();


        long fraudAssessments =
                assessments.stream()
                        .filter(
                                assessment ->
                                        assessment
                                                .getAssessmentType()
                                                == AssessmentType.FRAUD
                        )
                        .count();


        long flaggedAssessments =
                assessments.stream()
                        .filter(
                                assessment ->
                                        assessment.isFlagged()
                        )
                        .count();


        return new AdminDashboardResponse(
                users.size(),
                activeUsers,
                adminUsers,
                analystUsers,

                assessments.size(),
                riskAssessments,
                fraudAssessments,
                flaggedAssessments
        );
    }
}