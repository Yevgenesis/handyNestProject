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

The current code still contains legacy CRUD modules under `codezilla.handynestproject`. New development should move toward `/api/v1` and the package-by-feature architecture described in the specification.

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

4. Run the backend:

```bash
make run
```

5. Stop processes after checks:

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
