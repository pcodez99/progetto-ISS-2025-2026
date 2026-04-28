import pygame
from typing import Tuple, List
from src.model.settings.audio_settings import AudioSettings
from src.model.settings.settings_manager import SettingsManager
from src.model.render_system import Renderer, RenderLayer, Camera

class SettingsMenu:
    """
    Gestisce la logica di visualizzazione e modifica delle impostazioni.
    Agisce come 'View' visuale ma mantiene lo stato locale dell'interfaccia (cursore).
    """
    def __init__(self, audio_manager, settings_manager: SettingsManager, renderer: Renderer):
        self.audio = audio_manager
        self.settings_manager = settings_manager
        self.renderer = renderer
        
        # Carica settings attuali
        self.settings = self.settings_manager.load_audio_settings()
        self.audio.set_volumes(self.settings, context={"ui": "settings_init"})

        # UI State
        self.cursor_index = 0
        self.options = ["Master Volume", "Music Volume", "SFX Volume", "Save & Back"]
        
        # Visual Config
        self.font = pygame.font.SysFont("Arial", 24, bold=True)
        self.color_text = (255, 255, 255)
        self.color_selected = (255, 255, 0)
        self.color_bar_bg = (50, 50, 50)
        self.color_bar_fg = (0, 200, 0)

    def move_cursor(self, delta: int):
        self.cursor_index = (self.cursor_index + delta) % len(self.options)
        self.audio.play_sfx("sfx_ui_move.wav", volume_scale=0.5)

    def adjust_value(self, delta: float):
        """Modifica il valore dell'opzione selezionata."""
        step = 0.1 * delta # +0.1 o -0.1
        
        if self.cursor_index == 0: # Master
            new_val = self.settings.master + step
            self.settings = AudioSettings(master=new_val, music=self.settings.music, sfx=self.settings.sfx).clamp()
            self.audio.set_volumes(self.settings)
            
        elif self.cursor_index == 1: # Music
            new_val = self.settings.music + step
            self.settings = AudioSettings(master=self.settings.master, music=new_val, sfx=self.settings.sfx).clamp()
            self.audio.set_volumes(self.settings)
            
        elif self.cursor_index == 2: # SFX
            new_val = self.settings.sfx + step
            self.settings = AudioSettings(master=self.settings.master, music=self.settings.music, sfx=new_val).clamp()
            self.audio.set_volumes(self.settings)
            # Preview SFX volume changes
            if delta != 0:
                self.audio.play_sfx("sfx_ui_select.wav")

    def save_and_exit(self):
        self.settings_manager.save_audio_settings(self.settings)
        self.audio.play_sfx("sfx_ui_confirm.wav")

    def render(self, screen_dims: Tuple[int, int]):
        """Sottomette i comandi di rendering per il menu."""
        w, h = screen_dims
        center_x, center_y = w // 2, h // 2
        panel_w, panel_h = 400, 300
        panel_rect = pygame.Rect(center_x - panel_w//2, center_y - panel_h//2, panel_w, panel_h)

        def draw_ui(screen: pygame.Surface, camera: Camera):
            # Sfondo semitrasparente
            s = pygame.Surface((w, h), pygame.SRCALPHA)
            s.fill((0, 0, 0, 180))
            screen.blit(s, (0, 0))

            # Pannello
            pygame.draw.rect(screen, (30, 30, 40), panel_rect)
            pygame.draw.rect(screen, (200, 200, 200), panel_rect, 3)

            # Titolo
            title = self.font.render("SETTINGS", True, self.color_text)
            screen.blit(title, (center_x - title.get_width()//2, panel_rect.y + 20))

            # Opzioni
            start_y = panel_rect.y + 80
            gap_y = 50

            for i, label_text in enumerate(self.options):
                color = self.color_selected if i == self.cursor_index else self.color_text
                label = self.font.render(label_text, True, color)
                screen.blit(label, (panel_rect.x + 30, start_y + i * gap_y))

                # Disegna barre volume per le prime 3 opzioni
                if i < 3: 
                    val = 0.0
                    if i == 0: val = self.settings.master
                    elif i == 1: val = self.settings.music
                    elif i == 2: val = self.settings.sfx
                    
                    bar_x = panel_rect.x + 200
                    bar_y = start_y + i * gap_y + 5
                    bar_w = 150
                    bar_h = 20
                    
                    # Back bar
                    pygame.draw.rect(screen, self.color_bar_bg, (bar_x, bar_y, bar_w, bar_h))
                    # Fill bar
                    fill_w = int(bar_w * val)
                    pygame.draw.rect(screen, self.color_bar_fg, (bar_x, bar_y, fill_w, bar_h))
                    # Border
                    pygame.draw.rect(screen, (255, 255, 255), (bar_x, bar_y, bar_w, bar_h), 1)

        # Submit al renderer
        self.renderer.submit_ui(draw_ui, layer=RenderLayer.UI_MODAL)