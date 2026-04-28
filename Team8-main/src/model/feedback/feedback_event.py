from dataclasses import dataclass
from typing import Optional, Tuple


@dataclass(frozen=True)
class FeedbackEvent:
    log_text: str
    sfx_id: Optional[str] = None
    vfx_id: Optional[str] = None
    target_pos: Optional[Tuple[int, int]] = None
    kind: str = "combat"
