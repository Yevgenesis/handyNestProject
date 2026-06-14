#!/usr/bin/env bash
set -euo pipefail

DEV_PORT="${1:-18080}"
ENV_FILE="${2:-.env}"
COMPOSE_FILE="${3:-Docker/postgres.yml}"
BASE_URL="http://localhost:${DEV_PORT}"
LOG_FILE="target/dev-smoke.log"
APP_PID=""

cleanup() {
  set +e

  if [[ -n "${APP_PID}" ]] && kill -0 "${APP_PID}" 2>/dev/null; then
    kill "${APP_PID}" 2>/dev/null || true
    wait "${APP_PID}" 2>/dev/null || true
  fi

  for port in "${DEV_PORT}"; do
    lsof -nP -iTCP:"${port}" -sTCP:LISTEN >/dev/null 2>&1 && {
      echo "Application port ${port} is still in use after smoke cleanup:" >&2
      lsof -nP -iTCP:"${port}" -sTCP:LISTEN >&2 || true
    }
  done

  echo "PostgreSQL and MinIO remain running for local development."

  return 0
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
  tail -n 120 "${LOG_FILE}" >&2 || true
  exit 1
}

idempotency_key() {
  printf 'smoke-%s-%s' "$(date +%s%N)" "${RANDOM}"
}

require_file "${ENV_FILE}"
mkdir -p target

set -a
# shellcheck disable=SC1090
. "./${ENV_FILE}"
set +a

docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" up -d

echo "Waiting for PostgreSQL and MinIO health..."
for _ in $(seq 1 60); do
  unhealthy="$(docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" ps --format json \
    | grep -E '"Health":"(starting|unhealthy)"' || true)"
  if [[ -z "${unhealthy}" ]]; then
    break
  fi
  sleep 1
done

mvn spring-boot:run -Dspring-boot.run.arguments=--server.port="${DEV_PORT}" >"${LOG_FILE}" 2>&1 &
APP_PID="$!"

wait_for_url "${BASE_URL}/api/v1/geo/countries"

health_response="$(curl -fsS "${BASE_URL}/actuator/health")"
if ! printf '%s' "${health_response}" | grep -q '"status":"UP"'; then
  echo "Smoke actuator health did not return UP" >&2
  printf '%s\n' "${health_response}" >&2
  exit 1
fi

curl -fsS "${BASE_URL}/api/v1/categories" >/dev/null

email="smoke$(date +%s%N)@example.uz"
register_payload="$(
  printf '{"firstName":"Smoke","lastName":"User","email":"%s","password":"Test121314#","passwordConfirmation":"Test121314#","consents":[{"type":"TERMS_OF_SERVICE","documentVersion":"1.0"},{"type":"PRIVACY_POLICY","documentVersion":"1.0"},{"type":"PERSONAL_DATA_PROCESSING","documentVersion":"1.0"},{"type":"PERFORMER_RULES","documentVersion":"1.0"},{"type":"CUSTOMER_RULES","documentVersion":"1.0"},{"type":"PROHIBITED_SERVICES_POLICY","documentVersion":"1.0"},{"type":"PAYMENT_POLICY","documentVersion":"1.0"}]}' "${email}"
)"
register_response="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -H 'X-Forwarded-For: 203.0.113.240' \
    -d "${register_payload}" \
    "${BASE_URL}/api/v1/auth/register"
)"

access_token="$(printf '%s' "${register_response}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')"
if [[ -z "${access_token}" ]]; then
  echo "Smoke registration did not return accessToken" >&2
  printf '%s\n' "${register_response}" >&2
  exit 1
fi

curl -fsS \
  -H "Authorization: Bearer ${access_token}" \
  "${BASE_URL}/api/v1/auth/me" >/dev/null

curl -fsS \
  -H "Authorization: Bearer ${access_token}" \
  "${BASE_URL}/api/v1/users/me" >/dev/null

