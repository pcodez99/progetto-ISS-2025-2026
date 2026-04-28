"""
Interaction Menu State - Model for managing NPC/Object interaction menus.
Epic 16: User Story 63
"""
from dataclasses import dataclass, field
from typing import List, Optional

@dataclass
class MenuOption:
    label: str
    script_id: str

@dataclass
class InteractionMenuStateData:
    """
    Stato del menu di interazione.
    """
    is_open: bool = False
    title: str = "Interact"
    options: List[MenuOption] = field(default_factory=list)
    selected_index: int = 0

    def open(self, title: str, options: List[dict]):
        """
        Apre il menu con una lista di opzioni.
        options: list of {'label': str, 'script_id': str}
        """
        self.is_open = True
        self.title = title
        self.options = [MenuOption(o['label'], o['script_id']) for o in options]
        self.selected_index = 0

    def close(self):
        self.is_open = False
        self.options = []
        self.selected_index = 0

    def move_cursor(self, delta: int):
        if not self.options: return
        n = len(self.options)
        self.selected_index = (self.selected_index + delta) % n

    def get_selected_script(self) -> Optional[str]:
        if not self.is_open or not self.options: return None
        return self.options[self.selected_index].script_id