import logging
from typing import Optional

from src.model.feedback.feedback_event import FeedbackEvent
from src.model.feedback.combat_log import CombatLog
from src.model.vfx.vfx_manager import VFXManager


class FeedbackManager:
    """
    API unica: gameplay non chiama audio/log/vfx ovunque.
    - log sempre
    - sfx/vfx opzionali
    - missing asset: deve degradare (audio manager già gestisce missing; vfx gestito con try/except)
    """
    def __init__(self, combat_log: CombatLog, audio_manager, vfx_manager: VFXManager, logger: Optional[logging.Logger] = None):
        self.combat_log = combat_log
        self.audio = audio_manager
        self.vfx = vfx_manager
        self.logger = logger or logging.getLogger(__name__)

    def emit(self, event: FeedbackEvent, now_ms: int = 0) -> None:
        # 1) log ALWAYS
        self.combat_log.add(event.log_text)

        # 2) SFX opzionale
        if event.sfx_id:
            try:
                self.audio.play_sfx(event.sfx_id, context={"kind": event.kind, "log": event.log_text})
            except Exception as e:
                # degrade: non crash
                self.logger.warning("Feedback SFX failed: sfx_id=%s err=%s", event.sfx_id, e)

        # 3) VFX opzionale
        if event.vfx_id and event.target_pos is not None:
            try:
                self.vfx.spawn(event.vfx_id, event.target_pos, lifetime_ms=250, now_ms=now_ms)
            except Exception as e:
                self.logger.warning("Feedback VFX failed: vfx_id=%s err=%s", event.vfx_id, e)
