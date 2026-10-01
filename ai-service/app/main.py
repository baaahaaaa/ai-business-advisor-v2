from fastapi import FastAPI, HTTPException

from app.schemas import (
    AdvisorResponse,
    FraudAdvisorRequest,
    RiskAdvisorRequest,
)
from app.services.advisor_service import (
    AdvisorService,
    AdvisorUnavailableError,
)


app = FastAPI(
    title="AI Business Advisor - AI Service",
    version="0.2.0",
)


advisor_service = AdvisorService()


@app.get("/")
def root() -> dict[str, str]:
    return {
        "service": "AI Business Advisor - AI Advisor",
        "version": "0.2.0",
    }


@app.get("/health")
def health() -> dict[str, object]:
    return {
        "status": "healthy",
        "advisor": True,
    }


@app.get("/runtime")
def runtime() -> dict[str, object]:
    return advisor_service.runtime_info()


@app.post(
    "/v1/advisor/risk",
    response_model=AdvisorResponse,
)
def explain_risk(
    request: RiskAdvisorRequest,
) -> AdvisorResponse:

    try:
        return advisor_service.explain_risk(
            request
        )

    except AdvisorUnavailableError as exc:
        raise HTTPException(
            status_code=503,
            detail=str(exc),
        ) from exc


@app.post(
    "/v1/advisor/fraud",
    response_model=AdvisorResponse,
)
def explain_fraud(
    request: FraudAdvisorRequest,
) -> AdvisorResponse:

    try:
        return advisor_service.explain_fraud(
            request
        )

    except AdvisorUnavailableError as exc:
        raise HTTPException(
            status_code=503,
            detail=str(exc),
        ) from exc
