"""
Save System Data Transfer Objects
Epic 5: User Stories 16, 17, 18, 19
Updated: Added custom_name support.
"""

from dataclasses import dataclass, field, asdict
from typing import Optional, List, Dict, Any
from datetime import datetime

from src.model.save.constants import SlotStatus, CURRENT_SAVE_SCHEMA_VERSION


# ============== METADATA ==============
@dataclass
class SaveMeta:
    """Save metadata for slot display."""
    timestamp_iso: str = ""
    room_id: str = "unknown"
    playtime_seconds: int = 0
    aces_count: int = 0
    aces_collected: List[str] = field(default_factory=list)
    custom_name: str = ""  # <--- NUOVO CAMPO

    def to_dict(self) -> dict:
        return asdict(self)

    @classmethod
    def from_dict(cls, d: dict) -> 'SaveMeta':
        return cls(
            timestamp_iso=d.get('timestamp_iso', ''),
            room_id=d.get('room_id', 'unknown'),
            playtime_seconds=d.get('playtime_seconds', 0),
            aces_count=d.get('aces_count', 0),
            aces_collected=d.get('aces_collected', []),
            custom_name=d.get('custom_name', "")  # <--- LOAD
        )

    def format_display(self) -> str:
        """Format metadata for UI display."""
        h = self.playtime_seconds // 3600
        m = (self.playtime_seconds % 3600) // 60
        s = self.playtime_seconds % 60
        time_str = f"{h:02d}:{m:02d}:{s:02d}"

        try:
            dt = datetime.fromisoformat(self.timestamp_iso)
            date_str = dt.strftime("%Y-%m-%d %H:%M")
        except (ValueError, TypeError):
            date_str = self.timestamp_iso

        # Logica: se c'è un nome custom usa quello, altrimenti usa il nome della stanza
        display_name = self.custom_name if self.custom_name else self.room_id.capitalize()

        return f"{display_name} - {self.aces_count}/4 Aces - {time_str} - {date_str}"


@dataclass
class SlotInfo:
    """Information about a save slot."""
    slot_index: int
    status: SlotStatus
    meta: Optional[SaveMeta] = None

    def get_display_text(self) -> str:
        """Text to show in slot list."""
        if self.status == SlotStatus.EMPTY:
            return f"Slot {self.slot_index}: Empty"
        elif self.status == SlotStatus.CORRUPT:
            return f"Slot {self.slot_index}: [Corrupt]"
        elif self.meta:
            return f"Slot {self.slot_index}: {self.meta.format_display()}"
        return f"Slot {self.slot_index}: Unknown"


# ============== GAME DATA DTOs ==============
@dataclass
class WorldDTO:
    """World/room state data."""
    room_id: str = "hub"
    spawn_id: Optional[str] = None
    party_world_pos: List[int] = field(default_factory=lambda: [0, 0])

    def to_dict(self) -> dict:
        return asdict(self)

    @classmethod
    def from_dict(cls, d: dict) -> 'WorldDTO':
        return cls(
            room_id=d.get('room_id', 'hub'),
            spawn_id=d.get('spawn_id'),
            party_world_pos=d.get('party_world_pos', [0, 0])
        )


@dataclass
class PlayerDTO:
    """Player character data."""
    name: str = "Unknown"
    hp: int = 20
    max_hp: int = 20
    stats: Dict[str, int] = field(default_factory=dict)
    abilities: List[str] = field(default_factory=list)

    def to_dict(self) -> dict:
        return asdict(self)

    @classmethod
    def from_dict(cls, d: dict) -> 'PlayerDTO':
        return cls(
            name=d.get('name', 'Unknown'),
            hp=d.get('hp', 20),
            max_hp=d.get('max_hp', 20),
            stats=d.get('stats', {}),
            abilities=d.get('abilities', [])
        )


@dataclass
class InventoryDTO:
    """Inventory data."""
    items: List[Dict[str, Any]] = field(default_factory=list)
    capacity: int = 10

    def to_dict(self) -> dict:
        return asdict(self)

    @classmethod
    def from_dict(cls, d: dict) -> 'InventoryDTO':
        return cls(
            items=d.get('items', []),
            capacity=d.get('capacity', 10)
        )


@dataclass
class PartyDTO:
    """Party data (all characters + inventories)."""
    characters: List[PlayerDTO] = field(default_factory=list)
    num_humans: int = 1
    enabled_mask: List[bool] = field(default_factory=list)
    guest_id: Optional[str] = None
    inventories: List[InventoryDTO] = field(default_factory=list)

    def to_dict(self) -> dict:
        return {
            'characters': [c.to_dict() for c in self.characters],
            'num_humans': self.num_humans,
            'enabled_mask': self.enabled_mask,
            'guest_id': self.guest_id,
            'inventories': [i.to_dict() for i in self.inventories]
        }

    @classmethod
    def from_dict(cls, d: dict) -> 'PartyDTO':
        return cls(
            characters=[PlayerDTO.from_dict(c) for c in d.get('characters', [])],
            num_humans=d.get('num_humans', 1),
            enabled_mask=d.get('enabled_mask', []),
            guest_id=d.get('guest_id'),
            inventories=[InventoryDTO.from_dict(i) for i in d.get('inventories', [])]
        )


