"""
Render System - Pipeline di rendering con layer, camera e gestione draw order
Epic 3: User Story 10, 11
"""

from enum import IntEnum
from dataclasses import dataclass, field
from typing import Callable, Optional, List, Tuple, Literal, Any
import pygame


# ============== LAYER DEFINITIONS (US10) ==============
class RenderLayer(IntEnum):
    """
    Layer di rendering definiti in un unico posto.
    I valori determinano l'ordine di draw (valori più bassi = disegnati prima/sotto).
    """
    BACKGROUND = 0
    TILEMAP = 50
    ACTORS = 100
    ACTORS_FRONT = 150
    VFX = 200
    VFX_FRONT = 250
    UI_WORLD = 500
    UI = 1000
    UI_OVERLAY = 1100
    UI_MODAL = 1200
    DEBUG = 2000
    DEBUG_TEXT = 2100


# ============== RENDER COMMAND (US10) ==============
@dataclass
class RenderCommand:
    """
    Comando di rendering da sottomettere al Renderer.
    
    Attributes:
        layer: Layer di appartenenza (determina ordine principale)
        sort_key: Tupla per ordinamento secondario (es. (y,) per pseudo-depth)
        space: "world" o "screen" - determina se applicare camera offset
        draw_callable: Funzione che esegue il draw effettivo
        submit_index: Indice assegnato dal Renderer per tie-breaking stabile
    """
    layer: int
    sort_key: Tuple = field(default_factory=tuple)
    space: Literal["world", "screen"] = "world"
    draw_callable: Callable[[pygame.Surface, 'Camera'], None] = None
    submit_index: int = 0
    
    def __lt__(self, other: 'RenderCommand') -> bool:
        """Comparatore per ordinamento: (layer, sort_key, submit_index)"""
        return (self.layer, self.sort_key, self.submit_index) < \
               (other.layer, other.sort_key, other.submit_index)


# ============== CAMERA MODE (US11) ==============
class CameraMode(IntEnum):
    """Modalità della camera"""
    FIXED = 0
    FOLLOW = 1


# ============== CAMERA BOUNDS (US11) ==============
@dataclass
class CameraBounds:
    """Limiti del mondo per la camera"""
    x: int = 0
    y: int = 0
    width: int = 0
    height: int = 0
    
    def to_rect(self) -> pygame.Rect:
        return pygame.Rect(self.x, self.y, self.width, self.height)


# ============== CAMERA (US11) ==============
class Camera:
    """
    Sistema camera per gestione viewport e trasformazioni world->screen.
    
    Garantisce:
    - Coordinate intere per il rendering (no jitter)
    - Clamping ai bounds del mondo
    - Supporto per modalità FIXED e FOLLOW
    """
    
    def __init__(self, viewport_width: int, viewport_height: int):
        self._x: float = 0.0
        self._y: float = 0.0
        self.viewport_width: int = viewport_width
        self.viewport_height: int = viewport_height
        self.mode: CameraMode = CameraMode.FIXED
        self.bounds: Optional[CameraBounds] = None
        self.fixed_pos: Tuple[int, int] = (0, 0)
        self.follow_speed: float = 1.0
    
    @property
    def x(self) -> int:
        """Posizione X come intero (per rendering)"""
        return int(round(self._x))
    
    @property
    def y(self) -> int:
        """Posizione Y come intero (per rendering)"""
        return int(round(self._y))
    
    @property
    def position(self) -> Tuple[int, int]:
        """Posizione camera come tupla intera"""
        return (self.x, self.y)
    
    def set_mode(self, mode: CameraMode) -> None:
        """Imposta la modalità camera"""
        self.mode = mode
        if mode == CameraMode.FIXED:
            self._x, self._y = float(self.fixed_pos[0]), float(self.fixed_pos[1])
    
    def set_fixed_position(self, x: int, y: int) -> None:
        """Imposta la posizione fissa (per FIXED mode)"""
        self.fixed_pos = (x, y)
        if self.mode == CameraMode.FIXED:
            self._x, self._y = float(x), float(y)
    
    def set_bounds(self, bounds: Optional[CameraBounds]) -> None:
        """Imposta i limiti del mondo"""
        self.bounds = bounds
        self._clamp()
    
    def snap_to_center(self, world_x: int, world_y: int) -> None:
        """Centra istantaneamente la camera su una posizione mondo."""
        self._x = world_x - self.viewport_width // 2
        self._y = world_y - self.viewport_height // 2
        self._clamp()
    
    def snap_to_position(self, x: int, y: int) -> None:
        """Sposta istantaneamente la camera a una posizione specifica"""
        self._x = float(x)
        self._y = float(y)
        self._clamp()
    
    def update_follow(self, target_center_x: int, target_center_y: int, dt: float = 0.0) -> None:
        """Aggiorna la camera in FOLLOW mode per seguire un target."""
        if self.mode != CameraMode.FOLLOW:
            return
        
        target_cam_x = target_center_x - self.viewport_width // 2
        target_cam_y = target_center_y - self.viewport_height // 2
        
        if self.follow_speed >= 1.0 or dt <= 0:
            self._x = target_cam_x
            self._y = target_cam_y
        else:
            lerp_factor = min(1.0, self.follow_speed * dt * 60)
            self._x += (target_cam_x - self._x) * lerp_factor
            self._y += (target_cam_y - self._y) * lerp_factor
        
        self._clamp()
    
    def _clamp(self) -> None:
        """Applica il clamping ai bounds"""
        if self.bounds is None:
            return
        
        max_x = max(0, self.bounds.width - self.viewport_width)
        max_y = max(0, self.bounds.height - self.viewport_height)
        
        self._x = max(self.bounds.x, min(self._x, self.bounds.x + max_x))
        self._y = max(self.bounds.y, min(self._y, self.bounds.y + max_y))
    
    def apply(self, world_rect: pygame.Rect) -> pygame.Rect:
        """Trasforma un rect da world space a screen space (coordinate intere)."""
        return pygame.Rect(
            world_rect.x - self.x,
            world_rect.y - self.y,
            world_rect.width,
            world_rect.height
        )
    
    def apply_point(self, world_x: int, world_y: int) -> Tuple[int, int]:
        """Trasforma un punto da world space a screen space"""
        return (world_x - self.x, world_y - self.y)
    
    def screen_to_world(self, screen_x: int, screen_y: int) -> Tuple[int, int]:
        """Trasforma un punto da screen space a world space"""
        return (screen_x + self.x, screen_y + self.y)
    
    def get_viewport_rect(self) -> pygame.Rect:
        """Ritorna il rettangolo del viewport in world space"""
        return pygame.Rect(self.x, self.y, self.viewport_width, self.viewport_height)
    
    def is_visible(self, world_rect: pygame.Rect) -> bool:
        """Verifica se un rect in world space è visibile nel viewport"""
        return self.get_viewport_rect().colliderect(world_rect)


