"""
Inventory View - Renders the inventory state.
Updated: Added display name mapping for UI beautification (User Request).
"""
import pygame
from src.model.render_system import Renderer, RenderLayer, Camera
from src.view.ui_style import UIStyle, COLOR_SELECTED, COLOR_TEXT, COLOR_DISABLED

class InventoryView:
    # Mappa statica per tradurre gli ID tecnici in Nomi visualizzati
    DISPLAY_NAMES = {
        # --- ASSI ---
        "ace_denari": "Asso di Denari",
        "ace_bastoni": "Asso di Bastoni",
        "ace_spade": "Asso di Spade",
        "ace_coppe": "Asso di Coppe",
        
        # --- AURION ---
        "arancina_calda": "Arancina Fumante",
        "sacco_monete": "Sacco di Monete",
        "fascicolo_segreto": "Fascicolo Top Secret",
        
        # --- VIRIDOR ---
        "cesta_fichi": "Cesta di Fichi",
        "acqua_benedetta": "Acqua Benedetta",
        "cesoie_arrugginite": "Cesoie Antiche",
        
        # --- FERRUM ---
        "olio_lubrificante": "Olio Eterno",
        "scudo_torre": "Scudo Torre",
        "testa_orlando": "Testa di Pupo",
        
        # --- VINALIA ---
        "vino_eterno": "Vino Eterno",
        "aceto_madre": "Aceto Madre",
        "marranzano_oro": "Marranzano d'Oro",
        
        # --- FINALE ---
        "cannolo_bazooka": "Cannolo Bazooka",
        "liquore_leggendario": "Liquore Leggendario",
        
        # --- GENERICI ---
        "spiritu_fikudinnia": "Spiritu 're Fikudinnia"
    }

    def __init__(self, renderer: Renderer):
        self.renderer = renderer

    def _get_display_name(self, raw_name: str) -> str:
        """Traduce l'ID tecnico in un nome leggibile, se presente nella mappa."""
        return self.DISPLAY_NAMES.get(raw_name, raw_name)

    def render(self, screen_size: tuple, inventory_data: dict, selected_index: int):
        """
        Renders the inventory UI.
        inventory_data expected: {'items': [...], 'capacity': 10, 'player_name': '...'}
        """
        w, h = screen_size
        
        # Main Panel
        panel_w, panel_h = 600, 450
        panel_rect = pygame.Rect((w - panel_w)//2, (h - panel_h)//2, panel_w, panel_h)
        
        def draw_ui(screen: pygame.Surface, camera: Camera):
            UIStyle.draw_panel(screen, panel_rect)
            
            # Title
            UIStyle.draw_text(screen, f"Inventario: {inventory_data.get('player_name', 'Player')}", 
                              panel_rect.centerx, panel_rect.y + 30, font_type="title", align="center")
            
            # Item Grid/List
            items = inventory_data.get('items', [])
            start_x = panel_rect.x + 40
            start_y = panel_rect.y + 80
            line_height = 35
            
            # Draw Items
            if not items:
                UIStyle.draw_text(screen, "Zaino Vuoto", panel_rect.centerx, panel_rect.centery, align="center")
            else:
                for i, item in enumerate(items):
                    color = COLOR_SELECTED if i == selected_index else COLOR_TEXT
                    prefix = "> " if i == selected_index else "  "
                    
                    # --- APPLICAZIONE TRADUZIONE NOME ---
                    raw_name = item['name']
                    display_name = self._get_display_name(raw_name)
                    label = f"{prefix}{display_name}"
                    
                    UIStyle.draw_text(screen, label, start_x, start_y + i * line_height, color=color)
                    
                    # Draw Quantity if available (mock logic for now)
                    # UIStyle.draw_text(screen, "x1", panel_rect.right - 60, start_y + i * line_height, color=color)

            # Description Box
            desc_rect = pygame.Rect(panel_rect.x + 20, panel_rect.bottom - 100, panel_rect.width - 40, 80)
            UIStyle.draw_panel(screen, desc_rect, border_width=1)
            
            desc_text = "Nessun oggetto selezionato."
            if 0 <= selected_index < len(items):
                desc_text = items[selected_index].get('description', 'Nessuna descrizione.')
            
            UIStyle.draw_text(screen, desc_text, desc_rect.x + 10, desc_rect.y + 10, font_type="small")
            
            # Hints
            UIStyle.draw_text(screen, "[ESC] Chiudi   [INVIO] Usa/Equipaggia", panel_rect.right - 20, panel_rect.bottom + 10, 
                              align="right", font_type="small")

        self.renderer.submit_ui(draw_ui, layer=RenderLayer.UI_MODAL)