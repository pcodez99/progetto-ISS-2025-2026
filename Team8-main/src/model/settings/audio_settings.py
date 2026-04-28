from dataclasses import dataclass


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


@dataclass(frozen=True)
class AudioSettings:
    master: float = 1.0
    music: float = 1.0
    sfx: float = 1.0

    def clamp(self) -> "AudioSettings":
        return AudioSettings(
            master=_clamp01(self.master),
            music=_clamp01(self.music),
            sfx=_clamp01(self.sfx),
        )
