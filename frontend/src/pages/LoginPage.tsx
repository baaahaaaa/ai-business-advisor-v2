import { useState } from "react";
import type { FormEvent } from "react";
import { useAuth } from "../auth/useAuth";

export default function LoginPage() {
  const { signIn } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (loading) return;

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
      setShowPassword(false);
      setLoading(false);
    }
  }

  return (
    <main className="login-v2-layout">

      <section className="login-v2-hero">
        <div className="login-v2-hero-inner">

          <div className="login-v2-brand">
            <div className="login-v2-logo">AI</div>

            <div>
              <strong>AI Business Advisor</strong>
              <span>Insurance Intelligence Platform</span>
            </div>
          </div>

          <div className="login-v2-presentation">

            <div className="login-v2-tag">
              <span className="login-v2-dot" />
              INTELLIGENT DECISION SUPPORT
            </div>

            <h1>
              Smarter Insurance
              <span>Decisions Start Here.</span>
            </h1>

            <p className="login-v2-intro">
              Transform insurance data into actionable insights.
              Analyze risk, prioritize fraud investigations and
              make confident, data-driven decisions.
            </p>

            <div className="login-v2-features">

              <div className="login-v2-feature">
                <div className="login-v2-feature-icon">01</div>

                <div>
                  <strong>Risk Intelligence</strong>
                  <span>
                    Predict claim probability and expected frequency.
                  </span>
                </div>
              </div>

              <div className="login-v2-feature">
                <div className="login-v2-feature-icon">02</div>

                <div>
                  <strong>Fraud Detection</strong>
                  <span>
                    Prioritize suspicious claims for human review.
                  </span>
                </div>
              </div>

              <div className="login-v2-feature">
                <div className="login-v2-feature-icon">03</div>

                <div>
                  <strong>AI-Powered Advisor</strong>
                  <span>
                    Understand analytical results with clear explanations.
                  </span>
                </div>
              </div>

            </div>
          </div>

          <div className="login-v2-hero-footer">
            AI Business Advisor V2 · Insurance Decision Support
          </div>

        </div>
      </section>

      <section className="login-v2-access">

        <div className="login-v2-form-container">

          <div className="login-v2-mobile-brand">
            <div className="login-v2-logo">AI</div>
            <strong>AI Business Advisor</strong>
          </div>

          <div className="login-v2-heading">
            <span>SECURE WORKSPACE</span>

            <h2 id="login-title">Welcome back</h2>

            <p>
              Sign in to your account to access your
              insurance intelligence workspace.
            </p>
          </div>

          <form
            className="login-v2-form"
            onSubmit={handleSubmit}
            aria-busy={loading}
            aria-labelledby="login-title"
          >

            <div className="login-v2-field">
              <label htmlFor="login-email">
                Email address
              </label>

              <input
                id="login-email"
                type="email"
                autoComplete="username"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                placeholder="you@company.com"
                required
                disabled={loading}
              />
            </div>

            <div className="login-v2-field">
              <label htmlFor="login-password">
                Password
              </label>

              <div className="login-v2-password-wrapper">
                <input
                  id="login-password"
                  type={showPassword ? "text" : "password"}
                  autoComplete="current-password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  placeholder="Enter your password"
                  required
                  disabled={loading}
                />

                <button
                  type="button"
                  className="login-v2-password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  disabled={loading}
                  aria-label={
                    showPassword ? "Hide password" : "Show password"
                  }
                  aria-pressed={showPassword}
                >
                  {showPassword ? "Hide" : "Show"}
                </button>
              </div>
            </div>

            {error && (
              <div className="login-v2-error" role="alert">
                {error}
              </div>
            )}

            <button
              className="login-v2-submit"
              type="submit"
              disabled={loading}
            >
              {loading ? "Signing in..." : "Sign in to workspace"}
              {!loading && <span aria-hidden="true">→</span>}
            </button>

          </form>

          <div className="login-v2-security">
            <span aria-hidden="true">✓</span>
            Secure access for authorized analysts and administrators.
          </div>

          <div className="login-v2-bottom">
            Need access? Contact your platform administrator.
          </div>

        </div>
      </section>

    </main>
  );
}
