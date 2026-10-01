from openai import OpenAI

from app.llm_schemas import AdvisorNarrative
from app.providers.base import (
    LlmProvider,
    LlmProviderError,
)


class OpenAIProvider(LlmProvider):

    def __init__(
        self,
        *,
        api_key: str,
        model: str,
        reasoning_effort: str,
        timeout_seconds: float,
    ) -> None:

        if not api_key:
            raise ValueError(
                "OPENAI_API_KEY is not configured."
            )

        self.model = model
        self.reasoning_effort = reasoning_effort

        self.client = OpenAI(
            api_key=api_key,
            timeout=timeout_seconds,
            max_retries=2,
        )

    @property
    def provider_name(self) -> str:
        return "openai"

    def generate_narrative(
        self,
        *,
        instructions: str,
        user_input: str,
    ) -> AdvisorNarrative:

        try:
            response = self.client.responses.parse(
                model=self.model,
                instructions=instructions,
                input=user_input,
                text_format=AdvisorNarrative,
                reasoning={
                    "effort": self.reasoning_effort
                },
                max_output_tokens=700,
                store=False,
            )

            parsed = response.output_parsed

            if parsed is None:
                raise LlmProviderError(
                    "OpenAI returned no parsed output."
                )

            return parsed

        except LlmProviderError:
            raise

        except Exception as exc:
            raise LlmProviderError(
                "OpenAI generation failed."
            ) from exc
