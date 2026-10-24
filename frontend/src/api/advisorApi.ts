import { API_BASE_URL } from "./config";


export interface AdvisorResponse {
  title: string;
  summary: string;
  keyPoints: string[];
  disclaimer: string;
}


export interface RiskAdvisorRequest {
  claimProbability: number;
  claimProbabilityThreshold: number;
  technicalRiskFlag: boolean;
  predictedFrequency: number;
  exposure: number;
  expectedClaimCount: number;
}


export interface FraudAdvisorRequest {
  fraudProbability: number;
  investigationThreshold: number;
  investigationFlag: boolean;
}


export async function explainRisk(
  request: RiskAdvisorRequest,
  accessToken: string
): Promise<AdvisorResponse> {

  const response = await fetch(
    `${API_BASE_URL}/api/advisor/risk`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify(request),
    }
  );

  if (!response.ok) {
    throw new Error(
      `AI Advisor failed: HTTP ${response.status}`
    );
  }

  return response.json();
}


export async function explainFraud(
  request: FraudAdvisorRequest,
  accessToken: string
): Promise<AdvisorResponse> {

  const response = await fetch(
    `${API_BASE_URL}/api/advisor/fraud`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify(request),
    }
  );

  if (!response.ok) {
    throw new Error(
      `AI Advisor failed: HTTP ${response.status}`
    );
  }

  return response.json();
}
