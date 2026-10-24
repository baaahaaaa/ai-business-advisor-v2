param(
    [string]$BaseUrl = $(if ($env:BACKEND_BASE_URL) {
        $env:BACKEND_BASE_URL
    }
    else {
        "http://127.0.0.1:8080"
    }),

    [string]$AccessToken = $(if ($env:BACKEND_ACCESS_TOKEN) {
        $env:BACKEND_ACCESS_TOKEN
    }
    else {
        ""
    })
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($AccessToken)) {
    throw "AccessToken is required. Provide -AccessToken or BACKEND_ACCESS_TOKEN."
}

$authHeaders = @{
    Authorization = "Bearer $AccessToken"
}

$script:PSDefaultParameterValues = @{
    "Invoke-RestMethod:Headers" = $authHeaders
    "Invoke-WebRequest:Headers" = $authHeaders
}

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

Write-Host ""
Write-Host "=============================================="
Write-Host "AI BUSINESS ADVISOR - FALLBACK E2E"
Write-Host "=============================================="
Write-Host ""

# ============================================================
# AI SERVICE HEALTH
# ============================================================

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

# ============================================================
# AI SERVICE RUNTIME
# ============================================================

$runtime = Invoke-RestMethod `
    -Uri "http://127.0.0.1:8100/runtime" `
    -Method Get

if ($runtime.provider -ne "openai") {
    throw "Unexpected advisor provider: $($runtime.provider)"
}

if ($runtime.fallbackEnabled -ne $true) {
    throw "Advisor deterministic fallback is not enabled."
}

Write-Host "[PASS] Advisor provider -> $($runtime.provider)"
Write-Host "[PASS] Deterministic fallback enabled"
Write-Host ""

# ============================================================
# RISK ADVISOR THROUGH SPRING
# ============================================================

Write-Host "Testing risk advisor fallback through Spring..."

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
    throw "Unexpected risk advisor title: $($riskResponse.title)"
}

if ($riskResponse.keyPoints.Count -ne 3) {
    throw "Risk advisor must return exactly 3 key points."
}

if ([string]::IsNullOrWhiteSpace($riskResponse.summary)) {
    throw "Risk advisor summary is empty."
}

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

$riskRuntime = Invoke-RestMethod `
    -Uri "http://127.0.0.1:8100/runtime" `
    -Method Get

if ($riskRuntime.lastExecutionMode -ne "deterministic-fallback") {
    throw "Risk advisor did not use fallback mode. Mode: $($riskRuntime.lastExecutionMode)"
}

Write-Host "[PASS] Risk fallback response"
Write-Host "[PASS] Risk deterministic disclaimer"
Write-Host "[PASS] Risk execution mode -> deterministic-fallback"
Write-Host ""

# ============================================================
# FRAUD ADVISOR THROUGH SPRING
# ============================================================

Write-Host "Testing fraud advisor fallback through Spring..."

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
    throw "Unexpected fraud advisor title: $($fraudResponse.title)"
}

if ($fraudResponse.keyPoints.Count -ne 3) {
    throw "Fraud advisor must return exactly 3 key points."
}

if ([string]::IsNullOrWhiteSpace($fraudResponse.summary)) {
    throw "Fraud advisor summary is empty."
}

$fraudText = (
    @($fraudResponse.summary) +
    @($fraudResponse.keyPoints)
) -join " "

Assert-Contains `
    -Text $fraudText `
    -Expected "53.77%" `
    -Label "Fraud probability"

Assert-Contains `
    -Text $fraudText `
    -Expected "8.59%" `
    -Label "Fraud investigation threshold"

$expectedFraudDisclaimer =
    "The model output is intended only to prioritize claims for human investigation. It does not establish that fraud occurred."

if ($fraudResponse.disclaimer -ne $expectedFraudDisclaimer) {
    throw "Unexpected fraud disclaimer."
}

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

$fraudRuntime = Invoke-RestMethod `
    -Uri "http://127.0.0.1:8100/runtime" `
    -Method Get

if ($fraudRuntime.lastExecutionMode -ne "deterministic-fallback") {
    throw "Fraud advisor did not use fallback mode. Mode: $($fraudRuntime.lastExecutionMode)"
}

Write-Host "[PASS] Fraud fallback response"
Write-Host "[PASS] Fraud deterministic disclaimer"
Write-Host "[PASS] Fraud response contains no prohibited accusation"
Write-Host "[PASS] Fraud execution mode -> deterministic-fallback"
Write-Host ""

Write-Host "=============================================="
Write-Host "ALL ADVISOR FALLBACK E2E TESTS PASSED"
Write-Host "=============================================="
