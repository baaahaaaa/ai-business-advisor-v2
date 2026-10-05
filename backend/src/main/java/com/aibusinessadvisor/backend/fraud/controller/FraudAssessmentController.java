package com.aibusinessadvisor.backend.fraud.controller;

import com.aibusinessadvisor.backend.fraud.dto.FraudAssessmentRequest;
import com.aibusinessadvisor.backend.fraud.dto.FraudAssessmentResponse;
import com.aibusinessadvisor.backend.fraud.service.FraudAssessmentService;

import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/fraud")
public class FraudAssessmentController {

    private final FraudAssessmentService
            fraudAssessmentService;


    public FraudAssessmentController(
            FraudAssessmentService fraudAssessmentService
    ) {

        this.fraudAssessmentService =
                fraudAssessmentService;
    }


    @PostMapping("/assessment")
    public FraudAssessmentResponse assess(

            @Valid
            @RequestBody
            FraudAssessmentRequest request,

            @AuthenticationPrincipal
            Jwt jwt

    ) {

        return fraudAssessmentService
                .assess(
                        request,
                        jwt.getSubject()
                );
    }
}