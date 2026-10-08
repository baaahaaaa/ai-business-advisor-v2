package com.aibusinessadvisor.backend.assessment.controller;

import com.aibusinessadvisor.backend.assessment.dto.AssessmentHistoryItemResponse;
import com.aibusinessadvisor.backend.assessment.dto.AssessmentHistoryPageResponse;
import com.aibusinessadvisor.backend.assessment.model.AssessmentType;
import com.aibusinessadvisor.backend.assessment.service.AssessmentHistoryService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class AssessmentHistoryController {

    private final AssessmentHistoryService assessmentHistoryService;

    public AssessmentHistoryController(
            AssessmentHistoryService assessmentHistoryService
    ) {
        this.assessmentHistoryService = assessmentHistoryService;
    }

    // =====================================================
    // HISTORIQUE EXISTANT - CONSERVE
    // =====================================================

    @GetMapping("/assessments")
    public List<AssessmentHistoryItemResponse> assessments(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return assessmentHistoryService.getOwnHistory(
                jwt.getSubject()
        );
    }

    // =====================================================
    // NOUVEAU : HISTORIQUE PAGINE ET FILTRE
    // =====================================================

    @GetMapping("/assessments/paged")
    public AssessmentHistoryPageResponse pagedAssessments(

            @AuthenticationPrincipal Jwt jwt,

            @RequestParam(required = false)
            AssessmentType type,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {

        return assessmentHistoryService.getOwnHistoryPage(
                jwt.getSubject(),
                type,
                page,
                size
        );
    }
}
