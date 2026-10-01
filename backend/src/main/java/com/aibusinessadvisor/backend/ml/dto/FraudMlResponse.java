package com.aibusinessadvisor.backend.ml.dto;


public record FraudMlResponse(

        double fraud_probability,

        double technical_threshold,

        boolean investigation_flag

) {
}
