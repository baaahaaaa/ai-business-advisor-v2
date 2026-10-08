package com.aibusinessadvisor.backend.admin.assessment.service;

import com.aibusinessadvisor.backend.admin.assessment.dto.AdminAssessmentResponse;

import com.aibusinessadvisor.backend.assessment.model.AssessmentRecord;
import com.aibusinessadvisor.backend.assessment.repository.AssessmentRecordRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class AdminAssessmentService {

    private final AssessmentRecordRepository
            assessmentRecordRepository;


    public AdminAssessmentService(
            AssessmentRecordRepository
                    assessmentRecordRepository
    ) {

        this.assessmentRecordRepository =
                assessmentRecordRepository;
    }


    @Transactional(readOnly = true)
    public List<AdminAssessmentResponse>
            getAllAssessments() {

        return assessmentRecordRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(
                        this::toResponse
                )
                .toList();
    }


    private AdminAssessmentResponse toResponse(
            AssessmentRecord record
    ) {

        var user =
                record.getCreatedBy();


        return new AdminAssessmentResponse(
                record.getId(),
                record.getAssessmentType(),
                record.getCreatedAt(),

                record.getPrimaryScore(),
                record.getTechnicalThreshold(),
                record.isFlagged(),

                record.getPredictedFrequency(),
                record.getExpectedClaimCount(),
                record.getExposure(),

                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }
}