"""
Script actions for data-driven game scripts.
User Story 4: Triggers launch deterministic scripts made of ordered actions.
"""

from enum import Enum, auto
from dataclasses import dataclass, field
from typing import Any, Optional


class ActionType(Enum):
    """Types of script actions."""
    # Narrative/UI
    SHOW_DIALOGUE = auto()
    SHOW_CHOICE = auto()
    FADE_IN = auto()
    FADE_OUT = auto()
    WAIT = auto()
    
    # Progression
    SET_FLAG = auto()
    CLEAR_FLAG = auto()
    GIVE_ITEM = auto()
    CONSUME_ITEM = auto()
    GIVE_ACE = auto()
    SET_CHECKPOINT = auto()
    
    # World persistence
    REMOVE_ENTITY = auto()
    
    # Transitions
    CHANGE_ROOM = auto()
    CHANGE_STATE = auto()
    START_COMBAT = auto()
    
    # Party
    RECRUIT_GUEST = auto()


@dataclass
class ScriptAction:
    """
    A single action in a script.
    
    Blocking actions pause the runner until completed.
    Non-blocking actions complete immediately.
    """
    action_type: ActionType
    params: dict = field(default_factory=dict)
    blocking: bool = True
    cross_state: bool = False  # If True, continues across state transitions
    
    @classmethod
    def show_dialogue(cls, speaker: str, text: str, **kwargs) -> 'ScriptAction':
        """Create a ShowDialogue action (blocking)."""
        return cls(
            action_type=ActionType.SHOW_DIALOGUE,
            params={'speaker': speaker, 'text': text, **kwargs},
            blocking=True
        )
    
    @classmethod
    def show_choice(cls, prompt: str, choices: list[str], **kwargs) -> 'ScriptAction':
        """Create a ShowChoice action (blocking)."""
        return cls(
            action_type=ActionType.SHOW_CHOICE,
            params={'prompt': prompt, 'choices': choices, **kwargs},
            blocking=True
        )
    
    @classmethod
    def wait(cls, duration: float) -> 'ScriptAction':
        """Create a Wait action (blocking)."""
        return cls(
            action_type=ActionType.WAIT,
            params={'duration': duration},
            blocking=True
        )
    
    @classmethod
    def fade_in(cls, duration: float = 1.0) -> 'ScriptAction':
        """Create a FadeIn action (blocking)."""
        return cls(
            action_type=ActionType.FADE_IN,
            params={'duration': duration},
            blocking=True
        )
    
    @classmethod
    def fade_out(cls, duration: float = 1.0) -> 'ScriptAction':
        """Create a FadeOut action (blocking)."""
        return cls(
            action_type=ActionType.FADE_OUT,
            params={'duration': duration},
            blocking=True
        )
    
    @classmethod
    def set_flag(cls, flag_name: str, value: Any = True) -> 'ScriptAction':
        """Create a SetFlag action (non-blocking)."""
        return cls(
            action_type=ActionType.SET_FLAG,
            params={'flag_name': flag_name, 'value': value},
            blocking=False
        )
    
    @classmethod
    def clear_flag(cls, flag_name: str) -> 'ScriptAction':
        """Create a ClearFlag action (non-blocking)."""
        return cls(
            action_type=ActionType.CLEAR_FLAG,
            params={'flag_name': flag_name},
            blocking=False
        )
    
    @classmethod
    def give_item(cls, item_id: str, quantity: int = 1) -> 'ScriptAction':
        """Create a GiveItem action (non-blocking)."""
        return cls(
            action_type=ActionType.GIVE_ITEM,
            params={'item_id': item_id, 'quantity': quantity},
            blocking=False
        )
    
    @classmethod
    def consume_item(cls, item_id: str, quantity: int = 1) -> 'ScriptAction':
        """Create a ConsumeItem action (non-blocking)."""
        return cls(
            action_type=ActionType.CONSUME_ITEM,
            params={'item_id': item_id, 'quantity': quantity},
            blocking=False
        )
    
    @classmethod
    def give_ace(cls, ace_id: str) -> 'ScriptAction':
        """Create a GiveAce action (non-blocking)."""
        return cls(
            action_type=ActionType.GIVE_ACE,
            params={'ace_id': ace_id},
            blocking=False
        )
    
    @classmethod
    def set_checkpoint(cls, checkpoint_id: str) -> 'ScriptAction':
        """Create a SetCheckpoint action (non-blocking)."""
        return cls(
            action_type=ActionType.SET_CHECKPOINT,
            params={'checkpoint_id': checkpoint_id},
            blocking=False
        )
    
    @classmethod
    def remove_entity(cls, room_id: str, entity_id: str) -> 'ScriptAction':
        """Create a RemoveEntity action (non-blocking)."""
        return cls(
            action_type=ActionType.REMOVE_ENTITY,
            params={'room_id': room_id, 'entity_id': entity_id},
            blocking=False
        )
    
    @classmethod
    def change_room(cls, room_id: str, spawn_id: str = 'default') -> 'ScriptAction':
        """Create a ChangeRoom action (halts remaining actions)."""
        return cls(
            action_type=ActionType.CHANGE_ROOM,
            params={'room_id': room_id, 'spawn_id': spawn_id},
            blocking=False
        )
    
    @classmethod
    def change_state(cls, state_id: str, **kwargs) -> 'ScriptAction':
        """Create a ChangeState action (halts remaining actions)."""
        return cls(
            action_type=ActionType.CHANGE_STATE,
            params={'state_id': state_id, **kwargs},
            blocking=False
        )
    
    @classmethod
    def start_combat(cls, encounter_id: str) -> 'ScriptAction':
        """Create a StartCombat action (halts remaining actions)."""
        return cls(
            action_type=ActionType.START_COMBAT,
            params={'encounter_id': encounter_id},
            blocking=False
        )
    
    @classmethod
    def recruit_guest(cls, guest_id: str) -> 'ScriptAction':
        """Create a RecruitGuest action (non-blocking)."""
        return cls(
            action_type=ActionType.RECRUIT_GUEST,
            params={'guest_id': guest_id},
            blocking=False
        )


@dataclass
class GameScript:
    """
    A script is an ordered list of actions executed by ActionRunner.
    """
    script_id: str
    actions: list[ScriptAction] = field(default_factory=list)
    
    @classmethod
    def from_dict(cls, data: dict) -> 'GameScript':
        """Load a script from dictionary."""
        actions = []
        for action_data in data.get('actions', []):
            action_type = ActionType[action_data['type'].upper()]
            actions.append(ScriptAction(
                action_type=action_type,
                params=action_data.get('params', {}),
                blocking=action_data.get('blocking', True),
                cross_state=action_data.get('cross_state', False)
            ))
        return cls(
            script_id=data.get('script_id', ''),
            actions=actions
        )