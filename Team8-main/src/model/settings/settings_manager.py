import json
import os
import logging
from dataclasses import asdict
from typing import Optional

from src.model.settings.audio_settings import AudioSettings


def _clamp01(x: float) -> float:
    try:
        x = float(x)
    except Exception:
        return 1.0
    if x < 0.0:
        return 0.0
    if x > 1.0:
        return 1.0
    return x


class SettingsManager:
    """
    Settings unificati (Epic 29 US118 + Epic 10/US38 audio):
    - API Epic29: volume/fullscreen/keybinds + load/save + set_volume/set_fullscreen/set_keybind
    - API US38: load_audio_settings/save_audio_settings(settings: AudioSettings) + logger injectable
    Persistenza in un unico file JSON:
      {
        "volume": 0.3,
        "fullscreen": false,
        "keybinds": {...},
        "audio": {"master": 0.3, "music": 1.0, "sfx": 1.0}
      }
    """

    def __init__(self, path: str = "settings.json", logger: Optional[logging.Logger] = None):
        self.path = path
        self.logger = logger or logging.getLogger(__name__)

        # Epic29 API
        self.volume = 1.0
        self.fullscreen = False
        self.keybinds = {"attack": "SPACE", "inventory": "I"}

        # Audio channels (US38)
        self.audio = {"master": 1.0, "music": 1.0, "sfx": 1.0}

    # -----------------------
    # Epic29 persistence API
    # -----------------------
    def load(self) -> None:
        if not os.path.exists(self.path):
            return
        try:
            with open(self.path, "r", encoding="utf-8") as f:
                data = json.load(f)
        except Exception as e:
            # file corrotto => defaults (US119)
            self.logger.warning("Corrupt settings file. Using defaults. path=%s err=%s", self.path, e)
            return

        audio = data.get("audio", {})
        self.audio["master"] = _clamp01(audio.get("master", self.audio["master"]))
        self.audio["music"] = _clamp01(audio.get("music", self.audio["music"]))
        self.audio["sfx"] = _clamp01(audio.get("sfx", self.audio["sfx"]))

        # volume compat: se c'è volume lo usiamo, sennò master
        self.volume = _clamp01(data.get("volume", self.audio["master"]))
        # volume è alias di master
        self.audio["master"] = self.volume

        self.fullscreen = bool(data.get("fullscreen", self.fullscreen))
        self.keybinds = dict(data.get("keybinds", self.keybinds))

    def save(self) -> None:
        data = {
            "volume": _clamp01(self.volume),
            "fullscreen": bool(self.fullscreen),
            "keybinds": dict(self.keybinds),
            "audio": {
                "master": _clamp01(self.audio.get("master", 1.0)),
                "music": _clamp01(self.audio.get("music", 1.0)),
                "sfx": _clamp01(self.audio.get("sfx", 1.0)),
            },
        }
        os.makedirs(os.path.dirname(self.path) or ".", exist_ok=True)
        with open(self.path, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2)

    def apply(self, game=None) -> bool:
        """
        Hook immediato:
        - se game ha AudioManager, applica master/music/sfx
        - fullscreen: verrà gestito quando avrete pygame display attivo
        """
        if game is not None and hasattr(game, "audio"):
            try:
                game.audio.set_volumes(self.load_audio_settings(), context={"source": "settings.apply"})
            except Exception as e:
                self.logger.warning("Failed applying audio settings to AudioManager: err=%s", e)
        return True

    def set_volume(self, v: float) -> None:
        self.volume = _clamp01(v)
        self.audio["master"] = self.volume

    def set_fullscreen(self, b: bool) -> None:
        self.fullscreen = bool(b)

    def set_keybind(self, action: str, key: str) -> None:
        self.keybinds[str(action)] = str(key)

    # -----------------------
    # US38 audio settings API
    # -----------------------
    def load_audio_settings(self) -> AudioSettings:
        """
        Compatibilità con i test US38:
        - se file manca/corrotto => defaults + warning (corrotto)
        - clamp [0,1]
        """
        if not os.path.exists(self.path):
            return AudioSettings().clamp()

        try:
            with open(self.path, "r", encoding="utf-8") as fp:
                data = json.load(fp)
        except Exception as e:
            self.logger.warning("Corrupt/malformed settings file. Using defaults. path=%s err=%s", self.path, e)
            return AudioSettings().clamp()

        audio = data.get("audio", {})
        # supporta anche vecchio formato piatto (se mai lo avete usato)
        master = audio.get("master", data.get("master", data.get("volume", 1.0)))
        music = audio.get("music", data.get("music", 1.0))
        sfx = audio.get("sfx", data.get("sfx", 1.0))

        settings = AudioSettings(master=master, music=music, sfx=sfx).clamp()

        # mantieni sincronizzati i campi Epic29
        self.volume = settings.master
        self.audio["master"] = settings.master
        self.audio["music"] = settings.music
        self.audio["sfx"] = settings.sfx

        return settings

    def save_audio_settings(self, settings: AudioSettings) -> bool:
        """
        Compatibilità con i test US38: salva SOLO l'audio (ma nel file unificato).
        Ritorna True/False.
        """
        try:
            settings = settings.clamp()
            # aggiorna memoria
            self.volume = settings.master
            self.audio["master"] = settings.master
            self.audio["music"] = settings.music
            self.audio["sfx"] = settings.sfx

            # scrivi file unificato preservando fullscreen/keybinds se presenti
            existing = {}
            if os.path.exists(self.path):
                try:
                    with open(self.path, "r", encoding="utf-8") as fp:
                        existing = json.load(fp) or {}
                except Exception:
                    existing = {}

            existing["volume"] = settings.master
            existing["audio"] = asdict(settings)
            existing.setdefault("fullscreen", bool(self.fullscreen))
            existing.setdefault("keybinds", dict(self.keybinds))

            os.makedirs(os.path.dirname(self.path) or ".", exist_ok=True)
            with open(self.path, "w", encoding="utf-8") as fp:
                json.dump(existing, fp, indent=2)
            return True
        except Exception as e:
            self.logger.warning("Failed saving audio settings. path=%s err=%s", self.path, e)
            return False
