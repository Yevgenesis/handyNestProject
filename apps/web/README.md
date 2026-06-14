# HandyNest Web

Frontend HandyNest на Next.js App Router находится в `apps/web`.

## Запуск

Сначала из корня проекта поднимите инфраструктуру и backend:

```bash
make up
make run
```

В отдельном терминале запустите frontend:

```bash
make web-dev
```

Откройте `http://localhost:3000`.

Frontend использует `HANDYNEST_BACKEND_URL` на стороне Next.js и same-origin proxy `/api/v1/**`. Access token хранится только в памяти, refresh token обрабатывается как HttpOnly cookie.

## Переменные окружения

Пример находится в `.env.example`:

```text
HANDYNEST_BACKEND_URL=http://localhost:8080
NEXT_PUBLIC_API_BASE_URL=/api/v1
HANDYNEST_WEB_STORAGE_ORIGINS=http://localhost:9000,http://127.0.0.1:9000
```

`HANDYNEST_WEB_STORAGE_ORIGINS` задаёт разрешённые CSP origins для прямой загрузки и просмотра файлов через presigned MinIO/S3 URL. Значения перечисляются через запятую. В production укажите только фактические HTTPS origins object storage/CDN.

## Команды

Из корня проекта:

```bash
make web-dev       # локальный frontend
make web-format    # форматирование Prettier
make web-check     # lint, types, unit tests, production build
make web-e2e       # Playwright с локальным backend
```

Из `apps/web`:

```bash
corepack pnpm dev
corepack pnpm lint
corepack pnpm typecheck
corepack pnpm test
corepack pnpm build
corepack pnpm test:e2e
```

## OpenAPI types

Сначала обновите backend artifact, затем generated frontend types:

```bash
./mvnw -B test -Dtest=OpenApiArtifactTest
corepack pnpm --filter @handynest/web api:generate
```

Тестовый вход:

```text
customer@handynest.dev / Test121314#
```
