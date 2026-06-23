#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"

if [[ ! -f "$ENV_FILE" ]]; then
  umask 077
  printf 'TTS_PROVIDER=mistral\nMISTRAL_API_KEY=\n' >"$ENV_FILE"
  echo "Creato $ENV_FILE (ignorato da Git)."
  echo "Inserisci MISTRAL_API_KEY e rilancia questo script." >&2
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a

if [[ "${TTS_PROVIDER:-local}" == "mistral" && -z "${MISTRAL_API_KEY:-}" ]]; then
  echo "MISTRAL_API_KEY manca in $ENV_FILE." >&2
  exit 1
fi

cd "$ROOT_DIR"
exec ./gradlew lwjgl3:run
