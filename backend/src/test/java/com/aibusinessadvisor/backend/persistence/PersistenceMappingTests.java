package com.aibusinessadvisor.backend.persistence;

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

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
class PersistenceMappingTests {

    @Autowired
    private AppUserRepository appUserRepository;


    @Autowired
    private AssessmentRecordRepository
            assessmentRecordRepository;


    @Test
    void persistsUserAndRiskAssessment() {

        AppUser user = new AppUser(
                "persistence.test@ai-business-advisor.local",
                "test-password-hash",
                "Persistence",
                "Test",
                UserRole.ANALYST,
                true
        );


        AppUser savedUser =
                appUserRepository.saveAndFlush(
                        user
                );


        assertThat(
                savedUser.getId()
        ).isNotNull();

        assertThat(
                savedUser.getCreatedAt()
        ).isNotNull();


        AssessmentRecord assessment =
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


        AssessmentRecord savedAssessment =
                assessmentRecordRepository
                        .saveAndFlush(
                                assessment
                        );


        assertThat(
                savedAssessment.getId()
        ).isNotNull();

        assertThat(
                savedAssessment.getCreatedAt()
        ).isNotNull();

        assertThat(
                savedAssessment.getAssessmentType()
        ).isEqualTo(
                AssessmentType.RISK
        );

        assertThat(
                savedAssessment
                        .getCreatedBy()
                        .getId()
        ).isEqualTo(
                savedUser.getId()
        );

        assertThat(
                savedAssessment.isAdvisorRequested()
        ).isFalse();

        assertThat(
                savedAssessment.getAdvisorMode()
        ).isNull();
    }
}