"""
Exploration Turn Manager for hotseat multiplayer (US24)
"""
from src.model.character import Character

class ExplorationTurnManager:
    """
    Manages turn order in exploration (Hub/Room)
    Only one character is active at a time (Hotseat).
    """

    def __init__(self):
        self._active_index: int = 0  # Start with first character
        self._awaiting_confirm: bool = False
        self._confirm_owner: str | None = None

    def get_active_index(self) -> int:
        return self._active_index

    def get_active_character(self, party_chars: list[Character]) -> Character | None:
        if 0 <= self._active_index < len(party_chars):
            return party_chars[self._active_index]
        return None

    def next_turn(self, party_chars: list[Character], enabled_mask: list[bool]):
        """
        Advance to next turn (next enabled, non-KO character).
        Sets handoff confirmation required.
        """
        if not party_chars or not enabled_mask:
            return

        start_index = self._active_index
        next_index = (self._active_index + 1) % len(party_chars)
        
        # Keep looking until we find an eligible character or loop back
        while True:
            # Check bounds just in case list sizes mismatch
            if next_index >= len(party_chars) or next_index >= len(enabled_mask):
                break 

            char = party_chars[next_index]
            is_enabled = enabled_mask[next_index]
            is_alive = char.hp > 0
            
            if is_enabled and is_alive:
                break  # Found eligible character
            
            next_index = (next_index + 1) % len(party_chars)
            
            # Prevent infinite loop if all are KO/disabled
            if next_index == start_index:
                break
        
        self._active_index = next_index
        self._awaiting_confirm = True
        
        active_char = self.get_active_character(party_chars)
        self._confirm_owner = active_char.owner_id if active_char else None

    def is_awaiting_confirm(self) -> bool:
        return self._awaiting_confirm

    def confirm_handoff(self):
        self._awaiting_confirm = False
        self._confirm_owner = None

    def can_move(self) -> bool:
        return not self._awaiting_confirm

    def can_interact(self) -> bool:
        return not self._awaiting_confirm

    def can_confirm(self) -> bool:
        return self._awaiting_confirm

    def get_handoff_message(self) -> str:
        if self._awaiting_confirm and self._confirm_owner:
            return f"{self._confirm_owner} Turn - Press Confirm"
        return ""