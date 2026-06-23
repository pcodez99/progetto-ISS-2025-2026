#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="$SCRIPT_DIR/voxtral.pid"

if [[ ! -f "$PID_FILE" ]]; then
  echo "Nessun server Voxtral registrato."
  exit 0
fi

PID="$(cat "$PID_FILE")"
if kill -0 "$PID" 2>/dev/null; then
  kill "$PID"
  echo "Server Voxtral arrestato."
fi
rm -f "$PID_FILE"
