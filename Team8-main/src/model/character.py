"""
Character Model - Character creation and management
Epic 4 (Stats) + Epic 18 (Status) + Epic 23 (Ownership) + RPG Progression
Updated: Added persistent x,y coordinates and explicit stat growth methods.
"""
from enum import Enum

class OwnerId(str, Enum):
    """Immutable owner IDs for party characters (US 23)"""
    P1 = "P1"
    P2 = "P2"

class Character:
    """Represents a playable character with stats, abilities, inventory and statuses."""
    
    def __init__(self):
        self.name = ""
        self.hp = 0
        self.max_hp = 0
        self.atk = 0
        self.defense = 0
        self.magic = 0
        self.res = 0 
        self.spd = 0
        self.crit_rate = 5 
        self.inventory = None
        self.abilities = None
        self.special_abilities = []
        self.statuses = []
        
        # Epic 23: Ownership
        self._owner_id: str | None = None
        self._char_id: str | None = None
        
        # RPG Progression Tracking
        self.regions_completed = 0 

        # POSIZIONE PERSISTENTE (Per visualizzare 2 entità nell'Hub)
        self.x = 400
        self.y = 300

    @property
    def owner_id(self) -> str | None:
        return self._owner_id

    @owner_id.setter
    def owner_id(self, value: str):
        self._owner_id = value

    @property
    def char_id(self) -> str | None:
        return self._char_id

    @char_id.setter
    def char_id(self, value: str):
        self._char_id = value

    @property
    def current_hp(self):
        return self.hp
    
    @current_hp.setter
    def current_hp(self, value):
        self.hp = max(0, min(value, self.max_hp))

    @property
    def is_alive(self) -> bool:
        return self.hp > 0

    def add_status(self, s):
        self.statuses.append(s)

    def remove_status_by_id(self, sid: str) -> bool:
        before = len(self.statuses)
        self.statuses = [s for s in self.statuses if getattr(s, "id", None) != sid]
        return len(self.statuses) != before

    def get_stat(self, stat_name: str) -> int:
        val = 0
        stat_name = stat_name.lower()
        
        if stat_name == "atk": val = self.atk
        elif stat_name in ("def", "defense"): val = self.defense
        elif stat_name in ("matk", "magic"): val = self.magic
        elif stat_name in ("mdef", "res"): val = self.res
        elif stat_name == "spd": val = self.spd
        elif stat_name == "max_hp": val = self.max_hp
        elif stat_name == "crit_rate": val = self.crit_rate
        
        for status in self.statuses:
            if hasattr(status, "modify_stat"):
                val = status.modify_stat(stat_name, val)
                
        return int(max(0, val))

    def apply_permanent_bonus(self, stat_name: str, value: int):
        """
        Applies a permanent stat increase (Growth).
        Used by Regional Choices (Doors).
        """
        stat_name = stat_name.lower()
        if stat_name == "hp" or stat_name == "max_hp":
            self.max_hp += value
            self.hp += value # Healing and increasing cap
        elif stat_name in ["atk", "attack"]:
            self.atk += value
        elif stat_name in ["def", "defense"]:
            self.defense += value
        elif stat_name in ["mag", "magic", "matk"]:
            self.magic += value
        elif stat_name in ["res", "resistance", "mdef"]:
            self.res += value
        elif stat_name in ["spd", "speed"]:
            self.spd += value
        elif stat_name in ["crit", "crit_rate"]:
            self.crit_rate += value

    @property
    def is_stunned(self):
        return any(getattr(s, "id", "") == "stun" for s in self.statuses)

    def get_inventory_in_view_format(self):
        if self.inventory:
            return self.inventory.to_view_format(), self.inventory.max_capacity, self.inventory.number_of_items
        return [], 0, 0
    
    def get_abilities_in_view_format(self):
        view_format = []
        if self.abilities:
            for ability in self.abilities:
                view_format.append({"name": ability.name, "description": ability.description})
        for sp_ab in self.special_abilities:
             view_format.append({"name": f"[ACE] {sp_ab['name']}", "description": sp_ab['description']})
        return view_format

    def learn_special_ability(self, name: str, description: str):
        self.special_abilities.append({"name": name, "description": description})

# --- Factory & Inventory definitions ---

class Inventory:
    def __init__(self):
        self.max_capacity = 10
        self.number_of_items = 0
        self.items = []

    def to_view_format(self) -> list:
        return [{"name": i.name, "description": i.description} for i in self.items]
    
    def add_item(self, name: str, description: str) -> bool:
        if self.number_of_items >= self.max_capacity: return False
        to_add = Item(); to_add.name = name; to_add.description = description
        self.number_of_items += 1; self.items.append(to_add)
        return True
    
    def remove_item(self, name: str) -> bool:
        for i, item in enumerate(self.items):
            if item.name == name:
                self.items.pop(i); self.number_of_items -= 1; return True
        return False
    
    def has_item(self, name: str) -> bool:
        return any(item.name == name for item in self.items)

class Ability:
    def __init__(self):
        self.name = ""; self.description = ""

class Item:
    def __init__(self):
        self.name = ""; self.description = ""

class Char_Builder:
    def build_character(self, player_index: int = 1) -> Character:
        character = Character()
        character.name = f'Player{player_index}'
        character.hp = 100
        character.max_hp = 100
        character.atk = 10      
        character.defense = 5
        character.magic = 10
        character.res = 5
        character.spd = 5
        character.crit_rate = 5
        character.inventory = Inventory()
        
        ab1 = Ability(); ab1.name = "Attacco Base"; ab1.description = "Colpo standard"
        character.abilities = [ab1]
        
        character.owner_id = f"P{player_index}"
        character.char_id = f"char_p{player_index}"
        
        # Posizioni di default distinte nell'Hub per evitare sovrapposizioni
        character.x = 350 + (player_index * 50) 
        character.y = 300
        
        return character