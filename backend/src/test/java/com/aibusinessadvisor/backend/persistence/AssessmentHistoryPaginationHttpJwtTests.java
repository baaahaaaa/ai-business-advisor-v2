package com.aibusinessadvisor.backend.persistence;

import com.aibusinessadvisor.backend.assessment.repository.AssessmentRecordRepository;
import com.aibusinessadvisor.backend.assessment.service.AssessmentPersistenceService;
import com.aibusinessadvisor.backend.fraud.dto.FraudAssessmentResponse;
import com.aibusinessadvisor.backend.risk.dto.InsuranceRiskAssessmentResponse;
import com.aibusinessadvisor.backend.security.JwtService;
import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.model.UserRole;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AssessmentHistoryPaginationHttpJwtTests {

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
    void paginatesFiltersAndIsolatesAuthenticatedUsers() throws Exception {

        AppUser userA = createUser("pagination-a");
        AppUser userB = createUser("pagination-b");

        addRisk(userA, 0.11);
        addFraud(userA, 0.22);
        addRisk(userA, 0.33);
        addFraud(userA, 0.44);
        addRisk(userA, 0.55);

        addFraud(userB, 0.99);

        recordRepository.flush();

        String tokenA = jwtService.issueToken(userA);
        String tokenB = jwtService.issueToken(userB);

        // Page 0 : deux elements sur cinq
        MvcResult firstPage = mockMvc.perform(
                get("/api/history/assessments/paged")
                        .header("Authorization", "Bearer " + tokenA)
                        .param("page", "0")
                        .param("size", "2")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(2)))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(2))
        .andExpect(jsonPath("$.totalElements").value(5))
        .andExpect(jsonPath("$.totalPages").value(3))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.hasPrevious").value(false))
        .andReturn();

        // Page 1 : deux elements
        MvcResult secondPage = mockMvc.perform(
                get("/api/history/assessments/paged")
                        .header("Authorization", "Bearer " + tokenA)
                        .param("page", "1")
                        .param("size", "2")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(2)))
        .andExpect(jsonPath("$.page").value(1))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.hasPrevious").value(true))
        .andReturn();

        // Page 2 : dernier element
        MvcResult thirdPage = mockMvc.perform(
                get("/api/history/assessments/paged")
                        .header("Authorization", "Bearer " + tokenA)
                        .param("page", "2")
                        .param("size", "2")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.page").value(2))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.hasPrevious").value(true))
        .andReturn();

        // Aucun doublon entre les pages
        List<String> ids = new ArrayList<>();

        ids.addAll(JsonPath.read(
                firstPage.getResponse().getContentAsString(),
                "$.items[*].id"
        ));

        ids.addAll(JsonPath.read(
                secondPage.getResponse().getContentAsString(),
                "$.items[*].id"
        ));

        ids.addAll(JsonPath.read(
                thirdPage.getResponse().getContentAsString(),
                "$.items[*].id"
        ));

        String foreignId = recordRepository
                .findAllByCreatedByIdOrderByCreatedAtDesc(userB.getId())
                .get(0)
                .getId()
                .toString();

        assertThat(ids)
                .hasSize(5)
                .doesNotHaveDuplicates()
                .doesNotContain(foreignId);

        // Filtre RISK : 3 evaluations pour A
        mockMvc.perform(
                get("/api/history/assessments/paged")
                        .header("Authorization", "Bearer " + tokenA)
                        .param("type", "RISK")
                        .param("page", "0")
                        .param("size", "10")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(3))
        .andExpect(jsonPath("$.items", hasSize(3)))
        .andExpect(jsonPath(
                "$.items[*].assessmentType",
                containsInAnyOrder("RISK", "RISK", "RISK")
        ));

        // Filtre FRAUD : 2 evaluations pour A
        mockMvc.perform(
                get("/api/history/assessments/paged")
                        .header("Authorization", "Bearer " + tokenA)
                        .param("type", "FRAUD")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(2))
        .andExpect(jsonPath("$.items", hasSize(2)))
        .andExpect(jsonPath(
                "$.items[*].assessmentType",
                containsInAnyOrder("FRAUD", "FRAUD")
        ));

        // Utilisateur B : uniquement son evaluation
        mockMvc.perform(
                get("/api/history/assessments/paged")
                        .header("Authorization", "Bearer " + tokenB)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.items", hasSize(1)))
        .andExpect(jsonPath("$.items[0].assessmentType").value("FRAUD"))
        .andExpect(jsonPath("$.items[0].primaryScore").value(0.99));

        // Ancien endpoint toujours compatible
        mockMvc.perform(
                get("/api/history/assessments")
                        .header("Authorization", "Bearer " + tokenA)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    void rejectsInvalidParametersAndMissingJwt() throws Exception {

        AppUser user = createUser("pagination-validation");
        String token = jwtService.issueToken(user);

        // Page negative
        mockMvc.perform(
                get("/api/history/assessments/paged")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "-1")
        )
        .andExpect(status().isBadRequest());

        // Taille nulle
        mockMvc.perform(
                get("/api/history/assessments/paged")
                        .header("Authorization", "Bearer " + token)
                        .param("size", "0")
        )
        .andExpect(status().isBadRequest());

        // Taille superieure a 100
        mockMvc.perform(
                get("/api/history/assessments/paged")
                        .header("Authorization", "Bearer " + token)
                        .param("size", "101")
        )
        .andExpect(status().isBadRequest());

        // Type non reconnu
        mockMvc.perform(
                get("/api/history/assessments/paged")
                        .header("Authorization", "Bearer " + token)
                        .param("type", "UNKNOWN")
        )
        .andExpect(status().isBadRequest());

        // JWT obligatoire
        mockMvc.perform(
                get("/api/history/assessments/paged")
        )
        .andExpect(status().isUnauthorized());
    }

    private AppUser createUser(String prefix) {

        return userRepository.saveAndFlush(
                new AppUser(
                        prefix + "+" + UUID.randomUUID() + "@example.com",
                        "test-password-hash",
                        "Pagination",
                        "Test",
                        UserRole.ANALYST,
                        true
                )
        );
    }

    private void addRisk(AppUser user, double score) {

        persistenceService.recordRisk(
                user.getEmail(),
                new InsuranceRiskAssessmentResponse(
                        score,
                        0.10,
                        score >= 0.10,
                        0.50,
                        1.0,
                        0.50
                )
        );
    }

    private void addFraud(AppUser user, double score) {

        persistenceService.recordFraud(
                user.getEmail(),
                new FraudAssessmentResponse(
                        score,
                        0.10,
                        score >= 0.10
                )
        );
    }
}
