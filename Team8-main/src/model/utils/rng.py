import random 
from typing import Sequence, TypeVar, Optional

T = TypeVar('T')

class RNG:
    """Wrapper per random.Random per garantire determinismo e utilità comuni. Aggiornato per supportare Epic 18 (Combat System)."""
    def __init__(self, seed: Optional[int] = None): 
        self._r = random.Random(seed)
    def set_seed(self, seed: int) -> None:
        """Re-inizializza il generatore con un nuovo seed."""
        self._r.seed(seed)

    def randint(self, a: int, b: int) -> int:
        """Ritorna un intero casuale N tale che a <= N <= b."""
        return self._r.randint(a, b)

    def random(self) -> float:
        """Ritorna un float casuale tra 0.0 e 1.0."""
        return self._r.random()

    def choice(self, seq: Sequence[T]) -> T:
        """Ritorna un elemento casuale da una sequenza non vuota."""
        return self._r.choice(seq)

    def chance(self, percent: int) -> bool:
        """
        Ritorna True con una probabilità pari a 'percent' (0-100).
        Usato per Hit chance, Crit chance, ecc.
        """
        if percent >= 100: return True
        if percent <= 0: return False
        return self._r.randint(0, 99) < percent