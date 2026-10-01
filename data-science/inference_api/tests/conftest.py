import pytest

from fastapi.testclient import TestClient

from inference_api.app.main import app


@pytest.fixture(
    scope="session"
)
def client():

    with TestClient(app) as test_client:

        yield test_client