# ============== DEBUG SETTINGS (US10) ==============
class DebugSettings:
    """Impostazioni per la modalità debug"""
    
    def __init__(self):
        self.enabled: bool = False
        self.show_colliders: bool = True
        self.show_triggers: bool = True
        self.show_entity_bounds: bool = True
        self.show_fps: bool = True
        self.show_camera_info: bool = True
        
        self.collider_color: Tuple[int, int, int] = (255, 0, 0)
        self.trigger_color: Tuple[int, int, int] = (255, 255, 0)
        self.entity_bounds_color: Tuple[int, int, int] = (0, 255, 0)
        self.hitbox_color: Tuple[int, int, int] = (0, 255, 255)
    
    def toggle(self) -> None:
        """Attiva/disattiva la modalità debug"""
        self.enabled = not self.enabled


# ============== RENDERER (US10) ==============
class Renderer:
    """
    Pipeline di rendering centralizzata.
    
    Gestisce:
    - Raccolta comandi di rendering
    - Ordinamento per layer/sort_key/submit_index
    - Applicazione trasformazioni camera
    - Draw finale
    """
    
    def __init__(self, debug_settings: Optional[DebugSettings] = None):
        self._commands: List[RenderCommand] = []
        self._submit_counter: int = 0
        self._debug: DebugSettings = debug_settings or DebugSettings()
    
    @property
    def debug_settings(self) -> DebugSettings:
        return self._debug
    
    def begin_frame(self) -> None:
        """Inizia un nuovo frame di rendering."""
        self._commands.clear()
        self._submit_counter = 0
    
    def submit(self, command: RenderCommand) -> None:
        """Sottomette un comando di rendering."""
        command.submit_index = self._submit_counter
        self._submit_counter += 1
        self._commands.append(command)
    
    def submit_sprite(self, surface: pygame.Surface, world_rect: pygame.Rect,
                      layer: int = RenderLayer.ACTORS, 
                      sort_key: Tuple = None,
                      space: Literal["world", "screen"] = "world") -> None:
        """Helper per sottomettere uno sprite."""
        if sort_key is None:
            sort_key = (world_rect.bottom,)
        
        def draw(screen: pygame.Surface, camera: Camera):
            if space == "world":
                screen_rect = camera.apply(world_rect)
            else:
                screen_rect = world_rect
            screen.blit(surface, screen_rect.topleft)
        
        self.submit(RenderCommand(
            layer=layer,
            sort_key=sort_key,
            space=space,
            draw_callable=draw
        ))
    
    def submit_rect(self, color: Tuple[int, int, int], world_rect: pygame.Rect,
                    layer: int = RenderLayer.DEBUG,
                    width: int = 1,
                    space: Literal["world", "screen"] = "world") -> None:
        """Helper per sottomettere un rettangolo (debug)."""
        def draw(screen: pygame.Surface, camera: Camera):
            if space == "world":
                screen_rect = camera.apply(world_rect)
            else:
                screen_rect = world_rect
            pygame.draw.rect(screen, color, screen_rect, width)
        
        self.submit(RenderCommand(
            layer=layer,
            sort_key=(world_rect.y,),
            space=space,
            draw_callable=draw
        ))
    
    def submit_debug_collider(self, world_rect: pygame.Rect) -> None:
        """Helper per debug: disegna collider"""
        if self._debug.enabled and self._debug.show_colliders:
            self.submit_rect(self._debug.collider_color, world_rect, 
                           RenderLayer.DEBUG, width=2, space="world")
    
    def submit_debug_trigger(self, world_rect: pygame.Rect) -> None:
        """Helper per debug: disegna trigger"""
        if self._debug.enabled and self._debug.show_triggers:
            self.submit_rect(self._debug.trigger_color, world_rect,
                           RenderLayer.DEBUG, width=2, space="world")
    
    def submit_debug_entity_bounds(self, world_rect: pygame.Rect) -> None:
        """Helper per debug: disegna bounds entità"""
        if self._debug.enabled and self._debug.show_entity_bounds:
            self.submit_rect(self._debug.entity_bounds_color, world_rect,
                           RenderLayer.DEBUG, width=1, space="world")
    
    def submit_ui(self, draw_callable: Callable[[pygame.Surface, Camera], None],
                  layer: int = RenderLayer.UI, sort_key: Tuple = None) -> None:
        """Helper per sottomettere elementi UI (sempre in screen space)."""
        self.submit(RenderCommand(
            layer=layer,
            sort_key=sort_key or (0,),
            space="screen",
            draw_callable=draw_callable
        ))
    
    def flush(self, screen: pygame.Surface, camera: Camera) -> None:
        """Esegue il rendering di tutti i comandi sottomessi."""
        self._commands.sort()
        
        for cmd in self._commands:
            if cmd.draw_callable:
                cmd.draw_callable(screen, camera)
    
    def get_command_count(self) -> int:
        """Ritorna il numero di comandi nella coda"""
        return len(self._commands)


