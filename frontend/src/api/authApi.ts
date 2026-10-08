import { API_BASE_URL } from "./config";

export interface AuthUser {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: "ADMIN" | "ANALYST";
  enabled: boolean;
  createdAt: string;
  lastLoginAt: string | null;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: AuthUser;
}

export async function loginUser(
  email: string,
  password: string
): Promise<LoginResponse> {
  const response = await fetch(
    `${API_BASE_URL}/api/auth/login`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ email, password }),
    }
  );

  if (!response.ok) {
    if (response.status === 401) {
      throw new Error("Email ou mot de passe incorrect.");
    }

    throw new Error(
      `Connexion impossible : HTTP ${response.status}`
    );
  }

  const session = (await response.json()) as LoginResponse;

  if (!session.accessToken || !session.user?.enabled) {
    throw new Error("Session utilisateur invalide.");
  }

  return session;
}
