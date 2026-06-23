#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VENV_DIR="$SCRIPT_DIR/.venv"
PID_FILE="$SCRIPT_DIR/voxtral.pid"
LOG_FILE="$SCRIPT_DIR/voxtral.log"
BASE_URL="http://127.0.0.1:8000/v1"
MODEL="mlx-community/Voxtral-4B-TTS-2603-mlx-4bit"

# Il backend Xet puo bloccarsi su alcune reti domestiche; il download HTTP
# standard e piu lento ma riprendibile e prevedibile per questo modello.
export HF_HUB_DISABLE_XET="${HF_HUB_DISABLE_XET:-1}"
export HF_HUB_DOWNLOAD_TIMEOUT="${HF_HUB_DOWNLOAD_TIMEOUT:-600}"

if [[ ! -x "$VENV_DIR/bin/mlx_audio.server" ]]; then
  "$SCRIPT_DIR/setup-macos.sh"
fi

if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  echo "Il server Voxtral e gia in esecuzione con PID $(cat "$PID_FILE")."
  exit 0
fi

nohup "$VENV_DIR/bin/python" "$SCRIPT_DIR/macos_server.py" \
  --host 127.0.0.1 \
  --port 8000 \
  --model "$MODEL" \
  >"$LOG_FILE" 2>&1 &
SERVER_PID=$!
echo "$SERVER_PID" >"$PID_FILE"

cleanup() {
  if kill -0 "$SERVER_PID" 2>/dev/null; then
    kill "$SERVER_PID" 2>/dev/null || true
  fi
  rm -f "$PID_FILE"
}
trap cleanup EXIT INT TERM

echo "Attendo l'avvio del server MLX..."
for _ in $(seq 1 180); do
  if curl --fail --silent "$BASE_URL/models" >/dev/null 2>&1; then
    echo "Voxtral pronto su $BASE_URL"
    echo "Lascia questo terminale aperto; Ctrl+C arresta il server."
    wait "$SERVER_PID"
    exit 0
  fi
  sleep 1
done

echo "Il server non si e avviato entro 180 secondi. Controlla $LOG_FILE" >&2
exit 1
