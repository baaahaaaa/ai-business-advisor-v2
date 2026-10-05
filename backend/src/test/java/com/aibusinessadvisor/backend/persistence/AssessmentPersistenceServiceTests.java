package com.aibusinessadvisor.backend.persistence;

import com.aibusinessadvisor.backend.assessment.model.AssessmentRecord;
import com.aibusinessadvisor.backend.assessment.model.AssessmentType;
import com.aibusinessadvisor.backend.assessment.repository.AssessmentRecordRepository;
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
class AssessmentPersistenceServiceTests {

    @Autowired
    private AppUserRepository
            appUserRepository;

    @Autowired
    private AssessmentRecordRepository
            assessmentRecordRepository;

    @Autowired
    private AssessmentPersistenceService
            assessmentPersistenceService;


    @Test
    void persistsRiskAndFraudForAuthenticatedUser() {

        AppUser user =
                new AppUser(
                        "assessment.persistence@ai-business-advisor.local",
                        "test-password-hash",
                        "Assessment",
                        "Persistence",
                        UserRole.ANALYST,
                        true
                );


        AppUser savedUser =
                appUserRepository
                        .saveAndFlush(
                                user
                        );


        InsuranceRiskAssessmentResponse
                riskResponse =
                new InsuranceRiskAssessmentResponse(
                        0.4265319009621938,
                        0.10327080885569255,
                        true,
                        0.5954161286354065,
                        1.0,
                        0.5954161286354065
                );


        assessmentPersistenceService
                .recordRisk(
                        savedUser.getEmail(),
                        riskResponse
                );


        FraudAssessmentResponse fraudResponse =
                new FraudAssessmentResponse(
                        0.5377003003817469,
                        0.08585764735167348,
                        true
                );


        assessmentPersistenceService
                .recordFraud(
                        savedUser.getEmail(),
                        fraudResponse
                );


        assessmentRecordRepository.flush();


        List<AssessmentRecord> records =
                assessmentRecordRepository
                        .findAllByCreatedByIdOrderByCreatedAtAsc(
                                savedUser.getId()
                        );


        assertThat(records)
                .hasSize(2);


        AssessmentRecord risk =
                records.stream()
                        .filter(
                                record ->
                                        record.getAssessmentType()
                                                == AssessmentType.RISK
                        )
                        .findFirst()
                        .orElseThrow();


        assertThat(
                risk.getCreatedBy().getId()
        ).isEqualTo(
                savedUser.getId()
        );


        assertThat(
                risk.getPrimaryScore()
        ).isEqualTo(
                0.4265319009621938
        );


        assertThat(
                risk.getTechnicalThreshold()
        ).isEqualTo(
                0.10327080885569255
        );


        assertThat(
                risk.isFlagged()
        ).isTrue();


        assertThat(
                risk.getPredictedFrequency()
        ).isEqualTo(
                0.5954161286354065
        );


        assertThat(
                risk.getExpectedClaimCount()
        ).isEqualTo(
                0.5954161286354065
        );


        assertThat(
                risk.getExposure()
        ).isEqualTo(
                1.0
        );


        AssessmentRecord fraud =
                records.stream()
                        .filter(
                                record ->
                                        record.getAssessmentType()
                                                == AssessmentType.FRAUD
                        )
                        .findFirst()
                        .orElseThrow();


        assertThat(
                fraud.getCreatedBy().getId()
        ).isEqualTo(
                savedUser.getId()
        );


        assertThat(
                fraud.getPrimaryScore()
        ).isEqualTo(
                0.5377003003817469
        );


        assertThat(
                fraud.getTechnicalThreshold()
        ).isEqualTo(
                0.08585764735167348
        );


        assertThat(
                fraud.isFlagged()
        ).isTrue();


        assertThat(
                fraud.getPredictedFrequency()
        ).isNull();


        assertThat(
                fraud.getExpectedClaimCount()
        ).isNull();


        assertThat(
                fraud.getExposure()
        ).isNull();
    }
}