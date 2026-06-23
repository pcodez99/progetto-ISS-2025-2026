"""Server REST minimale per Voxtral MLX su Apple Silicon.

L'inferenza resta nel thread principale di Uvicorn. MLX mantiene gli stream
Metal per thread, quindi evitare broker/thread pool rende il server stabile e
coerente con il caso d'uso del gioco: una sola battuta NPC alla volta.
"""

from __future__ import annotations

import argparse
import io
from typing import Any

import numpy as np
import soundfile as sf
import uvicorn
from fastapi import FastAPI, HTTPException
from fastapi.responses import Response
from mlx_audio.tts.utils import load
from pydantic import BaseModel, Field


DEFAULT_MODEL = "mlx-community/Voxtral-4B-TTS-2603-mlx-4bit"
VOICES = [
    "casual_female",
    "casual_male",
    "cheerful_female",
    "neutral_female",
    "neutral_male",
    "ar_male",
    "de_female",
    "de_male",
    "es_female",
    "es_male",
    "fr_female",
    "fr_male",
    "hi_female",
    "hi_male",
    "it_female",
    "it_male",
    "nl_female",
    "nl_male",
    "pt_female",
    "pt_male",
]


class SpeechRequest(BaseModel):
    input: str = Field(min_length=1, max_length=1000)
    model: str = DEFAULT_MODEL
    voice: str = "it_male"
    response_format: str = "wav"
    speed: float = Field(default=1.0, ge=0.25, le=4.0)


def create_app(model_id: str) -> FastAPI:
    app = FastAPI(title="Viddani Voxtral TTS", version="1.0")
    print(f"Carico {model_id}...", flush=True)
    model = load(model_id)
    print("Modello Voxtral caricato.", flush=True)

    @app.get("/health")
    async def health() -> dict[str, str]:
        return {"status": "ok"}

    @app.get("/v1/models")
    async def models() -> dict[str, list[dict[str, Any]]]:
        return {"data": [{"id": model_id, "object": "model"}]}

    @app.get("/v1/audio/voices")
    async def voices() -> dict[str, list[str]]:
        return {"voices": VOICES}

    @app.post("/v1/audio/speech")
    async def speech(request: SpeechRequest) -> Response:
        if request.model != model_id:
            raise HTTPException(status_code=404, detail=f"Modello non caricato: {request.model}")
        if request.voice not in VOICES:
            raise HTTPException(status_code=400, detail=f"Voce non supportata: {request.voice}")
        if request.response_format.lower() != "wav":
            raise HTTPException(status_code=400, detail="Il server locale supporta output WAV.")

        chunks: list[np.ndarray] = []
        sample_rate: int | None = None
        try:
            for result in model.generate(
                text=request.input.strip(),
                voice=request.voice,
                speed=request.speed,
                stream=False,
                verbose=False,
            ):
                chunks.append(np.asarray(result.audio, dtype=np.float32))
                sample_rate = int(result.sample_rate)
        except Exception as exc:
            raise HTTPException(status_code=500, detail=f"Sintesi Voxtral fallita: {exc}") from exc

        if not chunks or sample_rate is None:
            raise HTTPException(status_code=500, detail="Voxtral non ha generato audio.")

        audio = np.concatenate(chunks)
        buffer = io.BytesIO()
        sf.write(buffer, audio, sample_rate, format="WAV", subtype="PCM_16")
        return Response(content=buffer.getvalue(), media_type="audio/wav")

    return app


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8000)
    parser.add_argument("--model", default=DEFAULT_MODEL)
    args = parser.parse_args()

    uvicorn.run(create_app(args.model), host=args.host, port=args.port, workers=1)


if __name__ == "__main__":
    main()
