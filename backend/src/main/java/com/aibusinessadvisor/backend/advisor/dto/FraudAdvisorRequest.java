package com.aibusinessadvisor.backend.advisor.dto;


public record FraudAdvisorRequest(

        double fraudProbability,

        double investigationThreshold,

        boolean investigationFlag

) {
}
