"""
Prompt Manager - Bridge between Sicily's logic and Amelia's StateMachine
"""
from typing import Callable, Any, Optional, List
from dataclasses import dataclass

# Amelia Imports
from src.model.states.base_state import StateID

@dataclass(frozen=True)
class PromptChoice:
    label: str
    value: Any

class PromptManager:
    """
    Gestore dei prompt che delega alla StateMachine.
    """
    def __init__(self, state_machine):
        self.state_machine = state_machine
        self.info_message: Optional[str] = None
        self.info_until_ms: Optional[int] = None

    def show_confirm(self, message: str, on_yes: Callable[[], None], on_no: Callable[[], None], title: str = "Confirm"):
        """Apre un prompt Si/No Modale."""
        if not self.state_machine: return
        
        self.state_machine.push_state(
            StateID.PROMPT,
            prompt_type='confirm',
            message=message,
            title=title,
            on_yes=on_yes,
            on_no=on_no
        )

    def show_choice(self, title: str, options: List[PromptChoice], on_select: Callable[[Any], None]):
        """Apre un prompt a scelta multipla."""
        if not self.state_machine: return

        self.state_machine.push_state(
            StateID.PROMPT,
            prompt_type='choice',
            title=title,
            options=options,
            on_select=on_select
        )

    def show_info(self, message: str, now_ms: int, timeout_ms: Optional[int] = None):
        """Mostra un messaggio non bloccante (overlay)."""
        self.info_message = str(message)
        if timeout_ms is None:
            self.info_until_ms = None
        else:
            self.info_until_ms = int(now_ms) + int(timeout_ms)

    def update(self, now_ms: int):
        if self.info_until_ms is not None and int(now_ms) >= int(self.info_until_ms):
            self.info_message = None
            self.info_until_ms = None