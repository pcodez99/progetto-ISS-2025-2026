"""
Persistent world state for tracking one-time interactions.
User Story 5.5: PersistentWorldState keyed by stable room/entity IDs.
"""

from dataclasses import dataclass, field
from typing import Any


@dataclass
class PersistentWorldState:
    """
    Tracks persistent world state across room reloads and saves.
    
    - removed_entities: Set of "room_id:entity_id" keys for removed entities
    - entity_vars: Optional dict for entity-specific variables
    """
    removed_entities: set[str] = field(default_factory=set)
    entity_vars: dict[str, dict[str, Any]] = field(default_factory=dict)
    
    def make_entity_key(self, room_id: str, entity_id: str) -> str:
        """
        Create a deterministic key for an entity.
        
        Args:
            room_id: The room ID.
            entity_id: The entity ID.
            
        Returns:
            A key in format "room_id:entity_id".
        """
        return f"{room_id}:{entity_id}"
    
    def remove_entity(self, room_id: str, entity_id: str):
        """
        Mark an entity as removed.
        
        Args:
            room_id: The room containing the entity.
            entity_id: The entity to remove.
        """
        key = self.make_entity_key(room_id, entity_id)
        self.removed_entities.add(key)
    
    def is_entity_removed(self, room_id: str, entity_id: str) -> bool:
        """
        Check if an entity has been removed.
        
        Args:
            room_id: The room ID.
            entity_id: The entity ID.
            
        Returns:
            True if the entity has been removed.
        """
        key = self.make_entity_key(room_id, entity_id)
        return key in self.removed_entities
    
    def set_entity_var(self, room_id: str, entity_id: str, var_name: str, value: Any):
        """
        Set a variable for an entity.
        
        Args:
            room_id: The room ID.
            entity_id: The entity ID.
            var_name: The variable name.
            value: The value to set.
        """
        key = self.make_entity_key(room_id, entity_id)
        if key not in self.entity_vars:
            self.entity_vars[key] = {}
        self.entity_vars[key][var_name] = value
    
    def get_entity_var(self, room_id: str, entity_id: str, var_name: str, default: Any = None) -> Any:
        """
        Get a variable for an entity.
        
        Args:
            room_id: The room ID.
            entity_id: The entity ID.
            var_name: The variable name.
            default: Default value if not found.
            
        Returns:
            The variable value or default.
        """
        key = self.make_entity_key(room_id, entity_id)
        return self.entity_vars.get(key, {}).get(var_name, default)
    
    def to_dict(self) -> dict:
        """
        Serialize to dictionary for saving.
        
        Returns:
            Dictionary representation.
        """
        return {
            'removed_entities': list(self.removed_entities),
            'entity_vars': self.entity_vars.copy()
        }
    
    @classmethod
    def from_dict(cls, data: dict) -> 'PersistentWorldState':
        """
        Deserialize from dictionary.
        
        Args:
            data: Dictionary representation.
            
        Returns:
            PersistentWorldState instance.
        """
        return cls(
            removed_entities=set(data.get('removed_entities', [])),
            entity_vars=data.get('entity_vars', {})
        )