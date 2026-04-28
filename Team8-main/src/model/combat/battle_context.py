"""
Battle Context - Holds the state of the current combat encounter.
Epic 9: Combat System Advanced
"""

from dataclasses import dataclass, field
from typing import List, Optional, Any

@dataclass
class BattleContext:
    """
    Rappresenta lo stato volatile di uno scontro.
    """
    encounter_id: str
    party: List[Any]  # Characters
    enemies: List[Any]  # Enemy Entities
    turn_counter: int = 0
    is_completed: bool = False
    won: bool = False
    
    # Per gestire la selezione dei target
    target_selection_mode: bool = False
    current_target_index: int = 0

    def get_all_participants(self) -> List[Any]:
        """Ritorna tutti i combattenti vivi."""
        return [p for p in self.party + self.enemies if getattr(p, "hp", 0) > 0]

    def get_living_enemies(self) -> List[Any]:
        return [e for e in self.enemies if getattr(e, "hp", 0) > 0]

    def get_living_party(self) -> List[Any]:
        return [p for p in self.party if getattr(p, "hp", 0) > 0]