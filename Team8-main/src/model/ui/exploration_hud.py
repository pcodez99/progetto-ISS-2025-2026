# src/model/ui/exploration_hud.py (FIXED)
"""
Exploration HUD with active character indication (US26)
"""

from dataclasses import dataclass
from typing import List, Optional, Any


@dataclass
class ExplorationHUDData:
    """Data for rendering exploration HUD"""
    active_owner: str
    active_name: str
    active_char_id: str
    party_hp_list: List[dict]  # All party members for UI
    zone_label: str
    interact_hint: str
    end_turn_hint: str = "TAB: End Turn"
    handoff_message: Optional[str] = None
    aces_count: int = 0
    aces_total: int = 4
    aces_collected: List[str] = None
    interact_text: Optional[str] = None # Legacy support


class ExplorationHUDBuilder:
    """Builds HUD data from game models"""

    @staticmethod
    def from_models(game_model, room_model, turn_state, input_hints: dict) -> ExplorationHUDData:
        """
        turn_state: ExplorationTurnManager object OR dict (legacy fallback)
        """
        party = game_model.gamestate.party
        
        # FIX: Handle turn_state as Object (New) or Dict (Old Tests)
        if isinstance(turn_state, dict):
            active_index = int(turn_state.get("active_index", 0))
            is_awaiting = False
            handoff_msg = None
        else:
            active_index = turn_state.get_active_index()
            is_awaiting = turn_state.is_awaiting_confirm()
            handoff_msg = turn_state.get_handoff_message()

        active_char = party.main_characters[active_index] if party.main_characters else None
        
        # Build party HP list (all characters, but show disabled/KO status)
        party_hp_list = []
        for i, char in enumerate(party.main_characters):
            # Safe access to enabled_mask
            is_enabled = party.enabled_mask[i] if i < len(party.enabled_mask) else False
            
            party_hp_list.append({
                "owner": char.owner_id,
                "name": char.name,
                "hp": char.hp,
                "max_hp": char.max_hp,
                "enabled": is_enabled,
                "is_ko": char.hp <= 0,
                "is_active": i == active_index
            })
        
        # Interact Text Logic (Hybrid: US 61 + US 32)
        interact_key = input_hints.get('interact', 'E')
        interact_text = None
        
        # 1. Check override from dict turn_state (legacy test support)
        if isinstance(turn_state, dict) and turn_state.get("can_interact"):
             interact_text = f"{interact_key}: Interact"
        # 2. Check Game Active Interactable
        elif hasattr(game_model.gamestate, "active_interactable") and game_model.gamestate.active_interactable:
             obj = game_model.gamestate.active_interactable
             label = obj.interaction_label or obj.entity_id
             interact_text = f"{interact_key}: {label}"

        # Aces Data
        aces_count = 0
        aces_list = []
        if hasattr(game_model, "get_ace_count"):
            aces_count = game_model.get_ace_count()
            aces_list = game_model.gamestate.aces_collected

        return ExplorationHUDData(
            active_owner=active_char.owner_id if active_char else "P1",
            active_name=active_char.name if active_char else "Unknown",
            active_char_id=active_char.char_id if active_char else "char_p1",
            party_hp_list=party_hp_list,
            zone_label=getattr(room_model, "name", "Unknown"),
            interact_hint=interact_text or "",
            interact_text=interact_text, # Legacy field
            handoff_message=handoff_msg if is_awaiting else None,
            aces_count=aces_count,
            aces_collected=aces_list
        )