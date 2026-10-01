from fastapi import FastAPI

from app.advisor import (
    explain_fraud,
    explain_risk,
)

from app.schemas import (
    AdvisorResponse,
    FraudAdvisorRequest,
    RiskAdvisorRequest,
)


app = FastAPI(
    title="AI Business Advisor Service",
    version="1.0.0",
)


@app.get("/")
def root():
    return {
        "service": "ai-business-advisor",
        "status": "running",
    }


@app.get("/health")
def health():
    return {
        "status": "healthy",
        "advisor": True,
    }


@app.post(
    "/v1/advisor/risk",
    response_model=AdvisorResponse,
)
def risk_advisor(
    request: RiskAdvisorRequest,
):
    return explain_risk(request)


@app.post(
    "/v1/advisor/fraud",
    response_model=AdvisorResponse,
)
def fraud_advisor(
    request: FraudAdvisorRequest,
):
    return explain_fraud(request)
