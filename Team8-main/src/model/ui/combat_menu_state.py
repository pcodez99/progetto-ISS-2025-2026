"""
Combat Menu State - Model for UI navigation during combat.
Updated for Epic 19 (Targeting & Scopes).
"""
from dataclasses import dataclass, field
from typing import List, Optional, Dict, Any

@dataclass
class CombatMenuState:
    """
    Rappresenta lo stato dell'interfaccia di combattimento.
    """
    # Modes: "root", "target_selection"
    mode: str = "root" 
    cursor_index: int = 0
    
    # Root menu options
    # In un'implementazione reale, queste verrebbero popolate dinamicamente in base alle abilità
    options: List[str] = field(default_factory=lambda: ["Attack", "Skill: Fireball (AOE)", "Skill: Snipe (Rnd)", "Defend", "Flee"])
    
    # Action Pending Execution (US 77)
    # Contiene i dati della mossa selezionata (scope, power, type, etc.)
    pending_action: Optional[Dict[str, Any]] = None

    # Target selection logic (US 77/78)
    valid_targets: List[Any] = field(default_factory=list) # Lista oggetti Character/Enemy
    selected_target_index: int = 0 # Indice nella lista valid_targets

    def reset(self):
        """Torna al menu principale."""
        self.mode = "root"
        self.cursor_index = 0
        self.selected_target_index = 0
        self.pending_action = None
        self.valid_targets = []

    def move_cursor(self, delta: int):
        """Muove il cursore nel menu o nella selezione target."""
        if self.mode == "root":
            n = len(self.options)
            if n > 0:
                self.cursor_index = (self.cursor_index + delta) % n
        elif self.mode == "target_selection":
            n = len(self.valid_targets)
            if n > 0:
                self.selected_target_index = (self.selected_target_index + delta) % n

    def get_selected_option(self) -> str:
        """Ritorna l'opzione correntemente selezionata nel root menu."""
        if 0 <= self.cursor_index < len(self.options):
            return self.options[self.cursor_index]
        return ""

    def start_target_selection(self, targets: List[Any]):
        """Passa alla modalità selezione bersaglio con una lista di candidati."""
        self.mode = "target_selection"
        self.valid_targets = targets
        self.selected_target_index = 0 

    def get_current_target(self) -> Optional[Any]:
        """Ritorna l'oggetto target correntemente evidenziato."""
        if self.mode == "target_selection" and self.valid_targets:
            if 0 <= self.selected_target_index < len(self.valid_targets):
                return self.valid_targets[self.selected_target_index]
        return None