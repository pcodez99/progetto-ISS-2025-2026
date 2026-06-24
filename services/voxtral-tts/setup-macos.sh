#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
VENV_DIR="$SCRIPT_DIR/.venv"
PYTHON_BIN="${PYTHON_BIN:-/opt/homebrew/bin/python3.12}"

if [[ "$(uname -s)" != "Darwin" ]] || [[ "$(uname -m)" != "arm64" ]]; then
  echo "Questo setup e destinato a macOS Apple Silicon." >&2
  exit 1
fi

if [[ ! -x "$PYTHON_BIN" ]]; then
  echo "Python 3.12 non trovato in $PYTHON_BIN." >&2
  echo "Imposta PYTHON_BIN oppure installa python@3.12 con Homebrew." >&2
  exit 1
fi

if [[ ! -x "$VENV_DIR/bin/python" ]]; then
  "$PYTHON_BIN" -m venv "$VENV_DIR"
fi

"$VENV_DIR/bin/python" -m pip install --upgrade pip
"$VENV_DIR/bin/python" -m pip install 'mlx-audio[server,tts]==0.4.4'

echo "Ambiente MLX pronto in $VENV_DIR"
