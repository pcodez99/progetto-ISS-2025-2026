"""
Unit tests for RoomManager (User Story 2).
Tests deterministic loading, world state filtering, and unloading.
"""

import unittest
from src.controller.room_manager import RoomManager
from src.model.room_data import RoomData
from src.model.persistent_world_state import PersistentWorldState


class TestRoomManagerLoading(unittest.TestCase):
    """Tests for User Story 2: Room loading/unloading."""
    
    def setUp(self):
        self.world_state = PersistentWorldState()
        self.room_manager = RoomManager(self.world_state)
        self.test_room_data = {
            'room_id': 'test_room',
            'spawns': {
                'default': {'x': 100, 'y': 100},
                'from_east': {'x': 200, 'y': 100}
            },
            'entities': [
                {'entity_id': 'item_01', 'entity_type': 'item', 'x': 50, 'y': 50},
                {'entity_id': 'npc_01', 'entity_type': 'npc', 'x': 150, 'y': 100}
            ]
        }
    
    def test_deterministic_load_with_same_world_state(self):
        """
        Test that loading twice with same PersistentWorldState 
        yields identical spawned entities.
        """
        # Load first time
        self.room_manager.load_room_from_dict(self.test_room_data)
        entities_first = self.room_manager.get_spawned_entities()
        self.room_manager.unload_room()
        
        # Load second time
        self.room_manager.load_room_from_dict(self.test_room_data)
        entities_second = self.room_manager.get_spawned_entities()
        
        self.assertEqual(len(entities_first), len(entities_second))
        for e1, e2 in zip(entities_first, entities_second):
            self.assertEqual(e1.entity_id, e2.entity_id)
    
    def test_removed_entity_filtered_on_load(self):
        """
        Test that entities in removed_entities are filtered out on load.
        RemoveEntity persists across room reload.
        """
        # Remove an entity
        self.world_state.remove_entity('test_room', 'item_01')
        
        # Load room
        self.room_manager.load_room_from_dict(self.test_room_data)
        entities = self.room_manager.get_spawned_entities()
        
        entity_ids = [e.entity_id for e in entities]
        self.assertNotIn('item_01', entity_ids)
        self.assertIn('npc_01', entity_ids)
    
    def test_unload_clears_entities_no_ghost_updates(self):
        """
        Test that after unload, no entities remain (no ghost updates).
        """
        self.room_manager.load_room_from_dict(self.test_room_data)
        self.assertTrue(self.room_manager.is_loaded())
        self.assertGreater(len(self.room_manager.get_spawned_entities()), 0)
        
        self.room_manager.unload_room()
        
        self.assertFalse(self.room_manager.is_loaded())
        self.assertEqual(len(self.room_manager.get_spawned_entities()), 0)
        self.assertIsNone(self.room_manager.get_current_room())


if __name__ == "__main__":
    unittest.main()