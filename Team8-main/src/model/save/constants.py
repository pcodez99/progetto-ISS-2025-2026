"""
Save System Constants and Enums
Epic 5: User Stories 16, 17, 18, 19
"""

import os
from enum import Enum

# ============== PATHS & LIMITS ==============
SAVE_DIR = "./saves"
MAX_SLOTS = 3
CURRENT_SAVE_SCHEMA_VERSION = 1


# ============== HELPER FUNCTIONS ==============
def get_slot_filename(slot_index: int) -> str:
    """Generate filename for a slot (1-based index)."""
    return f"slot_{slot_index:02d}.json"


def get_slot_filepath(slot_index: int) -> str:
    """Generate full path for a slot."""
    return os.path.join(SAVE_DIR, get_slot_filename(slot_index))


# ============== ENUMS ==============
class SlotStatus(Enum):
    """Status of a save slot."""
    EMPTY = "empty"
    OK = "ok"
    CORRUPT = "corrupt"