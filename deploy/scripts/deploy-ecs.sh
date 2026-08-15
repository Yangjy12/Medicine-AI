#!/usr/bin/env bash
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/medicine-ai}"
REPO_URL="${REPO_URL:-https://github.com/Yangjy12/Medicine-AI.git}"

echo "==> Preparing application directory: ${APP_DIR}"
mkdir -p "${APP_DIR}"

if [ ! -d "${APP_DIR}/.git" ]; then
  git clone "${REPO_URL}" "${APP_DIR}"
else
  git -C "${APP_DIR}" pull --ff-only
fi

cd "${APP_DIR}"

if [ ! -f .env ]; then
  cp .env.example .env
  echo "==> Created .env from .env.example. Please update passwords before production use."
fi

echo "==> Starting Docker services"
docker compose up -d --build

if [ -f .env ]; then
  set -a
  # shellcheck disable=SC1091
  . ./.env
  set +a
fi

MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-xinglin_mysql_pwd}"
MYSQL_DATABASE="${MYSQL_DATABASE:-xinglin_video}"

echo "==> Waiting for MySQL"
for attempt in $(seq 1 60); do
  if docker compose exec -T mysql mysqladmin ping -uroot -p"${MYSQL_ROOT_PASSWORD}" --silent >/dev/null 2>&1; then
    break
  fi
  if [ "${attempt}" -eq 60 ]; then
    echo "MySQL is not ready after waiting" >&2
    exit 1
  fi
  sleep 2
done

if [ -d deploy/mysql/migrations ]; then
  for migration in deploy/mysql/migrations/*.sql; do
    [ -e "${migration}" ] || continue
    echo "==> Applying migration ${migration}"
    docker compose exec -T mysql mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" "${MYSQL_DATABASE}" < "${migration}"
  done
fi

echo "==> Deployment finished"
docker compose ps
