#!/usr/bin/env bash
set -euo pipefail

DEV_PORT="${1:-18080}"
ENV_FILE="${2:-.env}"
COMPOSE_FILE="${3:-Docker/postgres.yml}"
BASE_URL="http://localhost:${DEV_PORT}"
LOG_FILE="target/dev-seed.log"
APP_PID=""

cleanup() {
  set +e
  if [[ -n "${APP_PID}" ]] && kill -0 "${APP_PID}" 2>/dev/null; then
    kill "${APP_PID}" 2>/dev/null || true
    wait "${APP_PID}" 2>/dev/null || true
  fi
}
trap cleanup EXIT

require_file() {
  if [[ ! -f "$1" ]]; then
    echo "Required file '$1' is missing. Run 'make ensure-env' first." >&2
    exit 1
  fi
}

wait_for_url() {
  local url="$1"
  local attempts="${2:-90}"

  for _ in $(seq 1 "${attempts}"); do
    if curl -fsS "${url}" >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done

  echo "Timed out waiting for ${url}" >&2
  tail -n 160 "${LOG_FILE}" >&2 || true
  exit 1
}

wait_for_compose_health() {
  for _ in $(seq 1 60); do
    unhealthy="$(docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" ps --format json \
      | grep -E '"Health":"(starting|unhealthy)"' || true)"
    if [[ -z "${unhealthy}" ]]; then
      return 0
    fi
    sleep 1
  done

  echo "Docker Compose services did not become healthy in time" >&2
  docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" ps >&2 || true
  exit 1
}

print_counts() {
  docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" exec -T base-project-pg \
    psql -U "${POSTGRES_USER:-postgres}" -d "${POSTGRES_DB:-handyDB}" -Atc "
      SELECT 'handy_user=' || count(*) FROM handy_user;
      SELECT 'customer_profile=' || count(*) FROM customer_profile;
      SELECT 'performer_profile=' || count(*) FROM performer_profile;
      SELECT 'performer_category=' || count(*) FROM performer_category;
      SELECT 'marketplace_task=' || count(*) FROM marketplace_task;
      SELECT 'task_offer=' || count(*) FROM task_offer;
    "
}

require_file "${ENV_FILE}"
mkdir -p target

set -a
# shellcheck disable=SC1090
. "./${ENV_FILE}"
set +a

docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" up -d
wait_for_compose_health

echo "Applying local/dev Liquibase seed on ${BASE_URL}..."
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port="${DEV_PORT}" >"${LOG_FILE}" 2>&1 &
APP_PID="$!"

wait_for_url "${BASE_URL}/actuator/health"

echo "Dev seed applied. Fixture counts:"
print_counts
echo "Spring Boot app stopped by script cleanup; PostgreSQL and MinIO remain running."
