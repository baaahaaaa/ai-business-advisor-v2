import logging
import re

from app.advisor import (
    explain_fraud as deterministic_explain_fraud,
)
from app.advisor import (
    explain_risk as deterministic_explain_risk,
)
from app.config import settings
from app.llm_schemas import AdvisorNarrative
from app.prompts import (
    SYSTEM_INSTRUCTIONS,
    build_fraud_prompt,
    build_risk_prompt,
)
from app.providers.base import (
    LlmProvider,
    LlmProviderError,
)
from app.providers.openai_provider import OpenAIProvider
from app.schemas import (
    AdvisorResponse,
    FraudAdvisorRequest,
    RiskAdvisorRequest,
)


logger = logging.getLogger(__name__)


RISK_TITLE = "Insurance risk analysis"

RISK_DISCLAIMER = (
    "These indicators support human decision-making "
    "and do not constitute an automatic underwriting decision."
)


FRAUD_TITLE = "Fraud investigation analysis"

FRAUD_DISCLAIMER = (
    "The model output is intended only to prioritize "
    "claims for human investigation. "
    "It does not establish that fraud occurred."
)


NUMBER_PATTERN = re.compile(
    r"(?<![\w.])\d+(?:\.\d+)?%?"
)


FRAUD_FORBIDDEN_PHRASES = (
    "fraud is confirmed",
    "fraud was confirmed",
    "committed fraud",
    "claimant is fraudulent",
    "claim is fraudulent",
    "fraudulent claim",
)


class AdvisorUnavailableError(RuntimeError):
    pass


