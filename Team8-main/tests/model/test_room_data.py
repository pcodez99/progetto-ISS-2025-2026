"""
Tests for RoomData schema
Epic 1-2 (US3) + Epic 3 (US11)
"""

import unittest
import logging
import pygame

pygame.init()

from src.model.room_data import RoomData
from src.model.render_system import CameraMode

logging.disable(logging.WARNING)


class TestRoomDataSchema(unittest.TestCase):
    """Tests for User Story 3: RoomData schema (Epic 1-2)."""
    
    def test_room_data_loads_from_valid_dict(self):
        """Test that RoomData correctly loads from a valid dictionary."""
        data = {
            'room_id': 'test_room_01',
            'background_id': 'bg_forest',
            'collisions': [[0, 0, 100, 10], [0, 90, 100, 10]],
            'spawns': {
                'default': {'x': 50, 'y': 50},
                'from_north': {'x': 50, 'y': 10}
            },
            'exits': [{
                'exit_id': 'north_exit',
                'dest_room': 'room_02',
                'dest_spawn': 'from_south',
                'rect': [40, 0, 20, 10]
            }],
            'entities': [{
                'entity_id': 'npc_01',
                'entity_type': 'npc',
                'x': 30,
                'y': 40,
                'script_id': 'npc_01_talk'
            }],
            'triggers': [{
                'trigger_id': 'entrance_trigger',
                'trigger_type': 'on_enter',
                'script_id': 'room_intro'
            }]
        }
        
        room = RoomData.from_dict(data)
        
        self.assertEqual(room.room_id, 'test_room_01')
        self.assertEqual(room.background_id, 'bg_forest')
        self.assertEqual(len(room.collisions), 2)
        self.assertEqual(len(room.spawns), 2)
        self.assertEqual(len(room.exits), 1)
        self.assertEqual(len(room.entities), 1)
    
    def test_missing_required_field_raises_error(self):
        """Test that missing room_id raises ValueError."""
        data = {'background_id': 'bg_forest'}
        
        with self.assertRaises(ValueError) as context:
            RoomData.from_dict(data)
        
        self.assertIn('room_id', str(context.exception))
    
    def test_loading_twice_yields_identical_data(self):
        """Test deterministic loading."""
        data = {
            'room_id': 'deterministic_test',
            'spawns': {'default': [100, 200]},
            'entities': [{'entity_id': 'item_01', 'entity_type': 'item', 'x': 10, 'y': 20}]
        }
        
        room1 = RoomData.from_dict(data)
        room2 = RoomData.from_dict(data)
        
        self.assertEqual(room1.room_id, room2.room_id)
        self.assertEqual(room1.get_spawn_position('default'), room2.get_spawn_position('default'))


class TestRoomDataCamera(unittest.TestCase):
    """Tests for User Story 11: Camera integration (Epic 3)."""
    
    def test_hub_is_fixed_camera(self):
        """Hub uses FIXED camera."""
        hub = RoomData.create_hub()
        self.assertEqual(hub.camera_mode, CameraMode.FIXED)
    
    def test_large_room_is_follow_camera_with_bounds(self):
        """Large room uses FOLLOW camera with bounds."""
        room = RoomData.create_large_room()
        self.assertEqual(room.camera_mode, CameraMode.FOLLOW)
        self.assertIsNotNone(room.camera_bounds)
    
    def test_trigger_activation_uses_world_coords(self):
        """Triggers activate based on world rect overlap."""
        room = RoomData.create_hub()
        player_rect = pygame.Rect(370, 10, 32, 32)
        triggered = room.check_triggers(player_rect)
        self.assertGreater(len(triggered), 0)


if __name__ == "__main__":
    unittest.main()