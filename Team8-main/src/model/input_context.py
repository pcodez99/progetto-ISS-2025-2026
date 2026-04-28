"""
Defines input contexts and their allowed actions.
User Story 7: Input contexts to prevent input leaking across states.
"""

from enum import Enum, auto
from src.model.input_actions import Action


class InputContext(Enum):
    """
    Canonical input contexts for the game.
    Each context defines which actions are allowed.
    """
    MAIN_MENU = auto()
    EXPLORATION = auto()
    COMBAT = auto()
    DIALOGUE = auto()
    CUTSCENE = auto()
    PAUSE = auto()
    PROMPT = auto()
    INTERACTION_MENU = auto()
    INVENTORY = auto()
    SAVE_LOAD = auto()
    SETTINGS = auto()
    CREDITS = auto()
    ERROR = auto()
    
    # Context for Card Games (Scopa, Briscola, etc)
    MINIGAME_CARD = auto()


# Allowlist of actions per context
# Disallowed actions always return False (down/pressed/released)
CONTEXT_ALLOWED_ACTIONS: dict[InputContext, set[Action]] = {
    InputContext.MAIN_MENU: {
        Action.MENU_UP, Action.MENU_DOWN, 
        Action.MENU_LEFT, Action.MENU_RIGHT,
        Action.CONFIRM, Action.CANCEL
    },
    
    InputContext.EXPLORATION: {
        Action.MOVE_UP, Action.MOVE_DOWN,
        Action.MOVE_LEFT, Action.MOVE_RIGHT,
        Action.INTERACT, Action.PAUSE,
        Action.CONFIRM, Action.CANCEL,
        Action.NEXT_CHARACTER  # <--- CRUCIALE: ABILITA IL TAB
    },
    
    InputContext.COMBAT: {
        Action.MENU_UP, Action.MENU_DOWN,
        Action.MENU_LEFT, Action.MENU_RIGHT,
        Action.CONFIRM, Action.CANCEL, Action.PAUSE
    },
    
    InputContext.DIALOGUE: {
        Action.CONFIRM, Action.CANCEL
    },
    
    InputContext.CUTSCENE: {
        Action.CONFIRM  # Only to skip/advance
    },
    
    InputContext.PAUSE: {
        Action.MENU_UP, Action.MENU_DOWN,
        Action.CONFIRM, Action.CANCEL, Action.PAUSE
    },
    
    InputContext.PROMPT: {
        Action.CONFIRM, Action.CANCEL
    },
    
    InputContext.INTERACTION_MENU: {
        Action.MENU_UP, Action.MENU_DOWN,
        Action.CONFIRM, Action.CANCEL
    },
    
    InputContext.INVENTORY: {
        Action.MENU_UP, Action.MENU_DOWN,
        Action.MENU_LEFT, Action.MENU_RIGHT,
        Action.CONFIRM, Action.CANCEL, Action.PAUSE
    },
    
    InputContext.SAVE_LOAD: {
        Action.MENU_UP, Action.MENU_DOWN,
        Action.CONFIRM, Action.CANCEL
    },
    
    InputContext.SETTINGS: {
        Action.MENU_UP, Action.MENU_DOWN,
        Action.MENU_LEFT, Action.MENU_RIGHT,
        Action.CONFIRM, Action.CANCEL
    },
    
    InputContext.CREDITS: {
        Action.CONFIRM, Action.CANCEL
    },
    
    InputContext.ERROR: {
        Action.CONFIRM
    },

    # Card Minigame: Usa tasti direzionali per selezione carte e conferma
    InputContext.MINIGAME_CARD: {
        Action.MENU_UP, Action.MENU_DOWN,
        Action.MENU_LEFT, Action.MENU_RIGHT,
        Action.CONFIRM, Action.CANCEL, Action.PAUSE
    }
}


def get_allowed_actions(context: InputContext) -> set[Action]:
    """
    Returns the set of allowed actions for a given context.
    
    Args:
        context: The input context to query.
        
    Returns:
        Set of allowed Action values for the context.
    """
    return CONTEXT_ALLOWED_ACTIONS.get(context, set())