class AdvisorService:

    def __init__(self) -> None:
        self.provider = self._build_provider()
        self.last_execution_mode = "not-run"

    def _build_provider(
        self,
    ) -> LlmProvider | None:

        if settings.llm_provider != "openai":
            logger.warning(
                "Unsupported LLM provider configured: %s",
                settings.llm_provider,
            )
            return None

        if not settings.openai_api_key:
            logger.warning(
                "OPENAI_API_KEY is not configured. "
                "Deterministic fallback will be used."
            )
            return None

        return OpenAIProvider(
            api_key=settings.openai_api_key,
            model=settings.openai_model,
            reasoning_effort=(
                settings.openai_reasoning_effort
            ),
            timeout_seconds=(
                settings.openai_timeout_seconds
            ),
        )

    @staticmethod
    def _validate_numeric_integrity(
        narrative: AdvisorNarrative,
        *,
        required_numbers: set[str],
        allowed_numbers: set[str],
    ) -> None:

        complete_text = " ".join(
            [
                narrative.summary,
                *narrative.keyPoints,
            ]
        )

        detected_numbers = set(
            NUMBER_PATTERN.findall(
                complete_text
            )
        )

        missing = (
            required_numbers
            - detected_numbers
        )

        unexpected = (
            detected_numbers
            - allowed_numbers
        )

        if missing:
            raise LlmProviderError(
                "LLM output omitted required "
                "model values."
            )

        if unexpected:
            raise LlmProviderError(
                "LLM output introduced unexpected "
                "numeric values."
            )

    @staticmethod
    def _validate_fraud_safety(
        narrative: AdvisorNarrative,
    ) -> None:

        complete_text = " ".join(
            [
                narrative.summary,
                *narrative.keyPoints,
            ]
        ).lower()

        for phrase in FRAUD_FORBIDDEN_PHRASES:
            if phrase in complete_text:
                raise LlmProviderError(
                    "LLM output contains an unsafe "
                    "fraud accusation."
                )

    def _fallback_risk(
        self,
        request: RiskAdvisorRequest,
    ) -> AdvisorResponse:

        self.last_execution_mode = (
            "deterministic-fallback"
        )

        return deterministic_explain_risk(
            request
        )

    def _fallback_fraud(
        self,
        request: FraudAdvisorRequest,
    ) -> AdvisorResponse:

        self.last_execution_mode = (
            "deterministic-fallback"
        )

        return deterministic_explain_fraud(
            request
        )

    def explain_risk(
        self,
        request: RiskAdvisorRequest,
    ) -> AdvisorResponse:

        probability = (
            f"{request.claimProbability * 100:.2f}%"
        )

        threshold = (
            f"{request.claimProbabilityThreshold * 100:.2f}%"
        )

        frequency = (
            f"{request.predictedFrequency:.4f}"
        )

        expected_count = (
            f"{request.expectedClaimCount:.4f}"
        )

        exposure = (
            f"{request.exposure:.2f}"
        )

        threshold_position = (
            "above"
            if request.technicalRiskFlag
            else "below"
        )

        payload = {
            "claimProbability": probability,
            "technicalThreshold": threshold,
            "thresholdPosition": threshold_position,
            "expectedFrequency": frequency,
            "expectedClaimCount": expected_count,
            "exposure": exposure,
        }

        if self.provider is None:
            if settings.llm_fallback_enabled:
                return self._fallback_risk(
                    request
                )

            raise AdvisorUnavailableError(
                "LLM provider is not configured."
            )

        try:
            narrative = (
                self.provider.generate_narrative(
                    instructions=(
                        SYSTEM_INSTRUCTIONS
                    ),
                    user_input=(
                        build_risk_prompt(
                            payload
                        )
                    ),
                )
            )

            self._validate_numeric_integrity(
                narrative,
                required_numbers={
                    probability,
                    threshold,
                    frequency,
                    expected_count,
                },
                allowed_numbers={
                    probability,
                    threshold,
                    frequency,
                    expected_count,
                    exposure,
                },
            )

            self.last_execution_mode = (
                self.provider.provider_name
            )

            logger.info(
                "Risk advisor generated by "
                "provider=%s model=%s",
                self.provider.provider_name,
                settings.openai_model,
            )

            return AdvisorResponse(
                title=RISK_TITLE,
                summary=narrative.summary,
                keyPoints=narrative.keyPoints,
                disclaimer=RISK_DISCLAIMER,
            )

        except Exception as exc:

            logger.warning(
                "Risk LLM generation failed; "
                "fallback=%s; error=%s",
                settings.llm_fallback_enabled,
                type(exc).__name__,
            )

            if settings.llm_fallback_enabled:
                return self._fallback_risk(
                    request
                )

            raise AdvisorUnavailableError(
                "Risk advisor generation failed."
            ) from exc

    def explain_fraud(
        self,
        request: FraudAdvisorRequest,
    ) -> AdvisorResponse:

        probability = (
            f"{request.fraudProbability * 100:.2f}%"
        )

        threshold = (
            f"{request.investigationThreshold * 100:.2f}%"
        )

        human_priority = (
            "yes"
            if request.investigationFlag
            else "no"
        )

        payload = {
            "fraudModelProbability": probability,
            "investigationThreshold": threshold,
            "humanReviewPriority": human_priority,
        }

        if self.provider is None:
            if settings.llm_fallback_enabled:
                return self._fallback_fraud(
                    request
                )

            raise AdvisorUnavailableError(
                "LLM provider is not configured."
            )

        try:
            narrative = (
                self.provider.generate_narrative(
                    instructions=(
                        SYSTEM_INSTRUCTIONS
                    ),
                    user_input=(
                        build_fraud_prompt(
                            payload
                        )
                    ),
                )
            )

            self._validate_numeric_integrity(
                narrative,
                required_numbers={
                    probability,
                    threshold,
                },
                allowed_numbers={
                    probability,
                    threshold,
                },
            )

            self._validate_fraud_safety(
                narrative
            )

            self.last_execution_mode = (
                self.provider.provider_name
            )

            logger.info(
                "Fraud advisor generated by "
                "provider=%s model=%s",
                self.provider.provider_name,
                settings.openai_model,
            )

            return AdvisorResponse(
                title=FRAUD_TITLE,
                summary=narrative.summary,
                keyPoints=narrative.keyPoints,
                disclaimer=FRAUD_DISCLAIMER,
            )

        except Exception as exc:

            logger.warning(
                "Fraud LLM generation failed; "
                "fallback=%s; error=%s",
                settings.llm_fallback_enabled,
                type(exc).__name__,
            )

            if settings.llm_fallback_enabled:
                return self._fallback_fraud(
                    request
                )

            raise AdvisorUnavailableError(
                "Fraud advisor generation failed."
            ) from exc

    def runtime_info(
        self,
    ) -> dict[str, object]:

        return {
            "provider": settings.llm_provider,
            "model": settings.openai_model,
            "llmConfigured": bool(
                settings.openai_api_key
            ),
            "llmReady": (
                self.provider is not None
            ),
            "fallbackEnabled": (
                settings.llm_fallback_enabled
            ),
            "lastExecutionMode": (
                self.last_execution_mode
            ),
            "responseStorage": False,
        }