verification_upload_response="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -d '{"originalFilename":"smoke-id.pdf","contentType":"application/pdf","sizeBytes":2048,"checksum":"sha256:smoke-verification"}' \
    "${BASE_URL}/api/v1/verification/documents/upload-url"
)"
verification_attachment_id="$(printf '%s' "${verification_upload_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
if [[ -z "${verification_attachment_id}" ]] \
  || ! printf '%s' "${verification_upload_response}" | grep -q '"attachmentType":"VERIFICATION_DOCUMENT"' \
  || ! printf '%s' "${verification_upload_response}" | grep -q '"uploadMethod":"PUT"'; then
  echo "Smoke verification document upload URL did not return expected metadata" >&2
  printf '%s\n' "${verification_upload_response}" >&2
  exit 1
fi

verification_documents_response="$(
  curl -fsS \
    -H "Authorization: Bearer ${access_token}" \
    "${BASE_URL}/api/v1/verification/documents"
)"
if ! printf '%s' "${verification_documents_response}" | grep -q "${verification_attachment_id}"; then
  echo "Smoke verification documents did not include created document" >&2
  printf '%s\n' "${verification_documents_response}" >&2
  exit 1
fi

performer_email="performer$(date +%s%N)@example.uz"
performer_register_payload="$(
  printf '{"firstName":"Smoke","lastName":"Performer","email":"%s","password":"Test121314#","passwordConfirmation":"Test121314#","consents":[{"type":"TERMS_OF_SERVICE","documentVersion":"1.0"},{"type":"PRIVACY_POLICY","documentVersion":"1.0"},{"type":"PERSONAL_DATA_PROCESSING","documentVersion":"1.0"},{"type":"PERFORMER_RULES","documentVersion":"1.0"},{"type":"CUSTOMER_RULES","documentVersion":"1.0"},{"type":"PROHIBITED_SERVICES_POLICY","documentVersion":"1.0"},{"type":"PAYMENT_POLICY","documentVersion":"1.0"}]}' "${performer_email}"
)"
performer_register_response="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -H 'X-Forwarded-For: 203.0.113.241' \
    -d "${performer_register_payload}" \
    "${BASE_URL}/api/v1/auth/register"
)"
performer_token="$(printf '%s' "${performer_register_response}" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')"
if [[ -z "${performer_token}" ]]; then
  echo "Smoke performer registration did not return accessToken" >&2
  printf '%s\n' "${performer_register_response}" >&2
  exit 1
fi

category_id="06CAT000000000000000000011"
city_id="06UZCT00000000000000000001"

performer_payload="$(
  printf '{"displayName":"Smoke Pro","description":"Smoke performer","skillsDescription":"Repair","cityId":"%s","serviceRadiusKm":30,"worksRemotely":false,"worksOnsite":true,"categories":[{"categoryId":"%s","experienceYears":3,"priceFrom":5000,"priceTo":20000,"currency":"UZS","primary":true}]}' \
    "${city_id}" \
    "${category_id}"
)"
curl -fsS \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${performer_token}" \
  -d "${performer_payload}" \
  "${BASE_URL}/api/v1/performers/me" > target/dev-smoke-performer.json
performer_id="$(sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p' target/dev-smoke-performer.json)"
if [[ -z "${performer_id}" ]]; then
  echo "Smoke performer profile creation did not return publicId" >&2
  cat target/dev-smoke-performer.json >&2
  exit 1
fi

