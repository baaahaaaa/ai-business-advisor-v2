import pytest


FREQUENCY_PAYLOAD = {
    "Exposure": 0.48,
    "VehPower": 9,
    "VehAge": 0,
    "DrivAge": 32,
    "BonusMalus": 61,
    "Density": 352,
    "Area": "C",
    "VehBrand": "B12",
    "VehGas": "Regular",
    "Region": "R41"
}


EXPECTED_FREQUENCY = (
    1.8306647539138794
)


EXPECTED_COUNT = (
    0.8787190818786621
)


# ============================================================
# GOLDEN PREDICTION TEST
# ============================================================

def test_claim_frequency_prediction(
    client
):

    response = client.post(
        "/v1/predict/claim-frequency",
        json=FREQUENCY_PAYLOAD
    )

    assert response.status_code == 200

    data = response.json()


    assert (
        data[
            "predicted_frequency"
        ]
        ==
        pytest.approx(
            EXPECTED_FREQUENCY,
            rel=1e-10,
            abs=1e-12
        )
    )


    assert (
        data[
            "exposure"
        ]
        ==
        pytest.approx(
            0.48
        )
    )


    assert (
        data[
            "expected_claim_count"
        ]
        ==
        pytest.approx(
            EXPECTED_COUNT,
            rel=1e-10,
            abs=1e-12
        )
    )


# ============================================================
# BUSINESS FORMULA TEST
# ============================================================

def test_expected_count_formula(
    client
):

    response = client.post(
        "/v1/predict/claim-frequency",
        json=FREQUENCY_PAYLOAD
    )

    assert response.status_code == 200

    data = response.json()


    calculated_expected_count = (
        data[
            "predicted_frequency"
        ]
        *
        data[
            "exposure"
        ]
    )


    assert (
        data[
            "expected_claim_count"
        ]
        ==
        pytest.approx(
            calculated_expected_count,
            rel=1e-12,
            abs=1e-12
        )
    )


# ============================================================
# BASIC OUTPUT SANITY TEST
# ============================================================

def test_frequency_output_is_valid(
    client
):

    response = client.post(
        "/v1/predict/claim-frequency",
        json=FREQUENCY_PAYLOAD
    )

    assert response.status_code == 200

    data = response.json()


    assert (
        data[
            "predicted_frequency"
        ]
        >= 0
    )


    assert (
        data[
            "expected_claim_count"
        ]
        >= 0
    )


    assert (
        data[
            "exposure"
        ]
        > 0
    )