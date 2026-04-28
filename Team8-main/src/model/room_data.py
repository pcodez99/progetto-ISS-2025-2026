"""
Room Data - Unified schema for room loading and runtime behavior
Epic 1-2 (US2, US3) + Epic 3 (US11) + Epic 15 (US 58, 60, 61)
Epic 27: Checkpoint support (US 110)
Updated: Added auto_trigger and label to TriggerZone for better UX.
"""

import json
import logging
import math
from dataclasses import dataclass, field
from typing import Optional, List, Dict, Any, Tuple
import pygame
from src.model.render_system import CameraMode, CameraBounds

logger = logging.getLogger(__name__)

# ============== DEFINITIONS ==============

@dataclass
class SpawnPoint:
    """Definition of a spawn point (unified)"""
    spawn_id: str
    x: int
    y: int
    facing: str = "down"

@dataclass
class Collider:
    """Collider for blocking movement (Epic 3/15)"""
    id: str
    rect: pygame.Rect

    def contains(self, x: int, y: int) -> bool:
        return self.rect.collidepoint(x, y)

@dataclass
class TriggerZone:
    """Runtime trigger zone for events/transitions (Epic 3/15)"""
    id: str
    rect: pygame.Rect
    trigger_type: str # "exit", "script"
    target_room: Optional[str] = None
    target_spawn: Optional[str] = None
    script_id: Optional[str] = None
    requires_confirm: bool = False
    prompt_text: Optional[str] = None
    data: Dict[str, Any] = field(default_factory=dict)
    
    # UX Improvements
    auto_trigger: bool = False  # If True, triggers automatically on enter
    label: Optional[str] = None # Text to show in HUD (e.g., "Enter Door")

    def get_center(self) -> Tuple[int, int]:
        return self.rect.center

