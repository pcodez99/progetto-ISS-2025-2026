"""
Handoff Overlay Model (US27)
"""
from dataclasses import dataclass
from typing import Optional

@dataclass
class HandoffModel:
    """Model for handoff state UI"""
    awaiting_confirm: bool = False
    owner_id: Optional[str] = None
    message: str = ""