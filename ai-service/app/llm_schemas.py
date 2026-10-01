from pydantic import BaseModel, Field


class AdvisorNarrative(BaseModel):
    summary: str = Field(
        min_length=1,
        max_length=1200,
    )

    keyPoints: list[str] = Field(
        min_length=3,
        max_length=3,
    )
