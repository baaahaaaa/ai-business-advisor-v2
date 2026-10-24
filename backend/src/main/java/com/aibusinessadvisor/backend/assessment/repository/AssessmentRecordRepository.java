package com.aibusinessadvisor.backend.assessment.repository;

import com.aibusinessadvisor.backend.assessment.model.AssessmentRecord;
import com.aibusinessadvisor.backend.assessment.model.AssessmentType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentRecordRepository
        extends JpaRepository<AssessmentRecord, UUID> {

    // Methodes existantes : conservees
    List<AssessmentRecord> findAllByCreatedByIdOrderByCreatedAtAsc(
            UUID createdById
    );

    List<AssessmentRecord> findAllByCreatedByIdOrderByCreatedAtDesc(
            UUID createdById
    );

    List<AssessmentRecord> findAllByOrderByCreatedAtDesc();

    // Nouveau : historique personnel pagine
    Page<AssessmentRecord> findByCreatedById(
            UUID createdById,
            Pageable pageable
    );

    // Nouveau : historique personnel pagine et filtre
    Page<AssessmentRecord> findByCreatedByIdAndAssessmentType(
            UUID createdById,
            AssessmentType assessmentType,
            Pageable pageable
    );
}