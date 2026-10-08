package com.aibusinessadvisor.backend.assessment.service;

import com.aibusinessadvisor.backend.assessment.dto.AssessmentHistoryItemResponse;
import com.aibusinessadvisor.backend.assessment.dto.AssessmentHistoryPageResponse;

import com.aibusinessadvisor.backend.assessment.model.AssessmentRecord;
import com.aibusinessadvisor.backend.assessment.model.AssessmentType;
import com.aibusinessadvisor.backend.assessment.repository.AssessmentRecordRepository;

import com.aibusinessadvisor.backend.user.model.AppUser;
import com.aibusinessadvisor.backend.user.repository.AppUserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class AssessmentHistoryService {

    private final AssessmentRecordRepository assessmentRecordRepository;
    private final AppUserRepository appUserRepository;

    public AssessmentHistoryService(
            AssessmentRecordRepository assessmentRecordRepository,
            AppUserRepository appUserRepository
    ) {
        this.assessmentRecordRepository = assessmentRecordRepository;
        this.appUserRepository = appUserRepository;
    }

    // =====================================================
    // HISTORIQUE EXISTANT - CONSERVE
    // =====================================================

    @Transactional(readOnly = true)
    public List<AssessmentHistoryItemResponse> getOwnHistory(
            String userEmail
    ) {

        AppUser user = requireActiveUser(userEmail);

        return assessmentRecordRepository
                .findAllByCreatedByIdOrderByCreatedAtDesc(
                        user.getId()
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // =====================================================
    // NOUVEAU : HISTORIQUE PAGINE AVEC FILTRE OPTIONNEL
    // =====================================================

    @Transactional(readOnly = true)
    public AssessmentHistoryPageResponse getOwnHistoryPage(
            String userEmail,
            AssessmentType type,
            int page,
            int size
    ) {

        // Validation de la pagination
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be greater than or equal to zero."
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page size must be between 1 and 100."
            );
        }

        // Identifier l'utilisateur authentifie et actif
        AppUser user = requireActiveUser(userEmail);

        // Tri decroissant avec departage par ID
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc("createdAt"),
                        Sort.Order.desc("id")
                )
        );

        Page<AssessmentRecord> results;

        // Tous les types ou filtre RISK/FRAUD
        if (type == null) {

            results = assessmentRecordRepository
                    .findByCreatedById(
                            user.getId(),
                            pageable
                    );

        } else {

            results = assessmentRecordRepository
                    .findByCreatedByIdAndAssessmentType(
                            user.getId(),
                            type,
                            pageable
                    );
        }

        List<AssessmentHistoryItemResponse> items =
                results.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return new AssessmentHistoryPageResponse(
                items,
                results.getNumber(),
                results.getSize(),
                results.getTotalElements(),
                results.getTotalPages(),
                results.hasNext(),
                results.hasPrevious()
        );
    }

    // =====================================================
    // CONVERSION ENTITE -> DTO
    // =====================================================

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

    // =====================================================
    // VALIDATION DE L'UTILISATEUR
    // =====================================================

    private AppUser requireActiveUser(
            String email
    ) {

        String normalizedEmail = email
                .trim()
                .toLowerCase(Locale.ROOT);

        AppUser user = appUserRepository
                .findByEmail(normalizedEmail)
                .orElseThrow(
                        () -> new ResponseStatusException(
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
