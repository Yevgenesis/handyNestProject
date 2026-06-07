# HandyNest Backend

HandyNest is a Kazakhstan-focused transactional marketplace for services.

The target flow from the approved backend specification is:

```text
Task -> TaskOffer -> Deal -> Chat -> Work Submission -> Acceptance / Revision / Dispute -> Feedback
```

The backend specification in `docs/HandyNest_TZ_for_Codex_ru_v6.md` is the source of truth for business logic and API.

## Current Foundation

- Java 21
- Spring Boot 3.5.x
- Maven
- PostgreSQL
- Liquibase
- Spring Security + JWT
- Testcontainers
- Docker Compose for local PostgreSQL and MinIO

The public backend contract is `/api/v1`. Legacy CRUD controllers are removed or blocked from runtime, and new development should stay within the package-by-feature `com.handynest` architecture described in the specification.

## Local Setup

1. Copy the example environment:

```bash
cp .env.example .env
```

2. Edit `.env` and replace all `change-me-*` values.

3. Start local infrastructure:

```bash
make up
```

`make up` creates `.env` from `.env.example` if it is missing. If you run Docker Compose directly, pass the same env file:

```bash
docker compose --env-file .env -f Docker/postgres.yml up -d
```

4. Run the backend on the default Spring port:

```bash
make run
```

If port `8080` is already used by another local project, run HandyNest on `18080`:

```bash
make dev-run
```

5. Run a local smoke check with automatic cleanup:

```bash
make smoke
```

The smoke check starts PostgreSQL and MinIO, runs the backend on `18080`, verifies public catalog/geo endpoints and auth/profile endpoints, then stops the HandyNest app and Docker project services.

6. Stop processes after manual checks:

```bash
make stop
```

## Useful Commands

```bash
make up        # start PostgreSQL and MinIO
make stop      # stop containers without deleting data
make down      # stop and remove containers
make db-reset  # remove volumes and recreate local infrastructure
make logs      # follow Docker Compose logs
make run       # load .env and run Spring Boot
make dev-run   # load .env and run Spring Boot on port 18080
make smoke     # run Docker-backed local smoke check and cleanup
make ports     # show listeners on project ports
make free-project-ports # stop project Docker services and show remaining listeners
make test      # run tests with test profile
make verify    # clean verify with test profile
```

## Tests

```bash
mvn -B test
mvn -B clean verify
```

Maven config activates the `test` profile for Surefire so tests use Testcontainers instead of the local PostgreSQL instance.

## Configuration

Runtime configuration lives in `src/main/resources/application.yml`.

Local secrets must come from `.env` or environment variables:

- `POSTGRES_PASSWORD`
- `SPRING_DATASOURCE_PASSWORD`
- `HANDYNEST_JWT_SIGNING_KEY`
- `MINIO_ROOT_USER`
- `MINIO_ROOT_PASSWORD`

Do not commit real secrets.

## Operations

Actuator health is available at:

```bash
curl http://localhost:8080/actuator/health
```

Only `health` and `info` are exposed over HTTP. Security allows anonymous access to `/actuator/health`; private application endpoints remain authenticated by default.

Every HTTP response includes:

- `X-Request-Id`
- `X-Correlation-Id`

If the client sends either header, HandyNest propagates it. Otherwise the backend generates a request id and uses it as the correlation id. Both values are also placed into the logging MDC.

Idempotency keys are retained for `HANDYNEST_IDEMPOTENCY_REQUEST_TTL` and cleaned by a scheduled worker every `HANDYNEST_IDEMPOTENCY_CLEANUP_INTERVAL`. Defaults are `24h` and `1h`.

## API Contract

The public backend contract is `/api/v1/**`. Legacy CRUD endpoints under paths such as `/users`, `/tasks` or `/feedbacks` are not part of the future frontend contract and are excluded from OpenAPI.

Legacy root endpoints are permanently blocked and return `410 Gone` with `ApiErrorResponse.code=LEGACY_API_DISABLED`. The old REST/WebSocket controllers, DTOs, mappers, service layer and legacy test fixtures have been removed; keep new behavior under `/api/v1`.

OpenAPI is available at:

```bash
curl http://localhost:8080/v3/api-docs
```

Swagger UI is available at:

```text
http://localhost:8080/swagger-ui-custom.html
```

Contract rules enforced by tests:

- all documented application paths start with `/api/v1/`;
- protected operations use the `bearerAuth` JWT security scheme;
- public catalog, geo, auth and payment webhook operations do not require bearer auth;
- critical retry-safe POST operations document required `Idempotency-Key`;
- standard error responses use `ApiErrorResponse`;
- key frontend DTO schemas do not expose numeric database ids.

Account deletion uses a two-step lifecycle:

- `DELETE /api/v1/users/me` soft-deletes the current account, revokes active refresh tokens, clears direct profile PII such as phone, geo preferences and avatar references, and clears the refresh cookie.
- The scheduled data-retention worker later anonymizes deleted user email/password after `deletedUserAnonymizationDays` from the active `data_retention_policy`.

Rejected verification documents are soft-deleted after `verificationDocumentRetentionDays`. Chat, deal, dispute and payment history are retained for audit and dispute handling; physical object-storage deletion is intentionally deferred to a dedicated storage lifecycle worker.

Local backup/restore notes:

```bash
# backup
docker compose --env-file .env -f Docker/postgres.yml exec -T base-project-pg \
  sh -c 'pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB"' > handynest-backup.sql

# restore into a reset local DB
make db-reset
docker compose --env-file .env -f Docker/postgres.yml exec -T base-project-pg \
  sh -c 'psql -U "$POSTGRES_USER" "$POSTGRES_DB"' < handynest-backup.sql
```

## File Storage

HandyNest stores file metadata in PostgreSQL and file bytes in S3-compatible object storage. Local/dev uses MinIO.

The upload flow is intentionally direct-to-storage:

1. The client asks backend for an upload URL.
2. Backend checks authentication, ownership, file type, size and visibility.
3. Backend creates an `Attachment` metadata row with `bucket + storageKey`.
4. Backend returns a short-lived presigned `PUT` URL.
5. The client uploads bytes directly to MinIO/S3 using that URL.

This keeps large file traffic out of business controllers and lets the backend remain the source of truth for access rules.

Configured buckets:

- `HANDYNEST_STORAGE_PUBLIC_BUCKET` for future public assets.
- `HANDYNEST_STORAGE_PARTICIPANTS_BUCKET` for chat files and dispute evidence.
- `HANDYNEST_STORAGE_VERIFICATION_BUCKET` for private verification documents.

Verification documents are `ADMIN_ONLY` attachments. They get upload URLs, but ordinary users do not receive download URLs for them. Performers submit them through `/api/v1/performers/me/verification-requests`; admins can list and approve/reject requests through `/api/v1/admin/verification-requests`.

## Moderation

Authenticated users can create complaints with `/api/v1/complaints`. Each complaint opens a `ModerationCase` for the same target so admins have a single review queue at `/api/v1/admin/moderation-cases`. Moderation targets use public ids and the backend validates that the selected target exists before creating a case.
