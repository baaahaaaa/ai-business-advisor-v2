import pytest


OCCURRENCE_PAYLOAD = {

    "Exposure":
        1.0,

    "VehPower":
        7,

    "VehAge":
        6,

    "DrivAge":
        79,

    "BonusMalus":
        62,

    "Density":
        399,

    "Area":
        "C",

    "VehBrand":
        "B1",

    "VehGas":
        "Regular",

    "Region":
        "R24"
}


EXPECTED_PROBABILITY = (
    0.4265319009621938
)


EXPECTED_THRESHOLD = (
    0.10327080885569255
)


def test_claim_occurrence_prediction(
    client
):

    response = client.post(
        "/v1/predict/claim-occurrence",
        json=OCCURRENCE_PAYLOAD
    )

    assert response.status_code == 200

    data = response.json()


    assert (
        data[
            "claim_probability"
        ]
        ==
        pytest.approx(
            EXPECTED_PROBABILITY,
            rel=1e-10,
            abs=1e-12
        )
    )


    assert (
        data[
            "technical_threshold"
        ]
        ==
        pytest.approx(
            EXPECTED_THRESHOLD
        )
    )


    assert (
        data[
            "technical_risk_flag"
        ]
        is True
    )