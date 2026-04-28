"""
UI Style - Shared resources for UI rendering.
Provides standard colors, fonts, and drawing primitives for the RPG look.
"""
import pygame

# Colors
COLOR_BG = (0, 0, 40)          # Dark Blue Background
COLOR_BORDER = (255, 255, 255) # White Border
COLOR_TEXT = (255, 255, 255)   # White Text
COLOR_SELECTED = (255, 255, 0) # Yellow Selection
COLOR_HP_BG = (50, 0, 0)       # Dark Red
COLOR_HP_FG = (0, 255, 0)      # Green
COLOR_HP_LOW = (255, 0, 0)     # Red (Low HP)
COLOR_DISABLED = (100, 100, 100)

class UIStyle:
    _font_main = None
    _font_small = None
    _font_title = None

    @classmethod
    def init_fonts(cls):
        if cls._font_main is None:
            pygame.font.init()
            cls._font_main = pygame.font.SysFont("Arial", 24, bold=True)
            cls._font_small = pygame.font.SysFont("Arial", 18)
            cls._font_title = pygame.font.SysFont("Arial", 32, bold=True)

    @classmethod
    def draw_panel(cls, surface: pygame.Surface, rect: pygame.Rect, border_width: int = 3):
        """Draws a standard RPG text box (Blue bg, White border)."""
        # Background with slight transparency
        s = pygame.Surface((rect.width, rect.height))
        s.set_alpha(230)
        s.fill(COLOR_BG)
        surface.blit(s, rect.topleft)
        
        # Border
        pygame.draw.rect(surface, COLOR_BORDER, rect, border_width)

    @classmethod
    def draw_text(cls, surface: pygame.Surface, text: str, x: int, y: int, 
                  color: tuple = COLOR_TEXT, font_type: str = "main", align: str = "left"):
        cls.init_fonts()
        font = cls._font_main
        if font_type == "small": font = cls._font_small
        elif font_type == "title": font = cls._font_title

        txt_surf = font.render(str(text), True, color)
        rect = txt_surf.get_rect()
        
        if align == "center":
            rect.center = (x, y)
        elif align == "right":
            rect.topright = (x, y)
        else:
            rect.topleft = (x, y)
            
        surface.blit(txt_surf, rect)
        return rect # Return rect for clickable areas or layout

    @classmethod
    def draw_bar(cls, surface: pygame.Surface, rect: pygame.Rect, current: int, max_val: int, color_fg=COLOR_HP_FG):
        """Draws a progress/HP bar."""
        pct = max(0.0, min(1.0, current / max(1, max_val)))
        
        # Background
        pygame.draw.rect(surface, COLOR_HP_BG, rect)
        
        # Foreground
        fg_rect = pygame.Rect(rect.x, rect.y, int(rect.width * pct), rect.height)
        
        # Color logic for HP (Yellow/Red if low)
        final_color = color_fg
        if pct < 0.25 and color_fg == COLOR_HP_FG:
            final_color = COLOR_HP_LOW
            
        pygame.draw.rect(surface, final_color, fg_rect)
        
        # Border
        pygame.draw.rect(surface, (200, 200, 200), rect, 1)