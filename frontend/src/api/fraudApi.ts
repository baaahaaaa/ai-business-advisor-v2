import { API_BASE_URL } from "./config";

export interface FraudAssessmentRequest {
  age: number | null;
  deductible: number;
  weekOfMonth: number;
  weekOfMonthClaimed: number;
  driverRating: number;
  month: string;
  dayOfWeek: string;
  make: string;
  accidentArea: string;
  dayOfWeekClaimed: string | null;
  monthClaimed: string | null;
  sex: string;
  maritalStatus: string;
  vehicleCategory: string;
  vehiclePrice: string;
  pastNumberOfClaims: string;
  ageOfVehicle: string;
  ageOfPolicyHolder: string;
  agentType: string;
  numberOfCars: string;
  basePolicy: string;
}

export interface FraudAssessmentResponse {
  fraudProbability: number;
  investigationThreshold: number;
  investigationFlag: boolean;
}

export async function assessFraud(
  request: FraudAssessmentRequest
): Promise<FraudAssessmentResponse> {

  const response = await fetch(
    `${API_BASE_URL}/api/fraud/assessment`,
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
      `Fraud assessment failed: HTTP ${response.status}`
    );
  }

  return response.json();
}
