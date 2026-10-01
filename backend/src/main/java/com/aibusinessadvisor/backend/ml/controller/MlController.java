package com.aibusinessadvisor.backend.ml.controller;

import com.aibusinessadvisor.backend.ml.client.MlInferenceClient;
import com.aibusinessadvisor.backend.ml.dto.ClaimFrequencyMlResponse;
import com.aibusinessadvisor.backend.ml.dto.ClaimOccurrenceMlResponse;
import com.aibusinessadvisor.backend.ml.dto.FraudMlRequest;
import com.aibusinessadvisor.backend.ml.dto.FraudMlResponse;
import com.aibusinessadvisor.backend.ml.dto.InsuranceRiskMlRequest;
import com.aibusinessadvisor.backend.ml.dto.MlHealthResponse;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/ml")
public class MlController {

    private final MlInferenceClient mlInferenceClient;


    public MlController(
            MlInferenceClient mlInferenceClient
    ) {

        this.mlInferenceClient =
                mlInferenceClient;
    }


    @GetMapping("/health")
    public MlHealthResponse health() {

        return mlInferenceClient.health();
    }


    @PostMapping("/claim-occurrence")
    public ClaimOccurrenceMlResponse claimOccurrence(

            @Valid
            @RequestBody
            InsuranceRiskMlRequest request

    ) {

        return mlInferenceClient
                .predictClaimOccurrence(
                        request
                );
    }


    @PostMapping("/claim-frequency")
    public ClaimFrequencyMlResponse claimFrequency(

            @Valid
            @RequestBody
            InsuranceRiskMlRequest request

    ) {

        return mlInferenceClient
                .predictClaimFrequency(
                        request
                );
    }


    @PostMapping("/fraud")
    public FraudMlResponse fraud(

            @Valid
            @RequestBody
            FraudMlRequest request

    ) {

        return mlInferenceClient
                .predictFraud(
                        request
                );
    }
}
