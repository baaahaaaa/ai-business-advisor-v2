package com.aibusinessadvisor.backend.assessment.repository;

import com.aibusinessadvisor.backend.assessment.model.AssessmentRecord;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface AssessmentRecordRepository
        extends JpaRepository<AssessmentRecord, UUID> {

    List<AssessmentRecord>
            findAllByCreatedByIdOrderByCreatedAtAsc(
                    UUID createdById
            );

    List<AssessmentRecord>
            findAllByCreatedByIdOrderByCreatedAtDesc(
                    UUID createdById
            );
}