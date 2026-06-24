#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker non trovato. Installa Docker con supporto NVIDIA." >&2
  exit 1
fi

if ! docker info >/dev/null 2>&1; then
  echo "Il daemon Docker non e in esecuzione." >&2
  exit 1
fi

if ! command -v nvidia-smi >/dev/null 2>&1; then
  echo "Voxtral richiede una macchina Linux/WSL2 con GPU NVIDIA da 16 GB di VRAM." >&2
  echo "Questo launcher non puo usare la GPU Metal di macOS dentro Docker." >&2
  exit 1
fi

docker compose -f "$SCRIPT_DIR/compose.yaml" up -d
echo "Avvio Voxtral in corso. Segui i log con:"
echo "docker compose -f '$SCRIPT_DIR/compose.yaml' logs -f"
