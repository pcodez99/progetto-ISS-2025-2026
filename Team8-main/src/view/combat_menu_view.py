"""
Combat Menu View - Renders the combat UI overlay.
Epic 9: Visual Integration.
Epic 19 Update: Target indicators for different scopes (AOE, etc).
"""
import pygame
from src.model.render_system import Renderer, RenderLayer, Camera
from src.model.ui.combat_menu_state import CombatMenuState
from src.model.combat.targeting_system import TargetingSystem

class CombatMenuView:
    def __init__(self, renderer: Renderer):
        self.renderer = renderer
        self.font = pygame.font.SysFont("Arial", 22, bold=True)
        self.panel_color = (0, 0, 100) # RPG Blue
        self.border_color = (255, 255, 255)
        self.text_color = (255, 255, 255)
        self.cursor_color = (255, 255, 0) # Yellow
        self.target_color = (255, 0, 0) # Red
        self.aoe_color = (255, 100, 0) # Orange

    def render(self, menu_state: CombatMenuState, battle_ctx, screen_dims: tuple):
        """
        Renderizza il menu e i cursori.
        """
        width, height = screen_dims
        
        # 1. Disegna il Pannello Comandi (in basso a sinistra)
        panel_rect = pygame.Rect(20, height - 200, 250, 180)
        
        def draw_ui(screen: pygame.Surface, camera: Camera):
            # Sfondo
            pygame.draw.rect(screen, self.panel_color, panel_rect)
            pygame.draw.rect(screen, self.border_color, panel_rect, 3)
            
            # Opzioni
            for i, option in enumerate(menu_state.options):
                color = self.cursor_color if (menu_state.mode == "root" and i == menu_state.cursor_index) else self.text_color
                label = self.font.render(option, True, color)
                screen.blit(label, (panel_rect.x + 30, panel_rect.y + 20 + (i * 30)))
                
                # Cursore "Hand" (Triangolo)
                if menu_state.mode == "root" and i == menu_state.cursor_index:
                    points = [
                        (panel_rect.x + 10, panel_rect.y + 25 + (i * 30)),
                        (panel_rect.x + 10, panel_rect.y + 35 + (i * 30)),
                        (panel_rect.x + 20, panel_rect.y + 30 + (i * 30))
                    ]
                    pygame.draw.polygon(screen, self.cursor_color, points)

            # Target Indicators (Epic 19)
            if menu_state.mode == "target_selection":
                self._draw_target_cursors(screen, menu_state, battle_ctx)

        # Sottometti al layer UI
        self.renderer.submit_ui(draw_ui, layer=RenderLayer.UI)

    def _draw_target_cursors(self, screen, menu_state, battle_ctx):
        """Disegna frecce o indicatori sui bersagli selezionati."""
        
        # Determina chi evidenziare
        targets_to_highlight = []
        action = menu_state.pending_action
        scope = action["scope"] if action else TargetingSystem.SCOPE_SINGLE_ENEMY
        
        cursor_target = menu_state.get_current_target()
        
        if scope == TargetingSystem.SCOPE_ALL_ENEMIES:
            # Evidenzia tutti i nemici
            targets_to_highlight = battle_ctx.get_living_enemies()
            indicator_color = self.aoe_color
        else:
            # Evidenzia solo quello sotto il cursore
            if cursor_target:
                targets_to_highlight = [cursor_target]
            indicator_color = self.target_color

        for target in targets_to_highlight:
            # Calcola posizione (Mock, in futuro usa target.pos o Render data)
            # Assumiamo che i nemici siano in una lista e abbiano posizioni fisse per ora
            # Nel sistema reale: target.rect.center
            
            # Recuperiamo l'indice per posizionamento mock
            idx = 0
            is_enemy = target in battle_ctx.enemies
            if is_enemy:
                idx = battle_ctx.enemies.index(target)
                ex, ey = 500, 100 + idx * 60 
            else:
                # Party member
                idx = battle_ctx.party.index(target)
                ex, ey = 150, 300 + idx * 60

            # Disegna freccia
            arrow_points = [
                (ex + 16, ey - 20),
                (ex + 6, ey - 30),
                (ex + 26, ey - 30)
            ]
            pygame.draw.polygon(screen, indicator_color, arrow_points)