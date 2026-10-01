package com.aibusinessadvisor.backend.ml.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;


public record FraudMlRequest(

        @PositiveOrZero
        Double Age,

        @PositiveOrZero
        double Deductible,

        @Min(1)
        @Max(5)
        int WeekOfMonth,

        @Min(1)
        @Max(5)
        int WeekOfMonthClaimed,

        @Min(1)
        int DriverRating,

        @NotBlank
        String Month,

        @NotBlank
        String DayOfWeek,

        @NotBlank
        String Make,

        @NotBlank
        String AccidentArea,

        String DayOfWeekClaimed,

        String MonthClaimed,

        @NotBlank
        String Sex,

        @NotBlank
        String MaritalStatus,

        @NotBlank
        String VehicleCategory,

        @NotBlank
        String VehiclePrice,

        @NotBlank
        String PastNumberOfClaims,

        @NotBlank
        String AgeOfVehicle,

        @NotBlank
        String AgeOfPolicyHolder,

        @NotBlank
        String AgentType,

        @NotBlank
        String NumberOfCars,

        @NotBlank
        String BasePolicy

) {
}
