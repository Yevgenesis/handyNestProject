#!/usr/bin/env bash
set -euo pipefail

BACKEND_PORT="${1:-18080}"
WEB_PORT="${2:-3000}"
ENV_FILE="${3:-.env}"
COMPOSE_FILE="${4:-Docker/postgres.yml}"
BACKEND_LOG="target/frontend-e2e-backend.log"
BACKEND_PID=""

cleanup() {
  set +e
  if [[ -n "${BACKEND_PID}" ]] && kill -0 "${BACKEND_PID}" 2>/dev/null; then
    kill "${BACKEND_PID}" 2>/dev/null || true
    wait "${BACKEND_PID}" 2>/dev/null || true
  fi
}
trap cleanup EXIT

wait_for_url() {
  local url="$1"
  for _ in $(seq 1 120); do
    if curl -fsS "${url}" >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done
  echo "Превышено время ожидания ${url}" >&2
  tail -n 160 "${BACKEND_LOG}" >&2 || true
  exit 1
}

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "Файл ${ENV_FILE} не найден. Сначала выполните 'make ensure-env'." >&2
  exit 1
fi

for port in "${BACKEND_PORT}" "${WEB_PORT}"; do
  if lsof -nP -iTCP:"${port}" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "Порт ${port} уже занят. Остановите процесс перед E2E." >&2
    exit 1
  fi
done

set -a
# shellcheck disable=SC1090
. "./${ENV_FILE}"
set +a

docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" up -d
mkdir -p target

mvn spring-boot:run -Dspring-boot.run.arguments=--server.port="${BACKEND_PORT}" >"${BACKEND_LOG}" 2>&1 &
BACKEND_PID="$!"
wait_for_url "http://127.0.0.1:${BACKEND_PORT}/actuator/health"

HANDYNEST_BACKEND_URL="http://127.0.0.1:${BACKEND_PORT}" \
PLAYWRIGHT_BASE_URL="http://127.0.0.1:${WEB_PORT}" \
corepack pnpm --filter @handynest/web test:e2e

cleanup
BACKEND_PID=""

for port in "${BACKEND_PORT}" "${WEB_PORT}"; do
  if lsof -nP -iTCP:"${port}" -sTCP:LISTEN >/dev/null 2>&1; then
    echo "После E2E порт проекта ${port} остался занят." >&2
    lsof -nP -iTCP:"${port}" -sTCP:LISTEN >&2 || true
    exit 1
  fi
done

echo "Frontend E2E пройден. App-порты свободны; PostgreSQL и MinIO продолжают работать."
