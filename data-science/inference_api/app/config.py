from pathlib import Path


# config.py se trouve dans :
# data-science/inference_api/app/config.py

DATA_SCIENCE_ROOT = (
    Path(__file__)
    .resolve()
    .parents[2]
)


ARTIFACTS_DIR = (
    DATA_SCIENCE_ROOT
    / "artifacts"
)


METADATA_DIR = (
    DATA_SCIENCE_ROOT
    / "data"
    / "metadata"
)


OCCURRENCE_MODEL_PATH = (
    ARTIFACTS_DIR
    / "claim_occurrence_xgb_isotonic.joblib"
)


FREQUENCY_MODEL_PATH = (
    ARTIFACTS_DIR
    / "claim_frequency_xgboost_poisson.joblib"
)


FRAUD_MODEL_PATH = (
    ARTIFACTS_DIR
    / "fraud_random_forest_isotonic.joblib"
)


SERVING_CONTRACT_PATH = (
    METADATA_DIR
    / "model_serving_contract.json"
)


OCCURRENCE_METADATA_PATH = (
    METADATA_DIR
    / "claim_occurrence_model_v1.json"
)


FREQUENCY_METADATA_PATH = (
    METADATA_DIR
    / "claim_frequency_model_v1.json"
)


FRAUD_METADATA_PATH = (
    METADATA_DIR
    / "fraud_model_v1.json"
)