@dataclass
class ProgressionDTO:
    """Progression data (aces, flags)."""
    aces: List[str] = field(default_factory=list)
    flags: Dict[str, Any] = field(default_factory=dict)

    def to_dict(self) -> dict:
        return asdict(self)

    @classmethod
    def from_dict(cls, d: dict) -> 'ProgressionDTO':
        return cls(
            aces=d.get('aces', []),
            flags=d.get('flags', {})
        )


@dataclass
class WorldStateDTO:
    """Persistent world state."""
    removed_entities: List[str] = field(default_factory=list)

    def to_dict(self) -> dict:
        return asdict(self)

    @classmethod
    def from_dict(cls, d: dict) -> 'WorldStateDTO':
        return cls(removed_entities=d.get('removed_entities', []))


@dataclass
class TurnStateDTO:
    """Exploration turn state."""
    exploration_active_index: int = 0
    awaiting_handoff_confirm: bool = False

    def to_dict(self) -> dict:
        return asdict(self)

    @classmethod
    def from_dict(cls, d: dict) -> 'TurnStateDTO':
        return cls(
            exploration_active_index=d.get('exploration_active_index', 0),
            awaiting_handoff_confirm=d.get('awaiting_handoff_confirm', False)
        )


@dataclass
class CheckpointDTO:
    """Checkpoint data."""
    checkpoint_room_id: Optional[str] = None
    checkpoint_spawn_id: Optional[str] = None

    def to_dict(self) -> dict:
        return asdict(self)

    @classmethod
    def from_dict(cls, d: dict) -> 'CheckpointDTO':
        return cls(
            checkpoint_room_id=d.get('checkpoint_room_id'),
            checkpoint_spawn_id=d.get('checkpoint_spawn_id')
        )


# ============== COMPOSITE DTOs ==============
@dataclass
class SaveDataDTO:
    """Main save data container."""
    world: WorldDTO = field(default_factory=WorldDTO)
    party: PartyDTO = field(default_factory=PartyDTO)
    progression: ProgressionDTO = field(default_factory=ProgressionDTO)
    world_state: WorldStateDTO = field(default_factory=WorldStateDTO)
    turn_state: TurnStateDTO = field(default_factory=TurnStateDTO)
    checkpoint: CheckpointDTO = field(default_factory=CheckpointDTO)

    def to_dict(self) -> dict:
        return {
            'world': self.world.to_dict(),
            'party': self.party.to_dict(),
            'progression': self.progression.to_dict(),
            'world_state': self.world_state.to_dict(),
            'turn_state': self.turn_state.to_dict(),
            'checkpoint': self.checkpoint.to_dict()
        }

    @classmethod
    def from_dict(cls, d: dict) -> 'SaveDataDTO':
        return cls(
            world=WorldDTO.from_dict(d.get('world', {})),
            party=PartyDTO.from_dict(d.get('party', {})),
            progression=ProgressionDTO.from_dict(d.get('progression', {})),
            world_state=WorldStateDTO.from_dict(d.get('world_state', {})),
            turn_state=TurnStateDTO.from_dict(d.get('turn_state', {})),
            checkpoint=CheckpointDTO.from_dict(d.get('checkpoint', {}))
        )


@dataclass
class SaveFileDTO:
    """Complete save file structure."""
    schema_version: int = CURRENT_SAVE_SCHEMA_VERSION
    meta: SaveMeta = field(default_factory=SaveMeta)
    data: SaveDataDTO = field(default_factory=SaveDataDTO)

    def to_dict(self) -> dict:
        return {
            'schema_version': self.schema_version,
            'meta': self.meta.to_dict(),
            'data': self.data.to_dict()
        }

    @classmethod
    def from_dict(cls, d: dict) -> 'SaveFileDTO':
        return cls(
            schema_version=d.get('schema_version', CURRENT_SAVE_SCHEMA_VERSION),
            meta=SaveMeta.from_dict(d.get('meta', {})),
            data=SaveDataDTO.from_dict(d.get('data', {}))
        )


# ============== RESULT TYPES ==============
@dataclass
class SaveResult:
    """Result of a save operation."""
    ok: bool
    message: str
    error: Optional[Exception] = None


@dataclass
class LoadResult:
    """Result of a load operation."""
    ok: bool
    message: str
    save_data: Optional[SaveFileDTO] = None
    error: Optional[Exception] = None


@dataclass
class ValidationResult:
    """Result of save validation."""
    ok: bool
    errors: List[str] = field(default_factory=list)