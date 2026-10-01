package com.aibusinessadvisor.backend.fraud.dto;


public record FraudAssessmentResponse(

        double fraudProbability,

        double investigationThreshold,

        boolean investigationFlag

) {
}
