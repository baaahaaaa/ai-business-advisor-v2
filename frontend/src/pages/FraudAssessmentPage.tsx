import { useState } from "react";
import "../App.css";

import {
  assessFraud,
  type FraudAssessmentRequest,
  type FraudAssessmentResponse,
} from "../api/fraudApi";

import {
  explainFraud,
  type AdvisorResponse,
} from "../api/advisorApi";


const initialForm: FraudAssessmentRequest = {
  age: null,
  deductible: 400,
  weekOfMonth: 1,
  weekOfMonthClaimed: 1,
  driverRating: 3,
  month: "Jan",
  dayOfWeek: "Saturday",
  make: "Honda",
  accidentArea: "Rural",
  dayOfWeekClaimed: "Tuesday",
  monthClaimed: "Jan",
  sex: "Male",
  maritalStatus: "Single",
  vehicleCategory: "Sedan",
  vehiclePrice: "more than 69000",
  pastNumberOfClaims: "none",
  ageOfVehicle: "new",
  ageOfPolicyHolder: "16 to 17",
  agentType: "External",
  numberOfCars: "1 vehicle",
  basePolicy: "All Perils",
};


function FraudAssessmentPage() {

  const [form, setForm] =
    useState<FraudAssessmentRequest>(initialForm);

  const [result, setResult] =
    useState<FraudAssessmentResponse | null>(null);

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


  function updateText(
    field: keyof FraudAssessmentRequest,
    value: string
  ) {

    setForm((current) => ({
      ...current,
      [field]: value,
    }));
  }


  function updateNumber(
    field: keyof FraudAssessmentRequest,
    value: string
  ) {

    setForm((current) => ({
      ...current,
      [field]: Number(value),
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

      const response =
        await assessFraud(form);

      setResult(response);

    } catch (exception) {

      if (exception instanceof Error) {
        setError(exception.message);
      } else {
        setError("An unexpected error occurred.");
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

      const response =
        await explainFraud({
          fraudProbability:
            result.fraudProbability,

          investigationThreshold:
            result.investigationThreshold,

          investigationFlag:
            result.investigationFlag,
        });

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
    <div className="app-shell">

      <header className="topbar">

        <div>

          <p className="eyebrow">
            AI BUSINESS ADVISOR
          </p>

          <h1>
            Fraud Investigation Assessment
          </h1>

          <p className="subtitle">
            Claim prioritization interface powered by
            the calibrated fraud detection model.
          </p>

        </div>


        <div className="status-badge">
          Fraud Model
          <span className="status-dot" />
        </div>

      </header>


      <main className="main-grid">

        <section className="panel">

          <div className="panel-heading">

            <div>

              <p className="section-label">
                CLAIM INPUT
              </p>

              <h2>
                Claim & policy profile
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
                Age

                <input
                  type="number"
                  min="0"
                  placeholder="Unknown"
                  value={form.age ?? ""}
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      age:
                        event.target.value === ""
                          ? null
                          : Number(event.target.value),
                    }))
                  }
                />
              </label>


              <label>
                Deductible

                <input
                  type="number"
                  min="0"
                  value={form.deductible}
                  onChange={(event) =>
                    updateNumber(
                      "deductible",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Week of month

                <input
                  type="number"
                  min="1"
                  max="5"
                  value={form.weekOfMonth}
                  onChange={(event) =>
                    updateNumber(
                      "weekOfMonth",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Week claimed

                <input
                  type="number"
                  min="1"
                  max="5"
                  value={form.weekOfMonthClaimed}
                  onChange={(event) =>
                    updateNumber(
                      "weekOfMonthClaimed",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Driver rating

                <input
                  type="number"
                  min="1"
                  value={form.driverRating}
                  onChange={(event) =>
                    updateNumber(
                      "driverRating",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Accident month

                <input
                  value={form.month}
                  onChange={(event) =>
                    updateText(
                      "month",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Accident day

                <input
                  value={form.dayOfWeek}
                  onChange={(event) =>
                    updateText(
                      "dayOfWeek",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Vehicle make

                <input
                  value={form.make}
                  onChange={(event) =>
                    updateText(
                      "make",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Accident area

                <input
                  value={form.accidentArea}
                  onChange={(event) =>
                    updateText(
                      "accidentArea",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Claim day

                <input
                  value={
                    form.dayOfWeekClaimed ?? ""
                  }
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      dayOfWeekClaimed:
                        event.target.value || null,
                    }))
                  }
                />
              </label>


              <label>
                Claim month

                <input
                  value={
                    form.monthClaimed ?? ""
                  }
                  onChange={(event) =>
                    setForm((current) => ({
                      ...current,
                      monthClaimed:
                        event.target.value || null,
                    }))
                  }
                />
              </label>


              <label>
                Sex

                <input
                  value={form.sex}
                  onChange={(event) =>
                    updateText(
                      "sex",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Marital status

                <input
                  value={form.maritalStatus}
                  onChange={(event) =>
                    updateText(
                      "maritalStatus",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Vehicle category

                <input
                  value={form.vehicleCategory}
                  onChange={(event) =>
                    updateText(
                      "vehicleCategory",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Vehicle price

                <input
                  value={form.vehiclePrice}
                  onChange={(event) =>
                    updateText(
                      "vehiclePrice",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Past claims

                <input
                  value={form.pastNumberOfClaims}
                  onChange={(event) =>
                    updateText(
                      "pastNumberOfClaims",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Vehicle age category

                <input
                  value={form.ageOfVehicle}
                  onChange={(event) =>
                    updateText(
                      "ageOfVehicle",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Policy holder age

                <input
                  value={form.ageOfPolicyHolder}
                  onChange={(event) =>
                    updateText(
                      "ageOfPolicyHolder",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Agent type

                <input
                  value={form.agentType}
                  onChange={(event) =>
                    updateText(
                      "agentType",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Number of cars

                <input
                  value={form.numberOfCars}
                  onChange={(event) =>
                    updateText(
                      "numberOfCars",
                      event.target.value
                    )
                  }
                  required
                />
              </label>


              <label>
                Base policy

                <input
                  value={form.basePolicy}
                  onChange={(event) =>
                    updateText(
                      "basePolicy",
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
                  ? "Running fraud model..."
                  : "Run fraud assessment"
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
                Investigation indicators
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
                  Run the fraud model to obtain
                  investigation-priority indicators.
                </p>

              </div>
            )
          }


          {
            loading && (
              <div className="empty-state">

                <div className="loader" />

                <h3>
                  Evaluating claim
                </h3>

                <p>
                  The calibrated fraud model is
                  processing the claim profile.
                </p>

              </div>
            )
          }


          {
            result && (
              <div className="results-content">

                <article className="metric-card primary-metric">

                  <p>
                    Fraud probability
                  </p>

                  <strong>
                    {
                      (
                        result.fraudProbability *
                        100
                      ).toFixed(2)
                    }%
                  </strong>

                  <span>
                    Calibrated model probability used
                    for investigation prioritization.
                  </span>

                </article>


                <article
                  className={
                    result.investigationFlag
                      ? "decision-card attention"
                      : "decision-card normal"
                  }
                >

                  <div>

                    <p>
                      Investigation priority
                    </p>

                    <strong>
                      {
                        result.investigationFlag
                          ? "Review recommended"
                          : "Below investigation threshold"
                      }
                    </strong>

                  </div>


                  <span className="decision-value">
                    {
                      (
                        result
                          .investigationThreshold *
                        100
                      ).toFixed(2)
                    }%
                  </span>

                </article>


                <div className="model-note">

                  <strong>
                    Important interpretation
                  </strong>

                  <p>
                    This score is intended to prioritize
                    claims for human investigation.
                    It does not establish that fraud
                    occurred and must not be used as an
                    automatic fraud accusation.
                  </p>

                </div>


                <div className="advisor-section">

                  <div className="advisor-heading">

                    <div>

                      <p className="section-label">
                        AI ADVISOR
                      </p>

                      <h3>
                        Investigation interpretation
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
                          Fraud model results remain
                          available above.
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
                            Investigation notice
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


export default FraudAssessmentPage;
