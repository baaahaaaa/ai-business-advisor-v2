package com.aibusinessadvisor.backend.ml.dto;


public record ClaimOccurrenceMlResponse(

        double claim_probability,

        double technical_threshold,

        boolean technical_risk_flag

) {
}
