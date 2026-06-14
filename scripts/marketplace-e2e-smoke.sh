#!/usr/bin/env bash
set -euo pipefail

DEV_PORT="${1:-18080}"
ENV_FILE="${2:-.env}"
COMPOSE_FILE="${3:-Docker/postgres.yml}"
BASE_URL="http://localhost:${DEV_PORT}"
LOG_FILE="target/marketplace-e2e-smoke.log"
APP_PID=""
CLIENT_IP_SUFFIX="${RANDOM}"

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
  tail -n 180 "${LOG_FILE}" >&2 || true
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

json_get() {
  local path="$1"
  python3 -c '
import json
import sys

value = json.load(sys.stdin)
for part in sys.argv[1].split("."):
    value = value[part]
print(value)
' "${path}"
}

idempotency_key() {
  printf 'marketplace-smoke-%s-%s' "$(date +%s%N)" "${RANDOM}"
}

api() {
  local method="$1"
  local path="$2"
  local token="${3:-}"
  local body="${4:-}"
  local key="${5:-}"
  local client_ip="${6:-203.0.113.${CLIENT_IP_SUFFIX}}"

  local args=(-fsS -X "${method}" -H "X-Forwarded-For: ${client_ip}")
  if [[ -n "${token}" ]]; then
    args+=(-H "Authorization: Bearer ${token}")
  fi
  if [[ -n "${key}" ]]; then
    args+=(-H "Idempotency-Key: ${key}")
  fi
  if [[ -n "${body}" ]]; then
    args+=(-H "Content-Type: application/json" -d "${body}")
  fi

  local response
  local status
  local body

  response="$(curl -sS -w $'\n%{http_code}' "${args[@]/-fsS/-sS}" "${BASE_URL}${path}")"
  status="${response##*$'\n'}"
  body="${response%$'\n'*}"

  if (( status < 200 || status >= 300 )); then
    echo "API ${method} ${path} failed with HTTP ${status}" >&2
    printf '%s\n' "${body}" >&2
    return 22
  fi

  printf '%s' "${body}"
}

login() {
  local email="$1"
  local client_ip="$2"
  local response
  response="$(
    api POST /api/v1/auth/login "" \
      "{\"email\":\"${email}\",\"password\":\"Test121314#\"}" \
      "" \
      "${client_ip}"
  )"
  printf '%s' "${response}" | json_get accessToken
}

assert_equals() {
  local actual="$1"
  local expected="$2"
  local message="$3"

  if [[ "${actual}" != "${expected}" ]]; then
    echo "${message}: expected '${expected}', got '${actual}'" >&2
    exit 1
  fi
}

cancel_previous_smoke_tasks() {
  local token="$1"
  local tasks
  tasks="$(api GET "/api/v1/my/tasks?status=OPEN&page=0&size=100" "${token}")"

  while IFS= read -r task_id; do
    [[ -z "${task_id}" ]] && continue
    api POST "/api/v1/tasks/${task_id}/cancel" "${token}" "" "$(idempotency_key)" \
      "203.0.113.20" >/dev/null
  done < <(
    printf '%s' "${tasks}" | python3 -c '
import json
import sys

for task in json.load(sys.stdin).get("content", []):
    if task.get("title", "").startswith("E2E smoke task "):
        print(task["publicId"])
'
  )
}

require_file "${ENV_FILE}"
mkdir -p target

set -a
# shellcheck disable=SC1090
. "./${ENV_FILE}"
set +a

docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" up -d
wait_for_compose_health

echo "Starting HandyNest marketplace smoke app on ${BASE_URL}..."
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port="${DEV_PORT}" >"${LOG_FILE}" 2>&1 &
APP_PID="$!"

wait_for_url "${BASE_URL}/actuator/health"
wait_for_url "${BASE_URL}/api/v1/geo/countries"

customer_token="$(login customer@handynest.dev "203.0.113.11")"
performer_token="$(login performer@handynest.dev "203.0.113.12")"
cancel_previous_smoke_tasks "${customer_token}"

performer_response="$(api GET /api/v1/performers/me "${performer_token}")"
performer_id="$(printf '%s' "${performer_response}" | json_get publicId)"
if [[ -z "${performer_id}" ]]; then
  echo "Smoke performer fixture did not return publicId" >&2
  printf '%s\n' "${performer_response}" >&2
  exit 1
fi

category_id="06CAT000000000000000000011"
city_id="06UZCT00000000000000000001"
run_id="$(date +%s%N)"

task_payload="$(
  printf '{"title":"E2E smoke task %s","description":"Marketplace E2E smoke task","categoryId":"%s","serviceMode":"ONSITE","priceType":"FIXED","fixedPrice":12000,"currency":"UZS","cityId":"%s","addressText":"Ташкент, smoke address"}' \
    "${run_id}" \
    "${category_id}" \
    "${city_id}"
)"
task_response="$(
  api POST /api/v1/tasks "${customer_token}" "${task_payload}" "$(idempotency_key)" "203.0.113.21"
)"
task_id="$(printf '%s' "${task_response}" | json_get publicId)"
task_status="$(printf '%s' "${task_response}" | json_get status)"
assert_equals "${task_status}" "OPEN" "Task creation status mismatch"

