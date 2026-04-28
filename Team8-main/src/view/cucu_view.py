"""
Cucù View - Rendering logic.
"""
import pygame
from src.model.render_system import Renderer, RenderLayer, RenderCommand
from src.view.ui_style import UIStyle, COLOR_SELECTED, COLOR_TEXT
from src.model.minigame.cucu_model import CucuModel

class CucuView:
    def __init__(self, renderer: Renderer, asset_manager):
        self.renderer = renderer
        self.assets = asset_manager
        
        self.CARD_W = 100
        self.CARD_H = 150
        self.COLOR_TABLE = (139, 69, 19) # Marrone legno per Viridor

    def render(self, screen_size: tuple, model: CucuModel, cursor_index: int):
        w, h = screen_size
        center_x = w // 2
        
        # 1. Background
        def draw_bg(screen, camera):
            bg = self.assets.get_image("sfondo_viridor", w, h, fallback_type="background")
            if bg: screen.blit(bg, (0, 0))
            else: screen.fill(self.COLOR_TABLE)
        
        self.renderer.submit(RenderCommand(layer=RenderLayer.BACKGROUND, space='screen', draw_callable=draw_bg))

        retro_img = self.assets.get_image("retro_carta", self.CARD_W, self.CARD_H, fallback_type="prop")

        # 2. Carte
        # Boss (Sinistra o Alto)
        pos_cpu = (center_x - 150, 100)
        # Player (Destra o Basso)
        pos_player = (center_x + 50, 100)
        
        # Draw CPU Card
        if model.state in ["ROUND_END", "GAME_OVER"]:
            # Rivela carta Boss
            img_c = self.assets.get_image(model.card_cpu.asset_key, self.CARD_W, self.CARD_H)
            self._submit_sprite(img_c, pos_cpu)
        else:
            self._submit_sprite(retro_img, pos_cpu)
            
        # Draw Player Card
        img_p = self.assets.get_image(model.card_player.asset_key, self.CARD_W, self.CARD_H)
        self._submit_sprite(img_p, pos_player)
        
        # Labels
        self._draw_ui_text("La Sphinx", pos_cpu[0], pos_cpu[1] - 30)
        self._draw_ui_text("TU", pos_player[0], pos_player[1] - 30, color=(0, 255, 0))

        # 3. Lives (Visual Tokens)
        self._draw_lives(model.lives_cpu, pos_cpu[0], pos_cpu[1] + self.CARD_H + 10)
        self._draw_lives(model.lives_player, pos_player[0], pos_player[1] + self.CARD_H + 10)

        # 4. Action Menu
        if model.state == "PLAYER_TURN":
            opts = ["TIENI", "SCAMBIA"]
            menu_y = h - 150
            
            for i, label in enumerate(opts):
                col = (255, 255, 0) if i == cursor_index else (200, 200, 200)
                px = center_x - 100 + i * 200
                self._draw_ui_text(label, px, menu_y, align="center", color=col, size="title")
                if i == cursor_index:
                    self._draw_ui_text("^", px, menu_y + 40, align="center", color=col)

        # 5. Message / Status
        self._draw_ui_text(model.message, center_x, h - 80, align="center", size="main")
        
        if model.state == "ROUND_END":
             self._draw_ui_text("Premi [INVIO] per il prossimo round", center_x, h - 40, align="center", size="small")

    def _draw_lives(self, count, x, y):
        """Disegna pallini/simboli per le vite."""
        def draw(screen, camera):
            for i in range(count):
                pygame.draw.circle(screen, (255, 0, 0), (x + 15 + i * 25, y + 10), 8)
                pygame.draw.circle(screen, (255, 255, 255), (x + 15 + i * 25, y + 10), 9, 1)
        self.renderer.submit(RenderCommand(layer=RenderLayer.UI, space='screen', draw_callable=draw))

    def render_game_over(self, screen_size, winner):
        w, h = screen_size
        def draw_end(screen, camera):
            ov = pygame.Surface((w, h))
            ov.set_alpha(200); ov.fill((0,0,0))
            screen.blit(ov, (0,0))
            
            title = "HAI VINTO!" if winner == "player" else "LA SPHINX VINCE..."
            col = (0, 255, 0) if winner == "player" else (255, 0, 0)
            
            UIStyle.draw_text(screen, title, w//2, h//2 - 50, align="center", color=col, font_type="title")
            UIStyle.draw_text(screen, "Premi [INVIO]", w//2, h//2 + 50, align="center")
            
        self.renderer.submit(RenderCommand(layer=RenderLayer.UI_MODAL, space='screen', draw_callable=draw_end))

    def _submit_sprite(self, surf, pos):
        def draw(screen, camera): screen.blit(surf, pos)
        self.renderer.submit(RenderCommand(layer=RenderLayer.ACTORS, space='screen', draw_callable=draw))

    def _draw_ui_text(self, text, x, y, align="left", color=(255, 255, 255), size="main"):
        def draw(screen, camera): UIStyle.draw_text(screen, text, x, y, color=color, font_type=size, align=align)
        self.renderer.submit(RenderCommand(layer=RenderLayer.UI, space='screen', draw_callable=draw))