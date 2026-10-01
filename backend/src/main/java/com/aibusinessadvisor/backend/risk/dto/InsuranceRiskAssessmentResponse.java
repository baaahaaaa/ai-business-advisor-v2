package com.aibusinessadvisor.backend.risk.dto;


public record InsuranceRiskAssessmentResponse(

        double claimProbability,

        double claimProbabilityThreshold,

        boolean technicalRiskFlag,

        double predictedFrequency,

        double exposure,

        double expectedClaimCount

) {
}
