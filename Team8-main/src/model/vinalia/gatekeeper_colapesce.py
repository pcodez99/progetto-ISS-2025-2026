from src.model.items.item_ids import ItemIds
from src.model.status.status_effects import ResistanceBuff


class GatekeeperColapesce:
    """
    Rami:
    - Wine: no fight, ma party penalizzata all'arrivo boss (hurt)
    - Vinegar: forced fight, vinegar retained for boss (flag)
    - Marranzano: skip fight + resistance buff
    """

    def resolve(self, choice_id: str, party: list) -> dict:
        outcome = {
            "branch": None,
            "party_hp_penalty": 0,
            "forced_fight": False,
            "buff_applied": False,
            "vinegar_retained": False,
        }

        # FIX: Usa nomi costanti italiani (VINO_ETERNO, ACETO_MADRE, MARRANZANO_ORO)
        if choice_id == ItemIds.VINO_ETERNO:
            outcome["branch"] = "wine_skip_with_penalty"
            penalty_total = 0
            for ch in party:
                # penalità: 20% max_hp (min 1), mai sotto 1 HP
                penalty = max(1, int(getattr(ch, "max_hp", 1) * 0.2))
                ch.hp = max(1, int(getattr(ch, "hp", 1)) - penalty)
                penalty_total += penalty
            outcome["party_hp_penalty"] = penalty_total
            outcome["forced_fight"] = False

        elif choice_id == ItemIds.ACETO_MADRE:
            outcome["branch"] = "vinegar_forced_fight"
            outcome["forced_fight"] = True
            outcome["vinegar_retained"] = True

        elif choice_id == ItemIds.MARRANZANO_ORO:
            outcome["branch"] = "marranzano_secret_passage_buff"
            outcome["forced_fight"] = False
            for ch in party:
                ch.add_status(ResistanceBuff(id="resistance_buff", amount=1))
            outcome["buff_applied"] = True

        else:
            raise ValueError(f"Invalid choice_id: {choice_id}")

        return outcome