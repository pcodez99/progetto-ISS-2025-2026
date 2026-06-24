#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${TTS_BASE_URL:-http://127.0.0.1:8000/v1}"
MODEL="${TTS_MODEL:-mistralai/Voxtral-4B-TTS-2603}"
OUTPUT_FILE="${1:-/tmp/viddani-voxtral-smoke.wav}"

curl --fail --silent --show-error "$BASE_URL/models" >/dev/null

curl --fail --silent --show-error \
  -X POST "$BASE_URL/audio/speech" \
  -H "Content-Type: application/json" \
  -d "{\"input\":\"Picciotti, sti cosi verdi mi hanno scassato il trattore!\",\"model\":\"$MODEL\",\"response_format\":\"wav\",\"voice\":\"it_male\"}" \
  --output "$OUTPUT_FILE"

echo "Audio generato: $OUTPUT_FILE"