performer_verification_upload_response="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${performer_token}" \
    -d '{"originalFilename":"smoke-performer-id.pdf","contentType":"application/pdf","sizeBytes":2048,"checksum":"sha256:smoke-performer-verification"}' \
    "${BASE_URL}/api/v1/verification/documents/upload-url"
)"
performer_verification_attachment_id="$(printf '%s' "${performer_verification_upload_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
if [[ -z "${performer_verification_attachment_id}" ]]; then
  echo "Smoke performer verification document upload did not return publicId" >&2
  printf '%s\n' "${performer_verification_upload_response}" >&2
  exit 1
fi

verification_request_payload="$(
  printf '{"requestedLevel":"ID_VERIFIED","documentIds":["%s"],"comment":"Smoke verification request"}' \
    "${performer_verification_attachment_id}"
)"
verification_request_response="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${performer_token}" \
    -d "${verification_request_payload}" \
    "${BASE_URL}/api/v1/performers/me/verification-requests"
)"
verification_request_id="$(printf '%s' "${verification_request_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
if [[ -z "${verification_request_id}" ]] \
  || ! printf '%s' "${verification_request_response}" | grep -q '"status":"PENDING"' \
  || ! printf '%s' "${verification_request_response}" | grep -q "${performer_verification_attachment_id}"; then
  echo "Smoke performer verification request did not return expected pending request" >&2
  printf '%s\n' "${verification_request_response}" >&2
  exit 1
fi

performer_verification_requests_response="$(
  curl -fsS \
    -H "Authorization: Bearer ${performer_token}" \
    "${BASE_URL}/api/v1/performers/me/verification-requests"
)"
if ! printf '%s' "${performer_verification_requests_response}" | grep -q "${verification_request_id}"; then
  echo "Smoke performer verification request list did not include created request" >&2
  printf '%s\n' "${performer_verification_requests_response}" >&2
  exit 1
fi

favorite_response="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -d '{"note":"Smoke favorite performer"}' \
    "${BASE_URL}/api/v1/my/favorite-performers/${performer_id}"
)"
if ! printf '%s' "${favorite_response}" | grep -q "${performer_id}"; then
  echo "Smoke favorite creation did not include performer id" >&2
  printf '%s\n' "${favorite_response}" >&2
  exit 1
fi

favorite_list_response="$(
  curl -fsS \
    -H "Authorization: Bearer ${access_token}" \
    "${BASE_URL}/api/v1/my/favorite-performers"
)"
if ! printf '%s' "${favorite_list_response}" | grep -q "${performer_id}"; then
  echo "Smoke favorite list did not include performer" >&2
  printf '%s\n' "${favorite_list_response}" >&2
  exit 1
fi

