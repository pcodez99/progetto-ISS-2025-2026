# Voxtral TTS sidecar

Il gioco chiama Voxtral tramite l'API REST OpenAI-compatible di vLLM-Omni.
Il modello non viene incluso nel JAR o nel repository: viene scaricato nel
volume Docker `huggingface-cache` al primo avvio.

## macOS Apple Silicon, 16 GB

Sul Mac del progetto usiamo la conversione MLX 4-bit, che occupa circa 2,5 GB
su disco e mantiene lo stesso endpoint REST OpenAI-compatible:

```bash
./services/voxtral-tts/setup-macos.sh
./services/voxtral-tts/start-macos.sh
./services/voxtral-tts/smoke-test-macos.sh
```

`start-macos.sh` resta in primo piano: lascia aperto quel terminale e usa un
secondo terminale per il gioco o per lo smoke test.

Per fermare il processo:

```bash
./services/voxtral-tts/stop-macos.sh
```

Il modello usato e `mlx-community/Voxtral-4B-TTS-2603-mlx-4bit`, conversione
quantizzata del checkpoint Mistral. `macos_server.py` espone una piccola API
FastAPI e mantiene l'inferenza nel thread Metal principale; resta in ascolto
soltanto su `127.0.0.1:8000`.

## Linux/WSL2 con NVIDIA, 16 GB

### Requisiti

- Linux oppure Windows con WSL2;
- Docker con NVIDIA Container Toolkit;
- GPU NVIDIA con 16 GB di VRAM;
- spazio libero sufficiente per i pesi e la cache del modello.

Docker Desktop su macOS non espone Metal ai container: su Mac va usato il
launcher MLX nativo descritto sopra.

Il compose applica un profilo dedicato ai 16 GB: concorrenza uno, batch ridotti,
CUDA graph disabilitati e budget GPU complessivo dell'88%. E il profilo giusto
per il gioco, che produce una sola battuta breve alla volta; privilegia la
memoria rispetto al throughput.

Se questa configurazione va ancora in OOM sulla scheda scelta, il passo
successivo e aggiungere 2-4 GiB di CPU offload allo stage 0. L'offload riduce la
VRAM ma aumenta la latenza, quindi non viene attivato senza una misura reale.

### Avvio

```bash
./services/voxtral-tts/start.sh
```

Il server ascolta solamente su `127.0.0.1:8000` dell'host.

Controllo manuale:

```bash
curl http://127.0.0.1:8000/health
curl http://127.0.0.1:8000/v1/audio/voices
./services/voxtral-tts/smoke-test.sh
```

Per fermarlo:

```bash
docker compose -f services/voxtral-tts/compose.yaml down
```

Il modello e le voci fornite sono distribuiti con licenza CC BY-NC 4.0. Prima
di un uso commerciale va rivalutata la licenza.
