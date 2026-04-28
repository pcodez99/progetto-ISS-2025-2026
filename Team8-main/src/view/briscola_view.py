"""
Briscola View - Rendering for the Briscola card game.
"""
import pygame
from src.model.render_system import Renderer, RenderLayer, RenderCommand
from src.view.ui_style import UIStyle, COLOR_SELECTED, COLOR_TEXT
from src.model.minigame.briscola_model import BriscolaModel

class BriscolaView:
    def __init__(self, renderer: Renderer, asset_manager):
        self.renderer = renderer
        self.assets = asset_manager
        
        self.CARD_W = 76
        self.CARD_H = 114
        self.SPACING = 20
        self.COLOR_TABLE = (20, 100, 40) # Verde più scuro per Briscola

    def render(self, screen_size: tuple, model: BriscolaModel, cursor_index: int):
        w, h = screen_size
        center_x = w // 2
        
        # 1. Background
        def draw_bg(screen, camera):
            bg = self.assets.get_image("sfondo_tavolo", w, h, fallback_type="background")
            if bg: screen.blit(bg, (0, 0))
            else: screen.fill(self.COLOR_TABLE)
        
        self.renderer.submit(RenderCommand(layer=RenderLayer.BACKGROUND, space='screen', draw_callable=draw_bg))

        # 2. Mazzo & Briscola
        retro_img = self.assets.get_image("retro_carta", self.CARD_W, self.CARD_H, fallback_type="prop")
        deck_pos = (40, h // 2 - self.CARD_H // 2)
        
        if model.carta_briscola:
            # Briscola accanto al mazzo (a destra), non sotto
            b_key = model.carta_briscola.asset_key
            b_img = self.assets.get_image(b_key, self.CARD_W, self.CARD_H, fallback_type="item")

            # Se vuoi mantenerla ruotata come “carta di briscola” tipica:
            b_surf = pygame.transform.rotate(b_img, 90)

            # Posizionamento: a destra del mazzo con un piccolo gap
            gap = 12
            b_pos = (deck_pos[0] + self.CARD_W + gap, deck_pos[1] + (self.CARD_H - b_surf.get_height()) // 2)

            self._submit_sprite(b_surf, b_pos)

        
        if model.mazzo:
            self._submit_sprite(retro_img, deck_pos)
            self._draw_ui_text(f"{len(model.mazzo)}", deck_pos[0]+10, deck_pos[1]-20)

        # 3. Carte in Tavolo
        # Disegniamo le carte giocate al centro
        table_center_y = h // 2 - self.CARD_H // 2
        for i, (card, owner) in enumerate(model.tavolo):
            # Offset per distinguerle
            x_pos = center_x - 50 + (i * 100)
            c_img = self.assets.get_image(card.asset_key, self.CARD_W, self.CARD_H)
            self._submit_sprite(c_img, (x_pos, table_center_y))
            # Label owner
            lbl = "TU" if owner == "P" else "LUI"
            self._draw_ui_text(lbl, x_pos + 20, table_center_y + self.CARD_H + 5, size="small")

        # 4. Mano Player
        hand_y = h - self.CARD_H - 20
        start_x_hand = center_x - ((len(model.mano_player) * (self.CARD_W + self.SPACING)) // 2)
        
        for i, card in enumerate(model.mano_player):
            pos_x = start_x_hand + i * (self.CARD_W + self.SPACING)
            # Sollevamento se selezionata
            pos_y = hand_y - 20 if i == cursor_index else hand_y
            
            c_img = self.assets.get_image(card.asset_key, self.CARD_W, self.CARD_H)
            
            def draw_p_card(screen, cam, img=c_img, r=pygame.Rect(pos_x, pos_y, self.CARD_W, self.CARD_H), sel=(i==cursor_index)):
                screen.blit(img, r)
                if sel:
                    pygame.draw.rect(screen, (255, 255, 0), r.inflate(4,4), 3)
            
            self.renderer.submit(RenderCommand(layer=RenderLayer.ACTORS, space='screen', draw_callable=draw_p_card))

        # 5. Mano CPU (Retro)
        cpu_y = 20
        start_x_cpu = center_x - ((len(model.mano_cpu) * (self.CARD_W + self.SPACING)) // 2)
        for i in range(len(model.mano_cpu)):
            pos_x = start_x_cpu + i * (self.CARD_W + self.SPACING)
            self._submit_sprite(retro_img, (pos_x, cpu_y))

        # 6. HUD Punteggi
        self._draw_ui_text(f"Punti: {model.punti_player}", 20, h - 50, color=(0, 255, 0))
        self._draw_ui_text(f"Peppino: {model.punti_cpu}", w - 150, 20, color=(255, 50, 50))
        
        # 7. Messaggio
        if model.message:
            self._draw_ui_text(model.message, center_x, h - 180, align="center", size="title")

    def render_game_over(self, screen_size, winner, score_p, score_c):
        w, h = screen_size
        def draw_end(screen, camera):
            ov = pygame.Surface((w, h))
            ov.set_alpha(200); ov.fill((0,0,0))
            screen.blit(ov, (0,0))
            
            title = "HAI VINTO!" if winner == "player" else ("HAI PERSO..." if winner == "cpu" else "PAREGGIO")
            col = (0, 255, 0) if winner == "player" else (255, 0, 0)
            
            UIStyle.draw_text(screen, title, w//2, 100, align="center", color=col, font_type="title")
            UIStyle.draw_text(screen, f"Tuoi Punti: {score_p}", w//2, 200, align="center")
            UIStyle.draw_text(screen, f"Punti Peppino: {score_c}", w//2, 250, align="center")
            UIStyle.draw_text(screen, "Premi [INVIO]", w//2, h - 100, align="center")
            
        self.renderer.submit(RenderCommand(layer=RenderLayer.UI_MODAL, space='screen', draw_callable=draw_end))

    def _submit_sprite(self, surf, pos):
        def draw(screen, camera): screen.blit(surf, pos)
        self.renderer.submit(RenderCommand(layer=RenderLayer.ACTORS, space='screen', draw_callable=draw))

    def _draw_ui_text(self, text, x, y, align="left", color=(255, 255, 255), size="main"):
        def draw(screen, camera): UIStyle.draw_text(screen, text, x, y, color=color, font_type=size, align=align)
        self.renderer.submit(RenderCommand(layer=RenderLayer.UI, space='screen', draw_callable=draw))