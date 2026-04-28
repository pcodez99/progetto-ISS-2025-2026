from __future__ import annotations
from dataclasses import dataclass
from typing import List, Optional


@dataclass(frozen=True)
class TurnPreviewRow:
    actor_id: str
    name: str
    is_ko: bool = False
    owner: Optional[str] = None  # "P1".. or None for enemies


@dataclass(frozen=True)
class CombatHUDData:
    active_name: str
    active_owner: Optional[str]
    turn_preview: List[TurnPreviewRow]
    status_ids: List[str]


class CombatHUDBuilder:
    """
    Regola scelta (coerente): KO non può essere active e viene escluso dal preview.
    """
    @staticmethod
    def from_combat(combat_state, party_ids: set[str], preview_n: int = 4) -> CombatHUDData:
        tm = getattr(combat_state, "turn_manager", None)
        log_model = getattr(combat_state, "log_model", None)  # non usato qui

        active = tm.active_actor() if tm else None
        active_is_ko = bool(getattr(active, "is_ko", False)) if active else False

        # Se active KO, chiedi al turn manager di advance o fallback "N/A"
        if active and active_is_ko and hasattr(tm, "advance_to_next_alive"):
            active = tm.advance_to_next_alive()

        active_name = getattr(active, "name", "N/A") if active else "N/A"
        active_id = getattr(active, "actor_id", None) if active else None

        def owner_badge(actor) -> Optional[str]:
            aid = getattr(actor, "actor_id", "")
            if aid in party_ids:
                # convention: party_ids è set di ids, owner viene da actor.owner_index (0..)
                oi = getattr(actor, "owner_index", 0)
                return f"P{int(oi)+1}"
            return None

        active_owner = owner_badge(active) if active else None

        # statuses list
        status_ids = []
        if active and hasattr(active, "status_ids"):
            status_ids = list(active.status_ids)

        # preview list
        preview: List[TurnPreviewRow] = []
        if tm and hasattr(tm, "peek_next"):
            next_list = tm.peek_next(int(preview_n))
            for a in next_list:
                if bool(getattr(a, "is_ko", False)):
                    continue  # rule: exclude
                preview.append(TurnPreviewRow(
                    actor_id=str(getattr(a, "actor_id", "")),
                    name=str(getattr(a, "name", "Unknown")),
                    is_ko=False,
                    owner=owner_badge(a),
                ))

        return CombatHUDData(
            active_name=str(active_name),
            active_owner=active_owner,
            turn_preview=preview,
            status_ids=[str(x) for x in status_ids],
        )
