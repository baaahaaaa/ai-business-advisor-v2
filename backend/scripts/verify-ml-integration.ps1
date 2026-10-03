param(
    [string]$BaseUrl = $(if ($env:BACKEND_BASE_URL) {
        $env:BACKEND_BASE_URL
    }
    else {
        "http://127.0.0.1:8080"
    })
)

$ErrorActionPreference = "Stop"
function Assert-Close {
    param(
        [double]$Actual,
        [double]$Expected,
        [double]$Tolerance = 1e-12,
        [string]$Name
    )

    $difference = [Math]::Abs(
        $Actual - $Expected
    )

    if ($difference -gt $Tolerance) {
        throw "$Name FAILED. Expected=$Expected Actual=$Actual Difference=$difference"
    }

    Write-Host "[PASS] $Name -> $Actual"
}


Write-Host ""
Write-Host "=============================================="
Write-Host "AI BUSINESS ADVISOR - ML INTEGRATION TESTS"
Write-Host "=============================================="
Write-Host ""


# ============================================================
# HEALTH
# ============================================================

Write-Host "Testing ML health..."

$health = Invoke-RestMethod `
    -Uri "$BaseUrl/api/ml/health" `
    -Method Get

if ($health.status -ne "healthy") {
    throw "ML health status is not healthy."
}

if (-not $health.models.claim_occurrence) {
    throw "Claim occurrence model is not loaded."
}

if (-not $health.models.claim_frequency) {
    throw "Claim frequency model is not loaded."
}

if (-not $health.models.fraud_detection) {
    throw "Fraud model is not loaded."
}

Write-Host "[PASS] ML health"
Write-Host ""


# ============================================================
# CLAIM OCCURRENCE
# ============================================================

Write-Host "Testing claim occurrence..."

$occurrenceBody = @{
    Exposure = 1.0
    VehPower = 7
    VehAge = 6
    DrivAge = 79
    BonusMalus = 62
    Density = 399
    Area = "C"
    VehBrand = "B1"
    VehGas = "Regular"
    Region = "R24"
} | ConvertTo-Json

$occurrence = Invoke-RestMethod `
    -Uri "$BaseUrl/api/ml/claim-occurrence" `
    -Method Post `
    -ContentType "application/json" `
    -Body $occurrenceBody

Assert-Close `
    -Actual $occurrence.claim_probability `
    -Expected 0.4265319009621938 `
    -Name "Claim occurrence probability"

Assert-Close `
    -Actual $occurrence.technical_threshold `
    -Expected 0.10327080885569255 `
    -Name "Claim occurrence threshold"

if ($occurrence.technical_risk_flag -ne $true) {
    throw "Claim occurrence technical_risk_flag FAILED."
}

Write-Host "[PASS] Claim occurrence flag"
Write-Host ""


# ============================================================
# CLAIM FREQUENCY
# ============================================================

Write-Host "Testing claim frequency..."

$frequencyBody = @{
    Exposure = 0.48
    VehPower = 9
    VehAge = 0
    DrivAge = 32
    BonusMalus = 61
    Density = 352
    Area = "C"
    VehBrand = "B12"
    VehGas = "Regular"
    Region = "R41"
} | ConvertTo-Json

$frequency = Invoke-RestMethod `
    -Uri "$BaseUrl/api/ml/claim-frequency" `
    -Method Post `
    -ContentType "application/json" `
    -Body $frequencyBody

Assert-Close `
    -Actual $frequency.predicted_frequency `
    -Expected 1.8306647539138794 `
    -Name "Predicted frequency"

Assert-Close `
    -Actual $frequency.exposure `
    -Expected 0.48 `
    -Name "Frequency exposure"

Assert-Close `
    -Actual $frequency.expected_claim_count `
    -Expected 0.8787190818786621 `
    -Name "Expected claim count"

$calculatedExpectedCount =
    $frequency.predicted_frequency *
    $frequency.exposure

Assert-Close `
    -Actual $frequency.expected_claim_count `
    -Expected $calculatedExpectedCount `
    -Name "Frequency x exposure consistency"

Write-Host ""


# ============================================================
# FRAUD
# ============================================================

Write-Host "Testing fraud detection..."

$fraudBody = @{
    Age = $null
    Deductible = 400
    WeekOfMonth = 1
    WeekOfMonthClaimed = 1
    DriverRating = 3
    Month = "Jan"
    DayOfWeek = "Saturday"
    Make = "Honda"
    AccidentArea = "Rural"
    DayOfWeekClaimed = "Tuesday"
    MonthClaimed = "Jan"
    Sex = "Male"
    MaritalStatus = "Single"
    VehicleCategory = "Sedan"
    VehiclePrice = "more than 69000"
    PastNumberOfClaims = "none"
    AgeOfVehicle = "new"
    AgeOfPolicyHolder = "16 to 17"
    AgentType = "External"
    NumberOfCars = "1 vehicle"
    BasePolicy = "All Perils"
} | ConvertTo-Json

$fraud = Invoke-RestMethod `
    -Uri "$BaseUrl/api/ml/fraud" `
    -Method Post `
    -ContentType "application/json" `
    -Body $fraudBody

Assert-Close `
    -Actual $fraud.fraud_probability `
    -Expected 0.5377003003817469 `
    -Name "Fraud probability"

Assert-Close `
    -Actual $fraud.technical_threshold `
    -Expected 0.08585764735167348 `
    -Name "Fraud threshold"

if ($fraud.investigation_flag -ne $true) {
    throw "Fraud investigation_flag FAILED."
}

Write-Host "[PASS] Fraud investigation flag"
Write-Host ""


# ============================================================
# SUCCESS
# ============================================================

Write-Host "=============================================="
Write-Host "ALL ML INTEGRATION TESTS PASSED"
Write-Host "=============================================="

