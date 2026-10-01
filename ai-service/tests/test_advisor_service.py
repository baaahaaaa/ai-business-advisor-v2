from dataclasses import replace

import pytest

import app.services.advisor_service as advisor_module
from app.llm_schemas import AdvisorNarrative
from app.providers.base import (
    LlmProvider,
    LlmProviderError,
)
from app.schemas import (
    FraudAdvisorRequest,
    RiskAdvisorRequest,
)
from app.services.advisor_service import (
    AdvisorService,
)


class FakeProvider(LlmProvider):

    @property
    def provider_name(self) -> str:
        return "fake"

    def generate_narrative(
        self,
        *,
        instructions: str,
        user_input: str,
    ) -> AdvisorNarrative:

        if "fraud-investigation" in user_input:
            return AdvisorNarrative(
                summary=(
                    "The model probability is 53.77%, "
                    "above the investigation threshold "
                    "of 8.59%. Human review has "
                    "technical priority."
                ),
                keyPoints=[
                    "Model probability: 53.77%.",
                    "Investigation threshold: 8.59%.",
                    "Human review priority: yes.",
                ],
            )

        return AdvisorNarrative(
            summary=(
                "The claim probability is 42.65%, "
                "above the technical threshold of "
                "10.33%. Expected frequency is "
                "0.5954 and expected claim count "
                "is 0.5954."
            ),
            keyPoints=[
                "Claim probability: 42.65%.",
                "Technical threshold: 10.33%.",
                (
                    "Expected frequency: 0.5954; "
                    "expected claims: 0.5954 "
                    "for exposure 1.00."
                ),
            ],
        )


class HallucinatingProvider(LlmProvider):

    @property
    def provider_name(self) -> str:
        return "fake"

    def generate_narrative(
        self,
        *,
        instructions: str,
        user_input: str,
    ) -> AdvisorNarrative:

        return AdvisorNarrative(
            summary=(
                "The claim probability is 42.65%, "
                "but the estimated future risk is 99.99%."
            ),
            keyPoints=[
                "Technical threshold: 10.33%.",
                "Expected frequency: 0.5954.",
                "Expected claims: 0.5954.",
            ],
        )


class UnsafeFraudProvider(LlmProvider):

    @property
    def provider_name(self) -> str:
        return "fake"

    def generate_narrative(
        self,
        *,
        instructions: str,
        user_input: str,
    ) -> AdvisorNarrative:

        return AdvisorNarrative(
            summary=(
                "The claim is fraudulent. "
                "The model probability is 53.77% "
                "and the threshold is 8.59%."
            ),
            keyPoints=[
                "Model probability: 53.77%.",
                "Investigation threshold: 8.59%.",
                "Human review priority: yes.",
            ],
        )


class FailingProvider(LlmProvider):

    @property
    def provider_name(self) -> str:
        return "fake"

    def generate_narrative(
        self,
        *,
        instructions: str,
        user_input: str,
    ) -> AdvisorNarrative:

        raise LlmProviderError(
            "Simulated provider failure."
        )


def make_service(
    provider: LlmProvider | None,
) -> AdvisorService:

    service = AdvisorService.__new__(
        AdvisorService
    )

    service.provider = provider
    service.last_execution_mode = "not-run"

    return service


@pytest.fixture
def risk_request() -> RiskAdvisorRequest:

    return RiskAdvisorRequest(
        claimProbability=0.4265319009621938,
        claimProbabilityThreshold=(
            0.10327080885569255
        ),
        technicalRiskFlag=True,
        predictedFrequency=(
            0.5954161286354065
        ),
        exposure=1.0,
        expectedClaimCount=(
            0.5954161286354065
        ),
    )


@pytest.fixture
def fraud_request() -> FraudAdvisorRequest:

    return FraudAdvisorRequest(
        fraudProbability=(
            0.5377003003817469
        ),
        investigationThreshold=(
            0.08585764735167348
        ),
        investigationFlag=True,
    )


@pytest.fixture(autouse=True)
def enable_fallback(
    monkeypatch: pytest.MonkeyPatch,
) -> None:

    test_settings = replace(
        advisor_module.settings,
        llm_fallback_enabled=True,
    )

    monkeypatch.setattr(
        advisor_module,
        "settings",
        test_settings,
    )


def test_risk_uses_fake_provider_without_api(
    risk_request: RiskAdvisorRequest,
) -> None:

    service = make_service(
        FakeProvider()
    )

    response = service.explain_risk(
        risk_request
    )

    assert response.title == (
        "Insurance risk analysis"
    )

    assert service.last_execution_mode == "fake"

    complete_text = (
        response.summary
        + " "
        + " ".join(response.keyPoints)
    )

    assert "42.65%" in complete_text
    assert "10.33%" in complete_text
    assert "0.5954" in complete_text

    assert len(response.keyPoints) == 3


def test_fraud_uses_fake_provider_without_api(
    fraud_request: FraudAdvisorRequest,
) -> None:

    service = make_service(
        FakeProvider()
    )

    response = service.explain_fraud(
        fraud_request
    )

    assert response.title == (
        "Fraud investigation analysis"
    )

    assert service.last_execution_mode == "fake"

    complete_text = (
        response.summary
        + " "
        + " ".join(response.keyPoints)
    )

    assert "53.77%" in complete_text
    assert "8.59%" in complete_text

    assert len(response.keyPoints) == 3


def test_numeric_hallucination_triggers_fallback(
    risk_request: RiskAdvisorRequest,
) -> None:

    service = make_service(
        HallucinatingProvider()
    )

    response = service.explain_risk(
        risk_request
    )

    assert (
        service.last_execution_mode
        == "deterministic-fallback"
    )

    complete_text = (
        response.summary
        + " "
        + " ".join(response.keyPoints)
    )

    assert "99.99%" not in complete_text
    assert "42.65%" in complete_text


def test_unsafe_fraud_accusation_triggers_fallback(
    fraud_request: FraudAdvisorRequest,
) -> None:

    service = make_service(
        UnsafeFraudProvider()
    )

    response = service.explain_fraud(
        fraud_request
    )

    assert (
        service.last_execution_mode
        == "deterministic-fallback"
    )

    complete_text = (
        response.summary
        + " "
        + " ".join(response.keyPoints)
    ).lower()

    assert "claim is fraudulent" not in complete_text

    assert (
        "does not establish that fraud occurred"
        in response.disclaimer.lower()
    )


def test_provider_failure_triggers_risk_fallback(
    risk_request: RiskAdvisorRequest,
) -> None:

    service = make_service(
        FailingProvider()
    )

    response = service.explain_risk(
        risk_request
    )

    assert (
        service.last_execution_mode
        == "deterministic-fallback"
    )

    assert response.title == (
        "Insurance risk analysis"
    )


def test_missing_provider_triggers_fraud_fallback(
    fraud_request: FraudAdvisorRequest,
) -> None:

    service = make_service(None)

    response = service.explain_fraud(
        fraud_request
    )

    assert (
        service.last_execution_mode
        == "deterministic-fallback"
    )

    assert response.title == (
        "Fraud investigation analysis"
    )
