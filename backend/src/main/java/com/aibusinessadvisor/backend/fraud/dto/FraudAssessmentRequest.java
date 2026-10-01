package com.aibusinessadvisor.backend.fraud.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;


public record FraudAssessmentRequest(

        @PositiveOrZero
        Double age,

        @PositiveOrZero
        double deductible,

        @Min(1)
        @Max(5)
        int weekOfMonth,

        @Min(1)
        @Max(5)
        int weekOfMonthClaimed,

        @Min(1)
        int driverRating,

        @NotBlank
        String month,

        @NotBlank
        String dayOfWeek,

        @NotBlank
        String make,

        @NotBlank
        String accidentArea,

        String dayOfWeekClaimed,

        String monthClaimed,

        @NotBlank
        String sex,

        @NotBlank
        String maritalStatus,

        @NotBlank
        String vehicleCategory,

        @NotBlank
        String vehiclePrice,

        @NotBlank
        String pastNumberOfClaims,

        @NotBlank
        String ageOfVehicle,

        @NotBlank
        String ageOfPolicyHolder,

        @NotBlank
        String agentType,

        @NotBlank
        String numberOfCars,

        @NotBlank
        String basePolicy

) {
}
