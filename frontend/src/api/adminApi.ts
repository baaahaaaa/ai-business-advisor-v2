import { API_BASE_URL } from "./config";

import type { AuthUser } from "./authApi";
import type { AssessmentType } from "./historyApi";

export interface AdminDashboardResponse {
  totalUsers: number;
  activeUsers: number;
  adminUsers: number;
  analystUsers: number;

  totalAssessments: number;
  riskAssessments: number;
  fraudAssessments: number;
  flaggedAssessments: number;
}

export type AdminUser = AuthUser;

export interface AdminAssessment {
  id: string;
  assessmentType: AssessmentType;
  createdAt: string;

  primaryScore: number;
  technicalThreshold: number;
  flagged: boolean;

  predictedFrequency: number | null;
  expectedClaimCount: number | null;
  exposure: number | null;

  createdById: string;
  createdByEmail: string;
  createdByFirstName: string;
  createdByLastName: string;
  createdByRole: AuthUser["role"];
}

async function adminGet<T>(
  accessToken: string,
  path: string,
  signal?: AbortSignal
): Promise<T> {
  if (!accessToken) {
    throw new Error("Authentication required.");
  }

  const response = await fetch(
    `${API_BASE_URL}${path}`,
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
      "Your session has expired. Please sign in again."
    );
  }

  if (response.status === 403) {
    throw new Error(
      "Administrator access is required."
    );
  }

  if (!response.ok) {
    throw new Error(
      `Admin API request failed: HTTP ${response.status}`
    );
  }

  return (await response.json()) as T;
}

export function getAdminDashboard(
  accessToken: string,
  signal?: AbortSignal
): Promise<AdminDashboardResponse> {
  return adminGet<AdminDashboardResponse>(
    accessToken,
    "/api/admin/dashboard",
    signal
  );
}

export function getAdminUsers(
  accessToken: string,
  signal?: AbortSignal
): Promise<AdminUser[]> {
  return adminGet<AdminUser[]>(
    accessToken,
    "/api/admin/users",
    signal
  );
}

export function getAdminAssessments(
  accessToken: string,
  signal?: AbortSignal
): Promise<AdminAssessment[]> {
  return adminGet<AdminAssessment[]>(
    accessToken,
    "/api/admin/assessments",
    signal
  );
}
export interface CreateAdminUserRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  role: "ADMIN" | "ANALYST";
}

async function adminMutation<T>(
  accessToken: string,
  path: string,
  method: "POST" | "PATCH",
  body: unknown
): Promise<T> {
  if (!accessToken) {
    throw new Error("Authentication required.");
  }

  const response = await fetch(
    `${API_BASE_URL}${path}`,
    {
      method,
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify(body),
    }
  );

  if (response.status === 401) {
    throw new Error(
      "Your session has expired. Please sign in again."
    );
  }

  if (response.status === 403) {
    throw new Error(
      "Administrator access is required."
    );
  }

  if (response.status === 409) {
    throw new Error(
      "An account with this email already exists."
    );
  }

  if (response.status === 400) {
    throw new Error(
      "Request rejected. Check the fields or account restrictions."
    );
  }

  if (!response.ok) {
    throw new Error(
      `User management failed: HTTP ${response.status}`
    );
  }

  return (await response.json()) as T;
}

export function createAdminUser(
  accessToken: string,
  request: CreateAdminUserRequest
): Promise<AdminUser> {
  const email = request.email.trim().toLowerCase();
  const firstName = request.firstName.trim();
  const lastName = request.lastName.trim();

  if (!email || !email.includes("@")) {
    throw new Error("A valid email address is required.");
  }

  if (
    request.password.length < 12 ||
    request.password.length > 100
  ) {
    throw new Error(
      "Password must contain between 12 and 100 characters."
    );
  }

  if (
    !firstName ||
    !lastName ||
    firstName.length > 100 ||
    lastName.length > 100
  ) {
    throw new Error(
      "First name and last name must contain 1 to 100 characters."
    );
  }

  if (
    request.role !== "ADMIN" &&
    request.role !== "ANALYST"
  ) {
    throw new Error("Invalid account role.");
  }

  return adminMutation<AdminUser>(
    accessToken,
    "/api/admin/users",
    "POST",
    {
      email,
      password: request.password,
      firstName,
      lastName,
      role: request.role,
    }
  );
}

export function setAdminUserEnabled(
  accessToken: string,
  userId: string,
  enabled: boolean
): Promise<AdminUser> {
  if (!userId) {
    throw new Error("User ID is required.");
  }

  if (typeof enabled !== "boolean") {
    throw new Error("Invalid account status.");
  }

  return adminMutation<AdminUser>(
    accessToken,
    `/api/admin/users/${encodeURIComponent(userId)}/enabled`,
    "PATCH",
    { enabled }
  );
}
