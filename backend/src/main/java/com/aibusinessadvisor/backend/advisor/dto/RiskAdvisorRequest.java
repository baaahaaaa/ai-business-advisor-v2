package com.aibusinessadvisor.backend.advisor.dto;


public record RiskAdvisorRequest(

        double claimProbability,

        double claimProbabilityThreshold,

        boolean technicalRiskFlag,

        double predictedFrequency,

        double exposure,

        double expectedClaimCount

) {
}
