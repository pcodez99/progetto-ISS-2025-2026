#!/usr/bin/env bash
set -euo pipefail

TTS_MODEL="mlx-community/Voxtral-4B-TTS-2603-mlx-4bit" \
  "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/smoke-test.sh" "${1:-/tmp/viddani-voxtral-smoke.wav}"
