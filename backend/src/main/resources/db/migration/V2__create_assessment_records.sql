CREATE TABLE assessment_records (
    id UUID PRIMARY KEY,
    assessment_type VARCHAR(16) NOT NULL,
    created_by UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,

    primary_score DOUBLE PRECISION NOT NULL,
    technical_threshold DOUBLE PRECISION NOT NULL,
    flagged BOOLEAN NOT NULL,

    predicted_frequency DOUBLE PRECISION NULL,
    expected_claim_count DOUBLE PRECISION NULL,
    exposure DOUBLE PRECISION NULL,

    advisor_requested BOOLEAN NOT NULL DEFAULT FALSE,
    advisor_mode VARCHAR(64) NULL,

    CONSTRAINT fk_assessment_created_by
        FOREIGN KEY (created_by)
        REFERENCES app_users(id),

    CONSTRAINT ck_assessment_type
        CHECK (assessment_type IN ('RISK', 'FRAUD'))
);

CREATE INDEX idx_assessment_created_by
    ON assessment_records(created_by);

CREATE INDEX idx_assessment_created_at
    ON assessment_records(created_at);

CREATE INDEX idx_assessment_type
    ON assessment_records(assessment_type);

CREATE INDEX idx_assessment_flagged
    ON assessment_records(flagged);
