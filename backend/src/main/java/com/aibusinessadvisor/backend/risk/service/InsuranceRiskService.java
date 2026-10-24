package com.aibusinessadvisor.backend.risk.service;

import com.aibusinessadvisor.backend.assessment.service.AssessmentPersistenceService;

import com.aibusinessadvisor.backend.ml.client.MlInferenceClient;
import com.aibusinessadvisor.backend.ml.dto.ClaimFrequencyMlResponse;
import com.aibusinessadvisor.backend.ml.dto.ClaimOccurrenceMlResponse;
import com.aibusinessadvisor.backend.ml.dto.InsuranceRiskMlRequest;

import com.aibusinessadvisor.backend.risk.dto.InsuranceRiskAssessmentResponse;
import com.aibusinessadvisor.backend.risk.dto.InsuranceRiskRequest;

import org.springframework.stereotype.Service;


@Service
public class InsuranceRiskService {

    private final MlInferenceClient mlInferenceClient;

    private final AssessmentPersistenceService
            assessmentPersistenceService;


    public InsuranceRiskService(
            MlInferenceClient mlInferenceClient,
            AssessmentPersistenceService
                    assessmentPersistenceService
    ) {

        this.mlInferenceClient =
                mlInferenceClient;

        this.assessmentPersistenceService =
                assessmentPersistenceService;
    }


    public InsuranceRiskAssessmentResponse assess(
            InsuranceRiskRequest request,
            String userEmail
    ) {

        InsuranceRiskMlRequest mlRequest =
                new InsuranceRiskMlRequest(
                        request.exposure(),
                        request.vehiclePower(),
                        request.vehicleAge(),
                        request.driverAge(),
                        request.bonusMalus(),
                        request.density(),
                        request.area(),
                        request.vehicleBrand(),
                        request.vehicleGas(),
                        request.region()
                );


        ClaimOccurrenceMlResponse occurrence =
                mlInferenceClient
                        .predictClaimOccurrence(
                                mlRequest
                        );


        ClaimFrequencyMlResponse frequency =
                mlInferenceClient
                        .predictClaimFrequency(
                                mlRequest
                        );


        InsuranceRiskAssessmentResponse response =
                new InsuranceRiskAssessmentResponse(
                        occurrence.claim_probability(),
                        occurrence.technical_threshold(),
                        occurrence.technical_risk_flag(),
                        frequency.predicted_frequency(),
                        frequency.exposure(),
                        frequency.expected_claim_count()
                );


        assessmentPersistenceService
                .recordRisk(
                        userEmail,
                        response
                );


        return response;
    }
}