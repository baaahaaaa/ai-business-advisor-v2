package com.aibusinessadvisor.backend.ml.dto;


public record ClaimFrequencyMlResponse(

        double predicted_frequency,

        double exposure,

        double expected_claim_count

) {
}
