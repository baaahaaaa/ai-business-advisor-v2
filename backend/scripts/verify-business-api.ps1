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

$PSDefaultParameterValues[
    "Invoke-RestMethod:Headers"
] = $authHeaders

$PSDefaultParameterValues[
    "Invoke-WebRequest:Headers"
] = $authHeaders
function Assert-Close {

    param(
        [double]$Actual,
        [double]$Expected,
        [double]$Tolerance = 1e-12,
        [string]$Name
    )

    $difference =
        [Math]::Abs(
            $Actual - $Expected
        )

    if ($difference -gt $Tolerance) {

        throw "$Name FAILED. Expected=$Expected Actual=$Actual Difference=$difference"
    }

    Write-Host "[PASS] $Name -> $Actual"
}


Write-Host ""
Write-Host "=============================================="
Write-Host "AI BUSINESS ADVISOR - BUSINESS API TESTS"
Write-Host "=============================================="
Write-Host ""


# ============================================================
# RISK ASSESSMENT
# ============================================================

Write-Host "Testing business risk assessment..."

$riskBody = @{

    exposure = 1.0

    vehiclePower = 7

    vehicleAge = 6

    driverAge = 79

    bonusMalus = 62

    density = 399

    area = "C"

    vehicleBrand = "B1"

    vehicleGas = "Regular"

    region = "R24"

} | ConvertTo-Json


$risk =
    Invoke-RestMethod `
        -Uri "$BaseUrl/api/risk/assessment" `
        -Method Post `
        -ContentType "application/json" `
        -Body $riskBody


Assert-Close `
    -Actual $risk.claimProbability `
    -Expected 0.4265319009621938 `
    -Name "Business claim probability"


Assert-Close `
    -Actual $risk.claimProbabilityThreshold `
    -Expected 0.10327080885569255 `
    -Name "Business claim threshold"


if ($risk.technicalRiskFlag -ne $true) {

    throw "Business technicalRiskFlag FAILED."
}


Write-Host "[PASS] Business technical risk flag"


Assert-Close `
    -Actual $risk.exposure `
    -Expected 1.0 `
    -Name "Business exposure"


Assert-Close `
    -Actual $risk.expectedClaimCount `
    -Expected (
        $risk.predictedFrequency *
        $risk.exposure
    ) `
    -Name "Business expected claim count consistency"


Write-Host ""


# ============================================================
# FRAUD ASSESSMENT
# ============================================================

Write-Host "Testing business fraud assessment..."

$fraudBody = @{

    age = $null

    deductible = 400

    weekOfMonth = 1

    weekOfMonthClaimed = 1

    driverRating = 3

    month = "Jan"

    dayOfWeek = "Saturday"

    make = "Honda"

    accidentArea = "Rural"

    dayOfWeekClaimed = "Tuesday"

    monthClaimed = "Jan"

    sex = "Male"

    maritalStatus = "Single"

    vehicleCategory = "Sedan"

    vehiclePrice = "more than 69000"

    pastNumberOfClaims = "none"

    ageOfVehicle = "new"

    ageOfPolicyHolder = "16 to 17"

    agentType = "External"

    numberOfCars = "1 vehicle"

    basePolicy = "All Perils"

} | ConvertTo-Json


$fraud =
    Invoke-RestMethod `
        -Uri "$BaseUrl/api/fraud/assessment" `
        -Method Post `
        -ContentType "application/json" `
        -Body $fraudBody


Assert-Close `
    -Actual $fraud.fraudProbability `
    -Expected 0.5377003003817469 `
    -Name "Business fraud probability"


Assert-Close `
    -Actual $fraud.investigationThreshold `
    -Expected 0.08585764735167348 `
    -Name "Business fraud threshold"


if ($fraud.investigationFlag -ne $true) {

    throw "Business investigationFlag FAILED."
}


Write-Host "[PASS] Business investigation flag"
Write-Host ""


# ============================================================
# VALIDATION
# ============================================================

Write-Host "Testing validation..."

$invalidRiskBody = @{

    exposure = 0

    vehiclePower = 7

    vehicleAge = 6

    driverAge = 79

    bonusMalus = 62

    density = 399

    area = "C"

    vehicleBrand = "B1"

    vehicleGas = "Regular"

    region = "R24"

} | ConvertTo-Json


try {

    Invoke-WebRequest `
        -Uri "$BaseUrl/api/risk/assessment" `
        -Method Post `
        -ContentType "application/json" `
        -Body $invalidRiskBody

    throw "Validation test FAILED: expected HTTP 400."

} catch {

    $statusCode =
        [int]$_.Exception.Response.StatusCode

    if ($statusCode -ne 400) {

        throw "Validation test FAILED. Expected 400, received $statusCode"
    }

    Write-Host "[PASS] Invalid risk request -> HTTP 400"
}


Write-Host ""
Write-Host "=============================================="
Write-Host "ALL BUSINESS API TESTS PASSED"
Write-Host "=============================================="

