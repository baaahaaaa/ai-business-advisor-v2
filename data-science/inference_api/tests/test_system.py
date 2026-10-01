def test_root(
    client
):

    response = client.get(
        "/"
    )

    assert response.status_code == 200

    data = response.json()


    assert (
        data["status"]
        ==
        "running"
    )

    assert (
        data["version"]
        ==
        "1.0.0"
    )


def test_health(
    client
):

    response = client.get(
        "/health"
    )

    assert response.status_code == 200

    data = response.json()

    assert (
        data["status"]
        ==
        "healthy"
    )

    assert (
        data["models"][
            "claim_occurrence"
        ]
        is True
    )

    assert (
        data["models"][
            "claim_frequency"
        ]
        is True
    )

    assert (
        data["models"][
            "fraud_detection"
        ]
        is True
    )


def test_runtime_versions(
    client
):

    response = client.get(
        "/runtime"
    )

    assert response.status_code == 200

    data = response.json()

    assert (
        data["scikit_learn"]
        ==
        "1.9.1"
    )

    assert (
        data["xgboost"]
        ==
        "3.4.1"
    )

    assert (
        data["pandas"]
        ==
        "3.0.6"
    )

    assert (
        data["numpy"]
        ==
        "2.5.3"
    )