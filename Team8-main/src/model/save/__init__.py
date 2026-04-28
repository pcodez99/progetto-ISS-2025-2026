"""
Save System Package
Epic 5: User Stories 16, 17, 18, 19

Re-exports all public classes and constants for convenient imports.
"""

from src.model.save.constants import (
    SAVE_DIR,
    MAX_SLOTS,
    CURRENT_SAVE_SCHEMA_VERSION,
    get_slot_filename,
    get_slot_filepath,
    SlotStatus,
)

from src.model.save.dtos import (
    SaveMeta,
    SlotInfo,
    WorldDTO,
    PlayerDTO,
    InventoryDTO,
    PartyDTO,
    ProgressionDTO,
    WorldStateDTO,
    TurnStateDTO,
    CheckpointDTO,
    SaveDataDTO,
    SaveFileDTO,
    SaveResult,
    LoadResult,
    ValidationResult,
)

from src.model.save.validator import (
    SaveValidator,
    SaveStateChecker,
)

from src.model.save.serializer import (
    GameSerializer,
)

# FIX: Importa da save_manager, non da manager
from src.model.save.save_manager import (
    SaveManager,
)

__all__ = [
    # Constants
    'SAVE_DIR',
    'MAX_SLOTS',
    'CURRENT_SAVE_SCHEMA_VERSION',
    'get_slot_filename',
    'get_slot_filepath',
    'SlotStatus',
    # DTOs
    'SaveMeta',
    'SlotInfo',
    'WorldDTO',
    'PlayerDTO',
    'InventoryDTO',
    'PartyDTO',
    'ProgressionDTO',
    'WorldStateDTO',
    'TurnStateDTO',
    'CheckpointDTO',
    'SaveDataDTO',
    'SaveFileDTO',
    'SaveResult',
    'LoadResult',
    'ValidationResult',
    # Classes
    'SaveValidator',
    'SaveStateChecker',
    'GameSerializer',
    'SaveManager',
]