import { useState } from "react";
import "../App.css";
import { useAuth } from "../auth/useAuth";

import {
  assessInsuranceRisk,
  type InsuranceRiskAssessmentResponse,
  type InsuranceRiskRequest,
} from "../api/riskApi";

import {
  explainRisk,
  type AdvisorResponse,
} from "../api/advisorApi";


const initialForm: InsuranceRiskRequest = {
  exposure: 1.0,
  vehiclePower: 7,
  vehicleAge: 6,
  driverAge: 79,
  bonusMalus: 62,
  density: 399,
  area: "C",
  vehicleBrand: "B1",
  vehicleGas: "Regular",
  region: "R24",
};


function RiskAssessmentPage() {

  const { accessToken } = useAuth();

  const [form, setForm] =
    useState<InsuranceRiskRequest>(initialForm);

  const [result, setResult] =
    useState<InsuranceRiskAssessmentResponse | null>(null);

  const [loading, setLoading] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);


  const [advisorResult, setAdvisorResult] =
    useState<AdvisorResponse | null>(null);

  const [advisorLoading, setAdvisorLoading] =
    useState(false);

  const [advisorError, setAdvisorError] =
    useState<string | null>(null);


  function updateNumber(
    field: keyof InsuranceRiskRequest,
    value: string
  ) {

    setForm((current) => ({
      ...current,
      [field]: Number(value),
    }));
  }


  function updateText(
    field: keyof InsuranceRiskRequest,
    value: string
  ) {

    setForm((current) => ({
      ...current,
      [field]: value,
    }));
  }


  async function handleSubmit(
    event: React.FormEvent<HTMLFormElement>
  ) {

    event.preventDefault();

    setLoading(true);
    setError(null);
    setResult(null);

    setAdvisorResult(null);
    setAdvisorError(null);

    try {

      if (!accessToken) {
        throw new Error("Session expired. Sign in again.");
      }

      const response =
        await assessInsuranceRisk(form, accessToken);

      setResult(response);

    } catch (exception) {

      if (exception instanceof Error) {
        setError(exception.message);
      } else {
        setError(
          "An unexpected error occurred."
        );
      }

    } finally {

      setLoading(false);
    }
  }


  async function handleAdvisor() {

    if (!result) {
      return;
    }

    setAdvisorLoading(true);
    setAdvisorError(null);
    setAdvisorResult(null);

    try {

      if (!accessToken) {
        throw new Error("Session expired. Sign in again.");
      }

      const response =
        await explainRisk({
          claimProbability:
            result.claimProbability,

          claimProbabilityThreshold:
            result.claimProbabilityThreshold,

          technicalRiskFlag:
            result.technicalRiskFlag,

          predictedFrequency:
            result.predictedFrequency,

          exposure:
            result.exposure,

          expectedClaimCount:
            result.expectedClaimCount,
        }, accessToken);

      setAdvisorResult(response);

    } catch (exception) {

      if (exception instanceof Error) {
        setAdvisorError(exception.message);
      } else {
        setAdvisorError(
          "Unable to generate AI interpretation."
        );
      }

    } finally {

      setAdvisorLoading(false);
    }
  }


  function resetForm() {

    setForm(initialForm);

    setResult(null);
    setError(null);

    setAdvisorResult(null);
    setAdvisorError(null);
  }


  return (
    <div className="app-shell risk-assessment-page">

      <header className="topbar">

        <div>

          <p className="eyebrow">
            AI BUSINESS ADVISOR
          </p>

          <h1>
            Insurance Risk Assessment
          </h1>

          <p className="subtitle">
            Decision-support interface powered by the
            claim occurrence and claim frequency models.
          </p>

        </div>


        <div className="status-badge">
          ML Service
          <span className="status-dot" />
        </div>

      </header>


      <main className="main-grid">

        <section className="panel">

          <div className="panel-heading">

            <div>

              <p className="section-label">
                INPUT
              </p>

              <h2>
                Policy & vehicle profile
              </h2>

            </div>


            <button
              type="button"
              className="secondary-button"
              onClick={resetForm}
            >
              Reset
            </button>

          </div>


          <form
            className="risk-form"
            onSubmit={handleSubmit}
          >

            <div className="form-grid">

              <label>
                Exposure

                <input
                  type="number"
                  min="0.01"
                  max="1"
                  step="0.01"
                  value={form.exposure}
                  onChange={(event) =>
                    updateNumber(
                      "exposure",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Vehicle power

                <input
                  type="number"
                  min="1"
                  value={form.vehiclePower}
                  onChange={(event) =>
                    updateNumber(
                      "vehiclePower",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Vehicle age

                <input
                  type="number"
                  min="0"
                  value={form.vehicleAge}
                  onChange={(event) =>
                    updateNumber(
                      "vehicleAge",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Driver age

                <input
                  type="number"
                  min="18"
                  value={form.driverAge}
                  onChange={(event) =>
                    updateNumber(
                      "driverAge",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Bonus-Malus

                <input
                  type="number"
                  min="0"
                  value={form.bonusMalus}
                  onChange={(event) =>
                    updateNumber(
                      "bonusMalus",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Population density

                <input
                  type="number"
                  min="0"
                  value={form.density}
                  onChange={(event) =>
                    updateNumber(
                      "density",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Area

                <select
                  value={form.area}
                  onChange={(event) =>
                    updateText(
                      "area",
                      event.target.value
                    )
                  }
                >
                  <option value="A">A</option>
                  <option value="B">B</option>
                  <option value="C">C</option>
                  <option value="D">D</option>
                  <option value="E">E</option>
                  <option value="F">F</option>
                </select>
              </label>


              <label>
                Vehicle brand

                <input
                  value={form.vehicleBrand}
                  onChange={(event) =>
                    updateText(
                      "vehicleBrand",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Fuel type

                <select
                  value={form.vehicleGas}
                  onChange={(event) =>
                    updateText(
                      "vehicleGas",
                      event.target.value
                    )
                  }
                >
                  <option value="Regular">
                    Regular
                  </option>

                  <option value="Diesel">
                    Diesel
                  </option>
                </select>
              </label>


              <label>
                Region

                <input
                  value={form.region}
                  onChange={(event) =>
                    updateText(
                      "region",
                      event.target.value
                    )
                  }
                  required
                />
              </label>

            </div>


            <button
              type="submit"
              className="primary-button"
              disabled={loading}
            >
              {
                loading
                  ? "Running models..."
                  : "Run risk assessment"
              }
            </button>

          </form>


          {
            error && (
              <div className="error-box">
                <strong>
                  Assessment failed
                </strong>

                <span>
                  {error}
                </span>
              </div>
            )
          }

        </section>


        <section className="panel results-panel">

          <div className="panel-heading">

            <div>

              <p className="section-label">
                MODEL OUTPUT
              </p>

              <h2>
                Risk indicators
              </h2>

            </div>

          </div>


          {
            !result && !loading && (
              <div className="empty-state">

                <div className="empty-icon">
                  AI
                </div>

                <h3>
                  No assessment yet
                </h3>

                <p>
                  Complete the profile and run the
                  assessment to obtain model outputs.
                </p>

              </div>
            )
          }


          {
            loading && (
              <div className="empty-state">

                <div className="loader" />

                <h3>
                  Evaluating risk
                </h3>

                <p>
                  Requesting predictions from the ML
                  inference service.
                </p>

              </div>
            )
          }


          {
            result && (
              <div className="results-content">

                <article className="metric-card primary-metric">

                  <p>
                    Claim probability
                  </p>

                  <strong>
                    {
                      (
                        result.claimProbability *
                        100
                      ).toFixed(2)
                    }%
                  </strong>

                  <span>
                    Calibrated probability of at least
                    one claim.
                  </span>

                </article>


                <div className="metric-grid">

                  <article className="metric-card">

                    <p>
                      Expected frequency
                    </p>

                    <strong>
                      {
                        result.predictedFrequency
                          .toFixed(4)
                      }
                    </strong>

                    <span>
                      Expected annual claim frequency.
                    </span>

                  </article>


                  <article className="metric-card">

                    <p>
                      Expected claims
                    </p>

                    <strong>
                      {
                        result.expectedClaimCount
                          .toFixed(4)
                      }
                    </strong>

                    <span>
                      Frequency adjusted by exposure.
                    </span>

                  </article>

                </div>


                <article
                  className={
                    result.technicalRiskFlag
                      ? "decision-card attention"
                      : "decision-card normal"
                  }
                >

                  <div>

                    <p>
                      Technical risk indicator
                    </p>

                    <strong>
                      {
                        result.technicalRiskFlag
                          ? "Above technical threshold"
                          : "Below technical threshold"
                      }
                    </strong>

                  </div>


                  <span className="decision-value">
                    {
                      (
                        result
                          .claimProbabilityThreshold *
                        100
                      ).toFixed(2)
                    }%
                  </span>

                </article>


                <div className="model-note">

                  <strong>
                    Interpretation
                  </strong>

                  <p>
                    These values are model-based
                    decision-support indicators.
                    They are not an automatic
                    underwriting decision.
                  </p>

                </div>


                <div className="advisor-section">

                  <div className="advisor-heading">

                    <div>

                      <p className="section-label">
                        AI ADVISOR
                      </p>

                      <h3>
                        Business interpretation
                      </h3>

                    </div>

                  </div>


                  {
                    !advisorResult && (
                      <button
                        type="button"
                        className="advisor-button"
                        onClick={handleAdvisor}
                        disabled={advisorLoading}
                      >
                        {
                          advisorLoading
                            ? "Generating interpretation..."
                            : "Generate AI interpretation"
                        }
                      </button>
                    )
                  }


                  {
                    advisorError && (
                      <div className="advisor-error">

                        <strong>
                          Advisor unavailable
                        </strong>

                        <p>
                          {advisorError}
                        </p>

                        <span>
                          ML predictions remain valid
                          and available above.
                        </span>

                      </div>
                    )
                  }


                  {
                    advisorResult && (
                      <article className="advisor-card">

                        <h3>
                          {advisorResult.title}
                        </h3>

                        <p className="advisor-summary">
                          {advisorResult.summary}
                        </p>


                        <ul>
                          {
                            advisorResult.keyPoints.map(
                              (point) => (
                                <li key={point}>
                                  {point}
                                </li>
                              )
                            )
                          }
                        </ul>


                        <div className="advisor-disclaimer">

                          <strong>
                            Decision-support notice
                          </strong>

                          <p>
                            {advisorResult.disclaimer}
                          </p>

                        </div>

                      </article>
                    )
                  }

                </div>

              </div>
            )
          }

        </section>

      </main>

    </div>
  );
}


export default RiskAssessmentPage;
