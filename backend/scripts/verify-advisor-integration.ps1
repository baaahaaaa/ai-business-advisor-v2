$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "=============================================="
Write-Host "AI BUSINESS ADVISOR - ADVISOR TESTS"
Write-Host "=============================================="
Write-Host ""


Write-Host "Testing AI Advisor health..."

$advisorHealth =
    Invoke-RestMethod `
        -Uri "http://127.0.0.1:8100/health" `
        -Method Get

if ($advisorHealth.status -ne "healthy") {
    throw "AI Advisor is not healthy."
}

Write-Host "[PASS] AI Advisor health"
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


$riskResponse =
    Invoke-RestMethod `
        -Uri "http://127.0.0.1:8080/api/advisor/risk" `
        -Method Post `
        -ContentType "application/json" `
        -Body $riskBody


if ($riskResponse.title -ne "Insurance risk analysis") {
    throw "Unexpected risk advisor title."
}

if (
    $riskResponse.summary -notmatch "42.65%" -or
    $riskResponse.summary -notmatch "10.33%"
) {
    throw "Risk advisor summary does not contain expected values."
}

Write-Host "[PASS] Risk advisor title"
Write-Host "[PASS] Risk advisor values"
Write-Host "[PASS] Risk advisor response"
Write-Host ""


Write-Host "Testing fraud advisor through Spring..."

$fraudBody = @{
    fraudProbability = 0.5377003003817469
    investigationThreshold = 0.08585764735167348
    investigationFlag = $true
} | ConvertTo-Json


$fraudResponse =
    Invoke-RestMethod `
        -Uri "http://127.0.0.1:8080/api/advisor/fraud" `
        -Method Post `
        -ContentType "application/json" `
        -Body $fraudBody


if ($fraudResponse.title -ne "Fraud investigation analysis") {
    throw "Unexpected fraud advisor title."
}

if (
    $fraudResponse.summary -notmatch "53.77%" -or
    $fraudResponse.summary -notmatch "8.59%"
) {
    throw "Fraud advisor summary does not contain expected values."
}

if (
    $fraudResponse.disclaimer -notmatch
        "does not establish that fraud occurred"
) {
    throw "Fraud disclaimer is missing."
}

Write-Host "[PASS] Fraud advisor title"
Write-Host "[PASS] Fraud advisor values"
Write-Host "[PASS] Fraud safety disclaimer"
Write-Host ""


Write-Host "=============================================="
Write-Host "ALL ADVISOR INTEGRATION TESTS PASSED"
Write-Host "=============================================="
