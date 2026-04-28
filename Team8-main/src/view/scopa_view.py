"""
Scopa View - Rendering for the Scopa card game.
Integrated with the MVC Renderer system.
"""
import pygame
# Aggiunto RenderCommand agli import
from src.model.render_system import Renderer, RenderLayer, Camera, RenderCommand
from src.view.ui_style import UIStyle, COLOR_SELECTED, COLOR_TEXT
from src.model.minigame.scopa_model import ScopaModel, ScopaCard

class ScopaView:
    def __init__(self, renderer: Renderer, asset_manager):
        self.renderer = renderer
        self.assets = asset_manager
        
        # Layout Constants
        self.CARD_W = 76
        self.CARD_H = 114
        self.SPACING = 12
        
        # Colors
        self.COLOR_TABLE_BG = (34, 139, 34)
        self.COLOR_HIGHLIGHT = (255, 215, 0)
        self.COLOR_SELECTION = (0, 255, 255)

    def render(self, screen_size: tuple, model: ScopaModel, cursor_state: dict):
        """
        Main render loop for Scopa.
        cursor_state: {'area': 'hand'|'table', 'index': int}
        """
        w, h = screen_size
        center_x = w // 2
        
        # 1. Background Table
        def draw_bg(screen, camera):
            bg = self.assets.get_image("sfondo_tavolo", w, h, fallback_type="background")
            if bg:
                screen.blit(bg, (0, 0))
            else:
                screen.fill(self.COLOR_TABLE_BG)
        
        # FIX: Uso RenderCommand reale invece di oggetto dinamico
        self.renderer.submit(RenderCommand(
            layer=RenderLayer.BACKGROUND, 
            space='screen', 
            draw_callable=draw_bg, 
            sort_key=(0,)
        ))

        # 2. Draw Cards
        
        # --- Tavolo ---
        start_x_tavolo = center_x - ((len(model.tavolo) * (self.CARD_W + self.SPACING)) // 2)
        y_tavolo = h // 2 - self.CARD_H // 2
        
        for i, card in enumerate(model.tavolo):
            pos = (start_x_tavolo + i * (self.CARD_W + self.SPACING), y_tavolo)
            is_selected = (cursor_state['area'] == 'table' and cursor_state['index'] == i)
            # Check if this card is part of a selected capture option
            is_highlighted = i in cursor_state.get('highlight_indices', [])
            
            self._draw_card(card, pos, is_selected, is_highlighted)

        # --- Mano Player ---
        start_x_hand = center_x - ((len(model.mano_player) * (self.CARD_W + self.SPACING)) // 2)
        y_hand = h - self.CARD_H - 20
        
        for i, card in enumerate(model.mano_player):
            pos_y = y_hand - 20 if (cursor_state['area'] == 'hand' and cursor_state['index'] == i) else y_hand
            pos = (start_x_hand + i * (self.CARD_W + self.SPACING), pos_y)
            is_selected = (cursor_state['area'] == 'hand' and cursor_state['index'] == i)
            
            self._draw_card(card, pos, is_selected)

        # --- Mano CPU (Backs) ---
        start_x_cpu = center_x - ((len(model.mano_cpu) * (self.CARD_W + self.SPACING)) // 2)
        y_cpu = 20
        retro_img = self.assets.get_image("retro_carta", self.CARD_W, self.CARD_H, fallback_type="prop")
        
        for i in range(len(model.mano_cpu)):
            pos = (start_x_cpu + i * (self.CARD_W + self.SPACING), y_cpu)
            self._submit_sprite(retro_img, pos)

        # --- Mazzo (Visual) ---
        if model.mazzo:
            self._submit_sprite(retro_img, (20, h // 2 - self.CARD_H // 2))
            # Text count
            self._draw_ui_text(f"Cards: {len(model.mazzo)}", 20, h // 2 + 60)

        # 3. UI Overlay (Score, Messages)
        self._draw_hud(w, h, model)
        
        # Message
        if model.message:
            self._draw_ui_text(model.message, center_x, h - 160, align="center", color=(255, 255, 255), size="title")

    def _draw_card(self, card: ScopaCard, pos: tuple, selected: bool = False, highlighted: bool = False):
        img = self.assets.get_image(card.asset_key, self.CARD_W, self.CARD_H, fallback_type="item")
        
        def draw_func(screen, camera):
            # Draw Card
            screen.blit(img, pos)
            # Draw Selection Border
            if selected:
                pygame.draw.rect(screen, self.COLOR_SELECTION, (pos[0]-2, pos[1]-2, self.CARD_W+4, self.CARD_H+4), 3)
            elif highlighted:
                pygame.draw.rect(screen, self.COLOR_HIGHLIGHT, (pos[0]-2, pos[1]-2, self.CARD_W+4, self.CARD_H+4), 3)

        # FIX: Uso RenderCommand reale
        self.renderer.submit(RenderCommand(
            layer=RenderLayer.ACTORS, 
            space='screen', 
            draw_callable=draw_func, 
            sort_key=(0,)
        ))

    def _submit_sprite(self, surf, pos):
        def draw(screen, camera):
            screen.blit(surf, pos)
        
        # FIX: Uso RenderCommand reale
        self.renderer.submit(RenderCommand(
            layer=RenderLayer.ACTORS, 
            space='screen', 
            draw_callable=draw, 
            sort_key=(0,)
        ))

    def _draw_ui_text(self, text, x, y, align="left", color=(255, 255, 255), size="main"):
        def draw(screen, camera):
            UIStyle.draw_text(screen, text, x, y, color=color, font_type=size, align=align)
        
        # FIX: Uso RenderCommand reale
        self.renderer.submit(RenderCommand(
            layer=RenderLayer.UI, 
            space='screen', 
            draw_callable=draw, 
            sort_key=(0,)
        ))

    def _draw_hud(self, w, h, model):
        stats_p, stats_c = model.calculate_stats()
        
        # Left Panel (Player)
        def draw_panels(screen, camera):
            # Player Stats
            rect_p = pygame.Rect(10, h - 140, 150, 130)
            UIStyle.draw_panel(screen, rect_p)
            UIStyle.draw_text(screen, "PARTY (You)", rect_p.x + 10, rect_p.y + 10, color=(0, 255, 0), font_type="small")
            UIStyle.draw_text(screen, f"Scope: {stats_p.scope}", rect_p.x + 10, rect_p.y + 35, font_type="small")
            UIStyle.draw_text(screen, f"Carte: {stats_p.carte}", rect_p.x + 10, rect_p.y + 55, font_type="small")
            UIStyle.draw_text(screen, f"Denari: {stats_p.denari}", rect_p.x + 10, rect_p.y + 75, font_type="small")
            UIStyle.draw_text(screen, f"7Bello: {stats_p.settebello}", rect_p.x + 10, rect_p.y + 95, font_type="small")

            # CPU Stats
            rect_c = pygame.Rect(w - 160, 10, 150, 130)
            UIStyle.draw_panel(screen, rect_c)
            UIStyle.draw_text(screen, "DON TANINO", rect_c.x + 10, rect_c.y + 10, color=(255, 50, 50), font_type="small")
            UIStyle.draw_text(screen, f"Scope: {stats_c.scope}", rect_c.x + 10, rect_c.y + 35, font_type="small")
            UIStyle.draw_text(screen, f"Carte: {stats_c.carte}", rect_c.x + 10, rect_c.y + 55, font_type="small")
            UIStyle.draw_text(screen, f"Denari: {stats_c.denari}", rect_c.x + 10, rect_c.y + 75, font_type="small")
            
        # FIX: Uso RenderCommand reale
        self.renderer.submit(RenderCommand(
            layer=RenderLayer.UI, 
            space='screen', 
            draw_callable=draw_panels, 
            sort_key=(0,)
        ))

    def render_game_over(self, screen_size, winner, stats_p, stats_c):
        w, h = screen_size
        def draw_end(screen, camera):
            # Overlay scuro
            ov = pygame.Surface((w, h))
            ov.set_alpha(200); ov.fill((0,0,0))
            screen.blit(ov, (0,0))
            
            title = "HAI VINTO!" if winner == "player" else "HAI PERSO..."
            col = (0, 255, 0) if winner == "player" else (255, 0, 0)
            
            UIStyle.draw_text(screen, title, w//2, 100, align="center", color=col, font_type="title")
            
            # Score Details
            score_p = stats_p.total_score(stats_c)
            score_c = stats_c.total_score(stats_p)
            
            UIStyle.draw_text(screen, f"Punteggio Party: {score_p}", w//2, 200, align="center")
            UIStyle.draw_text(screen, f"Punteggio Don Tanino: {score_c}", w//2, 250, align="center")
            
            UIStyle.draw_text(screen, "Premi [INVIO] per continuare", w//2, h - 100, align="center", font_type="small")
            
        # FIX: Uso RenderCommand reale
        self.renderer.submit(RenderCommand(
            layer=RenderLayer.UI_MODAL, 
            space='screen', 
            draw_callable=draw_end, 
            sort_key=(0,)
        ))