# Оставшаяся дорожная карта backend HandyNest

Документ фиксирует оставшуюся backend-работу относительно `docs/HandyNest_TZ_for_Codex_ru_v6.md`.

## Текущее состояние

Backend содержит канонический transactional marketplace flow, auth verification, правила доступа к private files, Uzbekistan-first geo/catalog foundation, typed platform settings, append-only legal consents, trust and safety administration, transactional outbox и production CI gates.

Первый активный рынок: Узбекистан; поддерживаются все 14 регионов и 120 городов, Ташкент используется по умолчанию. Валюта новых операций: `UZS`. Публичный MVP-каталог ограничен пятью направлениями из Uzbekistan addendum. Данные Казахстана сохранены как неподдерживаемый исторический рынок.

Дальнейшая backend-работа должна определяться результатами интеграции frontend и выбором production-провайдеров, а не legacy cleanup.

## Выполненные блоки

1. Marketplace lifecycle hardening:
   - явные переходы task/offer/deal;
   - moderation и expiration;
   - атомарное принятие offer и concurrency tests;
   - idempotency критических действий.
2. Category policy и administration:
   - локализованные `ru/uz` названия и поисковые синонимы;
   - risk, launch phase, verification и payment capability flags;
   - optimistic admin updates и audit history;
   - reconciliation performer assignments.
3. Uzbekistan MVP category seed:
   - пять публичных корневых направлений;
   - стабильные slug/publicId и `ru/uz` переводы;
   - metadata категорий;
   - сохранение связанных legacy rows.
4. File storage и verification access:
   - presigned upload/download URLs;
   - typed private verification documents;
   - audited admin access;
   - approval для risk categories.
5. Admin, moderation и dispute flows:
   - evidence attachments;
   - admin resolution;
   - complaint и risk-event handling.
6. Transactional notifications/outbox:
   - worker;
   - events для ключевых marketplace actions.
7. Payment abstraction:
   - state machine;
   - webhook/idempotency foundation;
   - отсутствие хранения card data.
8. Production hardening:
   - CI quality gates;
   - dependency scan;
   - OpenAPI artifact;
   - backup/restore docs;
   - state-machine и security tests.
9. Uzbekistan-first launch market:
   - `/api/v1/market/config`;
   - все 14 регионов и 120 городов Узбекистана;
   - Ташкент как город по умолчанию и его 12 районов;
   - UZS defaults без переписывания legacy KZT;
   - controlled contact reveal, deal cancellation audit и antifraud signals.

## Следующая рекомендуемая работа

- использовать `target/openapi/openapi.json` как контракт frontend;
- продолжать `apps/web` по frontend-фазам;
- выбрать production SMS/email/payment/storage providers;
- провести staging load, backup/restore и security drills перед запуском.

## Ограничения

- не изменять старые Liquibase changesets;
- не отдавать JPA entities через API;
- сохранять `/api/v1` единственным публичным контрактом;
- выполнять изменения проверяемыми bounded slices;
- после runtime-проверок освобождать app-порты.
