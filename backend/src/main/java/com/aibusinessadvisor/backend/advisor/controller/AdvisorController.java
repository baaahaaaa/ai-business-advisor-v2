package com.aibusinessadvisor.backend.advisor.controller;

import com.aibusinessadvisor.backend.advisor.client.AiAdvisorClient;
import com.aibusinessadvisor.backend.advisor.dto.AdvisorResponse;
import com.aibusinessadvisor.backend.advisor.dto.FraudAdvisorRequest;
import com.aibusinessadvisor.backend.advisor.dto.RiskAdvisorRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/advisor")
public class AdvisorController {

    private final AiAdvisorClient aiAdvisorClient;


    public AdvisorController(
            AiAdvisorClient aiAdvisorClient
    ) {

        this.aiAdvisorClient =
                aiAdvisorClient;
    }


    @PostMapping("/risk")
    public AdvisorResponse explainRisk(
            @RequestBody
            RiskAdvisorRequest request
    ) {

        return aiAdvisorClient
                .explainRisk(request);
    }


    @PostMapping("/fraud")
    public AdvisorResponse explainFraud(
            @RequestBody
            FraudAdvisorRequest request
    ) {

        return aiAdvisorClient
                .explainFraud(request);
    }
}
