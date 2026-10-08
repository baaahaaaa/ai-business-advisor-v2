package com.aibusinessadvisor.backend.persistence;

import com.aibusinessadvisor.backend.assessment.service.AssessmentHistoryService;
import com.aibusinessadvisor.backend.assessment.service.AssessmentPersistenceService;
import com.aibusinessadvisor.backend.assessment.repository.AssessmentRecordRepository;
import com.aibusinessadvisor.backend.fraud.dto.FraudAssessmentResponse;
import com.aibusinessadvisor.backend.risk.dto.InsuranceRiskAssessmentResponse;
import com.aibusinessadvisor.backend.security.JwtService;
import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.model.UserRole;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AssessmentHistoryHttpJwtTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private AssessmentRecordRepository recordRepository;

    @Autowired
    private AssessmentPersistenceService persistenceService;

    @Autowired
    private JwtService jwtService;

    @Test
    void authenticatedUsersOnlyReceiveTheirOwnHistory() throws Exception {

        String suffix = UUID.randomUUID().toString();

        AppUser userA = userRepository.saveAndFlush(
                new AppUser(
                        "http.history.a+" + suffix + "@example.com",
                        "test-hash",
                        "User",
                        "A",
                        UserRole.ANALYST,
                        true
                )
        );

        AppUser userB = userRepository.saveAndFlush(
                new AppUser(
                        "http.history.b+" + suffix + "@example.com",
                        "test-hash",
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

        String tokenA = jwtService.issueToken(userA);
        String tokenB = jwtService.issueToken(userB);

        mockMvc.perform(
                get("/api/history/assessments")
                        .header("Authorization", "Bearer " + tokenA)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath(
                "$[*].assessmentType",
                containsInAnyOrder("RISK", "FRAUD")
        ))
        .andExpect(jsonPath(
                "$[*].primaryScore",
                containsInAnyOrder(0.11, 0.22)
        ));

        mockMvc.perform(
                get("/api/history/assessments")
                        .header("Authorization", "Bearer " + tokenB)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].assessmentType").value("RISK"))
        .andExpect(jsonPath("$[0].primaryScore").value(0.33));
    }

    @Test
    void requestWithoutJwtIsRejected() throws Exception {

        mockMvc.perform(
                get("/api/history/assessments")
        )
        .andExpect(status().isUnauthorized());
    }

    @Test
    void disabledUserWithSignedJwtIsRejected() throws Exception {

        AppUser disabledUser = userRepository.saveAndFlush(
                new AppUser(
                        "http.history.disabled+"
                                + UUID.randomUUID()
                                + "@example.com",
                        "test-hash",
                        "Disabled",
                        "User",
                        UserRole.ANALYST,
                        false
                )
        );

        String token = jwtService.issueToken(disabledUser);

        mockMvc.perform(
                get("/api/history/assessments")
                        .header("Authorization", "Bearer " + token)
        )
        .andExpect(status().isUnauthorized());
    }
}