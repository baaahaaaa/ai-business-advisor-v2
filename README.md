# AI Business Advisor V2

[![CI](https://github.com/baaahaaaa/ai-business-advisor-v2/actions/workflows/ci.yml/badge.svg)](https://github.com/baaahaaaa/ai-business-advisor-v2/actions/workflows/ci.yml)

AI Business Advisor V2 is an end-to-end insurance decision-support platform combining machine learning, explainability, business APIs, a web interface, and a guarded LLM-based AI Advisor.

The system is designed to support human decision-making. Machine-learning models generate quantitative indicators, while the AI Advisor explains those indicators without recalculating model scores or making autonomous insurance decisions.

---

## 1. Project Overview

The platform currently addresses three insurance-related analytical tasks:

- Claim occurrence prediction
- Claim frequency estimation
- Fraud investigation prioritization

The application follows this general pipeline:

```text
DATA
  ↓
MACHINE LEARNING MODELS
  ↓
EVALUATION
  ↓
EXPLAINABILITY
  ↓
ML INFERENCE API
  ↓
SPRING BOOT BUSINESS API
  ↓
REACT FRONTEND
  ↓
AI ADVISOR
```

The AI Advisor is an explanation layer only.

It does not replace the machine-learning models and does not modify their predictions.

---

## 2. Global Architecture

```text
                        ┌──────────────────────┐
                        │       Browser        │
                        └──────────┬───────────┘
                                   │
                                   ▼
                        ┌──────────────────────┐
                        │ React + TypeScript   │
                        │ Nginx                │
                        │ localhost:5173       │
                        └──────────┬───────────┘
                                   │
                                   ▼
                        ┌──────────────────────┐
                        │ Spring Boot Backend  │
                        │ localhost:8081       │
                        │ container:8080       │
                        └──────┬────────┬──────┘
                               │        │
                     ┌─────────┘        └─────────┐
                     ▼                            ▼
          ┌──────────────────────┐     ┌──────────────────────┐
          │ ML Inference API     │     │ AI Advisor Service   │
          │ FastAPI              │     │ FastAPI              │
          │ localhost:8000       │     │ localhost:8100       │
          └──────────┬───────────┘     └──────────┬───────────┘
                     │                            │
          ┌──────────┼───────────┐                ▼
          ▼          ▼           ▼          ┌──────────────┐
      Occurrence  Frequency    Fraud         │  OpenAI API  │
                                             └──────────────┘
```

The frontend communicates only with the Spring Boot backend.

The backend is responsible for orchestrating calls to the ML API and the AI Advisor.

---

## 3. Technology Stack

### Data Science and Machine Learning

- Python 3.13
- NumPy
- Pandas
- SciPy
- scikit-learn
- XGBoost
- Joblib
- SHAP
- FastAPI
- Uvicorn

### Backend

- Java 17
- Spring Boot 4.1.1
- Maven
- Java HttpClient
- Spring Actuator

### AI Advisor

- Python
- FastAPI
- Pydantic
- OpenAI API
- Structured Outputs

### Frontend

- React 19
- TypeScript
- Vite 8
- Nginx

### Infrastructure

- Docker
- Docker Compose
- Git
- GitHub
- Git LFS

---

## 4. Machine Learning Tasks

### 4.1 Claim Occurrence

The claim occurrence model estimates the probability that at least one claim occurs.

Model:

```text
XGBoost Classifier
+
Isotonic Probability Calibration
```

Final test metrics:

| Metric | Value |
|---|---:|
| PR-AUC | 0.142379 |
| ROC-AUC | 0.710567 |
| Brier Score | 0.045621 |
| Top-decile Lift | 3.074 |

Technical threshold:

```text
0.10327080885569255
```

Validated golden-case probability:

```text
0.4265319009621938
```

---

### 4.2 Claim Frequency

The claim frequency model estimates the expected number of claims while taking exposure into account.

Model:

```text
XGBoost Poisson
```

Final test metrics:

| Metric | Value |
|---|---:|
| Poisson Deviance | 0.3014928111 |
| MAE | 0.09629424 |
| Bias | -0.301358% |
| Lift | 3.66533 |

Validated golden-case predicted frequency:

```text
1.8306647539138794
```

For an exposure equal to:

```text
0.48
```

the expected claim count is:

```text
0.8787190818786621
```

The relationship is:

```text
expectedClaimCount = predictedFrequency × exposure
```

---

### 4.3 Fraud Detection

The fraud model is used only to prioritize claims for human investigation.

It must not be interpreted as proof that fraud occurred.

Model:

```text
Random Forest
+
Isotonic Probability Calibration
```

Final test metrics for the 1996 test population:

| Metric | Value |
|---|---:|
| PR-AUC | 0.09112695 |
| ROC-AUC | 0.67478982 |
| Brier Score | 0.05107961 |
| Lift | 1.83233 |

Investigation threshold:

```text
0.08585764735167348
```

Validated golden-case probability:

```text
0.5377003003817469
```

---

## 5. Datasets

### Claim Occurrence and Frequency

Dataset:

```text
freMTPL2freq.csv
```

Size:

```text
678,013 rows
12 columns
```

This dataset is used for:

```text
Claim occurrence
Claim frequency
```

---

### Fraud Detection

Dataset:

```text
vehicle_claim_fraud.csv
```

Size:

```text
15,420 rows
33 columns
```

Target:

```text
FraudFound_P
```

The fraud dataset is kept separate from the frequency dataset.

No artificial merge between the two datasets is performed.

---

## 6. Scientific Scope

The project currently models:

```text
Claim occurrence
Claim frequency
Fraud investigation priority
```

The project does not currently include:

```text
Claim severity
Claim amount
Pure premium
```

A severity model was intentionally not created because the selected modeling data does not provide a valid claim-amount target for that purpose.

The final test datasets are used only for final generalization evaluation.

The production models are not retuned using the final-test results.

---

## 7. Explainability

SHAP is used to analyze model behavior and feature contributions.

Explainability is separated from prediction.

SHAP explanations are used to understand the trained models, not to modify predictions during inference.

---

## 8. ML Inference API

The ML inference service is implemented with FastAPI.

Docker host URL:

```text
http://localhost:8000
```

Main endpoints:

```text
GET  /
GET  /health
GET  /runtime
GET  /models

POST /v1/predict/claim-occurrence
POST /v1/predict/claim-frequency
POST /v1/predict/fraud
```

Example health response:

```json
{
  "status": "healthy",
  "models": {
    "claim_occurrence": true,
    "claim_frequency": true,
    "fraud_detection": true
  }
}
```

The service loads the production model artifacts at application startup.

---

## 9. Spring Boot Business API

The Spring Boot backend provides the business layer between the frontend and the AI services.

Host URL with Docker:

```text
http://localhost:8081
```

Internal container port:

```text
8080
```

Main business endpoints:

```text
POST /api/risk/assessment
POST /api/fraud/assessment

POST /api/advisor/risk
POST /api/advisor/fraud
```

Inside Docker, Spring communicates with:

```text
http://ml-api:8000
http://ai-service:8100
```

---

## 10. AI Advisor

The AI Advisor is implemented as a separate FastAPI service.

Host URL:

```text
http://localhost:8100
```

Main endpoints:

```text
GET  /health
GET  /runtime

POST /v1/advisor/risk
POST /v1/advisor/fraud
```

The validated provider is:

```text
OpenAI
```

The validated model is:

```text
gpt-6-luna
```

Structured outputs are validated using Pydantic.

Response storage is disabled.

Example runtime response:

```json
{
  "provider": "openai",
  "model": "gpt-6-luna",
  "llmConfigured": true,
  "llmReady": true,
  "fallbackEnabled": true,
  "lastExecutionMode": "openai",
  "responseStorage": false
}
```

---

## 11. AI Advisor Responsibilities

For risk analysis, the Advisor receives already-computed indicators:

```text
claimProbability
claimProbabilityThreshold
technicalRiskFlag
predictedFrequency
exposure
expectedClaimCount
```

For fraud analysis, it receives:

```text
fraudProbability
investigationThreshold
investigationFlag
```

The Advisor explains these values in natural language.

The Advisor must never:

```text
Recalculate ML predictions
Modify technical thresholds
Replace deterministic business logic
Automatically accept or reject a policy
Claim that fraud has occurred
Make an autonomous underwriting decision
```

Fraud outputs are used only to prioritize human investigation.

---

## 12. Frontend

The frontend is built using:

```text
React
TypeScript
Vite
```

The production frontend is served by Nginx.

Docker URL:

```text
http://localhost:5173
```

At build time, the frontend receives:

```text
VITE_API_BASE_URL=http://localhost:8081
```

The browser communicates with the Spring Boot backend only.

---

## 13. Docker Architecture

The application contains four Docker services:

```text
frontend
backend
ml-api
ai-service
```

Service ports:

| Service | Host Port | Container Port |
|---|---:|---:|
| Frontend | 5173 | 80 |
| Spring Backend | 8081 | 8080 |
| ML API | 8000 | 8000 |
| AI Advisor | 8100 | 8100 |

---

## 14. Running the Application with Docker

Clone the repository:

```bash
git clone https://github.com/baaahaaaa/ai-business-advisor-v2.git
```

Enter the project:

```bash
cd ai-business-advisor-v2
```

Make sure Git LFS is installed:

```bash
git lfs install
git lfs pull
```

Create the local AI service environment file:

```text
ai-service/.env
```

The OpenAI API key must be configured locally.

Do not commit this file.

Start the complete application:

```bash
docker compose up -d
```

Check the containers:

```bash
docker compose ps
```

Expected services:

```text
ai-service   healthy
ml-api       healthy
backend      Up
frontend     Up
```

---

## 15. Rebuilding the Application

To rebuild all images:

```bash
docker compose up -d --build
```

To stop all services:

```bash
docker compose down
```

---

## 16. Health Checks

ML API:

```text
http://localhost:8000/health
```

AI Advisor:

```text
http://localhost:8100/health
```

AI Advisor runtime:

```text
http://localhost:8100/runtime
```

Spring Boot:

```text
http://localhost:8081/actuator/health
```

Frontend:

```text
http://localhost:5173
```

---

## 17. Integration Tests

Three PowerShell verification scripts are provided:

```text
backend/scripts/verify-ml-integration.ps1
backend/scripts/verify-business-api.ps1
backend/scripts/verify-advisor-integration.ps1
```

For the Docker deployment:

```powershell
cd backend
```

Then run:

```powershell
.\scripts\verify-ml-integration.ps1 `
    -BaseUrl "http://127.0.0.1:8081"
```

```powershell
.\scripts\verify-business-api.ps1 `
    -BaseUrl "http://127.0.0.1:8081"
```

```powershell
.\scripts\verify-advisor-integration.ps1 `
    -BaseUrl "http://127.0.0.1:8081"
```

Validated results:

```text
ALL ML INTEGRATION TESTS PASSED

ALL BUSINESS API TESTS PASSED

ALL ADVISOR INTEGRATION TESTS PASSED
```

The scripts also support:

```powershell
$env:BACKEND_BASE_URL = "http://127.0.0.1:8081"
```

The default backend URL remains:

```text
http://127.0.0.1:8080
```

for local non-Docker execution.

---

## 18. Git LFS

Large machine-learning artifacts are managed using Git LFS.

Production artifacts:

```text
claim_occurrence_xgb_isotonic.joblib
claim_frequency_xgboost_poisson.joblib
fraud_random_forest_isotonic.joblib
```

After cloning:

```bash
git lfs install
git lfs pull
```

---

## 19. Repository Structure

```text
ai-business-advisor-v2/
│
├── ai-service/
│   ├── app/
│   ├── tests/
│   ├── Dockerfile
│   └── requirements.txt
│
├── backend/
│   ├── src/
│   ├── scripts/
│   ├── Dockerfile
│   └── pom.xml
│
├── data-science/
│   ├── artifacts/
│   ├── data/
│   ├── inference_api/
│   ├── notebooks/
│   ├── reports/
│   ├── Dockerfile
│   ├── requirements-ml.txt
│   └── requirements-inference.txt
│
├── frontend/
│   ├── src/
│   ├── Dockerfile
│   └── package.json
│
├── docker-compose.yml
└── README.md
```

---

## 20. Reproducibility

Core ML package versions:

```text
numpy==2.5.3
pandas==3.0.6
scipy==1.18.1
scikit-learn==1.9.1
xgboost==3.4.1
joblib==1.6.0
```

Model artifacts are loaded at serving time.

They are not retrained when the inference API starts.

---

## 21. Security

Sensitive credentials must never be stored in:

```text
Git history
README files
Docker images
Frontend variables
Source code
```

The OpenAI API key is injected only into the AI Advisor service.

The file:

```text
ai-service/.env
```

is ignored by Git.

The frontend never receives the OpenAI API key.

---

## 22. Version History

### v0.1.0-integration-baseline

```text
ML inference
Spring business APIs
Frontend integration
```

### v0.2.0-real-llm

```text
Guarded OpenAI AI Advisor
Structured Outputs
Risk and fraud explanations
```

### v0.3.0-docker-compose

```text
ML Docker image
AI Advisor Docker image
Spring Docker image
Frontend/Nginx Docker image
Docker Compose orchestration
End-to-end Docker validation
```

---

## 23. Current Status

```text
Frontend       Operational
Backend        Operational
ML API         Healthy
AI Advisor     Healthy
OpenAI         Validated
Docker Compose Validated
```

The complete application has been tested end-to-end.

---

## 24. Repository

GitHub:

```text
https://github.com/baaahaaaa/ai-business-advisor-v2
```

---

## 25. Disclaimer

AI Business Advisor V2 is a decision-support application.

Machine-learning predictions and AI-generated explanations are intended to assist qualified human reviewers.

They must not be interpreted as autonomous underwriting, fraud, claims, or insurance decisions.
