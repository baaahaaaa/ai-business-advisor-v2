package com.aibusinessadvisor.backend.assessment.model;

import com.aibusinessadvisor.backend.user.model.AppUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "assessment_records")
public class AssessmentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "assessment_type",
            nullable = false,
            length = 16
    )
    private AssessmentType assessmentType;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "created_by",
            nullable = false
    )
    private AppUser createdBy;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "primary_score",
            nullable = false
    )
    private double primaryScore;

    @Column(
            name = "technical_threshold",
            nullable = false
    )
    private double technicalThreshold;

    @Column(nullable = false)
    private boolean flagged;

    @Column(name = "predicted_frequency")
    private Double predictedFrequency;

    @Column(name = "expected_claim_count")
    private Double expectedClaimCount;

    @Column
    private Double exposure;


    protected AssessmentRecord() {
    }


    public AssessmentRecord(
            AssessmentType assessmentType,
            AppUser createdBy,
            double primaryScore,
            double technicalThreshold,
            boolean flagged,
            Double predictedFrequency,
            Double expectedClaimCount,
            Double exposure
    ) {

        this.assessmentType = assessmentType;
        this.createdBy = createdBy;
        this.primaryScore = primaryScore;
        this.technicalThreshold = technicalThreshold;
        this.flagged = flagged;
        this.predictedFrequency = predictedFrequency;
        this.expectedClaimCount = expectedClaimCount;
        this.exposure = exposure;
    }


    @PrePersist
    void prePersist() {

        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }


    public UUID getId() {
        return id;
    }


    public AssessmentType getAssessmentType() {
        return assessmentType;
    }


    public AppUser getCreatedBy() {
        return createdBy;
    }


    public Instant getCreatedAt() {
        return createdAt;
    }


    public double getPrimaryScore() {
        return primaryScore;
    }


    public double getTechnicalThreshold() {
        return technicalThreshold;
    }


    public boolean isFlagged() {
        return flagged;
    }


    public Double getPredictedFrequency() {
        return predictedFrequency;
    }


    public Double getExpectedClaimCount() {
        return expectedClaimCount;
    }


    public Double getExposure() {
        return exposure;
    }
}