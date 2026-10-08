package com.aibusinessadvisor.backend.assessment.service;

import com.aibusinessadvisor.backend.assessment.dto.AssessmentHistoryItemResponse;
import com.aibusinessadvisor.backend.assessment.model.AssessmentRecord;
import com.aibusinessadvisor.backend.assessment.repository.AssessmentRecordRepository;

import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;


@Service
public class AssessmentHistoryService {

    private final AssessmentRecordRepository
            assessmentRecordRepository;

    private final AppUserRepository
            appUserRepository;


    public AssessmentHistoryService(
            AssessmentRecordRepository
                    assessmentRecordRepository,
            AppUserRepository appUserRepository
    ) {

        this.assessmentRecordRepository =
                assessmentRecordRepository;

        this.appUserRepository =
                appUserRepository;
    }


    @Transactional(readOnly = true)
    public List<AssessmentHistoryItemResponse>
            getOwnHistory(
                    String userEmail
            ) {

        AppUser user =
                requireActiveUser(
                        userEmail
                );


        return assessmentRecordRepository
                .findAllByCreatedByIdOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }


    private AssessmentHistoryItemResponse toResponse(
            AssessmentRecord record
    ) {

        return new AssessmentHistoryItemResponse(
                record.getId(),
                record.getAssessmentType(),
                record.getCreatedAt(),
                record.getPrimaryScore(),
                record.getTechnicalThreshold(),
                record.isFlagged(),
                record.getPredictedFrequency(),
                record.getExpectedClaimCount(),
                record.getExposure()
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