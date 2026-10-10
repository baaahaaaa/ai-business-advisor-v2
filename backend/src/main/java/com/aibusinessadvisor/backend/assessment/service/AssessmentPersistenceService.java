package com.aibusinessadvisor.backend.assessment.service;

import com.aibusinessadvisor.backend.assessment.model.AssessmentRecord;
import com.aibusinessadvisor.backend.assessment.model.AssessmentType;
import com.aibusinessadvisor.backend.assessment.repository.AssessmentRecordRepository;

import com.aibusinessadvisor.backend.fraud.dto.FraudAssessmentResponse;

import com.aibusinessadvisor.backend.risk.dto.InsuranceRiskAssessmentResponse;

import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;


@Service
public class AssessmentPersistenceService {

    private final AssessmentRecordRepository
            assessmentRecordRepository;

    private final AppUserRepository
            appUserRepository;


    public AssessmentPersistenceService(
            AssessmentRecordRepository
                    assessmentRecordRepository,
            AppUserRepository appUserRepository
    ) {

        this.assessmentRecordRepository =
                assessmentRecordRepository;

        this.appUserRepository =
                appUserRepository;
    }


    @Transactional
    public void recordRisk(
            String userEmail,
            InsuranceRiskAssessmentResponse response
    ) {

        AppUser user =
                requireActiveUser(
                        userEmail
                );


        AssessmentRecord record =
                new AssessmentRecord(
                        AssessmentType.RISK,
                        user,
                        response.claimProbability(),
                        response.claimProbabilityThreshold(),
                        response.technicalRiskFlag(),
                        response.predictedFrequency(),
                        response.expectedClaimCount(),
                        response.exposure()
                );


        assessmentRecordRepository.save(
                record
        );
    }


    @Transactional
    public void recordFraud(
            String userEmail,
            FraudAssessmentResponse response
    ) {

        AppUser user =
                requireActiveUser(
                        userEmail
                );


        AssessmentRecord record =
                new AssessmentRecord(
                        AssessmentType.FRAUD,
                        user,
                        response.fraudProbability(),
                        response.investigationThreshold(),
                        response.investigationFlag(),
                        null,
                        null,
                        null
                );


        assessmentRecordRepository.save(
                record
        );
    }


    private AppUser requireActiveUser(
            String email
    ) {

        String normalizedEmail =
                email
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );


        AppUser user =
                appUserRepository
                        .findByEmail(
                                normalizedEmail
                        )
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.UNAUTHORIZED,
                                                "Authenticated user not found."
                                        )
                        );


        if (!user.isEnabled()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User account is disabled."
            );
        }


        return user;
    }
}