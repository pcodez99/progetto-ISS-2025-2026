"""
Tests per il Camera System
Epic 3: User Story 11 - Essential tests only
"""

import unittest
import pygame

pygame.init()

from src.model.render_system import Camera, CameraMode, CameraBounds


class TestCameraEssential(unittest.TestCase):
    """US11: Camera essential tests (REQUIRED)"""
    
    def test_camera_apply_offsets_world_rect_with_integer_output(self):
        """apply() returns integer screen coordinates"""
        camera = Camera(800, 600)
        camera.snap_to_position(100, 50)
        
        world_rect = pygame.Rect(250, 175, 32, 32)
        screen_rect = camera.apply(world_rect)
        
        self.assertEqual(screen_rect.x, 150)
        self.assertEqual(screen_rect.y, 125)
        self.assertIsInstance(screen_rect.x, int)
        self.assertIsInstance(screen_rect.y, int)
    
    def test_follow_snap_centers_target_and_is_integer_snapped(self):
        """FOLLOW mode centers target with integer coordinates"""
        camera = Camera(800, 600)
        camera.set_mode(CameraMode.FOLLOW)
        
        camera.snap_to_center(500, 400)
        
        self.assertEqual(camera.x, 100)  # 500 - 400
        self.assertEqual(camera.y, 100)  # 400 - 300
        self.assertIsInstance(camera.x, int)
    
    def test_clamp_left_top_limits(self):
        """Camera clamps to top-left bounds"""
        camera = Camera(800, 600)
        camera.set_bounds(CameraBounds(0, 0, 1600, 1200))
        
        camera.snap_to_position(-100, -50)
        
        self.assertEqual(camera.x, 0)
        self.assertEqual(camera.y, 0)
    
    def test_clamp_right_bottom_limits(self):
        """Camera clamps to bottom-right bounds"""
        camera = Camera(800, 600)
        camera.set_bounds(CameraBounds(0, 0, 1600, 1200))
        
        camera.snap_to_position(1000, 800)
        
        self.assertEqual(camera.x, 800)   # 1600 - 800
        self.assertEqual(camera.y, 600)   # 1200 - 600
    
    def test_small_map_clamps_to_zero_axis(self):
        """Map smaller than viewport clamps to 0"""
        camera = Camera(800, 600)
        camera.set_bounds(CameraBounds(0, 0, 400, 300))
        
        camera.snap_to_position(100, 100)
        
        self.assertEqual(camera.x, 0)
        self.assertEqual(camera.y, 0)


if __name__ == "__main__":
    unittest.main()