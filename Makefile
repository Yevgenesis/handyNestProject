COMPOSE_FILE ?= Docker/postgres.yml
ENV_FILE ?= .env
DEV_PORT ?= 18080

.PHONY: ensure-env up stop down db-reset db-backup db-restore logs run test verify dev-up dev-down dev-run dev-seed smoke marketplace-smoke smoke-clean ports free-project-ports web-dev web-format web-check web-e2e

ensure-env:
	@test -f $(ENV_FILE) || (cp .env.example $(ENV_FILE) && echo "Создан $(ENV_FILE) из .env.example. Перед production-запуском замените секреты.")

up: ensure-env
	docker compose --env-file $(ENV_FILE) -f $(COMPOSE_FILE) up -d

stop: ensure-env
	docker compose --env-file $(ENV_FILE) -f $(COMPOSE_FILE) stop

down: ensure-env
	docker compose --env-file $(ENV_FILE) -f $(COMPOSE_FILE) down

db-reset: ensure-env
	docker compose --env-file $(ENV_FILE) -f $(COMPOSE_FILE) down -v
	docker compose --env-file $(ENV_FILE) -f $(COMPOSE_FILE) up -d

db-backup: ensure-env
	ENV_FILE=$(abspath $(ENV_FILE)) COMPOSE_FILE=$(abspath $(COMPOSE_FILE)) ./scripts/db-backup.sh

db-restore: ensure-env
	@test "$(CONFIRM)" = "YES" || (echo "Для destructive restore укажите CONFIRM=YES" && exit 2)
	@test -n "$(BACKUP_FILE)" || (echo "Укажите BACKUP_FILE=/absolute/path/file.dump" && exit 2)
	ENV_FILE=$(abspath $(ENV_FILE)) COMPOSE_FILE=$(abspath $(COMPOSE_FILE)) ./scripts/db-restore.sh --confirm-destructive "$(BACKUP_FILE)"

logs: ensure-env
	docker compose --env-file $(ENV_FILE) -f $(COMPOSE_FILE) logs -f

run: ensure-env
	set -a; . ./$(ENV_FILE); set +a; mvn spring-boot:run

test:
	mvn -B test

verify:
	mvn -B clean verify

dev-up: up

dev-down: down

dev-run: ensure-env
	set -a; . ./$(ENV_FILE); set +a; mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=$(DEV_PORT)

smoke: ensure-env
	./scripts/dev-smoke.sh $(DEV_PORT) $(ENV_FILE) $(COMPOSE_FILE)

dev-seed: ensure-env
	./scripts/dev-seed.sh $(DEV_PORT) $(ENV_FILE) $(COMPOSE_FILE)

marketplace-smoke: ensure-env
	./scripts/marketplace-e2e-smoke.sh $(DEV_PORT) $(ENV_FILE) $(COMPOSE_FILE)

smoke-clean: down

ports:
	@for port in 8080 5432 9000 9001 $(DEV_PORT); do \
		echo "== port $$port =="; \
		lsof -nP -iTCP:$$port -sTCP:LISTEN || true; \
	done

free-project-ports: ensure-env
	docker compose --env-file $(ENV_FILE) -f $(COMPOSE_FILE) down
	@$(MAKE) ports

web-dev: ensure-env
	set -a; . ./$(ENV_FILE); set +a; corepack pnpm --filter @handynest/web dev

web-format:
	corepack pnpm --filter @handynest/web format

web-check:
	corepack pnpm --filter @handynest/web check

web-e2e:
	./scripts/frontend-e2e.sh $(DEV_PORT) 3000 $(ENV_FILE) $(COMPOSE_FILE)