task_payload="$(
  printf '{"title":"Smoke task","description":"Smoke marketplace flow","categoryId":"%s","serviceMode":"ONSITE","priceType":"FIXED","fixedPrice":12000,"currency":"UZS","cityId":"%s","addressText":"Smoke address"}' \
    "${category_id}" \
    "${city_id}"
)"
task_response="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    -d "${task_payload}" \
    "${BASE_URL}/api/v1/tasks"
)"
task_id="$(printf '%s' "${task_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
if [[ -z "${task_id}" ]]; then
  echo "Smoke task creation did not return publicId" >&2
  printf '%s\n' "${task_response}" >&2
  exit 1
fi

complaint_payload="$(
  printf '{"targetType":"TASK","targetId":"%s","reason":"Smoke moderation complaint","description":"Smoke complaint should create moderation case"}' \
    "${task_id}"
)"
complaint_response="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -d "${complaint_payload}" \
    "${BASE_URL}/api/v1/complaints"
)"
complaint_id="$(printf '%s' "${complaint_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
moderation_case_id="$(printf '%s' "${complaint_response}" | sed -n 's/.*"moderationCaseId":"\([^"]*\)".*/\1/p')"
if [[ -z "${complaint_id}" || -z "${moderation_case_id}" ]] \
  || ! printf '%s' "${complaint_response}" | grep -q '"status":"OPEN"'; then
  echo "Smoke complaint did not return expected open complaint and moderation case" >&2
  printf '%s\n' "${complaint_response}" >&2
  exit 1
fi

my_complaints_response="$(
  curl -fsS \
    -H "Authorization: Bearer ${access_token}" \
    "${BASE_URL}/api/v1/my/complaints"
)"
if ! printf '%s' "${my_complaints_response}" | grep -q "${complaint_id}"; then
  echo "Smoke my complaints did not include created complaint" >&2
  printf '%s\n' "${my_complaints_response}" >&2
  exit 1
fi

repeat_response="$(
  curl -fsS \
    -X POST \
    -H "Authorization: Bearer ${access_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    "${BASE_URL}/api/v1/tasks/${task_id}/repeat"
)"
repeat_task_id="$(printf '%s' "${repeat_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
if [[ -z "${repeat_task_id}" ]] || ! printf '%s' "${repeat_response}" | grep -q "\"repeatOfTaskId\":\"${task_id}\""; then
  echo "Smoke repeat task did not return expected repeatOfTaskId" >&2
  printf '%s\n' "${repeat_response}" >&2
  exit 1
fi

offer_response="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${performer_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    -d '{"message":"Ready for smoke task","proposedPrice":11000,"currency":"UZS","estimatedDuration":"2 hours","includesMaterials":true}' \
    "${BASE_URL}/api/v1/tasks/${task_id}/offers"
)"
offer_id="$(printf '%s' "${offer_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
if [[ -z "${offer_id}" ]]; then
  echo "Smoke offer creation did not return publicId" >&2
  printf '%s\n' "${offer_response}" >&2
  exit 1
fi

deal_response="$(
  curl -fsS \
    -X POST \
    -H "Authorization: Bearer ${access_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    "${BASE_URL}/api/v1/tasks/${task_id}/offers/${offer_id}/accept"
)"
deal_id="$(printf '%s' "${deal_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
chat_id="$(printf '%s' "${deal_response}" | sed -n 's/.*"chatId":"\([^"]*\)".*/\1/p')"
if [[ -z "${deal_id}" || -z "${chat_id}" ]]; then
  echo "Smoke offer acceptance did not return deal and chat ids" >&2
  printf '%s\n' "${deal_response}" >&2
  exit 1
fi

curl -fsS \
  -H "Authorization: Bearer ${performer_token}" \
  "${BASE_URL}/api/v1/deals/${deal_id}" >/dev/null

curl -fsS \
  -H "Authorization: Bearer ${access_token}" \
  "${BASE_URL}/api/v1/chats/${chat_id}" >/dev/null

message_response="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -d '{"text":"Smoke chat message before work submission"}' \
    "${BASE_URL}/api/v1/chats/${chat_id}/messages"
)"
if ! printf '%s' "${message_response}" | grep -q '"messageType":"TEXT"'; then
  echo "Smoke chat message did not return TEXT message" >&2
  printf '%s\n' "${message_response}" >&2
  exit 1
fi

chat_attachment_response="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -d '{"originalFilename":"smoke-photo.jpg","contentType":"image/jpeg","sizeBytes":2048,"checksum":"sha256:smoke-chat","text":"Smoke chat attachment"}' \
    "${BASE_URL}/api/v1/chats/${chat_id}/attachments"
)"
chat_attachment_id="$(printf '%s' "${chat_attachment_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
if [[ -z "${chat_attachment_id}" ]] \
  || ! printf '%s' "${chat_attachment_response}" | grep -q '"attachmentType":"CHAT_FILE"' \
  || ! printf '%s' "${chat_attachment_response}" | grep -q '"uploadMethod":"PUT"'; then
  echo "Smoke chat attachment did not return CHAT_FILE metadata" >&2
  printf '%s\n' "${chat_attachment_response}" >&2
  exit 1
fi

chat_attachments_response="$(
  curl -fsS \
    -H "Authorization: Bearer ${performer_token}" \
    "${BASE_URL}/api/v1/chats/${chat_id}/attachments"
)"
if ! printf '%s' "${chat_attachments_response}" | grep -q "${chat_attachment_id}"; then
  echo "Smoke chat attachments did not include created attachment" >&2
  printf '%s\n' "${chat_attachments_response}" >&2
  exit 1
fi

messages_response="$(
  curl -fsS \
    -H "Authorization: Bearer ${performer_token}" \
    "${BASE_URL}/api/v1/chats/${chat_id}/messages"
)"
if ! printf '%s' "${messages_response}" | grep -q "${chat_attachment_id}"; then
  echo "Smoke chat messages did not include attachment message" >&2
  printf '%s\n' "${messages_response}" >&2
  exit 1
fi

chat_download_response="$(
  curl -fsS \
    -X POST \
    -H "Authorization: Bearer ${performer_token}" \
    "${BASE_URL}/api/v1/attachments/${chat_attachment_id}/download-url"
)"
if ! printf '%s' "${chat_download_response}" | grep -q '"downloadMethod":"GET"'; then
  echo "Smoke chat attachment download URL was not issued" >&2
  printf '%s\n' "${chat_download_response}" >&2
  exit 1
