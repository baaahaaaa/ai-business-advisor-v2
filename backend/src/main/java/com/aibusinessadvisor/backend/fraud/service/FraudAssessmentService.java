package com.aibusinessadvisor.backend.fraud.service;

import com.aibusinessadvisor.backend.fraud.dto.FraudAssessmentRequest;
import com.aibusinessadvisor.backend.fraud.dto.FraudAssessmentResponse;

import com.aibusinessadvisor.backend.ml.client.MlInferenceClient;
import com.aibusinessadvisor.backend.ml.dto.FraudMlRequest;
import com.aibusinessadvisor.backend.ml.dto.FraudMlResponse;

import org.springframework.stereotype.Service;


@Service
public class FraudAssessmentService {

    private final MlInferenceClient mlInferenceClient;


    public FraudAssessmentService(
            MlInferenceClient mlInferenceClient
    ) {

        this.mlInferenceClient =
                mlInferenceClient;
    }


    public FraudAssessmentResponse assess(
            FraudAssessmentRequest request
    ) {

        FraudMlRequest mlRequest =
                new FraudMlRequest(

                        request.age(),

                        request.deductible(),

                        request.weekOfMonth(),

                        request.weekOfMonthClaimed(),

                        request.driverRating(),

                        request.month(),

                        request.dayOfWeek(),

                        request.make(),

                        request.accidentArea(),

                        request.dayOfWeekClaimed(),

                        request.monthClaimed(),

                        request.sex(),

                        request.maritalStatus(),

                        request.vehicleCategory(),

                        request.vehiclePrice(),

                        request.pastNumberOfClaims(),

                        request.ageOfVehicle(),

                        request.ageOfPolicyHolder(),

                        request.agentType(),

                        request.numberOfCars(),

                        request.basePolicy()
                );


        FraudMlResponse prediction =
                mlInferenceClient
                        .predictFraud(
                                mlRequest
                        );


        return new FraudAssessmentResponse(

                prediction.fraud_probability(),

                prediction.technical_threshold(),

                prediction.investigation_flag()
        );
    }
}
