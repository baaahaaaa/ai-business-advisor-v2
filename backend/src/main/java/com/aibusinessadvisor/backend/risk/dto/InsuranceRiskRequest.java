package com.aibusinessadvisor.backend.risk.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;


public record InsuranceRiskRequest(

        @Positive
        @DecimalMax("1.0")
        double exposure,

        @Min(1)
        int vehiclePower,

        @Min(0)
        int vehicleAge,

        @Min(18)
        int driverAge,

        @PositiveOrZero
        double bonusMalus,

        @PositiveOrZero
        double density,

        @NotBlank
        String area,

        @NotBlank
        String vehicleBrand,

        @NotBlank
        String vehicleGas,

        @NotBlank
        String region

) {
}
