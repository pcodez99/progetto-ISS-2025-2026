from dataclasses import dataclass
from typing import Tuple


@dataclass
class VFXEntity:
    vfx_id: str
    pos: Tuple[int, int]
    lifetime_ms: int
    created_at_ms: int

    def is_expired(self, now_ms: int) -> bool:
        return (now_ms - self.created_at_ms) >= self.lifetime_ms
