"""
Unit tests for PersistentWorldState (User Story 5.5).
Tests entity removal persistence across room reloads and saves.
"""

import unittest
from src.model.persistent_world_state import PersistentWorldState


class TestPersistentWorldState(unittest.TestCase):
    """Tests for User Story 5.5: PersistentWorldState."""
    
    def setUp(self):
        self.world_state = PersistentWorldState()
    
    def test_remove_entity_persists(self):
        """Test that removed entities are tracked."""
        self.assertFalse(self.world_state.is_entity_removed('room_01', 'chest_01'))
        
        self.world_state.remove_entity('room_01', 'chest_01')
        
        self.assertTrue(self.world_state.is_entity_removed('room_01', 'chest_01'))
        # Different entity in same room not affected
        self.assertFalse(self.world_state.is_entity_removed('room_01', 'chest_02'))
        # Same entity ID in different room not affected
        self.assertFalse(self.world_state.is_entity_removed('room_02', 'chest_01'))
    
    def test_save_load_preserves_removed_entities(self):
        """Test that removed_entities persists through save/load."""
        self.world_state.remove_entity('dungeon_01', 'key_item')
        self.world_state.remove_entity('dungeon_02', 'treasure')
        
        # Serialize
        data = self.world_state.to_dict()
        
        # Create new state and load
        new_state = PersistentWorldState.from_dict(data)
        
        self.assertTrue(new_state.is_entity_removed('dungeon_01', 'key_item'))
        self.assertTrue(new_state.is_entity_removed('dungeon_02', 'treasure'))
        self.assertFalse(new_state.is_entity_removed('dungeon_01', 'other'))
    
    def test_entity_key_format_is_deterministic(self):
        """Test that entity keys are created deterministically."""
        key1 = self.world_state.make_entity_key('room_a', 'entity_1')
        key2 = self.world_state.make_entity_key('room_a', 'entity_1')
        
        self.assertEqual(key1, key2)
        self.assertEqual(key1, 'room_a:entity_1')


if __name__ == "__main__":
    unittest.main()