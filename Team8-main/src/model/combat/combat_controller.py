from src.model.feedback.feedback_event import FeedbackEvent


class CombatController:
    """
    MVP: dimostra l'uso centralizzato di FeedbackManager.
    In futuro qui chiamerete danno reale, abilities, items, ecc.
    """
    def __init__(self, feedback_manager):
        self.feedback = feedback_manager

    def attack(self, attacker_name: str, target_name: str, damage: int, target_pos=(0, 0), now_ms: int = 0) -> FeedbackEvent:
        event = FeedbackEvent(
            log_text=f"{attacker_name} hits {target_name} for {damage}",
            sfx_id="sfx_hit.wav",
            vfx_id="vfx_slash",
            target_pos=target_pos,
            kind="combat",
        )
        self.feedback.emit(event, now_ms=now_ms)
        return event