@dataclass
class EntityDefinition:
    """Definition of an entity (NPC, Item, Prop)"""
    entity_id: str
    entity_type: str  # "npc", "enemy", "interactable", "item", "prop"
    x: int
    y: int
    width: int = 32
    height: int = 32
    properties: dict = field(default_factory=dict)
    
    # Simple interaction (US 61)
    script_id: Optional[str] = None
    interaction_label: Optional[str] = None
    
    # Advanced interaction (US 63)
    # List of {"label": "Talk", "script_id": "..."}
    actions: List[Dict[str, str]] = field(default_factory=list)
    
    # Persistence (US 65)
    # If set, entity only spawns if flag is FALSE
    once_flag: Optional[str] = None

    def get_rect(self) -> pygame.Rect:
        return pygame.Rect(self.x, self.y, self.width, self.height)

    def get_center(self) -> Tuple[int, int]:
        return (self.x + self.width // 2, self.y + self.height // 2)

@dataclass
class RoomData:
    """Unified room data schema."""
    room_id: str
    name: str = ""
    width: int = 800
    height: int = 600
    background_id: Optional[str] = None
    tilemap_id: Optional[str] = None
    background_color: Tuple[int, int, int] = (40, 40, 40)
    camera_mode: CameraMode = CameraMode.FIXED
    camera_fixed_pos: Tuple[int, int] = (0, 0)
    camera_bounds: Optional[CameraBounds] = None
    
    spawns: Dict[str, SpawnPoint] = field(default_factory=dict)
    default_spawn_id: str = "default"
    entities: List[EntityDefinition] = field(default_factory=list)
    colliders: List[Collider] = field(default_factory=list)
    triggers: List[TriggerZone] = field(default_factory=list)

    # Epic 27: Checkpoint System (US 110)
    is_checkpoint: bool = False

    # Legacy Schema Holders (kept for backward compatibility with older tests)
    exits: List[Any] = field(default_factory=list)
    triggers_schema: List[Any] = field(default_factory=list)
    collisions: List[Tuple[int, int, int, int]] = field(default_factory=list)

    def get_spawn_point(self, spawn_id: Optional[str] = None) -> SpawnPoint:
        if spawn_id is None:
            spawn_id = self.default_spawn_id
        if spawn_id in self.spawns:
            return self.spawns[spawn_id]
        if self.default_spawn_id in self.spawns:
            return self.spawns[self.default_spawn_id]
        return SpawnPoint("fallback", self.width // 2, int(self.height * 0.9))

    def get_spawn_position(self, spawn_id: str = None) -> tuple[int, int]:
        sp = self.get_spawn_point(spawn_id)
        return (sp.x, sp.y)

    def check_collision(self, rect: pygame.Rect) -> bool:
        for collider in self.colliders:
            if rect.colliderect(collider.rect):
                return True
        return False

    def check_triggers(self, rect: pygame.Rect) -> List[TriggerZone]:
        return [t for t in self.triggers if rect.colliderect(t.rect)]

    def get_closest_interactable(self, px: int, py: int, max_range: float) -> Optional[EntityDefinition]:
        nearest = None
        min_dist = float('inf')
        for entity in self.entities:
            if not entity.script_id and not entity.interaction_label:
                continue
            cx, cy = entity.get_center()
            dist = math.sqrt((px - cx)**2 + (py - cy)**2)
            if dist <= max_range and dist < min_dist:
                min_dist = dist
                nearest = entity
        return nearest

    def get_collider_rects(self) -> List[pygame.Rect]:
        return [c.rect for c in self.colliders]

    def get_trigger_rects(self) -> List[pygame.Rect]:
        return [t.rect for t in self.triggers]

    # ============== FACTORY & LOAD METHODS ==============

    @classmethod
    def from_dict(cls, data: dict) -> 'RoomData':
        """Load RoomData from a dictionary."""
        # Fix 1: Check key existence BEFORE access to avoid KeyError
        if 'room_id' not in data:
            raise ValueError("Missing required field: room_id")

        room = cls(
            room_id=data['room_id'],
            name=data.get('name', data['room_id']),
            width=data.get('width', 800),
            height=data.get('height', 600),
            background_id=data.get('background_id'),
            camera_mode=CameraMode(data.get('camera_mode', 0)) if isinstance(data.get('camera_mode'), int) else CameraMode.FIXED,
            is_checkpoint=data.get('is_checkpoint', False) # US 110
        )

        # Spawns parsing
        for sid, sdata in data.get('spawns', {}).items():
            if isinstance(sdata, list):
                room.spawns[sid] = SpawnPoint(sid, sdata[0], sdata[1])
            elif isinstance(sdata, dict):
                 room.spawns[sid] = SpawnPoint(sid, sdata.get('x'), sdata.get('y'))

        # Entities parsing
        for edata in data.get('entities', []):
            room.entities.append(EntityDefinition(
                entity_id=edata['entity_id'],
                entity_type=edata.get('entity_type', 'prop'),
                x=edata.get('x', 0),
                y=edata.get('y', 0),
                properties=edata.get('properties', {}),
                script_id=edata.get('script_id') or edata.get('properties', {}).get('interaction_script'),
                interaction_label=edata.get('properties', {}).get('label')
            ))

        # Runtime Collisions construction
        collisions_raw = data.get('collisions', [])
        # Fix 2: Populate legacy collisions list for TestRoomDataSchema
        room.collisions = [tuple(c) for c in collisions_raw]
        
        for i, c in enumerate(collisions_raw):
            room.colliders.append(Collider(f"col_{i}", pygame.Rect(*c)))
        
        # Runtime Triggers
        for t in data.get('triggers', []):
             if 'rect' in t:
                 room.triggers.append(TriggerZone(
                     id=t.get('trigger_id', 'trig'),
                     rect=pygame.Rect(*t['rect']),
                     trigger_type=t.get('trigger_type', 'script'),
                     script_id=t.get('script_id'),
                     requires_confirm=t.get('requires_confirm', False),
                     prompt_text=t.get('prompt_text', "Interact?"),
                     # US 108: data for gate logic
                     data=t.get('data', {}),
                     auto_trigger=t.get('auto_trigger', False),
                     label=t.get('label', None)
                 ))
        
        # Exits as Triggers
        for ex in data.get('exits', []):
             room.triggers.append(TriggerZone(
                 id=ex.get('exit_id', 'exit'),
                 rect=pygame.Rect(*ex.get('rect', [0,0,32,32])),
                 trigger_type='exit',
                 target_room=ex.get('dest_room'),
                 target_spawn=ex.get('dest_spawn'),
                 requires_confirm=ex.get('requires_confirm', False),
                 prompt_text=ex.get('prompt_text', "Go to next area?")
             ))
        
        # Populate legacy exits list for tests
        room.exits = data.get('exits', [])

        return room

    # ============== HELPER FACTORY METHODS ==============
    
    @classmethod
    def create_hub(cls) -> 'RoomData':
        """Factory method for testing/default hub."""
        r = cls(
            room_id="hub",
            width=800,
            height=600,
            camera_mode=CameraMode.FIXED,
            is_checkpoint=True # Hub is always safe
        )
        # Fix: Add test trigger for TestRoomDataCamera
        r.triggers.append(TriggerZone("test_trig", pygame.Rect(350, 0, 100, 50), "script"))
        return r

    @classmethod
    def create_large_room(cls) -> 'RoomData':
        """Factory method for testing large rooms with camera follow."""
        room = cls(
            room_id="large_room",
            width=1600,
            height=1200,
            camera_mode=CameraMode.FOLLOW
        )
        room.camera_bounds = CameraBounds(0, 0, 1600, 1200)
        room.spawns["default"] = SpawnPoint("default", 500, 400)
        return room