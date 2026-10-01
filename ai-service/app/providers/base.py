from abc import ABC, abstractmethod

from app.llm_schemas import AdvisorNarrative


class LlmProviderError(RuntimeError):
    pass


class LlmProvider(ABC):

    @property
    @abstractmethod
    def provider_name(self) -> str:
        raise NotImplementedError

    @abstractmethod
    def generate_narrative(
        self,
        *,
        instructions: str,
        user_input: str,
    ) -> AdvisorNarrative:
        raise NotImplementedError
