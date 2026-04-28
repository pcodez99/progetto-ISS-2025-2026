"""
Tests per il Render System
Epic 3: User Story 10 - Essential tests only
"""

import unittest
import pygame

pygame.init()

from src.model.render_system import (
    Renderer, RenderCommand, RenderLayer, Camera, DebugSettings
)


class TestRendererLayerOrdering(unittest.TestCase):
    """US10: Layer ordering tests (REQUIRED)"""
    
    def setUp(self):
        self.renderer = Renderer()
        self.camera = Camera(800, 600)
        self.screen = pygame.Surface((800, 600))
        self.draw_order = []
    
    def test_layer_ordering_mixed_submission(self):
        """Submit commands in mixed order, assert draw order matches layer order"""
        order_list = self.draw_order
        
        def make_draw(name):
            def draw(s, c):
                order_list.append(name)
            return draw
        
        self.renderer.begin_frame()
        
        # Submit in wrong order
        self.renderer.submit(RenderCommand(layer=RenderLayer.UI, sort_key=(0,), 
                                          space="screen", draw_callable=make_draw("ui")))
        self.renderer.submit(RenderCommand(layer=RenderLayer.BACKGROUND, sort_key=(0,),
                                          space="world", draw_callable=make_draw("bg")))
        self.renderer.submit(RenderCommand(layer=RenderLayer.ACTORS, sort_key=(0,),
                                          space="world", draw_callable=make_draw("actor")))
        
        self.renderer.flush(self.screen, self.camera)
        
        self.assertEqual(self.draw_order, ["bg", "actor", "ui"])
    
    def test_stable_tie_break_same_layer_same_sort_key(self):
        """Submit two commands with same layer/sort_key, assert submission order preserved"""
        order_list = self.draw_order
        
        self.renderer.begin_frame()
        
        for i in range(3):
            idx = i
            def make_draw(idx=idx):
                def draw(s, c):
                    order_list.append(f"cmd_{idx}")
                return draw
            
            self.renderer.submit(RenderCommand(
                layer=RenderLayer.ACTORS,
                sort_key=(100,),
                space="world",
                draw_callable=make_draw()
            ))
        
        self.renderer.flush(self.screen, self.camera)
        
        self.assertEqual(self.draw_order, ["cmd_0", "cmd_1", "cmd_2"])
    
    def test_world_space_applies_camera_offset(self):
        """World-space command drawn with camera offset applied"""
        self.camera.snap_to_position(100, 50)
        
        world_rect = pygame.Rect(200, 150, 32, 32)
        applied_rect = None
        
        def draw_world(screen, camera):
            nonlocal applied_rect
            applied_rect = camera.apply(world_rect)
        
        self.renderer.begin_frame()
        self.renderer.submit(RenderCommand(
            layer=RenderLayer.ACTORS, sort_key=(0,),
            space="world", draw_callable=draw_world
        ))
        self.renderer.flush(self.screen, self.camera)
        
        self.assertEqual(applied_rect.x, 100)  # 200 - 100
        self.assertEqual(applied_rect.y, 100)  # 150 - 50


if __name__ == "__main__":
    unittest.main()