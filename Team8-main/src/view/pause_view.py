"""
Pause & Save View - Gestione grafica del menu di pausa e salvataggio in-game.
"""
import pygame
from src.model.render_system import Renderer, RenderLayer, Camera
from src.view.ui_style import UIStyle, COLOR_SELECTED, COLOR_TEXT

class PauseView:
    def __init__(self, renderer: Renderer):
        self.renderer = renderer

    def render_pause(self, screen_size: tuple, cursor_index: int):
        """Disegna il piccolo menu di pausa al centro."""
        w, h = screen_size
        panel_w, panel_h = 300, 250
        rect = pygame.Rect((w - panel_w)//2, (h - panel_h)//2, panel_w, panel_h)

        def draw_ui(screen: pygame.Surface, camera: Camera):
            # Oscura leggermente lo sfondo
            overlay = pygame.Surface((w, h))
            overlay.set_alpha(150)
            overlay.fill((0, 0, 0))
            screen.blit(overlay, (0, 0))

            # Disegna Pannello
            UIStyle.draw_panel(screen, rect)
            UIStyle.draw_text(screen, "PAUSA", rect.centerx, rect.y + 30, font_type="title", align="center")

            options = ["Riprendi", "Salva Partita", "Menu Principale"]
            start_y = rect.y + 90
            
            for i, text in enumerate(options):
                color = COLOR_SELECTED if i == cursor_index else COLOR_TEXT
                prefix = "> " if i == cursor_index else ""
                UIStyle.draw_text(screen, f"{prefix}{text}", rect.centerx, start_y + i * 40, align="center", color=color)

        self.renderer.submit_ui(draw_ui, layer=RenderLayer.UI_MODAL)

    def render_save(self, screen_size: tuple, slots: list, cursor_index: int, is_input: bool = False, input_text: str = ""):
        """Disegna la lista slot per il salvataggio (Simile al Main Menu ma in-game)."""
        w, h = screen_size
        panel_w, panel_h = 500, 400
        rect = pygame.Rect((w - panel_w)//2, (h - panel_h)//2, panel_w, panel_h)

        def draw_ui(screen: pygame.Surface, camera: Camera):
            # Sfondo oscurato
            overlay = pygame.Surface((w, h))
            overlay.set_alpha(200)
            overlay.fill((0, 0, 0))
            screen.blit(overlay, (0, 0))

            UIStyle.draw_panel(screen, rect)
            UIStyle.draw_text(screen, "SALVA PARTITA", rect.centerx, rect.y + 30, font_type="title", align="center")

            # Disegna Slots
            slot_start_y = rect.y + 80
            for i, slot in enumerate(slots):
                # Box dello slot
                s_rect = pygame.Rect(rect.x + 30, slot_start_y + i * 70, rect.width - 60, 60)
                
                border_col = COLOR_SELECTED if i == cursor_index else (100, 100, 100)
                border_w = 3 if i == cursor_index else 1
                
                pygame.draw.rect(screen, (20, 20, 30), s_rect)
                pygame.draw.rect(screen, border_col, s_rect, border_w)

                # Info slot
                info = "Slot Vuoto"
                if slot.status.name == "OK" and slot.meta:
                    info = slot.meta.format_display()
                
                UIStyle.draw_text(screen, f"Slot {slot.slot_index}", s_rect.x + 10, s_rect.y + 10, color=(255, 255, 0), font_type="small")
                UIStyle.draw_text(screen, info, s_rect.x + 10, s_rect.y + 35, color=COLOR_TEXT, font_type="small")

            # Tasto Indietro
            back_idx = len(slots)
            color = COLOR_SELECTED if cursor_index == back_idx else COLOR_TEXT
            UIStyle.draw_text(screen, "Indietro", rect.centerx, rect.bottom - 40, align="center", color=color)

            if is_input:
                # Box centrale
                input_rect = pygame.Rect(rect.centerx - 150, rect.centery - 40, 300, 80)
                pygame.draw.rect(screen, (0, 0, 50), input_rect)
                pygame.draw.rect(screen, (255, 255, 0), input_rect, 3)
                
                UIStyle.draw_text(screen, "Nome Salvataggio:", input_rect.centerx, input_rect.y + 10, align="center", font_type="small")
                
                # Testo digitato
                UIStyle.draw_text(screen, input_text + "_", input_rect.centerx, input_rect.y + 40, align="center", color=(0, 255, 0), font_type="main")

        self.renderer.submit_ui(draw_ui, layer=RenderLayer.UI_MODAL)