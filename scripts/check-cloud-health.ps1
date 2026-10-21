# Read-only cloud health checks for the public Render demo.
# Usage (Windows PowerShell 5.1+ or PowerShell 7):
#   powershell -ExecutionPolicy Bypass -File .\scripts\check-cloud-health.ps1
# Health checks are not an end-to-end functional/security validation.
param(
    [ValidateRange(5, 180)]
    [int] $TimeoutSeconds = 90
)

$ErrorActionPreference = "Stop"

$targets = @(
    @{ Name = "Frontend"; Url = "https://ai-business-advisor-v2-frontend-cloud.onrender.com/" },
    @{ Name = "Backend"; Url = "https://ai-business-advisor-v2-backend-cloud.onrender.com/actuator/health" },
    @{ Name = "ML API"; Url = "https://ai-business-advisor-v2-ml-api-cloud.onrender.com/health" },
    @{ Name = "AI Advisor"; Url = "https://ai-business-advisor-v2-ai-service-cloud.onrender.com/health" }
)

$failed = 0

foreach ($target in $targets) {
    try {
        $response = Invoke-WebRequest -Uri $target.Url -Method Get -TimeoutSec $TimeoutSeconds -MaximumRedirection 5 -UseBasicParsing
        $status = [int]$response.StatusCode
        if ($status -lt 200 -or $status -ge 300) {
            throw "Unexpected HTTP $status"
        }

        # Spring Actuator may report degraded health in a JSON body; only report
        # the status field, never print arbitrary service responses or secrets.
        if ($target.Name -eq "Backend") {
            try {
                $payload = $response.Content | ConvertFrom-Json
                if ($payload.PSObject.Properties.Name -contains "status" -and $payload.status -ne "UP") {
                    throw "Spring Boot actuator reports status $($payload.status)"
                }
            }
            catch [System.ArgumentException] {
                # If an unexpected non-JSON body is returned, HTTP status remains the check.
            }
        }

        Write-Host ("[OK] {0}: HTTP {1}" -f $target.Name, $status)
    }
    catch {
        $failed++
        $code = "n/a"
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $code = [string][int]$_.Exception.Response.StatusCode
        }
        Write-Warning ("[FAIL] {0}: HTTP {1}; {2}" -f $target.Name, $code, $_.Exception.Message)
    }
}

Write-Host ("Checked {0} services. Failed: {1}." -f $targets.Count, $failed)
if ($failed -gt 0) { exit 1 }
exit 0
