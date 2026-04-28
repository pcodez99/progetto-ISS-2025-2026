"""
Turn Manager - Manages initiative and turn order.
Epic 9: Combat System Advanced
"""

import logging
from typing import List, Optional, Any

logger = logging.getLogger(__name__)

class TurnManager:
    """
    Gestisce l'ordine dei turni basato sulla Speed (SPD).
    """
    def __init__(self):
        self._participants: List[Any] = []
        self._turn_queue: List[Any] = []
        self._active_actor: Optional[Any] = None

    def start_battle(self, participants: List[Any]):
        """Inizializza la battaglia e calcola l'iniziativa iniziale."""
        self._participants = participants
        self._calculate_initiative()
        self._advance_queue()
        logger.info(f"Battle started with {len(participants)} participants.")

    def _calculate_initiative(self):
        """Ordina i partecipanti per SPD decrescente."""
        # Filtra i morti
        living = [p for p in self._participants if getattr(p, "hp", 0) > 0]
        # Sort stabile: SPD decrescente, poi nome/id
        self._turn_queue = sorted(
            living, 
            key=lambda p: getattr(p, "spd", 0), 
            reverse=True
        )

    def active_actor(self) -> Optional[Any]:
        return self._active_actor

    def next_turn(self) -> Optional[Any]:
        """Avanza al prossimo attore."""
        self._advance_queue()
        return self._active_actor

    def _advance_queue(self):
        """Logica interna per ciclare la coda."""
        if not self._turn_queue:
            # Se la coda è vuota (tutti morti o fine round), ricalcola
            self._calculate_initiative()
        
        if not self._turn_queue:
            self._active_actor = None
            return

        self._active_actor = self._turn_queue.pop(0)
        
        # Se l'attore è morto nel frattempo, salta
        if getattr(self._active_actor, "hp", 0) <= 0:
            self._advance_queue()

    def peek_next(self, n: int) -> List[Any]:
        """Anteprima dei prossimi N turni (per l'HUD)."""
        return self._turn_queue[:n]