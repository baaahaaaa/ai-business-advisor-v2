from pydantic import BaseModel, Field


class RiskAdvisorRequest(BaseModel):
    claimProbability: float = Field(ge=0.0, le=1.0)
    claimProbabilityThreshold: float = Field(ge=0.0, le=1.0)
    technicalRiskFlag: bool

    predictedFrequency: float = Field(ge=0.0)
    exposure: float = Field(gt=0.0)
    expectedClaimCount: float = Field(ge=0.0)


class FraudAdvisorRequest(BaseModel):
    fraudProbability: float = Field(ge=0.0, le=1.0)
    investigationThreshold: float = Field(ge=0.0, le=1.0)
    investigationFlag: bool


class AdvisorResponse(BaseModel):
    title: str
    summary: str
    keyPoints: list[str]
    disclaimer: str