fi

curl -fsS \
  -X POST \
  -H "Authorization: Bearer ${performer_token}" \
  "${BASE_URL}/api/v1/chats/${chat_id}/mark-read" >/dev/null

messages_response="$(
  curl -fsS \
    -H "Authorization: Bearer ${performer_token}" \
    "${BASE_URL}/api/v1/chats/${chat_id}/messages"
)"
if ! printf '%s' "${messages_response}" | grep -q '"messageType":"TEXT"'; then
  echo "Smoke chat messages did not include TEXT message" >&2
  printf '%s\n' "${messages_response}" >&2
  exit 1
fi

payment_response="$(
  curl -fsS \
    -X POST \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    -d '{"amount":12000,"currency":"UZS","paymentMode":"ON_PLATFORM_ESCROW"}' \
    "${BASE_URL}/api/v1/deals/${deal_id}/payments"
)"
payment_id="$(printf '%s' "${payment_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
if [[ -z "${payment_id}" ]] || ! printf '%s' "${payment_response}" | grep -q '"status":"PENDING"'; then
  echo "Smoke payment creation did not return expected pending payment" >&2
  printf '%s\n' "${payment_response}" >&2
  exit 1
fi

payment_authorize_response="$(
  curl -fsS \
    -X POST \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    -d '{}' \
    "${BASE_URL}/api/v1/payments/${payment_id}/authorize"
)"
if ! printf '%s' "${payment_authorize_response}" | grep -q '"status":"AUTHORIZED"'; then
  echo "Smoke payment authorize did not return AUTHORIZED" >&2
  printf '%s\n' "${payment_authorize_response}" >&2
  exit 1
fi

payment_hold_response="$(
  curl -fsS \
    -X POST \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    -d '{}' \
    "${BASE_URL}/api/v1/payments/${payment_id}/hold"
)"
if ! printf '%s' "${payment_hold_response}" | grep -q '"status":"HELD"'; then
  echo "Smoke payment hold did not return HELD" >&2
  printf '%s\n' "${payment_hold_response}" >&2
  exit 1
fi

submit_response="$(
  curl -fsS \
    -X POST \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${performer_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    -d '{"message":"Smoke work submitted"}' \
    "${BASE_URL}/api/v1/chats/${chat_id}/submit-work"
)"
if ! printf '%s' "${submit_response}" | grep -q '"status":"WORK_SUBMITTED"'; then
  echo "Smoke work submission did not move deal to WORK_SUBMITTED" >&2
  printf '%s\n' "${submit_response}" >&2
  exit 1
fi

messages_response="$(
  curl -fsS \
    -H "Authorization: Bearer ${access_token}" \
    "${BASE_URL}/api/v1/chats/${chat_id}/messages"
)"
if ! printf '%s' "${messages_response}" | grep -q '"messageType":"WORK_SUBMITTED"'; then
  echo "Smoke chat messages did not include WORK_SUBMITTED system message" >&2
  printf '%s\n' "${messages_response}" >&2
  exit 1
fi

accept_response="$(
  curl -fsS \
    -X POST \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    -d '{"message":"Smoke work accepted"}' \
    "${BASE_URL}/api/v1/chats/${chat_id}/accept-work"
)"
if ! printf '%s' "${accept_response}" | grep -q '"status":"COMPLETED"'; then
  echo "Smoke work acceptance did not move deal to COMPLETED" >&2
  printf '%s\n' "${accept_response}" >&2
  exit 1
fi

