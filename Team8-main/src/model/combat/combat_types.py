"""
Combat Types - Data definitions for Combat System
Epic 18: US 71, US 72
"""
from dataclasses import dataclass, field
from typing import List, Optional

@dataclass
class Encounter:
    """Definisce un incontro di combattimento (US 71)."""
    encounter_id: str
    enemy_ids: List[str]
    reward_script_id: Optional[str] = None
    background_id: Optional[str] = None

@dataclass
class DamageResult:
    """Risultato del calcolo del danno (US 72)."""
    damage: int
    is_crit: bool
    is_miss: bool
    hit_type: str  # "physical", "magical", "true"