"""
InputManager: Handles input processing with action mapping and context-based filtering.
User Story 6 & 7: Action mapping and input contexts.
"""

from src.model.input_actions import Action, get_default_keymap
from src.model.input_context import InputContext, CONTEXT_ALLOWED_ACTIONS

try:
    import pygame
    _PYGAME_AVAILABLE = True
except ImportError:
    _PYGAME_AVAILABLE = False


class InputManager:
    """
    MVC-friendly input manager that decouples gameplay code from raw Pygame keys.
    """
    
    def __init__(self, keymap: dict[Action, set[int]] = None):
        self._keymap = keymap if keymap is not None else get_default_keymap()
        self._keys_down: set[int] = set()
        self._keys_just_pressed: set[int] = set()
        self._keys_just_released: set[int] = set()
        self._context: InputContext = InputContext.MAIN_MENU
    
    def begin_frame(self):
        """Clears edge flags deterministically every frame."""
        self._keys_just_pressed.clear()
        self._keys_just_released.clear()
    
    def process_event(self, event):
        """Handles KEYDOWN and KEYUP events."""
        if not _PYGAME_AVAILABLE:
            return
            
        if event.type == pygame.KEYDOWN:
            if event.key not in self._keys_down:
                self._keys_down.add(event.key)
                self._keys_just_pressed.add(event.key)
        elif event.type == pygame.KEYUP:
            if event.key in self._keys_down:
                self._keys_down.discard(event.key)
                self._keys_just_released.add(event.key)
    
    def _is_action_allowed(self, action: Action) -> bool:
        allowed_actions = CONTEXT_ALLOWED_ACTIONS.get(self._context, set())
        return action in allowed_actions
    
    def is_down(self, action: Action) -> bool:
        if not self._is_action_allowed(action):
            return False
        bound_keys = self._keymap.get(action, set())
        return any(key in self._keys_down for key in bound_keys)
    
    def was_just_pressed(self, action: Action) -> bool:
        if not self._is_action_allowed(action):
            return False
        bound_keys = self._keymap.get(action, set())
        return any(key in self._keys_just_pressed for key in bound_keys)
    
    def was_just_released(self, action: Action) -> bool:
        if not self._is_action_allowed(action):
            return False
        bound_keys = self._keymap.get(action, set())
        return any(key in self._keys_just_released for key in bound_keys)
    
    def set_context(self, context: InputContext):
        """Changes the input context and flushes all input state."""
        self._context = context
        self._flush_input_state()
    
    def get_context(self) -> InputContext:
        return self._context
    
    def _flush_input_state(self):
        self._keys_down.clear()
        self._keys_just_pressed.clear()
        self._keys_just_released.clear()