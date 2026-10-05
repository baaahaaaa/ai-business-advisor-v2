package com.aibusinessadvisor.backend.risk.controller;

import com.aibusinessadvisor.backend.risk.dto.InsuranceRiskAssessmentResponse;
import com.aibusinessadvisor.backend.risk.dto.InsuranceRiskRequest;
import com.aibusinessadvisor.backend.risk.service.InsuranceRiskService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/risk")
public class InsuranceRiskController {

    private final InsuranceRiskService
            insuranceRiskService;


    public InsuranceRiskController(
            InsuranceRiskService insuranceRiskService
    ) {

        this.insuranceRiskService =
                insuranceRiskService;
    }


    @PostMapping("/assessment")
    public InsuranceRiskAssessmentResponse assess(

            @Valid
            @RequestBody
            InsuranceRiskRequest request,

            @AuthenticationPrincipal
            Jwt jwt

    ) {

        return insuranceRiskService
                .assess(
                        request,
                        jwt.getSubject()
                );
    }
}