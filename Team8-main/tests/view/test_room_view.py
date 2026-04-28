"""
Tests per Room View
Epic 3: User Story 10, 11 - Essential tests only
"""

import unittest
import pygame

pygame.init()

from src.model.render_system import Renderer, Camera, CameraMode
from src.model.room_data import RoomData
from src.view.room_view import RoomView


class TestRoomViewEssential(unittest.TestCase):
    """US10, US11: Essential integration test"""
    
    def test_load_room_sets_player_then_camera_same_frame(self):
        """load_room positions player and camera correctly in same frame"""
        renderer = Renderer()
        camera = Camera(800, 600)
        room_view = RoomView(renderer, camera)
        
        room = RoomData.create_large_room()
        player_x, player_y = room_view.load_room(room, "default")
        
        # Player at spawn
        spawn = room.get_spawn_point("default")
        self.assertEqual(player_x, spawn.x)
        self.assertEqual(player_y, spawn.y)
        
        # Camera centered on player (FOLLOW mode)
        self.assertEqual(camera.x, spawn.x - 400)
        self.assertEqual(camera.y, spawn.y - 300)


if __name__ == "__main__":
    unittest.main()