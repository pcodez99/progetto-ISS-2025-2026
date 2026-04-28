from enum import Enum, auto

try:
    import pygame
    _PYGAME_AVAILABLE = True
except ImportError:
    _PYGAME_AVAILABLE = False


class Action(Enum):
    # Movement
    MOVE_UP = auto()
    MOVE_DOWN = auto()
    MOVE_LEFT = auto()
    MOVE_RIGHT = auto()
    
    # Menu / Navigation
    MENU_UP = auto()
    MENU_DOWN = auto()
    MENU_LEFT = auto()
    MENU_RIGHT = auto()
    
    # Interaction
    CONFIRM = auto()
    CANCEL = auto()
    PAUSE = auto()    # ESC / P (Menu Pausa)
    INTERACT = auto() # E / Invio
    
    # Gameplay Specific
    NEXT_CHARACTER = auto() # TAB (Cambio turno esplorazione)


def get_default_keymap() -> dict:
    if not _PYGAME_AVAILABLE:
        raise RuntimeError("Pygame is required for keymap initialization")
    
    return {
        # WASD + Frecce
        Action.MOVE_UP: {pygame.K_w, pygame.K_UP},
        Action.MOVE_DOWN: {pygame.K_s, pygame.K_DOWN},
        Action.MOVE_LEFT: {pygame.K_a, pygame.K_LEFT},
        Action.MOVE_RIGHT: {pygame.K_d, pygame.K_RIGHT},
        
        # Menu (stessi tasti movimento)
        Action.MENU_UP: {pygame.K_w, pygame.K_UP},
        Action.MENU_DOWN: {pygame.K_s, pygame.K_DOWN},
        Action.MENU_LEFT: {pygame.K_a, pygame.K_LEFT},
        Action.MENU_RIGHT: {pygame.K_d, pygame.K_RIGHT},
        
        # Conferma / Interazione
        Action.CONFIRM: {pygame.K_RETURN, pygame.K_SPACE},
        Action.CANCEL: {pygame.K_BACKSPACE}, # ESC rimosso da qui per evitare conflitti con PAUSE
        
        # Tasto E per interagire, Invio per confermare
        Action.INTERACT: {pygame.K_e, pygame.K_RETURN},
        
        # Menu Pausa
        Action.PAUSE: {pygame.K_ESCAPE, pygame.K_p},

        # Cambio Giocatore (TAB)
        Action.NEXT_CHARACTER: {pygame.K_TAB}
    }