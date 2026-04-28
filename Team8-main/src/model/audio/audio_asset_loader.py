import os
import logging
from typing import Optional, Any, Dict


class AudioAssetLoader:
    """
    Carica e cache-a SFX come pygame.mixer.Sound.
    In test può essere usato con mock del mixer, quindi non richiede pygame reale.
    """

    def __init__(self, base_dir: str = "assets/audio", logger: Optional[logging.Logger] = None):
        self.base_dir = base_dir
        self._cache: Dict[str, Any] = {}
        self.logger = logger or logging.getLogger(__name__)

    def _resolve_path(self, sound_id_or_path: str) -> str:
        # Se è già un path (assoluto o già sotto base_dir), usalo
        if os.path.isabs(sound_id_or_path) or sound_id_or_path.startswith(self.base_dir):
            return sound_id_or_path
        return os.path.join(self.base_dir, sound_id_or_path)

    def get_sound(self, sound_id_or_path: str, context: Optional[dict] = None, mixer_module=None) -> Optional[Any]:
        """
        Ritorna Sound oppure None se manca / fallisce il load.
        mixer_module viene iniettato (pygame.mixer o mock) per testabilità.
        """
        if sound_id_or_path in self._cache:
            return self._cache[sound_id_or_path]

        path = self._resolve_path(sound_id_or_path)

        if not os.path.exists(path):
            self.logger.warning(
                "Missing SFX asset: id_or_path=%s resolved_path=%s context=%s",
                sound_id_or_path, path, context
            )
            self._cache[sound_id_or_path] = None
            return None

        if mixer_module is None:
            # Lazy import per non rompere i test se pygame non è disponibile.
            import pygame  # type: ignore
            mixer_module = pygame.mixer

        try:
            sound = mixer_module.Sound(path)
            self._cache[sound_id_or_path] = sound
            return sound
        except Exception as e:
            self.logger.warning(
                "Failed loading SFX asset: id_or_path=%s resolved_path=%s err=%s context=%s",
                sound_id_or_path, path, e, context
            )
            self._cache[sound_id_or_path] = None
            return None
