def test_occurrence_rejects_zero_exposure(
    client
):

    payload = {

        "Exposure":
            0,

        "VehPower":
            7,

        "VehAge":
            6,

        "DrivAge":
            40,

        "BonusMalus":
            60,

        "Density":
            100,

        "Area":
            "C",

        "VehBrand":
            "B1",

        "VehGas":
            "Regular",

        "Region":
            "R24"
    }


    response = client.post(
        "/v1/predict/claim-occurrence",
        json=payload
    )


    assert (
        response.status_code
        ==
        422
    )

def test_occurrence_rejects_unknown_field(
    client
):

    payload = {

        "Exposure":
            1.0,

        "VehPower":
            7,

        "VehAge":
            6,

        "DrivAge":
            40,

        "BonusMalus":
            60,

        "Density":
            100,

        "Area":
            "C",

        "VehBrand":
            "B1",

        "VehGas":
            "Regular",

        "Region":
            "R24",

        "FakeFeature":
            "should not be accepted"
    }


    response = client.post(
        "/v1/predict/claim-occurrence",
        json=payload
    )


    assert (
        response.status_code
        ==
        422
    )

def test_fraud_rejects_invalid_week(
    client
):

    payload = {

        "Age":
            30,

        "Deductible":
            400,

        "WeekOfMonth":
            8,

        "WeekOfMonthClaimed":
            1,

        "DriverRating":
            3,

        "Month":
            "Jan",

        "DayOfWeek":
            "Monday",

        "Make":
            "Honda",

        "AccidentArea":
            "Urban",

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
            "20000 to 29000",

        "PastNumberOfClaims":
            "none",

        "AgeOfVehicle":
            "new",

        "AgeOfPolicyHolder":
            "26 to 30",

        "AgentType":
            "External",

        "NumberOfCars":
            "1 vehicle",

        "BasePolicy":
            "Collision"
    }


    response = client.post(
        "/v1/predict/fraud",
        json=payload
    )


    assert (
        response.status_code
        ==
        422
    )