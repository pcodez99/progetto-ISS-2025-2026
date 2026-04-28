"""
Combat View - Handles the visualization of the combat scene.
Integrates Scene rendering, Actors (Sprites), and UI Overlay.
Updated: Added support for BossOste custom rendering.
"""
import pygame
from src.model.render_system import Renderer, RenderLayer, RenderCommand, Camera
from src.view.ui_style import UIStyle, COLOR_HP_FG, COLOR_HP_LOW

class CombatView:
    def __init__(self, renderer: Renderer):
        self.renderer = renderer
        
        # Layout configurations
        self.hero_positions = [
            (200, 200), (150, 280), (200, 360), (150, 440)
        ]
        self.enemy_positions = [
            (600, 200), (650, 280), (600, 360), (650, 440)
        ]

    def render(self, screen_size: tuple, battle_ctx, menu_state, vfx_list=None):
        if not battle_ctx: return

        # 1. Background
        def draw_bg(screen, camera):
            screen.fill((20, 10, 30)) 
            pygame.draw.ellipse(screen, (10, 5, 20), (50, 400, 700, 150))

        self.renderer.submit(RenderCommand(layer=RenderLayer.BACKGROUND, space="screen", draw_callable=draw_bg))

        # 2. Draw Combatants
        self._draw_party(battle_ctx.party)
        self._draw_enemies(battle_ctx.enemies)

        # 3. Draw VFX
        if vfx_list:
            self._draw_vfx(vfx_list)

        # 4. Draw UI
        self._draw_ui(screen_size, battle_ctx, menu_state)

    def _draw_party(self, party):
        for i, hero in enumerate(party):
            if hero.hp <= 0: continue 
            
            pos = self.hero_positions[i] if i < len(self.hero_positions) else (100, 100)
            rect = pygame.Rect(0, 0, 48, 64)
            rect.center = pos
            
            surface = pygame.Surface((48, 64))
            surface.fill((0, 200, 100)) # Green for Heroes
            
            self.renderer.submit_sprite(surface, rect, layer=RenderLayer.ACTORS, space="screen")

    def _draw_enemies(self, enemies):
        for i, enemy in enumerate(enemies):
            if enemy.hp <= 0: continue
            
            pos = self.enemy_positions[i] if i < len(self.enemy_positions) else (700, 100)
            
            # --- CHECK FOR CUSTOM BOSS MODEL (OSTE ETERNO) ---
            custom_model = getattr(enemy, 'custom_model', None)
            
            size = 64
            color = (200, 50, 50) # Default Red
            
            if custom_model:
                size = 120 # Boss is Bigger
                color = custom_model.get_phase_color()
            
            rect = pygame.Rect(0, 0, size, size)
            rect.center = pos
            
            # Create Surface
            surface = pygame.Surface((size, size))
            surface.fill(color)
            
            # Submit Sprite
            self.renderer.submit_sprite(surface, rect, layer=RenderLayer.ACTORS, space="screen")

            # --- CUSTOM UI FOR BOSS ---
            if custom_model:
                # Aura
                def draw_aura(screen, camera, r=rect, c=color):
                     s = pygame.Surface((r.width + 20, r.height + 20), pygame.SRCALPHA)
                     pygame.draw.rect(s, (*c, 50), s.get_rect(), border_radius=10)
                     screen.blit(s, (r.x - 10, r.y - 10))
                self.renderer.submit_ui(draw_aura, layer=RenderLayer.ACTORS) # Behind actor but same layer group

                # Phase Description Text
                desc = custom_model.get_phase_description()
                def draw_phase_text(screen, camera, txt=desc, r=rect, c=color):
                    UIStyle.draw_text(screen, txt, r.centerx, r.bottom + 25, align="center", color=c, font_type="small")
                self.renderer.submit_ui(draw_phase_text, layer=RenderLayer.UI_WORLD)

            # --- HP BAR ---
            e_hp, e_max = enemy.hp, enemy.max_hp
            def draw_enemy_hp(screen, camera, r=rect, h=e_hp, m=e_max):
                bar_w = r.width + 20
                bar_h = 6
                bar_rect = pygame.Rect(0, 0, bar_w, bar_h)
                bar_rect.centerx = r.centerx
                bar_rect.bottom = r.top - 5
                UIStyle.draw_bar(screen, bar_rect, h, m)
            
            self.renderer.submit_ui(draw_enemy_hp, layer=RenderLayer.UI_WORLD)

    def _draw_vfx(self, vfx_list):
        for vfx in vfx_list:
            surface = vfx.get('surface')
            rect = vfx.get('rect')
            if surface and rect:
                self.renderer.submit_sprite(surface, rect, layer=RenderLayer.VFX, space="screen")

    def _draw_ui(self, screen_size, battle_ctx, menu_state):
        w, h = screen_size
        
        def draw_hud(screen, camera):
            # --- Party Status Panel (Top Left) ---
            UIStyle.draw_panel(screen, pygame.Rect(10, 10, 250, 120))
            for i, hero in enumerate(battle_ctx.party):
                y_off = 20 + i * 25
                name_color = (255, 255, 255) if hero.hp > 0 else (100, 100, 100)
                UIStyle.draw_text(screen, f"{hero.name}", 20, y_off, color=name_color, font_type="small")
                
                # HP Bar
                bar_rect = pygame.Rect(120, y_off + 2, 100, 12)
                UIStyle.draw_bar(screen, bar_rect, hero.hp, hero.max_hp)
                UIStyle.draw_text(screen, f"{hero.hp}/{hero.max_hp}", 230, y_off, font_type="small", align="left")

            # --- Action Menu (Bottom Left) ---
            menu_rect = pygame.Rect(20, h - 180, 250, 160)
            UIStyle.draw_panel(screen, menu_rect)
            
            if menu_state.mode == "root":
                options = menu_state.options
                for i, opt in enumerate(options):
                    color = (255, 255, 0) if i == menu_state.cursor_index else (255, 255, 255)
                    prefix = "> " if i == menu_state.cursor_index else "  "
                    UIStyle.draw_text(screen, f"{prefix}{opt}", menu_rect.x + 20, menu_rect.y + 20 + i * 30, color=color)
            
            elif menu_state.mode == "target_selection":
                UIStyle.draw_text(screen, "Select Target:", menu_rect.x + 20, menu_rect.y + 20, color=(200, 200, 255))
                target = menu_state.get_current_target()
                if target:
                    UIStyle.draw_text(screen, f"> {target.name}", menu_rect.x + 20, menu_rect.y + 60, color=(255, 0, 0))
                    self._draw_target_cursor(screen, target, battle_ctx)

        self.renderer.submit_ui(draw_hud, layer=RenderLayer.UI)

    def _draw_target_cursor(self, screen, target, battle_ctx):
        pos = (0, 0)
        if target in battle_ctx.enemies:
            idx = battle_ctx.enemies.index(target)
            pos = self.enemy_positions[idx]
        elif target in battle_ctx.party:
            idx = battle_ctx.party.index(target)
            pos = self.hero_positions[idx]
        
        arrow_points = [
            (pos[0], pos[1] - 40),
            (pos[0] - 10, pos[1] - 50),
            (pos[0] + 10, pos[1] - 50)
        ]
        pygame.draw.polygon(screen, (255, 0, 0), arrow_points)