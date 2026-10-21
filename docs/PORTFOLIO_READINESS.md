# Portfolio readiness — AI Business Advisor V2

This page is an operational checklist, **not** a claim that all cloud services are currently healthy.

## Evidence available in GitHub

- Default branch: `master`.
- Project CI workflow: [CI](../../actions/workflows/ci.yml).
- Docker integration test workflow: [Docker E2E](../../actions/workflows/docker-e2e.yml).
- Newer authentication/admin/history work is proposed in [PR #9](../../pull/9) (draft at the time this checklist was prepared, 2026-10-21). Do not describe these additions as released on `master` until merged.

## Before linking the project in a CV

1. Check the latest **run on `master`**, not just the latest successful run on a feature branch.
2. Open each cloud service and confirm it responds: [frontend](https://ai-business-advisor-v2-frontend-cloud.onrender.com), [backend health](https://ai-business-advisor-v2-backend-cloud.onrender.com/actuator/health), [ML API health](https://ai-business-advisor-v2-ml-api-cloud.onrender.com/health), [AI Advisor health](https://ai-business-advisor-v2-ai-service-cloud.onrender.com/health).
3. Perform one real workflow through the frontend (e.g. a documented assessment), not just HTTP health checks. Do not use actual customer data.
4. Check environment variables and secrets in your deployment dashboard without printing credentials to logs.
5. Review and validate [PR #9](../../pull/9) in a staging environment before marking it ready and merging.
6. Record verification date, exact commit SHA, commands, and observed results in an issue or a dedicated release note.

## Claims that are safe and precise

- This repository contains React frontend, Spring Boot backend, ML inference API, and AI Advisor integration.
- Its CI and Docker E2E pipelines can be consulted under **Actions**.
- Passing a CI run is evidence for a specific commit, not a guarantee of live availability.

## Local / CI verification

From the GitHub Actions page, choose the workflow and verify the **branch + commit** shown in the run details. The existing Docker E2E workflow supports a manual trigger.

No credentials, tokens, real insurance cases, or private customer data should be committed or pasted into public issues.
