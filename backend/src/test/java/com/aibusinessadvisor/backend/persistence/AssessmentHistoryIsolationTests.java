package com.aibusinessadvisor.backend.persistence;

import com.aibusinessadvisor.backend.assessment.dto.AssessmentHistoryItemResponse;
import com.aibusinessadvisor.backend.assessment.model.AssessmentType;
import com.aibusinessadvisor.backend.assessment.repository.AssessmentRecordRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class AssessmentHistoryIsolationTests {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private AssessmentRecordRepository recordRepository;

    @Autowired
    private AssessmentPersistenceService persistenceService;

    @Autowired
    private AssessmentHistoryService historyService;

    @Test
    void usersOnlySeeTheirOwnAssessments() {

        String suffix = UUID.randomUUID().toString();

        AppUser userA = userRepository.saveAndFlush(
                new AppUser(
                        "history.a+" + suffix + "@example.com",
                        "test-password-hash",
                        "User",
                        "A",
                        UserRole.ANALYST,
                        true
                )
        );

        AppUser userB = userRepository.saveAndFlush(
                new AppUser(
                        "history.b+" + suffix + "@example.com",
                        "test-password-hash",
                        "User",
                        "B",
                        UserRole.ANALYST,
                        true
                )
        );

        persistenceService.recordRisk(
                userA.getEmail(),
                new InsuranceRiskAssessmentResponse(
                        0.11, 0.10, true,
                        0.30, 1.0, 0.30
                )
        );

        persistenceService.recordFraud(
                userA.getEmail(),
                new FraudAssessmentResponse(
                        0.22, 0.20, true
                )
        );

        persistenceService.recordRisk(
                userB.getEmail(),
                new InsuranceRiskAssessmentResponse(
                        0.33, 0.15, true,
                        0.40, 1.0, 0.40
                )
        );

        recordRepository.flush();

        List<AssessmentHistoryItemResponse> historyA =
                historyService.getOwnHistory(userA.getEmail());

        List<AssessmentHistoryItemResponse> historyB =
                historyService.getOwnHistory(userB.getEmail());

        assertThat(historyA).hasSize(2);
        assertThat(historyB).hasSize(1);

        assertThat(historyA)
                .extracting(AssessmentHistoryItemResponse::assessmentType)
                .containsExactlyInAnyOrder(
                        AssessmentType.RISK,
                        AssessmentType.FRAUD
                );

        assertThat(historyA)
                .extracting(AssessmentHistoryItemResponse::primaryScore)
                .containsExactlyInAnyOrder(0.11, 0.22);

        assertThat(historyB)
                .extracting(AssessmentHistoryItemResponse::primaryScore)
                .containsExactly(0.33);

        assertThat(historyA)
                .extracting(AssessmentHistoryItemResponse::id)
                .doesNotContain(historyB.get(0).id());
    }

    @Test
    void disabledUserCannotReadHistory() {

        AppUser disabledUser = userRepository.saveAndFlush(
                new AppUser(
                        "history.disabled+" + UUID.randomUUID() + "@example.com",
                        "test-password-hash",
                        "Disabled",
                        "User",
                        UserRole.ANALYST,
                        false
                )
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> historyService.getOwnHistory(
                        disabledUser.getEmail()
                )
        );

        assertThat(exception.getStatusCode().value())
                .isEqualTo(401);
    }
}