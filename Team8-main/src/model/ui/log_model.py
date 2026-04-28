from __future__ import annotations
from dataclasses import dataclass
from collections import deque
from typing import Deque, Optional, List


@dataclass(frozen=True)
class LogEntry:
    text: str
    kind: str = "system"  # damage/heal/status/system
    turn_index: Optional[int] = None
    actor_id: Optional[str] = None


class CombatLogModel:
    def __init__(self, max_entries: int = 100):
        self._entries: Deque[LogEntry] = deque(maxlen=int(max_entries))

    def add(self, entry: LogEntry) -> None:
        self._entries.append(entry)

    def tail(self, n: int) -> List[LogEntry]:
        n = max(0, int(n))
        if n == 0:
            return []
        return list(self._entries)[-n:]

    def __len__(self) -> int:
        return len(self._entries)
