import { API_BASE_URL } from "./config";

export type AssessmentType = "RISK" | "FRAUD";

export interface AssessmentHistoryItem {
  id: string;
  assessmentType: AssessmentType;
  createdAt: string;
  primaryScore: number;
  technicalThreshold: number;
  flagged: boolean;
  predictedFrequency: number | null;
  expectedClaimCount: number | null;
  exposure: number | null;
}

export interface AssessmentHistoryPage {
  items: AssessmentHistoryItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface HistoryQuery {
  page: number;
  size: number;
  type?: AssessmentType;
}

export async function getAssessmentHistory(
  accessToken: string,
  query: HistoryQuery,
  signal?: AbortSignal
): Promise<AssessmentHistoryPage> {
  if (!accessToken) {
    throw new Error("Authentication required.");
  }

  if (
    !Number.isInteger(query.page) ||
    query.page < 0 ||
    !Number.isInteger(query.size) ||
    query.size < 1 ||
    query.size > 100
  ) {
    throw new Error("Invalid pagination parameters.");
  }

  const params = new URLSearchParams({
    page: String(query.page),
    size: String(query.size),
  });

  if (query.type) {
    params.set("type", query.type);
  }

  const response = await fetch(
    `${API_BASE_URL}/api/history/assessments/paged?${params}`,
    {
      method: "GET",
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
      signal,
    }
  );

  if (response.status === 401) {
    throw new Error(
      "Your session is invalid or expired. Please sign in again."
    );
  }

  if (response.status === 403) {
    throw new Error(
      "You do not have permission to view this history."
    );
  }

  if (!response.ok) {
    throw new Error(
      `Unable to load assessment history: HTTP ${response.status}`
    );
  }

  return (await response.json()) as AssessmentHistoryPage;
}
