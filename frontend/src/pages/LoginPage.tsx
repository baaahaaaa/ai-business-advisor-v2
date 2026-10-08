import { useState } from "react";
import type { FormEvent } from "react";

import { useAuth } from "../auth/useAuth";

export default function LoginPage() {
  const { signIn } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setLoading(true);
    setError(null);

    try {
      await signIn(email.trim(), password);
    } catch (exception) {
      setError(
        exception instanceof Error
          ? exception.message
          : "Connexion impossible."
      );
    } finally {
      setPassword("");
      setLoading(false);
    }
  }

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <div className="auth-icon">
          <span>AI</span>
        </div>

        <p className="auth-eyebrow">
          AI BUSINESS ADVISOR
        </p>

        <h1>Welcome back</h1>

        <p className="auth-description">
          Sign in to access your insurance
          decision-support workspace.
        </p>

        <form onSubmit={handleSubmit} className="auth-form">
          <label htmlFor="login-email">
            Email address
          </label>

          <input
            id="login-email"
            type="email"
            autoComplete="username"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="you@example.com"
            required
          />

          <label htmlFor="login-password">
            Password
          </label>

          <input
            id="login-password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            placeholder="Enter your password"
            required
          />

          {error && (
            <div className="auth-error" role="alert">
              {error}
            </div>
          )}

          <button
            className="auth-submit"
            type="submit"
            disabled={loading}
          >
            {loading ? "Signing in..." : "Sign in"}
          </button>
        </form>

        <p className="auth-footer">
          Secure access for authorized analysts
          and administrators.
        </p>
      </section>
    </main>
  );
}
