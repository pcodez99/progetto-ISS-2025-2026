"""
FILE: ./src/view/room_view.py
Room View - Rendering of exploration rooms.
Updated: Background blit positioning.
"""

from typing import Optional, List, Dict, Any
import pygame

from src.model.render_system import (
    Renderer, Camera, RenderLayer, RenderCommand, 
    DebugSettings, DebugOverlay, CameraMode
)
from src.model.room_data import RoomData


class RoomView:
    def __init__(self, renderer: Renderer, camera: Camera):
        self.renderer = renderer
        self.camera = camera
        self.debug_overlay = DebugOverlay(renderer, renderer.debug_settings)
        self._background_surface: Optional[pygame.Surface] = None
        self._room_data: Optional[RoomData] = None
    
    def load_room(self, room_data: RoomData, spawn_id: Optional[str] = None, bg_image: Optional[pygame.Surface] = None) -> tuple:
        self._room_data = room_data
        
        if bg_image:
            self._background_surface = bg_image
        else:
            self._background_surface = pygame.Surface((room_data.width, room_data.height))
            self._background_surface.fill(room_data.background_color)
            self._draw_grid(self._background_surface)

        self.camera.set_mode(room_data.camera_mode)
        
        if room_data.camera_mode == CameraMode.FIXED:
            self.camera.set_fixed_position(*room_data.camera_fixed_pos)
        
        if room_data.camera_bounds:
            self.camera.set_bounds(room_data.camera_bounds)
        else:
            self.camera.set_bounds(None)
        
        spawn = room_data.get_spawn_point(spawn_id)
        px, py = (spawn.x, spawn.y) if spawn else (room_data.width // 2, room_data.height // 2)
        
        if room_data.camera_mode == CameraMode.FOLLOW:
            self.camera.snap_to_center(px, py)
        
        return (px, py)

    def _draw_grid(self, surface):
        color = (60, 60, 70)
        w, h = surface.get_size()
        for x in range(0, w, 50): pygame.draw.line(surface, color, (x, 0), (x, h))
        for y in range(0, h, 50): pygame.draw.line(surface, color, (0, y), (w, y))

    def update_camera(self, target_x: int, target_y: int, dt: float = 0.0):
        self.camera.update_follow(target_x, target_y, dt)
    
    def render(self, actors=None, vfx_list=None, ui_elements=None):
        if not self._room_data: return
        self._submit_background()
        if actors: self._submit_actors(actors)
        if vfx_list: self._submit_vfx(vfx_list)
        if ui_elements: self._submit_ui(ui_elements)
        self._submit_debug()
    
    def _submit_background(self):
        if not self._background_surface: return
        bg_surface = self._background_surface
        
        def draw_bg(screen: pygame.Surface, camera: Camera):
            # Posizione 0,0 nel mondo trasformata in screen space dalla camera
            screen_pos = camera.apply_point(0, 0)
            screen.blit(bg_surface, screen_pos)
        
        self.renderer.submit(RenderCommand(
            layer=RenderLayer.BACKGROUND,
            sort_key=(0,),
            space="world",
            draw_callable=draw_bg
        ))

    def _submit_actors(self, actors):
        for a in actors:
            if not a.get('surface') or not a.get('rect'): continue
            self.renderer.submit_sprite(a['surface'], a['rect'], a.get('layer', RenderLayer.ACTORS), (a.get('sort_y', a['rect'].bottom),))

    def _submit_vfx(self, vfx_list):
        for v in vfx_list:
            if not v.get('surface') or not v.get('rect'): continue
            self.renderer.submit_sprite(v['surface'], v['rect'], v.get('layer', RenderLayer.VFX), (v['rect'].y,))

    def _submit_ui(self, ui_elements):
        for u in ui_elements:
            if u.get('draw_func'): self.renderer.submit_ui(u['draw_func'], u.get('layer', RenderLayer.UI), u.get('sort_key', (0,)))

    def _submit_debug(self):
        self.debug_overlay.draw_colliders(self._room_data.get_collider_rects())
        self.debug_overlay.draw_triggers(self._room_data.get_trigger_rects())
        self.debug_overlay.draw_camera_info(self.camera)