chat_response="$(
  curl -fsS \
    -H "Authorization: Bearer ${access_token}" \
    "${BASE_URL}/api/v1/chats/${chat_id}"
)"
if ! printf '%s' "${chat_response}" | grep -q '"status":"READ_ONLY"'; then
  echo "Smoke work acceptance did not move chat to READ_ONLY" >&2
  printf '%s\n' "${chat_response}" >&2
  exit 1
fi

messages_response="$(
  curl -fsS \
    -H "Authorization: Bearer ${access_token}" \
    "${BASE_URL}/api/v1/chats/${chat_id}/messages"
)"
if ! printf '%s' "${messages_response}" | grep -q '"messageType":"WORK_ACCEPTED"'; then
  echo "Smoke chat messages did not include WORK_ACCEPTED system message" >&2
  printf '%s\n' "${messages_response}" >&2
  exit 1
fi

payment_release_response="$(
  curl -fsS \
    -X POST \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    -d '{}' \
    "${BASE_URL}/api/v1/payments/${payment_id}/release"
)"
if ! printf '%s' "${payment_release_response}" | grep -q '"status":"RELEASED"'; then
  echo "Smoke payment release did not return RELEASED" >&2
  printf '%s\n' "${payment_release_response}" >&2
  exit 1
fi

deal_payment_response="$(
  curl -fsS \
    -H "Authorization: Bearer ${access_token}" \
    "${BASE_URL}/api/v1/deals/${deal_id}"
)"
if ! printf '%s' "${deal_payment_response}" | grep -q '"paymentStatus":"RELEASED"'; then
  echo "Smoke deal payment status was not RELEASED" >&2
  printf '%s\n' "${deal_payment_response}" >&2
  exit 1
fi

feedback_response="$(
  curl -fsS \
    -X POST \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    -d '{"grade":5,"text":"Smoke feedback for completed work"}' \
    "${BASE_URL}/api/v1/tasks/${task_id}/feedbacks"
)"
feedback_id="$(printf '%s' "${feedback_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
if [[ -z "${feedback_id}" ]] || ! printf '%s' "${feedback_response}" | grep -q '"grade":5'; then
  echo "Smoke feedback creation did not return expected feedback" >&2
  printf '%s\n' "${feedback_response}" >&2
  exit 1
fi

performer_feedbacks_response="$(
  curl -fsS "${BASE_URL}/api/v1/performers/${performer_id}/feedbacks"
)"
if ! printf '%s' "${performer_feedbacks_response}" | grep -q "${feedback_id}"; then
  echo "Smoke performer feedbacks did not include created feedback" >&2
  printf '%s\n' "${performer_feedbacks_response}" >&2
  exit 1
fi

performer_profile_response="$(
  curl -fsS "${BASE_URL}/api/v1/performers/${performer_id}"
)"
if ! printf '%s' "${performer_profile_response}" | grep -q '"ratingCount":1'; then
  echo "Smoke performer rating was not updated after feedback" >&2
  printf '%s\n' "${performer_profile_response}" >&2
  exit 1
fi

