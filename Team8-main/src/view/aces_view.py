"""
Aces View - Displays the collected Aces status.
"""
import pygame
from src.model.render_system import Renderer, RenderLayer, RenderCommand, Camera
from src.view.ui_style import UIStyle, COLOR_TEXT

class AcesView:
    def __init__(self, renderer: Renderer, asset_manager):
        self.renderer = renderer
        self.assets = asset_manager
        
        # Dimensioni per visualizzazione "Showcase"
        self.CARD_W = 100
        self.CARD_H = 150
        self.SPACING = 40

    def render(self, screen_size: tuple, aces_collected: list):
        w, h = screen_size
        
        # Pannello Centrale
        panel_w, panel_h = 600, 400
        panel_rect = pygame.Rect((w - panel_w)//2, (h - panel_h)//2, panel_w, panel_h)
        
        def draw_ui(screen: pygame.Surface, camera: Camera):
            # Sfondo scuro
            overlay = pygame.Surface((w, h))
            overlay.set_alpha(180)
            overlay.fill((0, 0, 0))
            screen.blit(overlay, (0, 0))
            
            # Pannello
            UIStyle.draw_panel(screen, panel_rect)
            
            # Titolo
            count = len(aces_collected)
            UIStyle.draw_text(screen, f"COLLEZIONE ASSI ({count}/4)", panel_rect.centerx, panel_rect.y + 30, 
                              font_type="title", align="center", color=(255, 215, 0))
            
            # Griglia Assi
            suits = [("Denari", "Aurion"), ("Spade", "Ferrum"), ("Bastoni", "Viridor"), ("Coppe", "Vinalia")]
            start_x = panel_rect.centerx - ((len(suits) * (self.CARD_W + self.SPACING)) // 2) + 20
            card_y = panel_rect.centery - self.CARD_H // 2
            
            for i, (suit, region) in enumerate(suits):
                ace_id = f"ace_{suit.lower()}"
                has_ace = ace_id in aces_collected
                
                x_pos = start_x + i * (self.CARD_W + self.SPACING)
                card_rect = pygame.Rect(x_pos, card_y, self.CARD_W, self.CARD_H)
                
                if has_ace:
                    # Recupera asset (es. cards/denari_1)
                    key = f"cards/{suit.lower()}_1"
                    img = self.assets.get_image(key, self.CARD_W, self.CARD_H, fallback_type="item")
                    screen.blit(img, card_rect)
                    
                    # Label
                    UIStyle.draw_text(screen, suit, card_rect.centerx, card_rect.bottom + 10, align="center", color=(0, 255, 0))
                else:
                    # Placeholder mancante
                    pygame.draw.rect(screen, (30, 30, 30), card_rect)
                    pygame.draw.rect(screen, (100, 100, 100), card_rect, 2)
                    
                    # Punto di domanda
                    UIStyle.draw_text(screen, "?", card_rect.centerx, card_rect.centery, align="center", font_type="title", color=(100, 100, 100))
                    
                    # Label Regione (Hint)
                    UIStyle.draw_text(screen, region, card_rect.centerx, card_rect.bottom + 10, align="center", color=(150, 150, 150), font_type="small")

            # Footer
            UIStyle.draw_text(screen, "[ESC] Chiudi", panel_rect.centerx, panel_rect.bottom - 40, align="center", font_type="small")

        self.renderer.submit(RenderCommand(layer=RenderLayer.UI_MODAL, space='screen', draw_callable=draw_ui))