offer_payload='{"message":"Ready for E2E smoke task","proposedPrice":11000,"currency":"UZS","estimatedDuration":"2 hours","includesMaterials":true}'
offer_response="$(
  api POST "/api/v1/tasks/${task_id}/offers" "${performer_token}" "${offer_payload}" "$(idempotency_key)" "203.0.113.22"
)"
offer_id="$(printf '%s' "${offer_response}" | json_get publicId)"
offer_status="$(printf '%s' "${offer_response}" | json_get status)"
assert_equals "${offer_status}" "PENDING" "Offer creation status mismatch"

deal_response="$(
  api POST "/api/v1/tasks/${task_id}/offers/${offer_id}/accept" "${customer_token}" "" "$(idempotency_key)" "203.0.113.23"
)"
deal_id="$(printf '%s' "${deal_response}" | json_get publicId)"
chat_id="$(printf '%s' "${deal_response}" | json_get chatId)"
deal_status="$(printf '%s' "${deal_response}" | json_get status)"
assert_equals "${deal_status}" "ACTIVE" "Accepted deal status mismatch"

contact_response="$(
  api POST "/api/v1/deals/${deal_id}/contact-reveals" "${customer_token}" \
    '{"contactType":"PHONE"}' "$(idempotency_key)" "203.0.113.24"
)"
contact_value="$(printf '%s' "${contact_response}" | json_get contactValue)"
if [[ "${contact_value}" != +998* ]]; then
  echo "Contact reveal did not return a verified Uzbekistan phone" >&2
  printf '%s\n' "${contact_response}" >&2
  exit 1
fi

contact_history="$(api GET "/api/v1/deals/${deal_id}/contact-reveals" "${customer_token}")"
if printf '%s' "${contact_history}" | grep -q 'contactValue'; then
  echo "Contact reveal history exposed raw contact data" >&2
  printf '%s\n' "${contact_history}" >&2
  exit 1
fi

api POST "/api/v1/chats/${chat_id}/messages" "${performer_token}" '{"text":"Smoke message before work submission"}' "" "203.0.113.25" >/dev/null

submitted_deal_response="$(
  api POST "/api/v1/chats/${chat_id}/submit-work" "${performer_token}" '{"message":"Smoke work submitted"}' "$(idempotency_key)" "203.0.113.26"
)"
submitted_status="$(printf '%s' "${submitted_deal_response}" | json_get status)"
assert_equals "${submitted_status}" "WORK_SUBMITTED" "Work submission status mismatch"

completed_deal_response="$(
  api POST "/api/v1/chats/${chat_id}/accept-work" "${customer_token}" '{"message":"Smoke work accepted"}' "$(idempotency_key)" "203.0.113.27"
)"
completed_status="$(printf '%s' "${completed_deal_response}" | json_get status)"
assert_equals "${completed_status}" "COMPLETED" "Work acceptance status mismatch"

completed_task_response="$(api GET "/api/v1/tasks/${task_id}")"
completed_task_status="$(printf '%s' "${completed_task_response}" | json_get status)"
assert_equals "${completed_task_status}" "COMPLETED" "Completed task status mismatch"

chat_response="$(api GET "/api/v1/chats/${chat_id}" "${customer_token}")"
chat_status="$(printf '%s' "${chat_response}" | json_get status)"
assert_equals "${chat_status}" "READ_ONLY" "Completed chat status mismatch"

feedback_response="$(
  api POST "/api/v1/tasks/${task_id}/feedbacks" "${customer_token}" '{"grade":5,"text":"Smoke feedback for completed work"}' "$(idempotency_key)" "203.0.113.28"
)"
feedback_grade="$(printf '%s' "${feedback_response}" | json_get grade)"
feedback_performer_id="$(printf '%s' "${feedback_response}" | json_get performerId)"
assert_equals "${feedback_grade}" "5" "Feedback grade mismatch"
assert_equals "${feedback_performer_id}" "${performer_id}" "Feedback performer mismatch"

performer_feedbacks="$(api GET "/api/v1/performers/${performer_id}/feedbacks")"
if ! printf '%s' "${performer_feedbacks}" | grep -q "${task_id}"; then
  echo "Performer feedback list does not include smoke task feedback" >&2
  printf '%s\n' "${performer_feedbacks}" >&2
  exit 1
fi

echo "Marketplace E2E smoke passed:"
echo "  task=${task_id}"
echo "  offer=${offer_id}"
echo "  deal=${deal_id}"
echo "  chat=${chat_id}"
echo "Spring Boot app stopped by script cleanup; PostgreSQL and MinIO remain running."
