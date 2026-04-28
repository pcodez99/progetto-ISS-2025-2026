"""
Targeting System - Logic for target selection and resolution.
Epic 19: US 77, 78
"""
from typing import List, Any, Optional
from src.model.utils.rng import RNG

class TargetingSystem:
    """
    Gestisce la logica di selezione dei bersagli (Scope -> Candidates -> Final Targets).
    Pura logica, nessuna dipendenza da UI o State.
    """

    SCOPE_SINGLE_ENEMY = "single_enemy"
    SCOPE_ALL_ENEMIES = "all_enemies"
    SCOPE_RANDOM_ENEMY = "random_enemy"
    SCOPE_SINGLE_ALLY = "single_ally"
    SCOPE_SELF = "self"

    @staticmethod
    def get_candidates(scope: str, user: Any, battle_ctx: Any) -> List[Any]:
        """
        Restituisce la lista di candidati su cui il cursore può muoversi.
        
        Args:
            scope: Uno degli SCOPE_* costanti.
            user: L'entità che sta agendo.
            battle_ctx: Il BattleContext corrente.
        """
        alive_enemies = battle_ctx.get_living_enemies()
        alive_party = battle_ctx.get_living_party()

        if scope in [TargetingSystem.SCOPE_SINGLE_ENEMY, TargetingSystem.SCOPE_ALL_ENEMIES]:
            return alive_enemies
        
        elif scope == TargetingSystem.SCOPE_RANDOM_ENEMY:
            # Anche se è random, potremmo voler mostrare i nemici validi
            return alive_enemies

        elif scope == TargetingSystem.SCOPE_SINGLE_ALLY:
            return alive_party

        elif scope == TargetingSystem.SCOPE_SELF:
            return [user]

        return []

    @staticmethod
    def resolve_final_targets(scope: str, selected_target: Optional[Any], candidates: List[Any], rng: RNG) -> List[Any]:
        """
        Dato il bersaglio selezionato col cursore (o nessuno), restituisce la lista REALE
        di chi subisce l'effetto.
        """
        if not candidates:
            return []

        if scope == TargetingSystem.SCOPE_SINGLE_ENEMY:
            return [selected_target] if selected_target else []

        elif scope == TargetingSystem.SCOPE_SINGLE_ALLY:
            return [selected_target] if selected_target else []

        elif scope == TargetingSystem.SCOPE_SELF:
            return [selected_target] if selected_target else []

        elif scope == TargetingSystem.SCOPE_ALL_ENEMIES:
            # Colpisce TUTTI i candidati (tutti i nemici vivi) indipendentemente dal cursore
            return list(candidates)

        elif scope == TargetingSystem.SCOPE_RANDOM_ENEMY:
            # Seleziona a caso dalla lista dei candidati
            return [rng.choice(candidates)]

        return []