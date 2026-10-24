package com.aibusinessadvisor.backend.admin;

import com.aibusinessadvisor.backend.admin.assessment.service.AdminAssessmentService;
import com.aibusinessadvisor.backend.admin.dashboard.service.AdminDashboardService;

import com.aibusinessadvisor.backend.assessment.model.AssessmentRecord;
import com.aibusinessadvisor.backend.assessment.model.AssessmentType;
import com.aibusinessadvisor.backend.assessment.repository.AssessmentRecordRepository;

import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.model.UserRole;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class AdminAssessmentDashboardTests {

    @Autowired
    private AdminAssessmentService
            adminAssessmentService;

    @Autowired
    private AdminDashboardService
            adminDashboardService;

    @Autowired
    private AppUserRepository
            appUserRepository;

    @Autowired
    private AssessmentRecordRepository
            assessmentRecordRepository;


    @Test
    void returnsGlobalAssessmentsWithCreatorDetails() {

        AppUser user =
                new AppUser(
                        "admin.assessment.test@ai-business-advisor.local",
                        "test-password-hash",
                        "Assessment",
                        "Analyst",
                        UserRole.ANALYST,
                        true
                );

        AppUser savedUser =
                appUserRepository
                        .saveAndFlush(
                                user
                        );


        AssessmentRecord risk =
                new AssessmentRecord(
                        AssessmentType.RISK,
                        savedUser,
                        0.4265319009621938,
                        0.10327080885569255,
                        true,
                        0.5954161286354065,
                        0.5954161286354065,
                        1.0
                );


        AssessmentRecord fraud =
                new AssessmentRecord(
                        AssessmentType.FRAUD,
                        savedUser,
                        0.5377003003817469,
                        0.08585764735167348,
                        true,
                        null,
                        null,
                        null
                );


        assessmentRecordRepository
                .saveAllAndFlush(
                        List.of(
                                risk,
                                fraud
                        )
                );


        var responses =
                adminAssessmentService
                        .getAllAssessments()
                        .stream()
                        .filter(
                                response ->
                                        response.createdByEmail()
                                                .equals(
                                                        savedUser.getEmail()
                                                )
                        )
                        .toList();


        assertThat(
                responses
        ).hasSize(
                2
        );


        assertThat(
                responses
                        .stream()
                        .map(
                                response ->
                                        response.assessmentType()
                        )
        ).containsExactlyInAnyOrder(
                AssessmentType.RISK,
                AssessmentType.FRAUD
        );


        assertThat(
                responses
        ).allSatisfy(
                response -> {

                    assertThat(
                            response.createdById()
                    ).isEqualTo(
                            savedUser.getId()
                    );

                    assertThat(
                            response.createdByRole()
                    ).isEqualTo(
                            UserRole.ANALYST
                    );

                    assertThat(
                            response.createdByFirstName()
                    ).isEqualTo(
                            "Assessment"
                    );

                    assertThat(
                            response.createdByLastName()
                    ).isEqualTo(
                            "Analyst"
                    );
                }
        );
    }


    @Test
    void dashboardReflectsNewUsersAndAssessments() {

        var before =
                adminDashboardService
                        .getDashboard();


        AppUser admin =
                new AppUser(
                        "dashboard.admin.test@ai-business-advisor.local",
                        "test-password-hash",
                        "Dashboard",
                        "Admin",
                        UserRole.ADMIN,
                        true
                );


        AppUser analyst =
                new AppUser(
                        "dashboard.analyst.test@ai-business-advisor.local",
                        "test-password-hash",
                        "Dashboard",
                        "Analyst",
                        UserRole.ANALYST,
                        false
                );


        AppUser savedAdmin =
                appUserRepository
                        .saveAndFlush(
                                admin
                        );


        appUserRepository
                .saveAndFlush(
                        analyst
                );


        AssessmentRecord risk =
                new AssessmentRecord(
                        AssessmentType.RISK,
                        savedAdmin,
                        0.4265319009621938,
                        0.10327080885569255,
                        true,
                        0.5954161286354065,
                        0.5954161286354065,
                        1.0
                );


        AssessmentRecord fraud =
                new AssessmentRecord(
                        AssessmentType.FRAUD,
                        savedAdmin,
                        0.5377003003817469,
                        0.08585764735167348,
                        false,
                        null,
                        null,
                        null
                );


        assessmentRecordRepository
                .saveAllAndFlush(
                        List.of(
                                risk,
                                fraud
                        )
                );


        var after =
                adminDashboardService
                        .getDashboard();


        assertThat(
                after.totalUsers()
        ).isEqualTo(
                before.totalUsers() + 2
        );


        assertThat(
                after.activeUsers()
        ).isEqualTo(
                before.activeUsers() + 1
        );


        assertThat(
                after.adminUsers()
        ).isEqualTo(
                before.adminUsers() + 1
        );


        assertThat(
                after.analystUsers()
        ).isEqualTo(
                before.analystUsers() + 1
        );


        assertThat(
                after.totalAssessments()
        ).isEqualTo(
                before.totalAssessments() + 2
        );


        assertThat(
                after.riskAssessments()
        ).isEqualTo(
                before.riskAssessments() + 1
        );


        assertThat(
                after.fraudAssessments()
        ).isEqualTo(
                before.fraudAssessments() + 1
        );


        assertThat(
                after.flaggedAssessments()
        ).isEqualTo(
                before.flaggedAssessments() + 1
        );
    }
}