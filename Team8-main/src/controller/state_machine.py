"""
State Machine controller for managing game states.
User Story 1: As a system, I want to manage game states.

Transitions happen only via:
- change_state(id, **kwargs): Replace entire stack with new state
- push_state(id, **kwargs): Push overlay onto stack
- pop_state(): Remove top state from stack
"""

from typing import Optional
from src.model.states.base_state import BaseState, StateID
# RIMOSSO IMPORT GLOBALE PER EVITARE DIPENDENZE CIRCOLARI
# from src.model.states.game_states import STATE_CLASSES 
from src.model.input_context import InputContext


class StateMachine:
    """
    Manages game state transitions with a controlled stack.
    
    Features:
    - Exactly one active state OR controlled stack with explicit overlay rules
    - Only topmost state receives input and update
    - Underlying states never update while covered
    - Rendering underneath is configurable per overlay state
    - Input context derived from peek().input_context (Epic 2 integration)
    """
    
    def __init__(self, input_manager=None):
        """
        Initialize the state machine.
        
        Args:
            input_manager: Reference to InputManager for context switching (Epic 2).
        """
        self._state_stack: list[BaseState] = []
        self._registered_states: dict[StateID, BaseState] = {}
        self._input_manager = input_manager
        self.controller = None # Will be set by GameController
        self._pending_transition = None
    
    def register_state(self, state: BaseState):
        """
        Register a state instance with the state machine.
        
        Args:
            state: The state instance to register.
        """
        state.set_state_machine(self)
        self._registered_states[state.state_id] = state
    
    def register_all_states(self):
        """Register all default game states."""
        # IMPORT LOCALE (LAZY IMPORT) PER ROMPERE IL CICLO
        from src.model.states.game_states import STATE_CLASSES
        
        for state_id, state_class in STATE_CLASSES.items():
            state = state_class(self)
            self.register_state(state)
    
    def _get_state(self, state_id: StateID) -> Optional[BaseState]:
        """Get a registered state by ID."""
        return self._registered_states.get(state_id)
    
    def peek(self) -> Optional[BaseState]:
        """
        Returns the topmost state without removing it.
        
        Returns:
            The topmost state or None if stack is empty.
        """
        if self._state_stack:
            return self._state_stack[-1]
        return None
    
    def get_current_input_context(self) -> InputContext:
        """
        Returns the input context for the topmost state.
        Used by Epic 2 InputManager integration.
        
        Returns:
            The InputContext for the current state, or MAIN_MENU if no state.
        """
        top_state = self.peek()
        if top_state:
            return top_state.input_context
        return InputContext.MAIN_MENU
    
    def _update_input_context(self):
        """Update the InputManager's context based on current state."""
        if self._input_manager:
            self._input_manager.set_context(self.get_current_input_context())
    
    def change_state(self, state_id: StateID, **kwargs):
        """
        Replace the entire stack with a new state.
        Used for full-screen state transitions.
        
        Args:
            state_id: The ID of the state to change to.
            **kwargs: Arguments passed to state.enter().
        """
        new_state = self._get_state(state_id)
        if not new_state:
            raise ValueError(f"State {state_id} not registered")
        
        prev_state = self.peek()
        
        # Exit and clear all states in the stack
        while self._state_stack:
            old_state = self._state_stack.pop()
            old_state.exit(new_state)
        
        # Enter the new state
        self._state_stack.append(new_state)
        new_state.enter(prev_state, **kwargs)
        
        self._update_input_context()
    
    def push_state(self, state_id: StateID, **kwargs):
        """
        Push an overlay state onto the stack.
        Used for modal/overlay states like Pause, Dialogue, etc.
        
        Args:
            state_id: The ID of the overlay state to push.
            **kwargs: Arguments passed to state.enter().
        """
        new_state = self._get_state(state_id)
        if not new_state:
            raise ValueError(f"State {state_id} not registered")
        
        prev_state = self.peek()
        self._state_stack.append(new_state)
        new_state.enter(prev_state, **kwargs)
        
        self._update_input_context()
    
    def pop_state(self) -> Optional[BaseState]:
        """
        Remove and return the topmost state.
        
        Returns:
            The popped state, or None if stack was empty.
        """
        if not self._state_stack:
            return None
        
        old_state = self._state_stack.pop()
        next_state = self.peek()
        old_state.exit(next_state)
        
        self._update_input_context()
        
        return old_state
    
    def clear_stack(self):
        """Clear all states from the stack (used for GameOver/Error)."""
        while self._state_stack:
            state = self._state_stack.pop()
            state.exit(None)
        
        if self._input_manager:
            self._input_manager.set_context(InputContext.MAIN_MENU)
    
    def handle_event(self, event) -> bool:
        """
        Pass event to the topmost state only.
        
        Args:
            event: The event to handle.
            
        Returns:
            True if event was consumed.
        """
        top_state = self.peek()
        if top_state:
            return top_state.handle_event(event)
        return False
    
    def update(self, dt: float):
        """
        Update only the topmost state.
        Underlying states never update while covered.
        
        Args:
            dt: Delta time in seconds.
        """
        top_state = self.peek()
        if top_state:
            top_state.update(dt)
    
    def render(self, surface):
        """
        Render states based on overlay rules.
        If topmost state has render_below=True, render underlying states first.
        
        Args:
            surface: The pygame surface to render to.
        """
        if not self._state_stack:
            return
        
        # Find the first state to render (bottom of visible stack)
        render_start = len(self._state_stack) - 1
        
        # Walk down the stack to find where to start rendering
        for i in range(len(self._state_stack) - 1, -1, -1):
            state = self._state_stack[i]
            if not state.render_below:
                render_start = i
                break
            if i == 0:
                render_start = 0
        
        # Render from bottom to top
        for i in range(render_start, len(self._state_stack)):
            self._state_stack[i].render(surface)
    
    def is_empty(self) -> bool:
        """Returns True if the state stack is empty."""
        return len(self._state_stack) == 0
    
    def stack_size(self) -> int:
        """Returns the number of states in the stack."""
        return len(self._state_stack)
    
    def has_state(self, state_id: StateID) -> bool:
        """Check if a specific state is anywhere in the stack."""
        return any(s.state_id == state_id for s in self._state_stack)