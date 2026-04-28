"""
Status Effects - Definition and Logic
Epic 18: US 73 (Enhanced Status System)
"""
from dataclasses import dataclass, field
from typing import Optional, Callable, Dict, Any

@dataclass
class StatusEffect:
    """Base Definition (Type)."""
    id: str

@dataclass
class StatusInstance:
    """
    Istanza runtime di uno status.
    US 73: Supporta durata, hooks per inizio turno e modificatori stats.
    """
    name: str
    id: str
    duration: int
    # Hooks: funzione che prende (combatant) e ritorna log string o None
    on_turn_start_fn: Optional[Callable[[Any], Optional[str]]] = None
    # Modificatori: {"atk": 1.5} (moltiplicatore)
    stat_modifiers: Dict[str, float] = field(default_factory=dict)

    def tick(self) -> bool:
        """Riduce la durata. Ritorna True se scaduto."""
        if self.duration > 0:
            self.duration -= 1
        return self.duration <= 0

    def modify_stat(self, stat_name: str, current_value: int) -> int:
        """Applica modificatori alle statistiche (Buff/Debuff)."""
        if stat_name in self.stat_modifiers:
            mod = self.stat_modifiers[stat_name]
            return int(current_value * mod)
        return current_value

# --- Concrete Definitions for System Hooks ---

@dataclass
class Confusion(StatusEffect):
    id: str = "confusion"
    affects_input: bool = True

@dataclass
class ResistanceBuff(StatusEffect):
    id: str = "resistance_buff"
    amount: int = 1

@dataclass
class Stun(StatusEffect):
    id: str = "stun"
    turns: int = 1