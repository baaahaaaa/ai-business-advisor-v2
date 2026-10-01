import os
from dataclasses import dataclass
from pathlib import Path

from dotenv import load_dotenv


BASE_DIR = Path(__file__).resolve().parents[1]
ENV_FILE = BASE_DIR / ".env"

load_dotenv(ENV_FILE)


def _get_bool(
    name: str,
    default: bool,
) -> bool:
    raw = os.getenv(name)

    if raw is None:
        return default

    return raw.strip().lower() in {
        "1",
        "true",
        "yes",
        "on",
    }


@dataclass(frozen=True)
class Settings:
    llm_provider: str
    openai_api_key: str
    openai_model: str
    openai_reasoning_effort: str
    openai_timeout_seconds: float
    llm_fallback_enabled: bool


def load_settings() -> Settings:
    return Settings(
        llm_provider=os.getenv(
            "LLM_PROVIDER",
            "openai",
        ).strip().lower(),
        openai_api_key=os.getenv(
            "OPENAI_API_KEY",
            "",
        ).strip(),
        openai_model=os.getenv(
            "OPENAI_MODEL",
            "gpt-6-luna",
        ).strip(),
        openai_reasoning_effort=os.getenv(
            "OPENAI_REASONING_EFFORT",
            "low",
        ).strip().lower(),
        openai_timeout_seconds=float(
            os.getenv(
                "OPENAI_TIMEOUT_SECONDS",
                "20",
            )
        ),
        llm_fallback_enabled=_get_bool(
            "LLM_FALLBACK_ENABLED",
            True,
        ),
    )


settings = load_settings()
