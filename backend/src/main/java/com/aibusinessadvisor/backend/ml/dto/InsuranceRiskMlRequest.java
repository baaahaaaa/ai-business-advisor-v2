package com.aibusinessadvisor.backend.ml.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;


public record InsuranceRiskMlRequest(

        @Positive
        @DecimalMax("1.0")
        double Exposure,

        @Min(1)
        int VehPower,

        @Min(0)
        int VehAge,

        @Min(18)
        int DrivAge,

        @PositiveOrZero
        double BonusMalus,

        @PositiveOrZero
        double Density,

        @NotBlank
        String Area,

        @NotBlank
        String VehBrand,

        @NotBlank
        String VehGas,

        @NotBlank
        String Region

) {
}
