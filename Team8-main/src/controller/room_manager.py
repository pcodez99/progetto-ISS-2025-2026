"""
Room Manager for loading and unloading rooms.
User Story 2: Load and unload rooms deterministically.
"""

import logging
from typing import Optional
from src.model.room_data import RoomData, EntityDefinition
from src.model.persistent_world_state import PersistentWorldState

logger = logging.getLogger(__name__)


class RoomManager:
    """
    Manages room loading, unloading, and entity spawning.
    
    Features:
    - Deterministic loading of room data
    - Applies PersistentWorldState to filter removed entities
    - Clean unloading to prevent ghost updates
    """
    
    def __init__(self, world_state: PersistentWorldState = None):
        """
        Initialize the room manager.
        
        Args:
            world_state: Reference to persistent world state.
        """
        self._world_state = world_state or PersistentWorldState()
        self._current_room: Optional[RoomData] = None
        self._current_room_id: Optional[str] = None
        self._spawned_entities: list[EntityDefinition] = []
        self._room_cache: dict[str, RoomData] = {}
        self._is_loaded = False
    
    def set_world_state(self, world_state: PersistentWorldState):
        """Set the world state reference."""
        self._world_state = world_state
    
    def load_room(self, room_data: RoomData, spawn_id: str = 'default') -> tuple[int, int]:
        """
        Load a room and return the spawn position.
        
        Args:
            room_data: The room data to load.
            spawn_id: The spawn point ID to use.
            
        Returns:
            Tuple of (x, y) spawn coordinates.
        """
        # Unload current room if any
        if self._is_loaded:
            self.unload_room()
        
        self._current_room = room_data
        self._current_room_id = room_data.room_id
        
        # Filter entities based on persistent world state
        self._spawned_entities = self._filter_removed_entities(room_data)
        
        self._is_loaded = True
        
        # Get spawn position
        spawn_pos = room_data.get_spawn_position(spawn_id)
        
        logger.info(f"Loaded room '{room_data.room_id}' at spawn '{spawn_id}' -> {spawn_pos}")
        
        return spawn_pos
    
    def load_room_from_dict(self, data: dict, spawn_id: str = 'default') -> tuple[int, int]:
        """
        Load a room from a dictionary.
        
        Args:
            data: Dictionary containing room data.
            spawn_id: The spawn point ID to use.
            
        Returns:
            Tuple of (x, y) spawn coordinates.
        """
        room_data = RoomData.from_dict(data)
        return self.load_room(room_data, spawn_id)
    
    def _filter_removed_entities(self, room_data: RoomData) -> list[EntityDefinition]:
        """
        Filter out entities based on persistent state (removed or once_flag).
        """
        filtered = []
        # Accesso ai flag del gioco tramite game reference (se disponibile) o world_state esteso
        # Per ora usiamo world_state.is_entity_removed (US 5.5)
        # US 65: Integrazione con FlagManager sarebbe ideale, ma per ora usiamo removed_entities
        
        for entity in room_data.entities:
            # 1. Check if explicitly removed (US 5.5)
            if self._world_state.is_entity_removed(room_data.room_id, entity.entity_id):
                continue
                
            # 2. Check US 65 'once_flag'
            # (Richiede accesso ai flag globali. Assumiamo che RoomManager possa accedervi o che 
            #  il filtering avvenga a livello di GameState.
            #  FIX: Spostiamo questo check nel caricamento in GameState o iniettiamo i flags qui?)
            
            # Approccio: RoomManager filtra solo 'removed'. La logica 'once_flag' è gestita
            # marcando l'entità come 'removed' nel momento in cui il flag viene settato (via script).
            
            filtered.append(entity)
            
        return filtered
    
    def unload_room(self):
        """
        Unload the current room.
        Removes/disables all entities, colliders, triggers, and listeners.
        """
        if not self._is_loaded:
            return
        
        logger.info(f"Unloading room '{self._current_room_id}'")
        
        # Clear spawned entities
        self._spawned_entities.clear()
        
        # Clear references
        self._current_room = None
        self._current_room_id = None
        self._is_loaded = False
    
    def get_current_room(self) -> Optional[RoomData]:
        """Returns the currently loaded room data."""
        return self._current_room
    
    def get_spawned_entities(self) -> list[EntityDefinition]:
        """Returns the list of spawned entities (filtered by world state)."""
        return self._spawned_entities.copy()
    
    def is_loaded(self) -> bool:
        """Returns True if a room is currently loaded."""
        return self._is_loaded
    
    def remove_entity(self, entity_id: str):
        """
        Remove an entity from the current room and persist the change.
        
        Args:
            entity_id: The entity to remove.
        """
        if not self._is_loaded or not self._current_room_id:
            return
        
        # Remove from spawned entities
        self._spawned_entities = [
            e for e in self._spawned_entities if e.entity_id != entity_id
        ]
        
        # Persist the removal
        self._world_state.remove_entity(self._current_room_id, entity_id)
        
        logger.info(f"Removed entity '{entity_id}' from room '{self._current_room_id}'")