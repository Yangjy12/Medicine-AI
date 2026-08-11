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

echo "==> Deployment finished"
docker compose ps
