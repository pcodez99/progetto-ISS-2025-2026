"""
Base state class and state identifiers.
User Story 1: As a system, I want to manage game states.
"""

from abc import ABC, abstractmethod
from enum import Enum, auto
from src.model.input_context import InputContext


class StateID(Enum):
    MAIN_MENU = auto()
    NEW_GAME_SETUP = auto()
    CUTSCENE = auto()
    HUB = auto()
    ROOM = auto()
    COMBAT = auto()
    SAVE_LOAD = auto()
    SETTINGS = auto()
    CREDITS = auto()
    GAME_OVER = auto()
    ERROR = auto()
    
    # Overlay/modal states
    PAUSE = auto()
    DIALOGUE = auto()
    PROMPT = auto()
    INTERACTION_MENU = auto()
    INVENTORY = auto()
    ACES_MENU = auto()
    
    # Minigames
    SCOPA = auto()
    BRISCOLA = auto()
    SETTE_MEZZO = auto()
    CUCU = auto()
    
    # EPIC 28: BOSS FINALE CUSTOM
    BOSS_OSTE = auto()


# Mapping Definitivo Stato -> Contesto Input
STATE_INPUT_CONTEXTS: dict[StateID, InputContext] = {
    StateID.MAIN_MENU: InputContext.MAIN_MENU,
    StateID.NEW_GAME_SETUP: InputContext.MAIN_MENU,
    StateID.CUTSCENE: InputContext.CUTSCENE,
    StateID.HUB: InputContext.EXPLORATION,
    StateID.ROOM: InputContext.EXPLORATION,
    StateID.COMBAT: InputContext.COMBAT,
    StateID.SAVE_LOAD: InputContext.SAVE_LOAD,
    StateID.SETTINGS: InputContext.SETTINGS,
    StateID.CREDITS: InputContext.CREDITS,
    StateID.GAME_OVER: InputContext.MAIN_MENU,
    StateID.ERROR: InputContext.ERROR,
    StateID.PAUSE: InputContext.PAUSE,
    StateID.DIALOGUE: InputContext.DIALOGUE,
    StateID.PROMPT: InputContext.PROMPT,
    StateID.INTERACTION_MENU: InputContext.INTERACTION_MENU,
    StateID.INVENTORY: InputContext.INVENTORY,
    StateID.ACES_MENU: InputContext.INVENTORY,
    
    # Minigames
    StateID.SCOPA: InputContext.MINIGAME_CARD,
    StateID.BRISCOLA: InputContext.MINIGAME_CARD,
    StateID.SETTE_MEZZO: InputContext.MINIGAME_CARD,
    StateID.CUCU: InputContext.MINIGAME_CARD,
    
    # Boss Finale (Usa Mouse, quindi input generico o menu va bene)
    StateID.BOSS_OSTE: InputContext.MAIN_MENU
}


class BaseState(ABC):
    def __init__(self, state_id: StateID, state_machine=None):
        self._state_id = state_id
        self._state_machine = state_machine
        self._is_overlay = state_id in {
            StateID.PAUSE,
            StateID.DIALOGUE,
            StateID.PROMPT,
            StateID.INTERACTION_MENU,
            StateID.INVENTORY,
            StateID.ACES_MENU,
        }
        self._render_below = self._is_overlay
    
    @property
    def state_id(self) -> StateID:
        return self._state_id
    
    @property
    def input_context(self) -> InputContext:
        return STATE_INPUT_CONTEXTS.get(self._state_id, InputContext.MAIN_MENU)
    
    @property
    def is_overlay(self) -> bool:
        return self._is_overlay
    
    @property
    def render_below(self) -> bool:
        return self._render_below
    
    def set_state_machine(self, state_machine):
        self._state_machine = state_machine
    
    @abstractmethod
    def enter(self, prev_state: 'BaseState' = None, **kwargs):
        pass
    
    @abstractmethod
    def exit(self, next_state: 'BaseState' = None):
        pass
    
    @abstractmethod
    def handle_event(self, event) -> bool:
        pass
    
    @abstractmethod
    def update(self, dt: float):
        pass
    
    @abstractmethod
    def render(self, surface):
        pass