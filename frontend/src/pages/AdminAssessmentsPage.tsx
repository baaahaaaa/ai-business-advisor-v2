import { useEffect, useState } from "react";

import { useAuth } from "../auth/useAuth";
import {
  getAdminAssessments,
  type AdminAssessment,
} from "../api/adminApi";

import "./admin-assessments.css";

type TypeFilter = "ALL" | "RISK" | "FRAUD";
type FlagFilter = "ALL" | "FLAGGED" | "CLEAR";

function percentage(value: number): string {
  return `${(value * 100).toFixed(2)}%`;
}

function formatDate(value: string): string {
  const date = new Date(value);

  return Number.isNaN(date.getTime())
    ? "Unavailable"
    : date.toLocaleString("fr-FR");
}

export default function AdminAssessmentsPage() {
  const { user, accessToken } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const [records, setRecords] =
    useState<AdminAssessment[] | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const [search, setSearch] = useState("");
  const [typeFilter, setTypeFilter] = useState<TypeFilter>("ALL");
  const [flagFilter, setFlagFilter] = useState<FlagFilter>("ALL");
  const [pageSize, setPageSize] = useState(10);
  const [page, setPage] = useState(0);
  const [selectedId, setSelectedId] = useState<string | null>(null);

  useEffect(() => {
    if (!isAdmin || !accessToken) return;

    const controller = new AbortController();

    getAdminAssessments(accessToken, controller.signal)
      .then((response) => {
        if (!controller.signal.aborted) {
          setRecords(response);
        }
      })
      .catch((exception: unknown) => {
        if (!controller.signal.aborted) {
          setError(
            exception instanceof Error
              ? exception.message
              : "Unable to load assessments."
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

  function refreshRecords() {
    setLoading(true);
    setError(null);
    setRecords(null);
    setSelectedId(null);
    setRefreshKey((current) => current + 1);
  }

  function resetPage() {
    setPage(0);
    setSelectedId(null);
  }

  const allRecords = records ?? [];

  const totalRisk = allRecords.filter(
    (item) => item.assessmentType === "RISK"
  ).length;

  const totalFraud = allRecords.filter(
    (item) => item.assessmentType === "FRAUD"
  ).length;

  const totalFlagged = allRecords.filter(
    (item) => item.flagged
  ).length;

  const filtered = allRecords.filter((item) => {
    const query = search.trim().toLowerCase();

    const matchesSearch =
      !query ||
      item.createdByEmail.toLowerCase().includes(query) ||
      `${item.createdByFirstName} ${item.createdByLastName}`
        .toLowerCase()
        .includes(query) ||
      item.id.toLowerCase().includes(query);

    const matchesType =
      typeFilter === "ALL" ||
      item.assessmentType === typeFilter;

    const matchesFlag =
      flagFilter === "ALL" ||
      (flagFilter === "FLAGGED" && item.flagged) ||
      (flagFilter === "CLEAR" && !item.flagged);

    return matchesSearch && matchesType && matchesFlag;
  });

  const totalPages = Math.max(
    1,
    Math.ceil(filtered.length / pageSize)
  );

  const currentPage = Math.min(page, totalPages - 1);

  const visibleRecords = filtered.slice(
    currentPage * pageSize,
    (currentPage + 1) * pageSize
  );

  const selected = visibleRecords.find(
    (item) => item.id === selectedId
  );

  if (!isAdmin) {
    return (
      <main className="app-shell">
        <section className="admin-assessments-message">
          <h1>Access denied</h1>
          <p>Administrator access is required.</p>
        </section>
      </main>
    );
  }

  return (
    <div className="app-shell admin-assessments-page">
      <header className="topbar">
        <div>
          <p className="eyebrow">ADMINISTRATION WORKSPACE</p>
          <h1>All Assessments</h1>
          <p className="subtitle">
            Monitor risk and fraud evaluations across
            all registered users.
          </p>
        </div>

        <button
          type="button"
          className="secondary-button"
          onClick={refreshRecords}
          disabled={loading}
        >
          {loading ? "Loading..." : "Refresh assessments"}
        </button>
      </header>

      {!loading && !error && records && (
        <section className="admin-assessments-stats">
          <article>
            <span>Total Assessments</span>
            <strong>{records.length}</strong>
          </article>

          <article>
            <span>Risk Assessments</span>
            <strong>{totalRisk}</strong>
          </article>

          <article>
            <span>Fraud Assessments</span>
            <strong>{totalFraud}</strong>
          </article>

          <article>
            <span>Flagged Assessments</span>
            <strong>{totalFlagged}</strong>
          </article>
        </section>
      )}

      <section className="admin-assessments-panel">
        <div className="admin-assessments-heading">
          <div>
            <p className="section-label">PLATFORM ACTIVITY</p>
            <h2>Evaluation Records</h2>
          </div>
          <span>
            {loading ? "Loading..." : `${filtered.length} results`}
          </span>
        </div>

        <div className="admin-assessments-filters">
          <label>
            Search
            <input
              type="search"
              placeholder="User, email or assessment ID"
              value={search}
              onChange={(event) => {
                setSearch(event.target.value);
                resetPage();
              }}
            />
          </label>

          <label>
            Assessment Type
            <select
              value={typeFilter}
              onChange={(event) => {
                setTypeFilter(event.target.value as TypeFilter);
                resetPage();
              }}
            >
              <option value="ALL">All types</option>
              <option value="RISK">RISK</option>
              <option value="FRAUD">FRAUD</option>
            </select>
          </label>

          <label>
            Indicator
            <select
              value={flagFilter}
              onChange={(event) => {
                setFlagFilter(event.target.value as FlagFilter);
                resetPage();
              }}
            >
              <option value="ALL">All indicators</option>
              <option value="FLAGGED">Flagged</option>
              <option value="CLEAR">Below threshold</option>
            </select>
          </label>

          <label>
            Page Size
            <select
              value={pageSize}
              onChange={(event) => {
                setPageSize(Number(event.target.value));
                resetPage();
              }}
            >
              <option value={5}>5</option>
              <option value={10}>10</option>
              <option value={20}>20</option>
            </select>
          </label>
        </div>

        {loading && (
          <div className="admin-assessments-message" role="status">
            Loading platform assessments...
          </div>
        )}

        {!loading && error && (
          <div className="admin-assessments-error" role="alert">
            <strong>Unable to load assessments</strong>
            <p>{error}</p>
            <button type="button" onClick={refreshRecords}>
              Try again
            </button>
          </div>
        )}

        {!loading && !error && records && (
          <>
            <div className="admin-assessments-table-wrap">
              <table className="admin-assessments-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>User</th>
                    <th>Type</th>
                    <th>Score</th>
                    <th>Indicator</th>
                    <th>Details</th>
                  </tr>
                </thead>

                <tbody>
                  {visibleRecords.map((item) => (
                    <tr key={item.id}>
                      <td>{formatDate(item.createdAt)}</td>

                      <td>
                        <strong>
                          {item.createdByFirstName}{" "}
                          {item.createdByLastName}
                        </strong>
                        <small>{item.createdByEmail}</small>
                      </td>

                      <td>
                        <span
                          className={
                            item.assessmentType === "RISK"
                              ? "admin-assessments-type risk-type"
                              : "admin-assessments-type fraud-type"
                          }
                        >
                          {item.assessmentType}
                        </span>
                      </td>

                      <td className="admin-assessments-score">
                        {percentage(item.primaryScore)}
                      </td>

                      <td>
                        <span
                          className={
                            item.flagged
                              ? "admin-assessments-flag flagged"
                              : "admin-assessments-flag normal"
                          }
                        >
                          {item.flagged
                            ? "Flagged"
                            : "Below threshold"}
                        </span>
                      </td>

                      <td>
                        <button
                          type="button"
                          className="admin-assessments-view"
                          aria-expanded={selectedId === item.id}
                          onClick={() =>
                            setSelectedId(
                              selectedId === item.id
                                ? null
                                : item.id
                            )
                          }
                        >
                          {selectedId === item.id ? "Hide" : "View"}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {filtered.length === 0 && (
              <div className="admin-assessments-message">
                No assessments match the selected filters.
              </div>
            )}

            {selected && (
              <section className="admin-assessments-details">
                <h3>Assessment details</h3>

                <dl>
                  <div>
                    <dt>Assessment ID</dt>
                    <dd>{selected.id}</dd>
                  </div>
                  <div>
                    <dt>Author</dt>
                    <dd>{selected.createdByEmail}</dd>
                  </div>
                  <div>
                    <dt>Author role</dt>
                    <dd>{selected.createdByRole}</dd>
                  </div>
                  <div>
                    <dt>Model probability</dt>
                    <dd>{percentage(selected.primaryScore)}</dd>
                  </div>
                  <div>
                    <dt>Technical threshold</dt>
                    <dd>{percentage(selected.technicalThreshold)}</dd>
                  </div>
                  <div>
                    <dt>Flagged</dt>
                    <dd>{selected.flagged ? "Yes" : "No"}</dd>
                  </div>

                  {selected.assessmentType === "RISK" && (
                    <>
                      <div>
                        <dt>Predicted frequency</dt>
                        <dd>
                          {selected.predictedFrequency ?? "—"}
                        </dd>
                      </div>
                      <div>
                        <dt>Exposure</dt>
                        <dd>{selected.exposure ?? "—"}</dd>
                      </div>
                      <div>
                        <dt>Expected claim count</dt>
                        <dd>
                          {selected.expectedClaimCount ?? "—"}
                        </dd>
                      </div>
                    </>
                  )}
                </dl>

                <p>
                  These scores are decision-support indicators.
                  A flagged assessment is not proof of fraud.
                </p>
              </section>
            )}

            <div className="admin-assessments-pagination">
              <span>
                Page {currentPage + 1} of {totalPages}
              </span>

              <div>
                <button
                  type="button"
                  disabled={currentPage === 0}
                  onClick={() => {
                    setPage((value) => Math.max(0, value - 1));
                    setSelectedId(null);
                  }}
                >
                  Previous
                </button>

                <button
                  type="button"
                  disabled={currentPage >= totalPages - 1}
                  onClick={() => {
                    setPage((value) => value + 1);
                    setSelectedId(null);
                  }}
                >
                  Next
                </button>
              </div>
            </div>
          </>
        )}
      </section>
    </div>
  );
}
