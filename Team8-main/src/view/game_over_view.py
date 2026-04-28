"""
Game Over View - Renders the Game Over screen.
"""
import pygame
from src.model.render_system import Renderer, RenderLayer, Camera
from src.view.ui_style import UIStyle, COLOR_SELECTED, COLOR_TEXT

class GameOverView:
    def __init__(self, renderer: Renderer):
        self.renderer = renderer
        self.font_big = pygame.font.SysFont("Arial", 60, bold=True)

    def render(self, screen_size: tuple, cursor_index: int):
        w, h = screen_size
        
        def draw_ui(screen: pygame.Surface, camera: Camera):
            # Sfondo Rosso Scuro / Nero
            screen.fill((20, 0, 0))
            
            # Testo GAME OVER
            txt = self.font_big.render("GAME OVER", True, (255, 0, 0))
            screen.blit(txt, (w//2 - txt.get_width()//2, h//3))
            
            # Opzioni
            options = ["Riprova Battaglia", "Menu Principale"]
            start_y = h//2 + 50
            
            for i, opt in enumerate(options):
                color = COLOR_SELECTED if i == cursor_index else (150, 150, 150)
                prefix = "> " if i == cursor_index else ""
                UIStyle.draw_text(screen, f"{prefix}{opt}", w//2, start_y + i * 50, align="center", color=color, font_type="title")

        self.renderer.submit_ui(draw_ui, layer=RenderLayer.UI_MODAL)