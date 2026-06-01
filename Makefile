COMPOSE_FILE ?= Docker/postgres.yml

.PHONY: up stop down db-reset logs run test verify

up:
	docker compose --env-file .env -f $(COMPOSE_FILE) up -d

stop:
	docker compose --env-file .env -f $(COMPOSE_FILE) stop

down:
	docker compose --env-file .env -f $(COMPOSE_FILE) down

db-reset:
	docker compose --env-file .env -f $(COMPOSE_FILE) down -v
	docker compose --env-file .env -f $(COMPOSE_FILE) up -d

logs:
	docker compose --env-file .env -f $(COMPOSE_FILE) logs -f

run:
	set -a; . ./.env; set +a; mvn spring-boot:run

test:
	mvn -B test

verify:
	mvn -B clean verify
