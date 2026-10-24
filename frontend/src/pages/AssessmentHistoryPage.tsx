import { useEffect, useState } from "react";

import { useAuth } from "../auth/useAuth";

import {
  getAssessmentHistory,
  type AssessmentHistoryItem,
  type AssessmentHistoryPage,
  type AssessmentType,
} from "../api/historyApi";

import "./assessment-history.css";

type HistoryFilter = "ALL" | AssessmentType;

function percent(value: number): string {
  return `${(value * 100).toFixed(2)}%`;
}

function dateTime(value: string): string {
  return new Date(value).toLocaleString("fr-FR");
}

function optionalNumber(value: number | null): string {
  return value === null ? "—" : value.toFixed(4);
}

function statusLabel(item: AssessmentHistoryItem): string {
  if (item.assessmentType === "FRAUD") {
    return item.flagged
      ? "Investigation priority"
      : "Below investigation threshold";
  }

  return item.flagged
    ? "Above technical threshold"
    : "Below technical threshold";
}

export default function AssessmentHistoryPage() {
  const { accessToken } = useAuth();

  const [filter, setFilter] = useState<HistoryFilter>("ALL");
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [refresh, setRefresh] = useState(0);

  const [data, setData] =
    useState<AssessmentHistoryPage | null>(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [selectedId, setSelectedId] =
    useState<string | null>(null);

  useEffect(() => {
    if (!accessToken) return;

    const controller = new AbortController();
    let active = true;


    getAssessmentHistory(
      accessToken,
      {
        page,
        size,
        ...(filter !== "ALL" ? { type: filter } : {}),
      },
      controller.signal
    )
      .then((response) => {
        if (active) {
          setData(response);
        }
      })
      .catch((exception: unknown) => {
        if (!active) return;

        setError(
          exception instanceof Error
            ? exception.message
            : "Unable to load assessment history."
        );
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
      controller.abort();
    };
  }, [accessToken, filter, page, size, refresh]);

  const selected = data?.items.find(
    (item) => item.id === selectedId
  );

  function startLoad() {
    setLoading(true);
    setError(null);
    setData(null);
    setSelectedId(null);
  }

  function changeFilter(value: HistoryFilter) {
    startLoad();
    setFilter(value);
    setPage(0);
  }

  return (
    <div className="app-shell history-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">AI BUSINESS ADVISOR</p>
          <h1>Assessment History</h1>
          <p className="subtitle">
            Review your saved risk and fraud assessment
            results. All records are scoped to your account.
          </p>
        </div>

        <button
          type="button"
          className="secondary-button"
          onClick={() => {
            startLoad();
            setRefresh((value) => value + 1);
          }}
          disabled={loading}
        >
          Refresh history
        </button>
      </header>

      <section className="history-stats" aria-label="History summary">
        <article className="history-stat">
          <span>Matching assessments</span>
          <strong>{data?.totalElements ?? "—"}</strong>
        </article>

        <article className="history-stat">
          <span>Current page</span>
          <strong>{page + 1}</strong>
        </article>

        <article className="history-stat">
          <span>Results on this page</span>
          <strong>{data?.items.length ?? "—"}</strong>
        </article>
      </section>

      <section className="panel history-panel">
        <div className="history-heading">
          <div>
            <p className="section-label">ASSESSMENT RECORDS</p>
            <h2>Evaluation history</h2>
          </div>

          <div className="history-controls">
            <label>
              Type
              <select
                value={filter}
                onChange={(event) =>
                  changeFilter(event.target.value as HistoryFilter)
                }
              >
                <option value="ALL">All assessments</option>
                <option value="RISK">Risk</option>
                <option value="FRAUD">Fraud</option>
              </select>
            </label>

            <label>
              Page size
              <select
                value={size}
                onChange={(event) => {
                  startLoad();
                  setSize(Number(event.target.value));
                  setPage(0);
                }}
              >
                <option value={5}>5 results</option>
                <option value={10}>10 results</option>
                <option value={20}>20 results</option>
              </select>
            </label>
          </div>
        </div>

        {loading && (
          <div className="history-message" role="status">
            <div className="loader" />
            <p>Loading your assessments...</p>
          </div>
        )}

        {error && !loading && (
          <div className="error-box" role="alert">
            <strong>History unavailable</strong>
            <span>{error}</span>
          </div>
        )}

        {!loading && !error && data?.items.length === 0 && (
          <div className="history-message">
            <h3>No assessments found</h3>
            <p>
              No records match your current filter.
            </p>
          </div>
        )}

        {!loading && !error && data && data.items.length > 0 && (
          <>
            <div className="history-table-wrap">
              <table className="history-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Type</th>
                    <th>Score</th>
                    <th>Threshold</th>
                    <th>Indicator</th>
                    <th>Details</th>
                  </tr>
                </thead>

                <tbody>
                  {data.items.map((item) => (
                    <tr key={item.id}>
                      <td>{dateTime(item.createdAt)}</td>

                      <td>
                        <span
                          className={
                            item.assessmentType === "RISK"
                              ? "history-type history-risk"
                              : "history-type history-fraud"
                          }
                        >
                          {item.assessmentType}
                        </span>
                      </td>

                      <td className="history-score">
                        {percent(item.primaryScore)}
                      </td>

                      <td>
                        {percent(item.technicalThreshold)}
                      </td>

                      <td>
                        <span
                          className={
                            item.flagged
                              ? "history-flag history-alert"
                              : "history-flag history-normal"
                          }
                        >
                          {statusLabel(item)}
                        </span>
                      </td>

                      <td>
                        <button
                          type="button"
                          className="history-details-button"
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

            {selected && (
              <section className="history-details">
                <div className="history-details-title">
                  <h3>Assessment details</h3>
                  <span>{selected.assessmentType}</span>
                </div>

                <dl className="history-details-grid">
                  <div>
                    <dt>Assessment ID</dt>
                    <dd className="history-id">{selected.id}</dd>
                  </div>
                  <div>
                    <dt>Created at</dt>
                    <dd>{dateTime(selected.createdAt)}</dd>
                  </div>
                  <div>
                    <dt>Model probability</dt>
                    <dd>{percent(selected.primaryScore)}</dd>
                  </div>
                  <div>
                    <dt>Technical threshold</dt>
                    <dd>{percent(selected.technicalThreshold)}</dd>
                  </div>
                  <div>
                    <dt>Indicator</dt>
                    <dd>{statusLabel(selected)}</dd>
                  </div>

                  {selected.assessmentType === "RISK" && (
                    <>
                      <div>
                        <dt>Predicted frequency</dt>
                        <dd>
                          {optionalNumber(selected.predictedFrequency)}
                        </dd>
                      </div>
                      <div>
                        <dt>Exposure</dt>
                        <dd>{optionalNumber(selected.exposure)}</dd>
                      </div>
                      <div>
                        <dt>Expected claim count</dt>
                        <dd>
                          {optionalNumber(selected.expectedClaimCount)}
                        </dd>
                      </div>
                    </>
                  )}
                </dl>

                <p className="history-disclaimer">
                  These results are decision-support indicators.
                  A fraud investigation flag does not establish
                  that fraud occurred, and risk indicators do not
                  constitute an automatic underwriting decision.
                </p>
              </section>
            )}
          </>
        )}

        {!loading && !error && data && (
          <div className="history-pagination">
            <span>
              Page {data.page + 1} of{" "}
              {Math.max(1, data.totalPages)}
            </span>

            <div className="history-pagination-actions">
              <button
                type="button"
                disabled={!data.hasPrevious}
                onClick={() => {
                  startLoad();
                  setPage((value) => Math.max(0, value - 1));
                }}
              >
                Previous
              </button>

              <button
                type="button"
                disabled={!data.hasNext}
                onClick={() => {
                  startLoad();
                  setPage((value) => value + 1);
                }}
              >
                Next
              </button>
            </div>
          </div>
        )}
      </section>
    </div>
  );
}
