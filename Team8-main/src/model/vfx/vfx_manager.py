from typing import List, Tuple, Optional
from src.model.vfx.vfx_entity import VFXEntity


class VFXManager:
    """
    MVP: mantiene lista VFXEntity e le rimuove deterministicamente dopo lifetime.
    In Epic 3 le renderizzerete su LAYER_VFX.
    """
    def __init__(self):
        self.entities: List[VFXEntity] = []

    def spawn(self, vfx_id: str, pos: Tuple[int, int], lifetime_ms: int, now_ms: int) -> None:
        self.entities.append(VFXEntity(vfx_id=vfx_id, pos=pos, lifetime_ms=int(lifetime_ms), created_at_ms=int(now_ms)))

    def update(self, now_ms: int) -> None:
        now_ms = int(now_ms)
        self.entities = [e for e in self.entities if not e.is_expired(now_ms)]
