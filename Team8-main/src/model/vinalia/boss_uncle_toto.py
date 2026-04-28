from src.model.items.item_ids import ItemIds
from src.model.status.status_effects import Confusion, Stun


class BossUncleToto:
    """
    Boss applica Confusion.
    Counter: se party ha Mother Vinegar => rimuove confusion da party e stunna il boss (placeholder).
    """
    def __init__(self):
        self.statuses = []
        self.log = []

    def add_status(self, s):
        self.statuses.append(s)

    def remove_status_by_id(self, sid: str) -> bool:
        before = len(self.statuses)
        self.statuses = [s for s in self.statuses if getattr(s, "id", None) != sid]
        return len(self.statuses) != before

    def apply_confusion(self, target):
        target.add_status(Confusion(id="confusion", affects_input=True))
        self.log.append("Boss applied CONFUSION")

    def on_player_use_vinegar(self, party: list, global_items: dict) -> dict:
        """
        Esplicito: richiede possesso vinegar.
        Effetto: rimuove confusion su party + stunna boss.
        """
        if (global_items or {}).get(ItemIds.MOTHER_VINEGAR, 0) <= 0:
            self.log.append("Vinegar used but not available")
            return {"ok": False, "reason": "no_vinegar"}

        removed_any = False
        for ch in party:
            removed_any = ch.remove_status_by_id("confusion") or removed_any

        self.add_status(Stun(id="stun", turns=1))
        self.log.append("Vinegar counter: confusion cleared + boss stunned")

        return {"ok": True, "confusion_removed": removed_any, "boss_stunned": True}
