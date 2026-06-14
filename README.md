# HandyNest

HandyNest — transactional marketplace услуг с первым рынком в Узбекистане. Доступны все города страны, Ташкент выбран по умолчанию.

Основной процесс платформы:

```text
Task -> TaskOffer -> Deal -> Chat -> Work Submission -> Acceptance / Revision / Dispute -> Feedback
```

Backend-ТЗ в `docs/HandyNest_TZ_for_Codex_ru_v6.md` является главным источником бизнес-логики и API. Дополнение `docs/HandyNest_Uzbekistan_Market_TZ_Addendum_ru.md` имеет приоритет для рынка, geo, валюты, локализации и открытия контактов. Frontend-ТЗ находится в `docs/HandyNest_Frontend_TZ_for_Codex_ru.md`.

Текущие market defaults: `UZ`, Ташкент, `UZS`, активные языки `ru/uz`. В публичном geo-справочнике доступны 14 регионов и 120 городов Узбекистана. Казахстанские данные сохранены для истории и multi-country архитектуры, но исключены из публичной витрины.

## Структура проекта

- backend: Java 21, Spring Boot 3.5, PostgreSQL, Liquibase, MinIO, JWT;
- frontend: `apps/web`, Next.js 16, React 19, TypeScript, Tailwind CSS, TanStack Query;
- публичный API: `/api/v1`;
- OpenAPI: `/v3/api-docs`;
- Swagger UI: `/swagger-ui-custom.html`.

## Требования для локального запуска

Установите:

- Java 21;
- Docker Desktop;
- Node.js 22 или новее;
- Corepack, входящий в Node.js;
- Maven или используйте `./mvnw` из проекта.

Проверьте версии:

```bash
java -version
docker --version
node --version
corepack --version
```

## Первый запуск платформы

### 1. Подготовьте переменные окружения

Из корня проекта:

```bash
cp .env.example .env
```

Для локальной разработки допустимо оставить dev-значения из примера. Реальные production-секреты нельзя коммитить.

Обязательные переменные:

- `POSTGRES_PASSWORD`;
- `SPRING_DATASOURCE_PASSWORD`;
- `HANDYNEST_JWT_SIGNING_KEY`;
- `MINIO_ROOT_USER`;
- `MINIO_ROOT_PASSWORD`.

Для frontend также задаётся `HANDYNEST_WEB_STORAGE_ORIGINS`: это список разрешённых CSP origins для presigned загрузки и просмотра файлов. Локальное значение уже есть в `.env.example`; в production разрешайте только фактические HTTPS origins object storage/CDN.

### 2. Установите frontend-зависимости

```bash
corepack enable
corepack pnpm install
```

### 3. Поднимите PostgreSQL и MinIO

```bash
make up
```

Если нужно вызвать Docker Compose напрямую:

```bash
docker compose --env-file .env -f Docker/postgres.yml up -d
```

### 4. Примените миграции и dev-данные

```bash
make dev-seed
```

Команда запускает backend только на время применения Liquibase и dev seed, затем останавливает Spring Boot. PostgreSQL и MinIO остаются запущенными.

Тестовые локальные аккаунты:

```text
customer@handynest.dev / Test121314#
performer@handynest.dev / Test121314#
admin@handynest.dev / Test121314#
```

### 5. Запустите backend

В первом терминале:

```bash
make run
```

Backend будет доступен по адресу `http://localhost:8080`.

Если порт `8080` занят:

```bash
make dev-run
```

В этом режиме backend работает на `http://localhost:18080`. Для frontend тогда задайте `HANDYNEST_BACKEND_URL=http://localhost:18080`.

### 6. Запустите frontend

Во втором терминале:

```bash
make web-dev
```

Откройте:

```text
http://localhost:3000
```

Browser обращается к относительным `/api/v1/**`. Next.js proxy перенаправляет запросы на `HANDYNEST_BACKEND_URL`, по умолчанию `http://localhost:8080`.

## Обычный повторный запуск

После первого запуска достаточно выполнить:

```bash
make up
```

Затем в двух отдельных терминалах:

```bash
make run
```

```bash
make web-dev
```

## Проверка состояния

Backend health:

```bash
curl http://localhost:8080/actuator/health
```

Контейнеры:

```bash
docker compose --env-file .env -f Docker/postgres.yml ps
```

Порты проекта:

```bash
make ports
```

Локальные адреса:

- frontend: `http://localhost:3000`;
- backend: `http://localhost:8080`;
- Swagger UI: `http://localhost:8080/swagger-ui-custom.html`;
- PostgreSQL: `localhost:5432`;
- MinIO API: `http://localhost:9000`;
- MinIO Console: `http://localhost:9001`.

## Проверки backend

```bash
make test
make verify
make marketplace-smoke
```

`make verify` запускает тесты, Spotless, JaCoCo и генерацию `target/openapi/openapi.json`.

`make marketplace-smoke` проверяет основной HTTP-процесс marketplace и после проверки останавливает только Spring Boot, оставляя PostgreSQL и MinIO запущенными.

## Проверки frontend

```bash
make web-format
make web-check
make web-e2e
```

`make web-check` выполняет Prettier check, ESLint, TypeScript, Vitest и production build.

`make web-e2e` запускает backend на `18080`, frontend на `3000`, выполняет Playwright-сценарии и освобождает app-порты. PostgreSQL и MinIO остаются запущенными.

## OpenAPI и generated types

После изменения backend-контракта:

```bash
./mvnw -B test -Dtest=OpenApiArtifactTest
corepack pnpm --filter @handynest/web api:generate
```

Generated TypeScript types находятся в `apps/web/src/shared/api/generated/schema.ts`.

## Остановка

Остановить только PostgreSQL и MinIO без удаления данных:

```bash
make stop
```

Остановить и удалить контейнеры проекта:

```bash
make down
```

Удалить volumes и создать чистую локальную инфраструктуру:

```bash
make db-reset
```

Полностью освободить проектные порты:

```bash
make free-project-ports
```

Не используйте `db-reset`, если локальные данные нужно сохранить.

## Резервное копирование

```bash
make db-backup
make db-restore BACKUP_FILE=/absolute/path/file.dump CONFIRM=YES
```

Restore является destructive-операцией. Production-процедуры описаны в `docs/operations/backup-restore.md`, очистка файлов — в `docs/operations/file-retention.md`.

## Безопасность

- не храните JWT, refresh token, OTP и пароли в логах;
- access token frontend хранит только в памяти;
- refresh token передаётся только через HttpOnly cookie;
- не публикуйте private verification documents;
- backend остаётся источником прав доступа;
- не коммитьте `.env` и реальные секреты.
