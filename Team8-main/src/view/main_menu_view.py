"""
Main Menu View - Visualizzazione del menu iniziale.
Gestisce: Schermata Titolo, Selezione Slot Salvataggio.
Updated: Background image support via AssetManager.
"""
import pygame
from src.model.render_system import Renderer, RenderLayer, Camera
from src.view.ui_style import UIStyle, COLOR_SELECTED, COLOR_TEXT, COLOR_DISABLED, COLOR_BG
# Import opzionale per type hinting, non strettamente necessario a runtime se usiamo duck typing
from src.model.assets.asset_manager import AssetManager 

class MainMenuView:
    def __init__(self, renderer: Renderer, asset_manager: AssetManager):
        self.renderer = renderer
        self.assets = asset_manager
        pygame.font.init()
        self.title_font = pygame.font.SysFont("Times New Roman", 60, bold=True)
        UIStyle.init_fonts()

    def render(self, screen_size: tuple, menu_state):
        """
        Disegna il menu in base allo stato corrente.
        """
        w, h = screen_size
        center_x, center_y = w // 2, h // 2

        def draw_ui(screen: pygame.Surface, camera: Camera):
            UIStyle.init_fonts()

            # 1. Background
            # Richiediamo l'immagine "main_menu_bg" adattata alle dimensioni schermo
            bg_img = self.assets.get_image("main_menu_bg", w, h, fallback_type="background")
            
            if bg_img:
                screen.blit(bg_img, (0, 0))
            else:
                # Fallback colore solido se manca l'immagine
                screen.fill((10, 10, 20)) 
            
            # 2. Titolo (Sicily: Chronicles...)
            # Aggiungo un'ombra/outline per renderlo leggibile su qualsiasi sfondo
            def draw_text_with_shadow(text, font, color, x, y):
                shadow = font.render(text, True, (0, 0, 0))
                main = font.render(text, True, color)
                screen.blit(shadow, (x + 3, y + 3))
                screen.blit(main, (x, y))

            t_surf = self.title_font.render("SIKULA", True, (255, 200, 0))
            draw_text_with_shadow("SIKULA", self.title_font, (255, 200, 0), center_x - t_surf.get_width()//2, 80)
            
            st_surf = UIStyle._font_main.render("L'ultimo brindisi", True, (200, 200, 200))
            draw_text_with_shadow("L'ultimo brindisi", UIStyle._font_main, (200, 200, 200), center_x - st_surf.get_width()//2, 150)

            # 3. Contenuto del Menu
            if menu_state.sub_menu == "root":
                self._draw_options(screen, center_x, 300, 
                                   ["Nuova Partita", "Carica Partita", "Esci"], 
                                   menu_state.cursor_index)
            
            elif menu_state.sub_menu == "load_game":
                UIStyle.draw_text(screen, "Seleziona Slot:", center_x, 230, align="center", color=(0, 255, 255))
                self._draw_save_slots(screen, center_x, 280, menu_state.save_slots, menu_state.cursor_index)

            # Footer
            UIStyle.draw_text(screen, f"Ver: {menu_state.version}", 10, h - 30, font_type="small", color=(100, 100, 100))
            UIStyle.draw_text(screen, "[Frecce] Naviga   [INVIO] Conferma   [ESC] Indietro", w - 10, h - 30, font_type="small", align="right", color=(150, 150, 150))

        self.renderer.submit_ui(draw_ui, layer=RenderLayer.UI)

    def _draw_options(self, screen, x, start_y, options, selected_idx):
        gap = 50
        for i, text in enumerate(options):
            color = COLOR_SELECTED if i == selected_idx else COLOR_TEXT
            prefix = "> " if i == selected_idx else ""
            # Shadow effect
            UIStyle.draw_text(screen, f"{prefix}{text}", x + 2, start_y + i * gap + 2, align="center", color=(0, 0, 0))
            UIStyle.draw_text(screen, f"{prefix}{text}", x, start_y + i * gap, align="center", color=color)

    def _draw_save_slots(self, screen, center_x, start_y, slots, selected_idx):
        panel_w, panel_h = 500, 80
        gap = 90
        
        for i, slot in enumerate(slots):
            rect = pygame.Rect(center_x - panel_w//2, start_y + i * gap, panel_w, panel_h)
            
            # Highlight border if selected
            border_col = COLOR_SELECTED if i == selected_idx else (100, 100, 100)
            border_w = 3 if i == selected_idx else 1
            
            # Background (semi-transparent)
            s = pygame.Surface((rect.width, rect.height))
            s.set_alpha(200)
            s.fill((20, 20, 30))
            screen.blit(s, rect.topleft)
            
            pygame.draw.rect(screen, border_col, rect, border_w)
            
            # Text info
            slot_name = f"Slot {slot.slot_index}"
            slot_info = "Empty"
            
            if slot.status.name == "OK" and slot.meta:
                slot_info = slot.meta.format_display()
            elif slot.status.name == "CORRUPT":
                slot_info = "Corrupted Data"

            UIStyle.draw_text(screen, slot_name, rect.x + 15, rect.y + 15, color=(255, 255, 0), font_type="small")
            UIStyle.draw_text(screen, slot_info, rect.x + 15, rect.y + 40, color=COLOR_TEXT)

        # Draw "Back" option as the last item
        back_idx = len(slots)
        back_y = start_y + len(slots) * gap + 20
        color = COLOR_SELECTED if selected_idx == back_idx else COLOR_TEXT
        prefix = "> " if selected_idx == back_idx else ""
        
        # Shadow
        UIStyle.draw_text(screen, f"{prefix}Indietro", center_x+2, back_y+2, align="center", color=(0,0,0))
        UIStyle.draw_text(screen, f"{prefix}Indietro", center_x, back_y, align="center", color=color)