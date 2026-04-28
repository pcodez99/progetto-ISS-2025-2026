"""
Action Runner for executing game scripts.
User Story 4: Triggers launch deterministic scripts.
"""

import logging
from typing import Optional, Callable, Any
from src.model.script_actions import GameScript, ScriptAction, ActionType

logger = logging.getLogger(__name__)


class ActionRunner:
    """
    Executes game scripts deterministically.
    
    Features:
    - Ordered execution of actions
    - Blocking vs non-blocking actions
    - State transition halts remaining actions (unless cross_state=True)
    - Deterministic: same state + same script + same choices = same results
    """
    
    # Actions that cause state transitions and halt the script
    TRANSITION_ACTIONS = {
        ActionType.CHANGE_ROOM,
        ActionType.CHANGE_STATE,
        ActionType.START_COMBAT,
    }
    
    def __init__(self):
        """Initialize the action runner."""
        self._current_script: Optional[GameScript] = None
        self._action_index: int = 0
        self._is_running: bool = False
        self._is_waiting: bool = False
        self._wait_timer: float = 0
        self._choice_result: Optional[int] = None
        
        # Action handlers registered by the game systems
        self._handlers: dict[ActionType, Callable] = {}

        self.game_ref = None
    
    def register_handler(self, action_type: ActionType, handler: Callable):
        """
        Register a handler for an action type.
        
        Args:
            action_type: The action type to handle.
            handler: Callable that takes (params: dict) and returns Any.
        """
        self._handlers[action_type] = handler

    def run_script_by_id(self, script_id: str):
        """Lancia uno script cercandolo nel registry dinamico o statico."""
        if not self.game_ref:
            logger.error("Game reference not set in ActionRunner")
            return

        from src.model.scripting.scripts_registry import ScriptsRegistry
        
        # 1. Cerca nel Registry Dinamico (Python code)
        script = ScriptsRegistry.get_script(script_id, self.game_ref)
        
        # 2. Se vuoto (noop), prova a cercare nei contenuti statici (JSON)
        if script.script_id == "noop":
             # Fallback logica JSON (non implementata in questo snippet, ma punto di estensione)
             pass
             
        self.run_script(script)
    
    def run_script(self, script: GameScript):
        """
        Start running a script.
        
        Args:
            script: The script to execute.
        """
        self._current_script = script
        self._action_index = 0
        self._is_running = True
        self._is_waiting = False
        
        logger.info(f"Starting script '{script.script_id}'")
        
        # Execute non-blocking actions immediately
        self._process_actions()
    
    def _process_actions(self):
        """Process actions until blocked or finished."""
        while self._is_running and not self._is_waiting:
            if self._action_index >= len(self._current_script.actions):
                self._finish_script()
                return
            
            action = self._current_script.actions[self._action_index]
            
            # Execute the action
            should_halt = self._execute_action(action)
            
            if should_halt:
                # Transition action halts the script
                if not action.cross_state:
                    self._finish_script()
                    return
            
            self._action_index += 1
            
            # If action is blocking, wait for completion
            if action.blocking:
                self._is_waiting = True
                return
    
    def _execute_action(self, action: ScriptAction) -> bool:
        """
        Execute a single action.
        
        Args:
            action: The action to execute.
            
        Returns:
            True if this action should halt the script (transition).
        """
        handler = self._handlers.get(action.action_type)
        
        if handler:
            handler(action.params)
        else:
            logger.warning(f"No handler for action type: {action.action_type}")
        
        # Check if this is a transition action
        return action.action_type in self.TRANSITION_ACTIONS
    
    def complete_blocking_action(self, result: Any = None):
        """
        Signal that a blocking action has completed.
        Called by the game systems when dialogue is dismissed, choice is made, etc.
        
        Args:
            result: Optional result (e.g., choice index).
        """
        if not self._is_waiting:
            return
        
        self._choice_result = result
        self._is_waiting = False
        
        # Continue processing
        self._process_actions()
    
    def update(self, dt: float):
        """
        Update the action runner (for Wait actions).
        
        Args:
            dt: Delta time in seconds.
        """
        if not self._is_running or not self._is_waiting:
            return
        
        current_action = self._current_script.actions[self._action_index]
        
        if current_action.action_type == ActionType.WAIT:
            self._wait_timer += dt
            if self._wait_timer >= current_action.params.get('duration', 0):
                self._wait_timer = 0
                self.complete_blocking_action()
    
    def _finish_script(self):
        """Clean up after script completion."""
        if self._current_script:
            logger.info(f"Finished script '{self._current_script.script_id}'")
        self._current_script = None
        self._action_index = 0
        self._is_running = False
        self._is_waiting = False
    
    def halt(self):
        """Force halt the current script."""
        self._finish_script()
    
    def is_running(self) -> bool:
        """Returns True if a script is currently running."""
        return self._is_running
    
    def is_waiting(self) -> bool:
        """Returns True if waiting for a blocking action to complete."""
        return self._is_waiting
    
    def get_choice_result(self) -> Optional[int]:
        """Returns the result of the last choice action."""
        return self._choice_result
    
    def _handle_set_checkpoint(self, params):
        room_id = params.get('checkpoint_id')
       
        # Se params è vuoto, usa posizione corrente
        self.game.gamestate.set_checkpoint()