from contextlib import asynccontextmanager

import sys
import sklearn
import xgboost
import pandas
import numpy
import joblib
import scipy

from fastapi import (
    FastAPI,
    Request
)

from .model_registry import ModelRegistry

from .prediction_service import (
    predict_claim_occurrence,
    predict_claim_frequency,
    predict_fraud
)

from .schemas import (
    InsuranceRiskRequest,
    ClaimOccurrenceResponse,
    ClaimFrequencyResponse,
    FraudRequest,
    FraudResponse
)


# ============================================================
# STARTUP / SHUTDOWN
# ============================================================

@asynccontextmanager
async def lifespan(
    app: FastAPI
):
    """
    Load the ML models once when FastAPI starts.

    The models are stored in app.state.registry and reused
    for every inference request.
    """

    print("=" * 70)
    print("AI BUSINESS ADVISOR — ML INFERENCE API")
    print("=" * 70)

    print("Loading ML models...")

    registry = ModelRegistry.load()

    app.state.registry = registry

    print("✅ All ML models loaded.")

    print(
        "Occurrence model :",
        registry.occurrence_model is not None
    )

    print(
        "Frequency model :",
        registry.frequency_model is not None
    )

    print(
        "Fraud model :",
        registry.fraud_model is not None
    )

    print("=" * 70)

    yield

    print("ML API shutdown.")


# ============================================================
# FASTAPI APPLICATION
# ============================================================

app = FastAPI(
    title="AI Business Advisor — ML Inference API",
    description=(
        "Inference service exposing the validated machine-learning "
        "models for Claim Occurrence, Claim Frequency and Fraud Detection."
    ),
    version="1.0.0",
    lifespan=lifespan
)


# ============================================================
# ROOT
# ============================================================

@app.get(
    "/",
    tags=["System"]
)
def root():
    """
    Basic service information.
    """

    return {
        "service":
            "AI Business Advisor ML API",

        "version":
            "1.0.0",

        "status":
            "running"
    }
@app.get(
    "/runtime",
    tags=["System"]
)
def runtime():

    return {
        "python":
            sys.version,

        "scikit_learn":
            sklearn.__version__,

        "xgboost":
            xgboost.__version__,

        "pandas":
            pandas.__version__,

        "numpy":
            numpy.__version__,

        "joblib":
            joblib.__version__,

        "scipy":
            scipy.__version__
    }

# ============================================================
# RUNTIME INFORMATION
# ============================================================

@app.get(
    "/runtime",
    tags=["System"]
)
def runtime():
    """
    Expose the main runtime/library versions used by
    the inference service.

    Useful for reproducibility and debugging.
    """

    return {
        "python":
            sys.version,

        "scikit_learn":
            sklearn.__version__,

        "xgboost":
            xgboost.__version__,

        "pandas":
            pandas.__version__,

        "numpy":
            numpy.__version__
    }


# ============================================================
# HEALTH
# ============================================================

@app.get(
    "/health",
    tags=["System"]
)
def health(
    request: Request
):
    """
    Verify that the three ML models are loaded.
    """

    registry = (
        request.app.state.registry
    )

    occurrence_loaded = (
        registry.occurrence_model
        is not None
    )

    frequency_loaded = (
        registry.frequency_model
        is not None
    )

    fraud_loaded = (
        registry.fraud_model
        is not None
    )

    all_models_loaded = (
        occurrence_loaded
        and frequency_loaded
        and fraud_loaded
    )

    return {
        "status":
            (
                "healthy"
                if all_models_loaded
                else "degraded"
            ),

        "models": {
            "claim_occurrence":
                occurrence_loaded,

            "claim_frequency":
                frequency_loaded,

            "fraud_detection":
                fraud_loaded
        }
    }


# ============================================================
# MODEL INFORMATION
# ============================================================

@app.get(
    "/models",
    tags=["System"]
)
def models(
    request: Request
):
    """
    Return basic information about the deployed models
    from the serving contract.
    """

    registry = (
        request.app.state.registry
    )

    contract = (
        registry.serving_contract
    )

    return {

        "claim_occurrence": {

            "artifact":
                contract[
                    "claim_occurrence"
                ][
                    "artifact"
                ],

            "meaning":
                contract[
                    "claim_occurrence"
                ][
                    "meaning"
                ],

            "threshold":
                contract[
                    "claim_occurrence"
                ].get(
                    "threshold"
                )
        },


        "claim_frequency": {

            "artifact":
                contract[
                    "claim_frequency"
                ][
                    "artifact"
                ],

            "formula":
                contract[
                    "claim_frequency"
                ][
                    "formula"
                ],

            "additional_input":
                contract[
                    "claim_frequency"
                ].get(
                    "additional_input",
                    []
                )
        },


        "fraud_detection": {

            "artifact":
                contract[
                    "fraud_detection"
                ][
                    "artifact"
                ],

            "meaning":
                contract[
                    "fraud_detection"
                ][
                    "meaning"
                ],

            "threshold":
                contract[
                    "fraud_detection"
                ][
                    "threshold"
                ]
        }
    }


# ============================================================
# CLAIM OCCURRENCE
# ============================================================

@app.post(
    "/v1/predict/claim-occurrence",
    response_model=ClaimOccurrenceResponse,
    tags=["Predictions"]
)
def claim_occurrence(
    payload: InsuranceRiskRequest,
    request: Request
):
    """
    Predict the probability that at least one claim occurs
    during the observed exposure period.

    The returned flag uses the previously selected
    technical threshold.
    """

    registry = (
        request.app.state.registry
    )

    result = predict_claim_occurrence(
        registry=registry,
        request=payload
    )

    return result


# ============================================================
# CLAIM FREQUENCY
# ============================================================

@app.post(
    "/v1/predict/claim-frequency",
    response_model=ClaimFrequencyResponse,
    tags=["Predictions"]
)
def claim_frequency(
    payload: InsuranceRiskRequest,
    request: Request
):
    """
    Predict expected claim frequency.

    The expected claim count is calculated as:

        predicted_frequency × Exposure
    """

    registry = (
        request.app.state.registry
    )

    result = predict_claim_frequency(
        registry=registry,
        request=payload
    )

    return result


# ============================================================
# FRAUD DETECTION
# ============================================================

@app.post(
    "/v1/predict/fraud",
    response_model=FraudResponse,
    tags=["Predictions"]
)
def fraud_detection(
    payload: FraudRequest,
    request: Request
):
    """
    Predict the calibrated fraud probability.

    investigation_flag indicates whether the score
    exceeds the technical prioritization threshold.

    It must not be interpreted as an automatic
    determination that fraud occurred.
    """

    registry = (
        request.app.state.registry
    )

    result = predict_fraud(
        registry=registry,
        request=payload
    )

    return result