# ============== DEBUG OVERLAY HELPER (US10) ==============
class DebugOverlay:
    """Helper per disegnare overlay di debug"""
    
    def __init__(self, renderer: Renderer, debug_settings: DebugSettings):
        self.renderer = renderer
        self.debug = debug_settings
        self._font: Optional[pygame.font.Font] = None
    
    def _get_font(self) -> pygame.font.Font:
        """Ottiene il font per il testo debug (lazy init)"""
        if self._font is None:
            pygame.font.init()
            self._font = pygame.font.Font(None, 20)
        return self._font
    
    def draw_colliders(self, colliders: List[pygame.Rect]) -> None:
        """Disegna tutti i collider"""
        if not self.debug.enabled or not self.debug.show_colliders:
            return
        for rect in colliders:
            self.renderer.submit_debug_collider(rect)
    
    def draw_triggers(self, triggers: List[pygame.Rect]) -> None:
        """Disegna tutti i trigger"""
        if not self.debug.enabled or not self.debug.show_triggers:
            return
        for rect in triggers:
            self.renderer.submit_debug_trigger(rect)
    
    def draw_text(self, text: str, screen_pos: Tuple[int, int], 
                  color: Tuple[int, int, int] = (255, 255, 255)) -> None:
        """Disegna testo debug in screen space"""
        if not self.debug.enabled:
            return
        
        font = self._get_font()
        
        def draw(screen: pygame.Surface, camera: Camera):
            text_surface = font.render(text, True, color)
            screen.blit(text_surface, screen_pos)
        
        self.renderer.submit(RenderCommand(
            layer=RenderLayer.DEBUG_TEXT,
            sort_key=(screen_pos[1],),
            space="screen",
            draw_callable=draw
        ))
    
    def draw_fps(self, fps: float, screen_pos: Tuple[int, int] = (10, 10)) -> None:
        """Disegna FPS counter"""
        if self.debug.enabled and self.debug.show_fps:
            self.draw_text(f"FPS: {fps:.1f}", screen_pos)
    
    def draw_camera_info(self, camera: Camera, 
                         screen_pos: Tuple[int, int] = (10, 30)) -> None:
        """Disegna informazioni camera"""
        if self.debug.enabled and self.debug.show_camera_info:
            mode_str = "FIXED" if camera.mode == CameraMode.FIXED else "FOLLOW"
            self.draw_text(f"Camera: ({camera.x}, {camera.y}) Mode: {mode_str}", 
                          screen_pos)