dispute_task_payload="$(
  printf '{"title":"Smoke dispute task","description":"Smoke dispute flow","categoryId":"%s","serviceMode":"ONSITE","priceType":"FIXED","fixedPrice":14000,"currency":"UZS","cityId":"%s","addressText":"Smoke dispute address"}' \
    "${category_id}" \
    "${city_id}"
)"
dispute_task_response="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    -d "${dispute_task_payload}" \
    "${BASE_URL}/api/v1/tasks"
)"
dispute_task_id="$(printf '%s' "${dispute_task_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
if [[ -z "${dispute_task_id}" ]]; then
  echo "Smoke dispute task creation did not return publicId" >&2
  printf '%s\n' "${dispute_task_response}" >&2
  exit 1
fi

dispute_offer_response="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${performer_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    -d '{"message":"Ready for dispute smoke task","proposedPrice":13000,"currency":"UZS","estimatedDuration":"3 hours","includesMaterials":true}' \
    "${BASE_URL}/api/v1/tasks/${dispute_task_id}/offers"
)"
dispute_offer_id="$(printf '%s' "${dispute_offer_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
if [[ -z "${dispute_offer_id}" ]]; then
  echo "Smoke dispute offer creation did not return publicId" >&2
  printf '%s\n' "${dispute_offer_response}" >&2
  exit 1
fi

dispute_deal_response="$(
  curl -fsS \
    -X POST \
    -H "Authorization: Bearer ${access_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    "${BASE_URL}/api/v1/tasks/${dispute_task_id}/offers/${dispute_offer_id}/accept"
)"
dispute_chat_id="$(printf '%s' "${dispute_deal_response}" | sed -n 's/.*"chatId":"\([^"]*\)".*/\1/p')"
if [[ -z "${dispute_chat_id}" ]]; then
  echo "Smoke dispute offer acceptance did not return chat id" >&2
  printf '%s\n' "${dispute_deal_response}" >&2
  exit 1
fi

curl -fsS \
  -X POST \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${performer_token}" \
  -H "Idempotency-Key: $(idempotency_key)" \
  -d '{"message":"Smoke disputed work submitted"}' \
  "${BASE_URL}/api/v1/chats/${dispute_chat_id}/submit-work" >/dev/null

dispute_response="$(
  curl -fsS \
    -X POST \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -H "Idempotency-Key: $(idempotency_key)" \
    -d '{"reason":"Smoke dispute opened","description":"Smoke dispute details"}' \
    "${BASE_URL}/api/v1/chats/${dispute_chat_id}/open-dispute"
)"
dispute_id="$(printf '%s' "${dispute_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
if [[ -z "${dispute_id}" ]] || ! printf '%s' "${dispute_response}" | grep -q '"status":"OPEN"'; then
  echo "Smoke open dispute did not return OPEN dispute" >&2
  printf '%s\n' "${dispute_response}" >&2
  exit 1
fi

dispute_attachment_response="$(
  curl -fsS \
    -H 'Content-Type: application/json' \
    -H "Authorization: Bearer ${access_token}" \
    -d '{"originalFilename":"smoke-evidence.pdf","contentType":"application/pdf","sizeBytes":4096,"checksum":"sha256:smoke-dispute","text":"Smoke dispute evidence"}' \
    "${BASE_URL}/api/v1/disputes/${dispute_id}/attachments"
)"
dispute_attachment_id="$(printf '%s' "${dispute_attachment_response}" | sed -n 's/.*"publicId":"\([^"]*\)".*/\1/p')"
if [[ -z "${dispute_attachment_id}" ]] \
  || ! printf '%s' "${dispute_attachment_response}" | grep -q '"attachmentType":"DISPUTE_EVIDENCE"' \
  || ! printf '%s' "${dispute_attachment_response}" | grep -q '"uploadMethod":"PUT"'; then
  echo "Smoke dispute attachment did not return DISPUTE_EVIDENCE metadata" >&2
  printf '%s\n' "${dispute_attachment_response}" >&2
  exit 1
fi

dispute_attachments_response="$(
  curl -fsS \
    -H "Authorization: Bearer ${performer_token}" \
    "${BASE_URL}/api/v1/disputes/${dispute_id}/attachments"
)"
if ! printf '%s' "${dispute_attachments_response}" | grep -q "${dispute_attachment_id}"; then
  echo "Smoke dispute attachments did not include created evidence" >&2
  printf '%s\n' "${dispute_attachments_response}" >&2
  exit 1
fi

my_disputes_response="$(
  curl -fsS \
    -H "Authorization: Bearer ${access_token}" \
    "${BASE_URL}/api/v1/my/disputes"
)"
if ! printf '%s' "${my_disputes_response}" | grep -q "${dispute_id}"; then
  echo "Smoke my disputes did not include opened dispute" >&2
  printf '%s\n' "${my_disputes_response}" >&2
  exit 1
fi

echo "Dev smoke passed on ${BASE_URL}. Cleanup will stop HandyNest and Docker project services."
