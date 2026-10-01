from app.schemas import (
    AdvisorResponse,
    FraudAdvisorRequest,
    RiskAdvisorRequest,
)


def explain_risk(
    request: RiskAdvisorRequest,
) -> AdvisorResponse:

    probability_pct = request.claimProbability * 100
    threshold_pct = request.claimProbabilityThreshold * 100

    threshold_status = (
        "above"
        if request.technicalRiskFlag
        else "below"
    )

    return AdvisorResponse(
        title="Insurance risk analysis",
        summary=(
            f"The calibrated claim probability is "
            f"{probability_pct:.2f}%, which is "
            f"{threshold_status} the technical "
            f"threshold of {threshold_pct:.2f}%. "
            f"The expected claim frequency is "
            f"{request.predictedFrequency:.4f}, "
            f"corresponding to an expected claim "
            f"count of {request.expectedClaimCount:.4f} "
            f"for an exposure of {request.exposure:.2f}."
        ),
        keyPoints=[
            f"Claim probability: {probability_pct:.2f}%",
            f"Expected frequency: {request.predictedFrequency:.4f}",
            f"Expected claims: {request.expectedClaimCount:.4f}",
        ],
        disclaimer=(
            "These indicators support human decision-making "
            "and do not constitute an automatic underwriting decision."
        ),
    )


def explain_fraud(
    request: FraudAdvisorRequest,
) -> AdvisorResponse:

    probability_pct = request.fraudProbability * 100
    threshold_pct = request.investigationThreshold * 100

    if request.investigationFlag:
        priority_text = (
            "The claim is above the technical investigation "
            "threshold and should receive human review priority."
        )
    else:
        priority_text = (
            "The claim is below the technical investigation threshold."
        )

    return AdvisorResponse(
        title="Fraud investigation analysis",
        summary=(
            f"The calibrated fraud model probability is "
            f"{probability_pct:.2f}%. "
            f"The investigation threshold is "
            f"{threshold_pct:.2f}%. "
            f"{priority_text}"
        ),
        keyPoints=[
            f"Fraud model probability: {probability_pct:.2f}%",
            f"Investigation threshold: {threshold_pct:.2f}%",
            (
                "Human review priority: "
                + ("yes" if request.investigationFlag else "no")
            ),
        ],
        disclaimer=(
            "The model output is intended only to prioritize "
            "claims for human investigation. "
            "It does not establish that fraud occurred."
        ),
    )
