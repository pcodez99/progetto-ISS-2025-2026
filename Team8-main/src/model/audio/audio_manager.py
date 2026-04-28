import logging
from typing import Optional
from src.model.audio.audio_asset_loader import AudioAssetLoader
from src.model.settings.audio_settings import AudioSettings



class AudioManager:
    """
    Unico punto autorizzato a interagire con pygame.mixer (centralizzazione).
    Supporta:
    - BGM via mixer.music
    - SFX overlapping via canali SFX dedicati (pool)
    - missing assets => warning + skip, no crash
    """

    def __init__(
        self,
        loader: Optional[AudioAssetLoader] = None,
        mixer_module=None,
        logger: Optional[logging.Logger] = None,
        sfx_channels: int = 8,
        default_bgm_volume: float = 1.0,
        default_sfx_volume: float = 1.0,
       
    ):
        self.logger = logger or logging.getLogger(__name__)
        self.loader = loader or AudioAssetLoader(logger=self.logger)
        self.mixer = mixer_module  # pygame.mixer o mock

        self.sfx_channels = int(sfx_channels)
        self.default_bgm_volume = float(default_bgm_volume)
        self.default_sfx_volume = float(default_sfx_volume)

        # Volumi "logici" (US38): master/music/sfx
        self._master = 1.0
        self._music = 1.0
        self._sfx = 1.0

        self._initialized = False
        self._current_bgm_id: Optional[str] = None
        self._next_sfx_channel_index = 0

    def initialize(self, frequency: int = 44100, size: int = -16, channels: int = 2, buffer: int = 512) -> None:
        """
        Bootstrap minimale consentito. Non deve crashare mai il gioco.
        """
        if self._initialized:
            return

        if self.mixer is None:
            try:
                import pygame  # type: ignore
                self.mixer = pygame.mixer
            except Exception as e:
                # pygame non disponibile (es. ambiente CI/test): audio disabilitato, no crash
                self.logger.warning("Audio disabled (pygame not available): err=%s", e)
                self._initialized = False
                return


        try:
            self.mixer.init(frequency=frequency, size=size, channels=channels, buffer=buffer)
            self.mixer.set_num_channels(self.sfx_channels)
            self._initialized = True
        except Exception as e:
            self.logger.warning("Audio init failed: err=%s", e)
            self._initialized = False

    def play_bgm(self, track_id: str, fade_ms: int = 500, loop: bool = True, context: Optional[dict] = None) -> None:
        """
        Deterministico:
        - se track_id == current => non fa nulla (continua)
        - altrimenti: fadeout precedente + load/play nuovo con fade-in
        """
        if track_id == self._current_bgm_id:
            return

        if self.mixer is None:
            self.initialize()

        if self.mixer is None:
            # audio disabilitato (pygame non disponibile o init fallita)
            return

        bgm_path = f"assets/audio/bgm/{track_id}"

        import os
        if not os.path.exists(bgm_path):
            self.logger.warning(
                "Missing BGM track: track_id=%s resolved_path=%s context=%s",
                track_id, bgm_path, context
            )
            return

        try:
            # Fadeout vecchia traccia (best-effort)
            try:
                self.mixer.music.fadeout(fade_ms)
            except Exception:
                pass

            self.mixer.music.load(bgm_path)
            self.mixer.music.set_volume(self.default_bgm_volume)
            loops = -1 if loop else 0
            self.mixer.music.play(loops=loops, fade_ms=fade_ms)

            self._current_bgm_id = track_id
        except Exception as e:
            self.logger.warning("Failed playing BGM: track_id=%s err=%s context=%s", track_id, e, context)

    def stop_bgm(self, fade_ms: int = 500, context: Optional[dict] = None) -> None:
        if self.mixer is None:
            self.initialize()

        try:
            self.mixer.music.fadeout(fade_ms)
        except Exception as e:
            self.logger.warning("Failed stopping BGM: err=%s context=%s", e, context)
        finally:
            self._current_bgm_id = None

    def play_sfx(self, sfx_id: str, volume_scale: float = 1.0, context: Optional[dict] = None) -> None:
        """
        Overlapping playback:
        usa un pool di canali; se occupati tutti, sovrascrive in round-robin.
        """
        if self.mixer is None:
            self.initialize()

        if self.mixer is None:
            return


        sound = self.loader.get_sound(sfx_id, context=context, mixer_module=self.mixer)
        if sound is None:
            # In prod spesso il loader logga già, ma in test può essere mockato:
            self.logger.warning("Missing SFX (or failed load): sfx_id=%s context=%s", sfx_id, context)
            return


        try:
            ch_index = self._next_sfx_channel_index % max(1, self.sfx_channels)
            self._next_sfx_channel_index += 1

            channel = self.mixer.Channel(ch_index)
            effective = self._master * self._sfx * float(volume_scale)
            sound.set_volume(effective)
            channel.play(sound)
        except Exception as e:
            self.logger.warning("Failed playing SFX: sfx_id=%s err=%s context=%s", sfx_id, e, context)


    def set_volumes(self, settings: AudioSettings, context: Optional[dict] = None) -> None:
        """
        Applica immediatamente:
        - music volume: pygame.mixer.music.set_volume(master * music)
        - sfx: usato come moltiplicatore in play_sfx
        """
        settings = settings.clamp()
        self._master = settings.master
        self._music = settings.music
        self._sfx = settings.sfx

        if self.mixer is None:
            self.initialize()

        try:
            # master*music
            effective_music = self._master * self._music
            self.mixer.music.set_volume(effective_music)
        except Exception as e:
            self.logger.warning("Failed applying music volume: err=%s context=%s", e, context)
