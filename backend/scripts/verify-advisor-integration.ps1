param(
    [string]$BaseUrl = $(if ($env:BACKEND_BASE_URL) {
        $env:BACKEND_BASE_URL
    }
    else {
        "http://127.0.0.1:8080"
    })
)

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "=============================================="
Write-Host "AI BUSINESS ADVISOR - ADVISOR INTEGRATION"
Write-Host "=============================================="
Write-Host ""


function Assert-Contains {
    param(
        [string]$Text,
        [string]$Expected,
        [string]$Label
    )

    if (-not $Text.Contains($Expected)) {
        throw "$Label does not contain expected value: $Expected"
    }

    Write-Host "[PASS] $Label -> $Expected"
}


Write-Host "Testing AI Advisor health..."

$health = Invoke-RestMethod `
    -Uri "http://127.0.0.1:8100/health" `
    -Method Get

if ($health.status -ne "healthy") {
    throw "AI Advisor is not healthy."
}

if ($health.advisor -ne $true) {
    throw "AI Advisor flag is not true."
}

Write-Host "[PASS] AI Advisor health"
Write-Host ""


Write-Host "Testing AI Advisor runtime..."

$runtime = Invoke-RestMethod `
    -Uri "http://127.0.0.1:8100/runtime" `
    -Method Get

if ($runtime.provider -ne "openai") {
    throw "Unexpected LLM provider: $($runtime.provider)"
}

if ($runtime.llmConfigured -ne $true) {
    throw "OpenAI API is not configured."
}

if ($runtime.llmReady -ne $true) {
    throw "OpenAI provider is not ready."
}

Write-Host "[PASS] Provider -> $($runtime.provider)"
Write-Host "[PASS] Model -> $($runtime.model)"
Write-Host "[PASS] LLM configured"
Write-Host "[PASS] LLM ready"
Write-Host ""


Write-Host "Testing risk advisor through Spring..."

$riskBody = @{
    claimProbability = 0.4265319009621938
    claimProbabilityThreshold = 0.10327080885569255
    technicalRiskFlag = $true
    predictedFrequency = 0.5954161286354065
    exposure = 1.0
    expectedClaimCount = 0.5954161286354065
} | ConvertTo-Json


$riskResponse = Invoke-RestMethod `
    -Uri "$BaseUrl/api/advisor/risk" `
    -Method Post `
    -ContentType "application/json" `
    -Body $riskBody


if ($riskResponse.title -ne "Insurance risk analysis") {
    throw "Unexpected risk advisor title."
}

Write-Host "[PASS] Risk advisor title"


if ($riskResponse.keyPoints.Count -ne 3) {
    throw "Risk advisor must return exactly 3 key points."
}

Write-Host "[PASS] Risk advisor key points count -> 3"


$riskText = (
    @($riskResponse.summary) +
    @($riskResponse.keyPoints)
) -join " "


Assert-Contains `
    -Text $riskText `
    -Expected "42.65%" `
    -Label "Risk claim probability"

Assert-Contains `
    -Text $riskText `
    -Expected "10.33%" `
    -Label "Risk technical threshold"

Assert-Contains `
    -Text $riskText `
    -Expected "0.5954" `
    -Label "Risk frequency/count"


$expectedRiskDisclaimer =
    "These indicators support human decision-making and do not constitute an automatic underwriting decision."

if ($riskResponse.disclaimer -ne $expectedRiskDisclaimer) {
    throw "Unexpected risk disclaimer."
}

Write-Host "[PASS] Risk deterministic disclaimer"


$riskRuntime = Invoke-RestMethod `
    -Uri "http://127.0.0.1:8100/runtime" `
    -Method Get

if ($riskRuntime.lastExecutionMode -ne "openai") {
    throw "Risk advisor did not use OpenAI. Mode: $($riskRuntime.lastExecutionMode)"
}

Write-Host "[PASS] Risk execution mode -> openai"
Write-Host ""


Write-Host "Testing fraud advisor through Spring..."

$fraudBody = @{
    fraudProbability = 0.5377003003817469
    investigationThreshold = 0.08585764735167348
    investigationFlag = $true
} | ConvertTo-Json


$fraudResponse = Invoke-RestMethod `
    -Uri "$BaseUrl/api/advisor/fraud" `
    -Method Post `
    -ContentType "application/json" `
    -Body $fraudBody


if ($fraudResponse.title -ne "Fraud investigation analysis") {
    throw "Unexpected fraud advisor title."
}

Write-Host "[PASS] Fraud advisor title"


if ($fraudResponse.keyPoints.Count -ne 3) {
    throw "Fraud advisor must return exactly 3 key points."
}

Write-Host "[PASS] Fraud advisor key points count -> 3"


$fraudText = (
    @($fraudResponse.summary) +
    @($fraudResponse.keyPoints)
) -join " "


Assert-Contains `
    -Text $fraudText `
    -Expected "53.77%" `
    -Label "Fraud model probability"

Assert-Contains `
    -Text $fraudText `
    -Expected "8.59%" `
    -Label "Fraud investigation threshold"


$expectedFraudDisclaimer =
    "The model output is intended only to prioritize claims for human investigation. It does not establish that fraud occurred."

if ($fraudResponse.disclaimer -ne $expectedFraudDisclaimer) {
    throw "Unexpected fraud disclaimer."
}

Write-Host "[PASS] Fraud deterministic disclaimer"


$forbiddenFraudStatements = @(
    "fraud is confirmed",
    "fraud was confirmed",
    "committed fraud",
    "claimant is fraudulent",
    "claim is fraudulent"
)

$fraudLower = $fraudText.ToLowerInvariant()

foreach ($statement in $forbiddenFraudStatements) {
    if ($fraudLower.Contains($statement)) {
        throw "Unsafe fraud statement detected: $statement"
    }
}

Write-Host "[PASS] Fraud response contains no prohibited accusation"


$fraudRuntime = Invoke-RestMethod `
    -Uri "http://127.0.0.1:8100/runtime" `
    -Method Get

if ($fraudRuntime.lastExecutionMode -ne "openai") {
    throw "Fraud advisor did not use OpenAI. Mode: $($fraudRuntime.lastExecutionMode)"
}

Write-Host "[PASS] Fraud execution mode -> openai"
Write-Host ""


Write-Host "=============================================="
Write-Host "ALL ADVISOR INTEGRATION TESTS PASSED"
Write-Host "=============================================="

