import pytest


FRAUD_PAYLOAD = {

    "Age":
        None,

    "Deductible":
        400,

    "WeekOfMonth":
        1,

    "WeekOfMonthClaimed":
        1,

    "DriverRating":
        3,

    "Month":
        "Jan",

    "DayOfWeek":
        "Saturday",

    "Make":
        "Honda",

    "AccidentArea":
        "Rural",

    "DayOfWeekClaimed":
        "Tuesday",

    "MonthClaimed":
        "Jan",

    "Sex":
        "Male",

    "MaritalStatus":
        "Single",

    "VehicleCategory":
        "Sedan",

    "VehiclePrice":
        "more than 69000",

    "PastNumberOfClaims":
        "none",

    "AgeOfVehicle":
        "new",

    "AgeOfPolicyHolder":
        "16 to 17",

    "AgentType":
        "External",

    "NumberOfCars":
        "1 vehicle",

    "BasePolicy":
        "All Perils"
}


EXPECTED_FRAUD_PROBABILITY = (
    0.5377003003817469
)


EXPECTED_FRAUD_THRESHOLD = (
    0.08585764735167348
)


def test_fraud_prediction(
    client
):

    response = client.post(
        "/v1/predict/fraud",
        json=FRAUD_PAYLOAD
    )

    assert response.status_code == 200

    data = response.json()


    assert (
        data[
            "fraud_probability"
        ]
        ==
        pytest.approx(
            EXPECTED_FRAUD_PROBABILITY,
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
            EXPECTED_FRAUD_THRESHOLD
        )
    )


    assert (
        data[
            "investigation_flag"
        ]
        is True
    )