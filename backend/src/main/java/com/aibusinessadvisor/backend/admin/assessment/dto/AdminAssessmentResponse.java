package com.aibusinessadvisor.backend.admin.assessment.dto;

import com.aibusinessadvisor.backend.assessment.model.AssessmentType;
import com.aibusinessadvisor.backend.user.model.UserRole;

import java.time.Instant;
import java.util.UUID;


public record AdminAssessmentResponse(

        UUID id,
        AssessmentType assessmentType,
        Instant createdAt,

        double primaryScore,
        double technicalThreshold,
        boolean flagged,

        Double predictedFrequency,
        Double expectedClaimCount,
        Double exposure,

        UUID createdById,
        String createdByEmail,
        String createdByFirstName,
        String createdByLastName,
        UserRole createdByRole

) {
}