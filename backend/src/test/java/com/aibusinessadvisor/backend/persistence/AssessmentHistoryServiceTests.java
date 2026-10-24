package com.aibusinessadvisor.backend.persistence;

import com.aibusinessadvisor.backend.assessment.dto.AssessmentHistoryItemResponse;
import com.aibusinessadvisor.backend.assessment.service.AssessmentHistoryService;
import com.aibusinessadvisor.backend.assessment.service.AssessmentPersistenceService;

import com.aibusinessadvisor.backend.fraud.dto.FraudAssessmentResponse;

import com.aibusinessadvisor.backend.risk.dto.InsuranceRiskAssessmentResponse;

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
class AssessmentHistoryServiceTests {

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private AssessmentPersistenceService
            assessmentPersistenceService;

    @Autowired
    private AssessmentHistoryService
            assessmentHistoryService;


    @Test
    void returnsOnlyAuthenticatedUsersAssessments() {

        AppUser user =
                new AppUser(
                        "history.test@ai-business-advisor.local",
                        "test-password-hash",
                        "History",
                        "Test",
                        UserRole.ANALYST,
                        true
                );


        AppUser savedUser =
                appUserRepository
                        .saveAndFlush(
                                user
                        );


        assessmentPersistenceService.recordRisk(
                savedUser.getEmail(),
                new InsuranceRiskAssessmentResponse(
                        0.4265319009621938,
                        0.10327080885569255,
                        true,
                        0.5954161286354065,
                        1.0,
                        0.5954161286354065
                )
        );


        assessmentPersistenceService.recordFraud(
                savedUser.getEmail(),
                new FraudAssessmentResponse(
                        0.5377003003817469,
                        0.08585764735167348,
                        true
                )
        );


        List<AssessmentHistoryItemResponse>
                history =
                assessmentHistoryService
                        .getOwnHistory(
                                savedUser.getEmail()
                        );


        assertThat(history)
                .hasSize(2);


        assertThat(
                history
                        .get(0)
                        .createdAt()
        ).isAfterOrEqualTo(
                history
                        .get(1)
                        .createdAt()
        );


        assertThat(
                history
                        .stream()
                        .map(
                                AssessmentHistoryItemResponse
                                        ::assessmentType
                        )
        ).containsExactlyInAnyOrder(
                com.aibusinessadvisor.backend
                        .assessment.model.AssessmentType.RISK,
                com.aibusinessadvisor.backend
                        .assessment.model.AssessmentType.FRAUD
        );
    }
}