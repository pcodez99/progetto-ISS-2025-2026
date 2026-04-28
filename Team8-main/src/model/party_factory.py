"""
Party Factory - Manages 2-slot party composition (Updated for Turn-Based RPG req)
"""
from src.model.character import Char_Builder, Character

class PartyModel:
    """Model for managing party composition (2 mains + 1 guest)"""

    def __init__(self):
        self.main_characters: list[Character] = []
        # Mask: True if player slot is active (Human or AI enabled)
        self.enabled_mask: list[bool] = [False, False] # Reduced to 2
        self.guest_id: str | None = None

    def set_enabled_count(self, count: int):
        """Enable the first N characters, disable the rest"""
        count = max(1, min(2, count))  # Clamp to 1-2
        self.enabled_mask = [True] * count + [False] * (2 - count)

    def has_guest(self) -> bool:
        return self.guest_id is not None
    
    def has_guest_id(self, gid: str) -> bool:
        return self.guest_id == gid

    def get_active_character(self, active_index: int) -> Character | None:
        """Get the currently active character based on exploration index"""
        if 0 <= active_index < len(self.main_characters):
            return self.main_characters[active_index]
        return None

    def get_enabled_characters(self) -> list[Character]:
        """Get only enabled characters"""
        return [char for char, enabled in zip(self.main_characters, self.enabled_mask) if enabled]

class PartyFactory:
    """Factory for creating standard parties"""

    def create_main_party(self) -> PartyModel:
        """Create a party with 2 main characters (RPG Mode)"""
        party = PartyModel()
        
        # Player 1 (Il Leader)
        p1 = Char_Builder().build_character(player_index=1)
        p1.name = "Turiddu" # Nome siciliano tipico
        p1.owner_id = "P1"
        party.main_characters.append(p1)

        # Player 2 (Il Compagno)
        p2 = Char_Builder().build_character(player_index=2)
        p2.name = "Rosalia" # Nome siciliano tipico
        p2.owner_id = "P2"
        party.main_characters.append(p2)
        
        return party