"""
FILE: ./src/controller/render_controller.py
Render Controller - Coordina il rendering tra View e Model
Updated: Passed AssetManager to MainMenuView for background rendering.
"""

from typing import Optional, List, Dict, Any
import pygame

from src.model.render_system import (
    Renderer, Camera, DebugSettings, RenderLayer, CameraMode
)
from src.model.room_data import RoomData
from src.view.room_view import RoomView
from src.view.combat_view import CombatView
from src.view.inventory_view import InventoryView
from src.view.main_menu_view import MainMenuView
from src.view.pause_view import PauseView
from src.view.game_over_view import GameOverView
from src.model.assets.asset_manager import AssetManager
from src.model.ui.exploration_hud import ExplorationHUDBuilder, ExplorationHUDData
from src.view.aces_view import AcesView
from src.view.ui_style import UIStyle 

class RenderController:
    """Controller per coordinare il rendering."""
    
    def __init__(self, screen_width: int = 800, screen_height: int = 600):
        self.debug_settings = DebugSettings()
        self.renderer = Renderer(self.debug_settings)
        self.camera = Camera(screen_width, screen_height)
        self.asset_manager = AssetManager()
        
        self.room_view = RoomView(self.renderer, self.camera)
        self.combat_view = CombatView(self.renderer)
        self.inventory_view = InventoryView(self.renderer)
        
        self.main_menu_view = MainMenuView(self.renderer, self.asset_manager)
        
        self.pause_view = PauseView(self.renderer)
        self.game_over_view = GameOverView(self.renderer)
        self.aces_view = AcesView(self.renderer, self.asset_manager)
        
        self._current_room: Optional[RoomData] = None
        self._fps: float = 0.0
        self._font_ui = pygame.font.SysFont("Consolas", 14)
    
    def toggle_debug(self) -> bool:
        self.debug_settings.toggle()
        return self.debug_settings.enabled
    
    def load_room(self, room_data: RoomData, spawn_id: Optional[str] = None) -> tuple:
        self._current_room = room_data
        
        bg_image = None
        if room_data.background_id:
            bg_image = self.asset_manager.get_image(
                key=room_data.background_id, 
                width=room_data.width, 
                height=room_data.height, 
                fallback_type="background"
            )
            
        return self.room_view.load_room(room_data, spawn_id, bg_image)
    
    def update_camera(self, target_x: int, target_y: int, dt: float = 0.0) -> None:
        if self._current_room and self._current_room.camera_mode == CameraMode.FOLLOW:
            self.room_view.update_camera(target_x, target_y, dt)
    
    def update_fps(self, fps: float) -> None:
        self._fps = fps
    
    def begin_frame(self) -> None:
        self.renderer.begin_frame()

    def render_game_state(self, screen: pygame.Surface, game_model, dt: float):
        room_mgr = game_model.gamestate
        
        actors = []
        
        # A. Entità della stanza (Con logica di scaling personalizzata)
        if self._current_room:
            for entity in self._current_room.entities:
                
                # Definiamo le dimensioni di rendering in base all'ID o al Tipo
                target_w = entity.width
                target_h = entity.height
                
                # Keywords per identificare i Boss e i Gatekeeper
                eid = entity.entity_id.lower()
                is_boss = any(x in eid for x in ["boss", "tanino", "peppino", "goats", "toto", "oste", "ciccio"])
                is_gatekeeper = any(x in eid for x in ["guards", "golem", "sphinx", "colapesce"])
                
                fallback = "prop"

                # 1. BOSS (Giganti)
                if is_boss:
                    target_w = 120
                    target_h = 160
                    fallback = "enemy"
                
                # 2. GATEKEEPER (Grandi)
                elif is_gatekeeper:
                    target_w = 80
                    target_h = 110
                    fallback = "enemy"

                # 3. NPC Standard (Giufà, ecc.) - Alti come il player standard
                elif entity.entity_type == "npc":
                    target_w = 32
                    target_h = 64
                    fallback = "npc"
                    
                # 4. Oggetti/Props (Scala 2x standard)
                elif entity.entity_type in ["prop", "item", "interactable"]:
                    target_w = entity.width * 2
                    target_h = entity.height * 2
                    fallback = entity.entity_type

                # --- CARICAMENTO SPRITE ---
                sprite = self.asset_manager.get_image(
                    key=entity.entity_id, 
                    width=target_w, 
                    height=target_h,
                    fallback_type=fallback
                )
                
                # --- ALLINEAMENTO AI PIEDI ---
                # Calcolo centro X logico
                logic_center_x = entity.x + (entity.width // 2)
                
                # Nuova X di disegno (per centrare la sprite larga sulla hitbox stretta)
                draw_x = logic_center_x - (target_w // 2)
                
                # Nuova Y di disegno (la base della sprite coincide con la base della hitbox)
                draw_y = (entity.y + entity.height) - target_h
                
                draw_rect = pygame.Rect(draw_x, draw_y, target_w, target_h)
                
                # Ordinamento Z (Depth): Si basa sempre sulla coordinata Y dei piedi (Bottom)
                sort_y = entity.y + entity.height

                actors.append({
                    "surface": sprite,
                    "rect": draw_rect,
                    "layer": RenderLayer.ACTORS,
                    "sort_y": sort_y
                })

        # B. Party Members (DYNAMIC SCALING)
        active_char = game_model.gamestate.get_active_player()
        is_hub = (game_model.gamestate.current_room_id == "hub")
        
        # LOGICA DIMENSIONI DINAMICHE
        if is_hub:
            # Hub: Standard Size
            P_WIDTH = 32
            P_HEIGHT = 64
        else:
            # Regioni: Zoomed In (1.5x)
            P_WIDTH = 48
            P_HEIGHT = 96
            
        # Calcolo Offset Y per mantenere i piedi a terra
        # L'hitbox logica è alta 32. 
        # Offset = AltezzaVisiva - AltezzaLogica
        P_OFFSET_Y = P_HEIGHT - 32 
        
        if is_hub:
            # Nell'Hub, disegniamo TUTTI i personaggi abilitati
            for char in game_model.gamestate.party.main_characters:
                if char.is_alive: 
                    
                    fallback_key = "player" 
                    if "Rosalia" in char.name: fallback_key = "player2" 
                    
                    p_sprite = self.asset_manager.get_image(
                        key=f"characters/{char.name}", 
                        width=P_WIDTH, height=P_HEIGHT, 
                        fallback_type=fallback_key
                    )
                    
                    # Centriamo orizzontalmente se lo sprite è più largo della hitbox (32)
                    # Se P_WIDTH è 32, offset_x è 0. Se è 48, offset_x è (48-32)/2 = 8
                    offset_x = (P_WIDTH - 32) // 2
                    
                    p_rect = pygame.Rect(char.x - offset_x, char.y - P_OFFSET_Y, P_WIDTH, P_HEIGHT)
                    
                    actors.append({
                        "surface": p_sprite,
                        "rect": p_rect,
                        "layer": RenderLayer.ACTORS_FRONT,
                        "sort_y": char.y + 32 
                    })
        else:
            # Nelle Regioni, disegniamo SOLO il personaggio attivo
            if active_char:
                px, py = game_model.gamestate.party_position 
                
                # Centratura orizzontale per sprite larghi
                offset_x = (P_WIDTH - 32) // 2
                
                p_rect = pygame.Rect(px - offset_x, py - P_OFFSET_Y, P_WIDTH, P_HEIGHT)
                
                fallback_key = "player" 
                if "Rosalia" in active_char.name: fallback_key = "player2" 
                
                p_sprite = self.asset_manager.get_image(
                    key=f"characters/{active_char.name}", 
                    width=P_WIDTH, height=P_HEIGHT, 
                    fallback_type=fallback_key
                )
                
                actors.append({
                    "surface": p_sprite,
                    "rect": p_rect,
                    "layer": RenderLayer.ACTORS_FRONT,
                    "sort_y": py + 32
                })

        # 3. UI Elements
        ui_elements = []
        hud_data = ExplorationHUDBuilder.from_models(
            game_model, 
            self._current_room, 
            game_model.gamestate.exploration_turn_manager,
            input_hints={"interact": "E"}
        )
        
        def draw_hud(surface, camera):
            self._draw_exploration_hud(surface, hud_data)

        ui_elements.append({
            "draw_func": draw_hud,
            "layer": RenderLayer.UI
        })

        if game_model.prompts.info_message:
            msg = game_model.prompts.info_message
            def draw_info(surface, camera):
                w, h = surface.get_size()
                text_surf = self._font_ui.render(msg, True, (255, 255, 0))
                bg_rect = text_surf.get_rect(center=(w//2, 100)).inflate(20, 10)
                s = pygame.Surface((bg_rect.width, bg_rect.height)); s.set_alpha(200); s.fill((0, 0, 0))
                surface.blit(s, bg_rect.topleft)
                pygame.draw.rect(surface, (255, 255, 255), bg_rect, 1)
                surface.blit(text_surf, text_surf.get_rect(center=(w//2, 100)))

            ui_elements.append({
                "draw_func": draw_info,
                "layer": RenderLayer.UI_OVERLAY 
            })

        self.room_view.render(actors=actors, ui_elements=ui_elements)
        
        if self.debug_settings.enabled and self.debug_settings.show_fps:
            self.room_view.debug_overlay.draw_fps(self._fps)
        
        self.renderer.flush(screen, self.camera)

    def _draw_exploration_hud(self, surface: pygame.Surface, data: ExplorationHUDData):
        panel_rect = pygame.Rect(10, surface.get_height() - 130, 220, 120)
        s = pygame.Surface((panel_rect.width, panel_rect.height))
        s.set_alpha(200)
        s.fill((0, 0, 40))
        surface.blit(s, panel_rect.topleft)
        pygame.draw.rect(surface, (255, 255, 255), panel_rect, 2)

        y_off = 5
        lbl = self._font_ui.render(f"Zone: {data.zone_label}", True, (200, 200, 255))
        surface.blit(lbl, (panel_rect.x + 10, panel_rect.y + y_off))
        y_off += 20
        
        lbl = self._font_ui.render(f"Leader: {data.active_name}", True, (255, 255, 0))
        surface.blit(lbl, (panel_rect.x + 10, panel_rect.y + y_off))
        y_off += 25

        for p in data.party_hp_list:
            base_col = (0, 255, 0) if p['enabled'] else (100, 100, 100)
            if p['is_ko']: base_col = (255, 0, 0)
            prefix = "> " if p['is_active'] else "  "
            color = (255, 255, 0) if p['is_active'] else base_col
            
            n_surf = self._font_ui.render(f"{prefix}{p['name']}", True, color)
            surface.blit(n_surf, (panel_rect.x + 10, panel_rect.y + y_off))
            
            pct = p['hp'] / max(1, p['max_hp'])
            pygame.draw.rect(surface, (50,0,0), (panel_rect.x + 100, panel_rect.y + y_off + 4, 50, 6))
            pygame.draw.rect(surface, color, (panel_rect.x + 100, panel_rect.y + y_off + 4, 50 * pct, 6))
            y_off += 15

        if data.interact_hint:
            hint = self._font_ui.render(data.interact_hint, True, (0, 255, 255))
            w = hint.get_width()
            surface.blit(hint, (surface.get_width()//2 - w//2, surface.get_height() - 50))

    def render_combat(self, screen: pygame.Surface, combat_state):
        self.combat_view.render(screen.get_size(), combat_state.battle_ctx, combat_state.menu_state)
        self.renderer.flush(screen, self.camera)

    def render_scopa(self, screen: pygame.Surface, scopa_state):
        scopa_state.render(screen)
        self.renderer.flush(screen, self.camera)

    def render_briscola(self, screen: pygame.Surface, briscola_state):
        briscola_state.render(screen)
        self.renderer.flush(screen, self.camera)

    def render_sette_mezzo(self, screen: pygame.Surface, sm_state):
        sm_state.render(screen)
        self.renderer.flush(screen, self.camera)

    def render_cucu(self, screen: pygame.Surface, cucu_state):
        cucu_state.render(screen)
        self.renderer.flush(screen, self.camera)

    def render_aces_menu(self, screen: pygame.Surface, aces_state):
        collected = aces_state.game.gamestate.aces_collected
        self.aces_view.render(screen.get_size(), collected)
        self.renderer.flush(screen, self.camera)

    def render_inventory(self, screen: pygame.Surface, inventory_state):
        game = inventory_state.game
        player = game.gamestate.get_active_player()
        data = {
            'player_name': player.name if player else "Unknown",
            'items': player.get_inventory_in_view_format()[0] if player else [],
            'capacity': 10
        }
        self.inventory_view.render(screen.get_size(), data, inventory_state.selected_index)
        self.renderer.flush(screen, self.camera)

    def render_main_menu(self, screen: pygame.Surface, menu_state):
        self.main_menu_view.render(screen.get_size(), menu_state)
        self.renderer.flush(screen, self.camera)

    def render_pause_menu(self, screen: pygame.Surface, pause_state):
        self.pause_view.render_pause(screen.get_size(), pause_state.cursor_index)
        self.renderer.flush(screen, self.camera)

    def render_save_load(self, screen: pygame.Surface, sl_state):
        self.pause_view.render_save(
            screen.get_size(), 
            sl_state.slots, 
            sl_state.cursor_index,
            is_input=sl_state.is_input_active,
            input_text=sl_state.input_text
        )
        self.renderer.flush(screen, self.camera)

    def render_game_over(self, screen: pygame.Surface, go_state):
        self.game_over_view.render(screen.get_size(), go_state.cursor_index)
        self.renderer.flush(screen, self.camera)