#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ENV_FILE:-$ROOT_DIR/.env}"
COMPOSE_FILE="${COMPOSE_FILE:-$ROOT_DIR/Docker/postgres.yml}"
BACKUP_DIR="${BACKUP_DIR:-$ROOT_DIR/backups/postgresql}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Environment file not found: $ENV_FILE" >&2
  exit 1
fi

set -a
source "$ENV_FILE"
set +a

mkdir -p "$BACKUP_DIR"
OUTPUT_FILE="${1:-$BACKUP_DIR/handynest-$(date -u +%Y%m%dT%H%M%SZ).dump}"

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T base-project-pg \
  pg_dump --username "${POSTGRES_USER:-postgres}" --dbname "${POSTGRES_DB:-handyDB}" \
  --format=custom --no-owner --no-privileges > "$OUTPUT_FILE"

echo "PostgreSQL backup created: $OUTPUT_FILE"
