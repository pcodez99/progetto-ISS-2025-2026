"""
Sette e Mezzo View - Rendering logic.
"""
import pygame
from src.model.render_system import Renderer, RenderLayer, RenderCommand
from src.view.ui_style import UIStyle, COLOR_SELECTED, COLOR_TEXT
from src.model.minigame.sette_mezzo_model import SetteMezzoModel

class SetteMezzoView:
    def __init__(self, renderer: Renderer, asset_manager):
        self.renderer = renderer
        self.assets = asset_manager
        
        self.CARD_W = 76
        self.CARD_H = 114
        self.SPACING = 20
        self.COLOR_TABLE = (80, 20, 40) # Rosso vino per Vinalia

    def render(self, screen_size: tuple, model: SetteMezzoModel, cursor_index: int):
        w, h = screen_size
        center_x = w // 2
        
        # 1. Background
        def draw_bg(screen, camera):
            bg = self.assets.get_image("sfondo_vinalia", w, h, fallback_type="background")
            if bg: screen.blit(bg, (0, 0))
            else: screen.fill(self.COLOR_TABLE)
        
        self.renderer.submit(RenderCommand(layer=RenderLayer.BACKGROUND, space='screen', draw_callable=draw_bg))

        retro_img = self.assets.get_image("retro_carta", self.CARD_W, self.CARD_H, fallback_type="prop")

        # 2. Mano CPU (Alto)
        start_y_cpu = 50
        total_w_cpu = len(model.mano_cpu) * (self.CARD_W + self.SPACING)
        start_x_cpu = center_x - (total_w_cpu // 2)
        
        for i, card in enumerate(model.mano_cpu):
            pos_x = start_x_cpu + i * (self.CARD_W + self.SPACING)
            
            # La prima carta è coperta se è ancora il turno del player
            is_hidden = (i == 0 and model.state == "PLAYER_TURN")
            
            if is_hidden:
                self._submit_sprite(retro_img, (pos_x, start_y_cpu))
            else:
                c_img = self.assets.get_image(card.asset_key, self.CARD_W, self.CARD_H)
                self._submit_sprite(c_img, (pos_x, start_y_cpu))

        # Score CPU (Nascosto se turno player)
        score_cpu_txt = "?" if model.state == "PLAYER_TURN" else str(model.score_cpu)
        self._draw_ui_text(f"Zio Totò: {score_cpu_txt}", center_x, start_y_cpu + self.CARD_H + 10, align="center")

        # 3. Mano Player (Basso)
        start_y_player = h - self.CARD_H - 100
        total_w_p = len(model.mano_player) * (self.CARD_W + self.SPACING)
        start_x_p = center_x - (total_w_p // 2)
        
        for i, card in enumerate(model.mano_player):
            pos_x = start_x_p + i * (self.CARD_W + self.SPACING)
            c_img = self.assets.get_image(card.asset_key, self.CARD_W, self.CARD_H)
            self._submit_sprite(c_img, (pos_x, start_y_player))

        self._draw_ui_text(f"Tuoi Punti: {model.score_player}", center_x, start_y_player - 30, align="center", color=(0, 255, 0))

        # 4. Action Menu (Se tocca al player)
        if model.state == "PLAYER_TURN":
            opts = ["CARTA", "STAI"]
            menu_y = h - 60
            gap = 150
            start_menu_x = center_x - (len(opts) * gap) // 4 # centered rough calculation
            
            for i, label in enumerate(opts):
                col = (255, 255, 0) if i == cursor_index else (200, 200, 200)
                px = center_x - 100 + i * 200
                self._draw_ui_text(label, px, menu_y, align="center", color=col, size="title")
                
                if i == cursor_index:
                    # Arrow indicator
                    self._draw_ui_text("^", px, menu_y + 30, align="center", color=col)

        # 5. Message
        if model.message:
            self._draw_ui_text(model.message, center_x, h // 2, align="center", size="title")

    def render_game_over(self, screen_size, winner, score_p, score_c):
        w, h = screen_size
        def draw_end(screen, camera):
            ov = pygame.Surface((w, h))
            ov.set_alpha(200); ov.fill((0,0,0))
            screen.blit(ov, (0,0))
            
            title = "HAI VINTO!" if winner == "player" else "ZIO TOTÒ VINCE..."
            col = (0, 255, 0) if winner == "player" else (255, 0, 0)
            
            UIStyle.draw_text(screen, title, w//2, 100, align="center", color=col, font_type="title")
            UIStyle.draw_text(screen, f"Tuoi Punti: {score_p}", w//2, 200, align="center")
            UIStyle.draw_text(screen, f"Punti Banco: {score_c}", w//2, 250, align="center")
            
            if winner == "player":
                UIStyle.draw_text(screen, "Hai ottenuto l'Asso di Coppe!", w//2, 350, align="center", color=(255, 215, 0))
            
            UIStyle.draw_text(screen, "Premi [INVIO]", w//2, h - 100, align="center")
            
        self.renderer.submit(RenderCommand(layer=RenderLayer.UI_MODAL, space='screen', draw_callable=draw_end))

    def _submit_sprite(self, surf, pos):
        def draw(screen, camera): screen.blit(surf, pos)
        self.renderer.submit(RenderCommand(layer=RenderLayer.ACTORS, space='screen', draw_callable=draw))

    def _draw_ui_text(self, text, x, y, align="left", color=(255, 255, 255), size="main"):
        def draw(screen, camera): UIStyle.draw_text(screen, text, x, y, color=color, font_type=size, align=align)
        self.renderer.submit(RenderCommand(layer=RenderLayer.UI, space='screen', draw_callable=draw))