#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ENV_FILE:-$ROOT_DIR/.env}"
COMPOSE_FILE="${COMPOSE_FILE:-$ROOT_DIR/Docker/postgres.yml}"
CONFIRMATION="${1:-}"
BACKUP_FILE="${2:-}"

if [[ "$CONFIRMATION" != "--confirm-destructive" || -z "$BACKUP_FILE" ]]; then
  echo "Usage: $0 --confirm-destructive /absolute/path/to/backup.dump" >&2
  exit 2
fi
if [[ ! -f "$BACKUP_FILE" ]]; then
  echo "Backup file not found: $BACKUP_FILE" >&2
  exit 1
fi
if [[ ! -f "$ENV_FILE" ]]; then
  echo "Environment file not found: $ENV_FILE" >&2
  exit 1
fi

set -a
source "$ENV_FILE"
set +a

echo "Restoring $BACKUP_FILE into ${POSTGRES_DB:-handyDB}; existing objects will be replaced."
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T base-project-pg \
  pg_restore --username "${POSTGRES_USER:-postgres}" --dbname "${POSTGRES_DB:-handyDB}" \
  --clean --if-exists --no-owner --no-privileges < "$BACKUP_FILE"

echo "PostgreSQL restore completed. Run application startup to validate Liquibase and Hibernate."
