COMPOSE_FILE ?= Docker/postgres.yml
ENV_FILE ?= .env
DEV_PORT ?= 18080

.PHONY: ensure-env up stop down db-reset logs run test verify dev-up dev-down dev-run smoke ports free-project-ports

ensure-env:
	@test -f $(ENV_FILE) || (cp .env.example $(ENV_FILE) && echo "Created $(ENV_FILE) from .env.example. Review secrets before production use.")

up: ensure-env
	docker compose --env-file $(ENV_FILE) -f $(COMPOSE_FILE) up -d

stop: ensure-env
	docker compose --env-file $(ENV_FILE) -f $(COMPOSE_FILE) stop

down: ensure-env
	docker compose --env-file $(ENV_FILE) -f $(COMPOSE_FILE) down

db-reset: ensure-env
	docker compose --env-file $(ENV_FILE) -f $(COMPOSE_FILE) down -v
	docker compose --env-file $(ENV_FILE) -f $(COMPOSE_FILE) up -d

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

ports:
	@for port in 8080 5432 9000 9001 $(DEV_PORT); do \
		echo "== port $$port =="; \
		lsof -nP -iTCP:$$port -sTCP:LISTEN || true; \
	done

free-project-ports: ensure-env
	docker compose --env-file $(ENV_FILE) -f $(COMPOSE_FILE) down
	@$(MAKE) ports
