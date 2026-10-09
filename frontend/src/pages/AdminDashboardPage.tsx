import { useEffect, useState } from "react";

import { useAuth } from "../auth/useAuth";

import {
  getAdminDashboard,
  type AdminDashboardResponse,
} from "../api/adminApi";

import "./admin-dashboard.css";

function percentage(value: number, total: number): number {
  if (total <= 0) return 0;

  return Math.min(
    100,
    Math.max(0, Math.round((value / total) * 100))
  );
}

export default function AdminDashboardPage() {
  const { user, accessToken } = useAuth();

  const isAdmin = user?.role === "ADMIN";

  const [data, setData] =
    useState<AdminDashboardResponse | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    if (!isAdmin || !accessToken) return;

    const controller = new AbortController();

    getAdminDashboard(accessToken, controller.signal)
      .then((response) => {
        if (!controller.signal.aborted) {
          setData(response);
        }
      })
      .catch((exception: unknown) => {
        if (!controller.signal.aborted) {
          setError(
            exception instanceof Error
              ? exception.message
              : "Unable to load administrator dashboard."
          );
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      });

    return () => controller.abort();
  }, [accessToken, isAdmin, refreshKey]);

  function refreshDashboard() {
    setData(null);
    setError(null);
    setLoading(true);
    setRefreshKey((value) => value + 1);
  }

  if (!isAdmin) {
    return (
      <main className="app-shell">
        <div className="admin-access-message">
          <h1>Access denied</h1>
          <p>This workspace is reserved for administrators.</p>
        </div>
      </main>
    );
  }

  if (!accessToken) {
    return (
      <main className="app-shell">
        <div className="admin-access-message">
          <h1>Authentication required</h1>
          <p>Please sign in again.</p>
        </div>
      </main>
    );
  }

  return (
    <div className="app-shell admin-dashboard">
      <header className="topbar">
        <div>
          <p className="eyebrow">
            ADMINISTRATION WORKSPACE
          </p>
          <h1>Admin Dashboard</h1>
          <p className="subtitle">
            Monitor users, insurance assessments
            and overall platform activity.
          </p>
        </div>

        <button
          className="secondary-button"
          type="button"
          onClick={refreshDashboard}
          disabled={loading}
        >
          {loading ? "Loading..." : "Refresh data"}
        </button>
      </header>

      {loading && (
        <div className="admin-message" role="status">
          <h2>Loading dashboard...</h2>
          <p>Fetching live platform statistics.</p>
        </div>
      )}

      {!loading && error && (
        <div className="admin-error" role="alert">
          <h2>Dashboard unavailable</h2>
          <p>{error}</p>
          <button
            type="button"
            onClick={refreshDashboard}
          >
            Try again
          </button>
        </div>
      )}

      {!loading && !error && data && (
        <>
          <section
            className="admin-stats-grid"
            aria-label="Platform statistics"
          >
            <article className="admin-stat-card">
              <span className="admin-stat-icon">01</span>
              <p>Total Users</p>
              <strong>{data.totalUsers}</strong>
              <small>Registered accounts</small>
            </article>

            <article className="admin-stat-card">
              <span className="admin-stat-icon">02</span>
              <p>Active Users</p>
              <strong>{data.activeUsers}</strong>
              <small>Enabled accounts</small>
            </article>

            <article className="admin-stat-card">
              <span className="admin-stat-icon">03</span>
              <p>Total Assessments</p>
              <strong>{data.totalAssessments}</strong>
              <small>Risk and Fraud evaluations</small>
            </article>

            <article className="admin-stat-card">
              <span className="admin-stat-icon">04</span>
              <p>Flagged Assessments</p>
              <strong>{data.flaggedAssessments}</strong>
              <small>Require further review</small>
            </article>
          </section>

          <div className="admin-analytics-grid">
            <section className="admin-analytics-card">
              <div className="admin-section-heading">
                <div>
                  <p className="section-label">ANALYTICS</p>
                  <h2>Assessment Distribution</h2>
                </div>
                <span className="admin-summary-pill">
                  {data.totalAssessments} records
                </span>
              </div>

              <div className="admin-chart-rows">
                <div className="admin-chart-row">
                  <div className="admin-chart-label">
                    <span>Risk Assessments</span>
                    <strong>{data.riskAssessments}</strong>
                  </div>

                  <div className="admin-chart-track">
                    <div
                      className="admin-chart-fill admin-risk-fill"
                      style={{
                        width: `${percentage(
                          data.riskAssessments,
                          data.totalAssessments
                        )}%`,
                      }}
                    />
                  </div>
                </div>

                <div className="admin-chart-row">
                  <div className="admin-chart-label">
                    <span>Fraud Assessments</span>
                    <strong>{data.fraudAssessments}</strong>
                  </div>

                  <div className="admin-chart-track">
                    <div
                      className="admin-chart-fill admin-fraud-fill"
                      style={{
                        width: `${percentage(
                          data.fraudAssessments,
                          data.totalAssessments
                        )}%`,
                      }}
                    />
                  </div>
                </div>
              </div>

              <div className="admin-insight">
                <strong>
                  {percentage(
                    data.flaggedAssessments,
                    data.totalAssessments
                  )}%
                </strong>
                <span>
                  of recorded assessments are flagged
                  for review.
                </span>
              </div>
            </section>

            <section className="admin-analytics-card">
              <div className="admin-section-heading">
                <div>
                  <p className="section-label">ACCESS MANAGEMENT</p>
                  <h2>User Distribution</h2>
                </div>
                <span className="admin-summary-pill">
                  {data.totalUsers} users
                </span>
              </div>

              <div className="admin-chart-rows">
                <div className="admin-chart-row">
                  <div className="admin-chart-label">
                    <span>Administrators</span>
                    <strong>{data.adminUsers}</strong>
                  </div>

                  <div className="admin-chart-track">
                    <div
                      className="admin-chart-fill admin-admin-fill"
                      style={{
                        width: `${percentage(
                          data.adminUsers,
                          data.totalUsers
                        )}%`,
                      }}
                    />
                  </div>
                </div>

                <div className="admin-chart-row">
                  <div className="admin-chart-label">
                    <span>Analysts</span>
                    <strong>{data.analystUsers}</strong>
                  </div>

                  <div className="admin-chart-track">
                    <div
                      className="admin-chart-fill admin-analyst-fill"
                      style={{
                        width: `${percentage(
                          data.analystUsers,
                          data.totalUsers
                        )}%`,
                      }}
                    />
                  </div>
                </div>
              </div>

              <div className="admin-insight">
                <strong>
                  {percentage(
                    data.activeUsers,
                    data.totalUsers
                  )}%
                </strong>
                <span>
                  of registered user accounts are enabled.
                </span>
              </div>
            </section>
          </div>

          <div className="admin-notice">
            <strong>Decision-support notice</strong>
            <p>
              Risk scores and fraud investigation flags
              are analytical indicators. A flagged record
              is not proof of fraud.
            </p>
          </div>
        </>
      )}
    </div>
  );
}
