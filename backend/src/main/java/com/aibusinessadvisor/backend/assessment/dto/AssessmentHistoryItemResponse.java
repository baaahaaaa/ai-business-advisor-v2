package com.aibusinessadvisor.backend.assessment.dto;

import com.aibusinessadvisor.backend.assessment.model.AssessmentType;

import java.time.Instant;
import java.util.UUID;


public record AssessmentHistoryItemResponse(

        UUID id,
        AssessmentType assessmentType,
        Instant createdAt,

        double primaryScore,
        double technicalThreshold,
        boolean flagged,

        Double predictedFrequency,
        Double expectedClaimCount,
        Double exposure

) {
}