import numpy as np
import pandas as pd

from .schemas import (
    InsuranceRiskRequest,
    FraudRequest
)

from .model_registry import (
    ModelRegistry
)


# ============================================================
# COMMON RISK TRANSFORMATION
# ============================================================

def build_insurance_features(
    request: InsuranceRiskRequest
):

    values = (
        request.model_dump()
    )

    density = (
        values.pop(
            "Density"
        )
    )

    values[
        "log_density"
    ] = float(
        np.log1p(
            density
        )
    )

    return values


# ============================================================
# CLAIM OCCURRENCE
# ============================================================

def predict_claim_occurrence(
    registry: ModelRegistry,
    request: InsuranceRiskRequest
):

    values = build_insurance_features(
        request
    )


    required_features = (
        registry
        .serving_contract[
            "claim_occurrence"
        ][
            "required_features"
        ]
    )


    X = pd.DataFrame([
        values
    ])


    X = X[
        required_features
    ]


    probability = float(
        registry
        .occurrence_model
        .predict_proba(
            X
        )[0, 1]
    )


    threshold = float(
        registry
        .serving_contract[
            "claim_occurrence"
        ][
            "threshold"
        ]
    )


    return {
        "claim_probability":
            probability,

        "technical_threshold":
            threshold,

        "technical_risk_flag":
            bool(
                probability
                >=
                threshold
            )
    }


# ============================================================
# CLAIM FREQUENCY
# ============================================================

def predict_claim_frequency(
    registry: ModelRegistry,
    request: InsuranceRiskRequest
):

    values = build_insurance_features(
        request
    )


    exposure = float(
        values.pop(
            "Exposure"
        )
    )


    required_features = (
        registry
        .serving_contract[
            "claim_frequency"
        ][
            "required_model_features"
        ]
    )


    X = pd.DataFrame([
        values
    ])


    X = X[
        required_features
    ]


    predicted_frequency = float(
        registry
        .frequency_model
        .predict(
            X
        )[0]
    )


    predicted_frequency = max(
        predicted_frequency,
        0.0
    )


    expected_claim_count = (
        predicted_frequency
        *
        exposure
    )


    return {
        "predicted_frequency":
            predicted_frequency,

        "exposure":
            exposure,

        "expected_claim_count":
            float(
                expected_claim_count
            )
    }


# ============================================================
# FRAUD
# ============================================================

def predict_fraud(
    registry: ModelRegistry,
    request: FraudRequest
):

    values = (
        request.model_dump()
    )


    # Age=None correspond à la valeur manquante
    # utilisée par notre pipeline Fraud.

    if values[
        "Age"
    ] is None:

        values[
            "Age"
        ] = np.nan


    required_features = (
        registry
        .serving_contract[
            "fraud_detection"
        ][
            "required_features"
        ]
    )


    X = pd.DataFrame([
        values
    ])


    X = X[
        required_features
    ]


    probability = float(
        registry
        .fraud_model
        .predict_proba(
            X
        )[0, 1]
    )


    threshold = float(
        registry
        .serving_contract[
            "fraud_detection"
        ][
            "threshold"
        ]
    )


    return {
        "fraud_probability":
            probability,

        "technical_threshold":
            threshold,

        "investigation_flag":
            bool(
                probability
                >=
                threshold
            )
    }