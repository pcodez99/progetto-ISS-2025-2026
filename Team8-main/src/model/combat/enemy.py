"""
Enemy Model - Represents an enemy entity in combat.
Epic 18: US 71
"""
from dataclasses import dataclass, field
from typing import List, Any

@dataclass(eq=False)
class Enemy:
    """
    Rappresenta un nemico istanziato in combattimento.
    Impostato eq=False per usare l'identità dell'oggetto per l'hashing,
    permettendo di usare le istanze come chiavi nei dizionari (es. enemy_brains).
    """
    name: str
    hp: int
    max_hp: int
    atk: int
    defense: int
    magic: int = 0
    mdef: int = 0  # Magic Defense
    spd: int = 1
    ai_behavior: str = "aggressive" # aggressive, healer, boss
    statuses: List[Any] = field(default_factory=list)
    
    # Per compatibilità con Character nel DamageCalculator
    @property
    def current_hp(self):
        return self.hp
    
    @current_hp.setter
    def current_hp(self, value):
        self.hp = max(0, min(value, self.max_hp))

    @property
    def is_alive(self) -> bool:
        return self.hp > 0

    def add_status(self, status):
        self.statuses.append(status)

    def remove_status_by_id(self, sid: str) -> bool:
        before = len(self.statuses)
        self.statuses = [s for s in self.statuses if getattr(s, "id", None) != sid]
        return len(self.statuses) != before

    def get_stat(self, stat_name: str) -> int:
        """
        Calcola la statistica finale includendo i modificatori degli status (US 73).
        """
        # Mapping base stats
        val = 0
        if stat_name == "atk": val = self.atk
        elif stat_name == "def": val = self.defense
        elif stat_name == "matk": val = self.magic
        elif stat_name == "mdef": val = self.mdef
        elif stat_name == "spd": val = self.spd
        elif stat_name == "hp": val = self.hp
        elif stat_name == "max_hp": val = self.max_hp
        elif stat_name == "crit_rate": val = 5 # Base crit rate for enemies
        
        # Apply modifiers
        for status in self.statuses:
            if hasattr(status, "modify_stat"):
                val = status.modify_stat(stat_name, val)
        
        return int(max(0, val))

    @property
    def is_stunned(self):
        return any(getattr(s, "id", "") == "stun" for s in self.statuses)