import json
import joblib

from dataclasses import dataclass
from typing import Any

from .config import (
    OCCURRENCE_MODEL_PATH,
    FREQUENCY_MODEL_PATH,
    FRAUD_MODEL_PATH,
    SERVING_CONTRACT_PATH,
    OCCURRENCE_METADATA_PATH,
    FREQUENCY_METADATA_PATH,
    FRAUD_METADATA_PATH
)


def load_json(path):

    with open(
        path,
        "r",
        encoding="utf-8"
    ) as f:

        return json.load(f)


@dataclass
class ModelRegistry:

    occurrence_model: Any

    frequency_model: Any

    fraud_model: Any

    serving_contract: dict

    occurrence_metadata: dict

    frequency_metadata: dict

    fraud_metadata: dict


    @classmethod
    def load(cls):

        required_paths = [
            OCCURRENCE_MODEL_PATH,
            FREQUENCY_MODEL_PATH,
            FRAUD_MODEL_PATH,
            SERVING_CONTRACT_PATH,
            OCCURRENCE_METADATA_PATH,
            FREQUENCY_METADATA_PATH,
            FRAUD_METADATA_PATH
        ]


        for path in required_paths:

            if not path.exists():

                raise FileNotFoundError(
                    f"Required model resource not found: {path}"
                )


        occurrence_model = joblib.load(
            OCCURRENCE_MODEL_PATH
        )

        frequency_model = joblib.load(
            FREQUENCY_MODEL_PATH
        )

        fraud_model = joblib.load(
            FRAUD_MODEL_PATH
        )


        registry = cls(

            occurrence_model=
                occurrence_model,

            frequency_model=
                frequency_model,

            fraud_model=
                fraud_model,

            serving_contract=
                load_json(
                    SERVING_CONTRACT_PATH
                ),

            occurrence_metadata=
                load_json(
                    OCCURRENCE_METADATA_PATH
                ),

            frequency_metadata=
                load_json(
                    FREQUENCY_METADATA_PATH
                ),

            fraud_metadata=
                load_json(
                    FRAUD_METADATA_PATH
                )
        )


        return registry