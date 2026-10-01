import { API_BASE_URL } from "./config";

export interface InsuranceRiskRequest {
  exposure: number;
  vehiclePower: number;
  vehicleAge: number;
  driverAge: number;
  bonusMalus: number;
  density: number;
  area: string;
  vehicleBrand: string;
  vehicleGas: string;
  region: string;
}

export interface InsuranceRiskAssessmentResponse {
  claimProbability: number;
  claimProbabilityThreshold: number;
  technicalRiskFlag: boolean;
  predictedFrequency: number;
  exposure: number;
  expectedClaimCount: number;
}

export async function assessInsuranceRisk(
  request: InsuranceRiskRequest
): Promise<InsuranceRiskAssessmentResponse> {

  const response = await fetch(
    `${API_BASE_URL}/api/risk/assessment`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    }
  );

  if (!response.ok) {
    throw new Error(
      `Risk assessment failed: HTTP ${response.status}`
    );
  }

  return response.json();
}
