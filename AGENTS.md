# AGENTS.md — HandyNest Fullstack

This file contains short persistent instructions for Codex.

Full specifications:

```text
docs/HandyNest_TZ_for_Codex_ru_v6.md
docs/HandyNest_Frontend_TZ_for_Codex_ru.md
```

Backend specification is the source of truth for business logic and API.
Frontend specification is the source of truth for web UI structure and frontend stack.

---

## 1. Project

HandyNest is a Kazakhstan-focused service marketplace.

Core flow:

```text
Task -> TaskOffer -> Deal -> Chat -> Work Submission -> Acceptance / Revision / Dispute -> Feedback
```

Do not implement it as a simple classifieds board.

---

## 2. Backend stack

```text
Java 21
Spring Boot 3.5.x
Maven
Spring Security
JWT + refresh token
Spring Data JPA / Hibernate
PostgreSQL
Liquibase
MinIO / S3-compatible storage
MapStruct
Lombok
Bean Validation
SpringDoc OpenAPI
JUnit 5
Testcontainers
Docker Compose
```

---

## 3. Frontend stack

```text
TypeScript
React
Next.js App Router
Tailwind CSS
shadcn/ui
TanStack Query
React Hook Form
Zod
next-intl or equivalent i18n
Vitest
Playwright
ESLint
Prettier
```

Recommended location:

```text
apps/web
```

---

## 4. Architecture rules

Backend:

- modular monolith;
- no business logic in controllers;
- DTOs for API;
- do not expose JPA entities;
- Liquibase for DB changes;
- no `@Data` on JPA entities;
- ownership checks in service layer.

Frontend:

- mobile-first;
- feature-oriented structure;
- no business rules duplicated as authority;
- backend remains source of truth for permissions;
- use TanStack Query for server state;
- use React Hook Form + Zod for forms;
- use env for API URL.

---

## 5. Security

Never:

- hardcode secrets;
- log JWT/refresh tokens/OTP/passwords/documents;
- use global `permitAll`;
- expose private contacts before allowed deal event;
- expose verification documents publicly;
- trust frontend-only role checks.

---

## 6. Commands

Backend:

```bash
./mvnw clean verify
# or
mvn clean verify
```

Frontend:

```bash
pnpm lint
pnpm test
pnpm build
```

If pnpm is not used, follow the existing package manager in the repo.

---

## 7. Workflow

Before editing:

1. Read this file.
2. Read relevant backend/frontend specification.
3. Inspect current code.
4. Make a short plan for non-trivial changes.

After editing, report:

```text
Summary
Files changed
Database migrations
Tests added/updated
Commands run
Test results
Security considerations
Known limitations
Next recommended step
```

Do not claim tests passed if they were not run.

---

## 8. Do not do

Do not:

- implement all phases at once;
- add unapproved technologies;
- modify old Liquibase changesets;
- weaken security to pass tests;
- duplicate the full TЗ in AGENTS.md;
- create one giant PR for backend and frontend together unless explicitly requested.
