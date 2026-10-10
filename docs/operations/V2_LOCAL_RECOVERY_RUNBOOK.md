# AI Business Advisor V2
## Local Recovery and Rollback Runbook

Status: DRAFT - Review required before operational use.
Reference commit: 419e4aa
Phase 9 backend revision: da6fb0b

## 1. Objective

Provide a safe procedure for verifying and recovering the
local AI Business Advisor V2 infrastructure.

This document does not authorize automatic service restarts.

## 2. Validated architecture

| Component | Name | Access |
|---|---|---|
| Primary JWT backend | ai-business-advisor-phase9-primary | 127.0.0.1:8082 |
| Secondary backend | ai-business-advisor-v2-backend-1 | 8081 |
| PostgreSQL | ai-business-advisor-v2-postgres-1 | Docker internal |
| ML API | ai-business-advisor-v2-ml-api-1 | 8000 |
| AI Advisor | ai-business-advisor-v2-ai-service-1 | 8100 |
| Frontend | Local Node/Vite process | localhost:5173 |
| Rollback backend | ai-business-advisor-phase7-check | Stopped |

Docker network: ai-business-advisor-v2_default

PostgreSQL volume: ai-business-advisor-v2_postgres_data

The frontend listens on IPv6 loopback.
Use http://localhost:5173/ for local access.

## 3. Safety requirements

- Preserve the modified ML notebook.
- Never delete the PostgreSQL volume.
- Never execute Docker Compose down with volume removal.
- Never recreate the full stack during routine recovery.
- Do not change Docker restart policies automatically.
- Do not print credentials or JWT secrets.
- Do not modify the separate Final Demo environment.
- Do not run database migrations without explicit approval.
- Do not replace the primary Phase 9 backend with Compose backend 8081.

## 4. Read-only health verification

Required checks:

- PostgreSQL container is running and healthy.
- ML API responds at http://127.0.0.1:8000/health
- AI Advisor responds at http://127.0.0.1:8100/health
- Backend responds at http://127.0.0.1:8082/actuator/health
- Frontend responds at http://localhost:5173/
- Phase 7 rollback backend remains stopped.

HTTP checks alone do not prove that business API
and authentication workflows are fully functional.

## 5. Manual recovery order

Recovery requires explicit authorization.

Before starting any component, inspect its current state.

Suggested dependency order:

1. Confirm Docker Desktop and Docker Engine availability.
2. Verify the existing PostgreSQL container and volume.
3. Start PostgreSQL only if it is stopped and approved.
4. Wait until PostgreSQL is healthy.
5. Verify the ML API and AI Advisor.
6. Start individual AI services only if necessary and approved.
7. Verify the existing Phase 9 JWT backend image.
8. Start the Phase 9 backend only if stopped and approved.
9. Verify the backend on port 8082.
10. Verify the existing Node/Vite frontend on localhost:5173.
11. Validate login, permissions and business API operations.

Do not replace these steps with a full Docker Compose rebuild.

## 6. Frontend recovery

The validated frontend runs through a local Node/Vite process.

Before any frontend recovery:

- Identify the existing Node process.
- Confirm whether port 5173 is occupied.
- Check that the frontend uses the Phase 9 API on port 8082.
- Confirm the working directory and environment.
- Avoid starting a second frontend on the same port.

The project's generic Vite fallback configuration
does not by itself prove the effective local API URL.

## 7. PostgreSQL backup

Existing Phase 9 backup:

C:\Users\user\ai-business-advisor-backups\phase9-precutover-20261009-232051-31c9f5.dump

Last observed size: 10071 bytes.

Current validation:

- File exists.
- SHA256 was calculated.
- PostgreSQL volume exists.
- PostgreSQL health is healthy.

Not yet validated:

- Successful database restoration.
- Compatibility with the current live database state.

A restore rehearsal must use an isolated environment.
Never restore into the live V2 database without approval.

## 8. Rollback policy

The existing Phase 7 backend is retained but stopped.

Phase 7 predates the corrected Phase 9 JWT security checks.
Rolling back could reintroduce authentication vulnerabilities.

Before any rollback:

1. Identify the failure affecting Phase 9.
2. Confirm that a rollback is necessary.
3. Evaluate the security risks of Phase 7.
4. Verify the image and networking configuration.
5. Check database compatibility.
6. Verify available backups.
7. Obtain explicit authorization.
8. Prepare a separate reversible procedure.

Never start Phase 7 as an automatic fallback.
Never restore the database merely to change backend images.

## 9. Final Demo isolation

Final Demo uses a separate Docker network and data stores.

Its MySQL and MongoDB containers are stopped.
Its backend has experienced repeated DNS/JDBC failures.

Do not connect Final Demo to the V2 PostgreSQL network.
Do not start or remove Final Demo components without approval.
Preserve its existing MySQL and MongoDB volumes.

## 10. Recovery acceptance checklist

- All required containers have expected states.
- PostgreSQL is healthy.
- ML API and AI Advisor are reachable.
- Phase 9 backend is healthy on port 8082.
- Frontend is reachable on localhost:5173.
- JWT authentication works.
- ADMIN endpoints reject unauthorized requests.
- ANALYST access restrictions remain effective.
- Assessment history is available.
- No unintended Docker or database changes occurred.
- The ML notebook remains unchanged.

## 11. Validation status

GitHub CI: SUCCESS.
GitHub Docker E2E: SUCCESS.
Local health checks: SUCCESS.
PostgreSQL restore rehearsal: PENDING.
Manual recovery rehearsal: PENDING.
Rollback rehearsal: PENDING.

This document is a recovery runbook,
not proof of full disaster recovery readiness.