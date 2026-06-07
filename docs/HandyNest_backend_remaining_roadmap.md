# HandyNest Backend Remaining Roadmap

This checklist tracks the remaining backend work needed to converge the current codebase to
`docs/HandyNest_TZ_for_Codex_ru_v6.md`.

## Current Focus

The backend already contains most canonical MVP foundations. Legacy CRUD controllers are removed
or blocked from runtime, and the Java source is converging on feature-owned `com.handynest`
packages without changing database tables unnecessarily.

## Slices

1. Harden canonical `MarketplaceTask` lifecycle:
   - status transitions;
   - moderation/open flow;
   - auto-expire jobs;
   - search/filter behavior.
2. Harden `TaskOffer -> Deal -> Chat`:
   - optimistic locking;
   - concurrency tests;
   - idempotency checks for critical POST actions.
3. Complete file storage flow:
   - presigned upload/download URLs;
   - bucket and visibility rules;
   - private verification document access.
4. Complete admin/moderation/dispute flows:
   - evidence attachments;
   - admin resolution;
   - complaint/risk-event handling.
5. Complete notification/outbox processing:
   - worker;
   - event mapping for key marketplace actions.
6. Complete payment abstraction:
   - state machine;
   - webhook/idempotency skeleton;
   - no card data storage.
7. Finish production hardening:
   - CI quality gates;
   - dependency scan;
   - OpenAPI artifact generation;
   - backup/restore documentation;
   - expanded state-machine and security tests.

## Guardrails

- Do not modify old Liquibase changesets.
- Do not expose JPA entities through API.
- Keep `/api/v1` as the only public contract.
- Prefer small, verifiable slices over a large rewrite.
- Free project ports after smoke or local runtime checks.
