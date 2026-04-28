# ===== FILE: ./src/model/states/boss_oste_state.py =====
"""
Boss Oste State - Custom Implementation for the Final Boss Fight.
Updated: REAL RPG STATS IMPLEMENTATION + PORTRAITS.
Updated: True Ending Sequence (No Choice, Dialogue + Main Menu Return).

Base Attack scales with Player ATK.
Incoming Damage is reduced by Player DEF.

Visuals: Character portraits and Boss sprite added.
"""

import pygame
import sys
import random
import math

from src.model.states.base_state import BaseState, StateID
from src.model.items.item_ids import ItemIds
from src.model.script_actions import GameScript, ScriptAction

# --- CONFIGURAZIONI ---
SCREEN_WIDTH = 800
SCREEN_HEIGHT = 600
COOLDOWN_TURNS = 3

# --- COLORI ---
WHITE = (255, 255, 255)
BLACK = (10, 10, 10)
DARK_BG = (15, 15, 20)
RED = (220, 50, 50)
GREEN = (50, 200, 50)
BLUE = (50, 100, 220)
GOLD = (255, 215, 0)
PURPLE = (148, 0, 211)
CYAN = (0, 255, 255)
GRAY_UI = (40, 40, 50)
ORANGE = (255, 140, 0)
IMMORTAL_WHITE = (240, 240, 255)
DISABLED_GRAY = (60, 60, 60)

# --- LAYOUT AREE ---
PARTY_AREA = pygame.Rect(0, 0, SCREEN_WIDTH // 2, SCREEN_HEIGHT)
BOSS_AREA  = pygame.Rect(SCREEN_WIDTH // 2, 0, SCREEN_WIDTH // 2, SCREEN_HEIGHT)

# --- MAPPING ASSI -> EFFETTI BOSS FIGHT ---
ACE_DATA_MAP = {
    "Denari": {
        "skill_name": "Corruzione Aurea",
        "zone": "Aurion",
        "effect": {
            "type": "stun",
            "dmg": 0,
            "heal": 0,
            "desc": "Stun: Il boss salta il turno.",
        },
    },
    "Spade": {
        "skill_name": "Fendente d'Onore",
        "zone": "Ferrum",
        "effect": {
            "type": "crit",
            "dmg": 200,
            "heal": 0,
            "desc": "Critico: Danno massiccio.",
        },
    },
    "Bastoni": {
        "skill_name": "Forza della Natura",
        "zone": "Viridor",
        "effect": {
            "type": "revive",
            "dmg": 0,
            "heal": 100,
            "desc": "Benedizione: Cura/Revive potente.",
        },
    },
    "Coppe": {
        "skill_name": "Ebbrezza Mistica",
        "zone": "Vinalia",
        "effect": {
            "type": "miss",
            "dmg": 0,
            "heal": 0,
            "desc": "Ebbrezza: Il boss mancherà il colpo.",
        },
    },
}

ZONE_COLORS = {
    "Aurion": GOLD,
    "Denari": GOLD,
    "Viridor": GREEN,
    "Bastoni": GREEN,
    "Ferrum": RED,
    "Spade": RED,
    "Vinalia": PURPLE,
    "Coppe": PURPLE,
    "Nessuna": WHITE,
}

def scale_to_fit(img: pygame.Surface, max_w: int, max_h: int) -> pygame.Surface:
    iw, ih = img.get_size()
    if iw == 0 or ih == 0:
        return img
    s = min(max_w / iw, max_h / ih)
    new_size = (max(1, int(iw * s)), max(1, int(ih * s)))
    return pygame.transform.smoothscale(img, new_size)


class EffettoVisivo:
    def __init__(self, x, y, text, color, icona="", duration=60, big=False):
        self.x = x
        self.y = y
        self.text = text
        self.color = color
        self.icona = icona
        self.timer = duration
        self.speed = 1.0
        self.big = big

    def update(self):
        self.y -= self.speed
        self.timer -= 1

    def draw(self, screen, fonts):
        if self.timer > 0:
            font_to_use = fonts["big_msg"] if self.big else fonts["dmg"]
            label = f"{self.icona} {self.text}" if self.icona else str(self.text)
            txt = font_to_use.render(label, True, self.color)
            rect = txt.get_rect(center=(self.x, self.y))
            screen.blit(txt, rect)


class Abilita:
    def __init__(self, nome, danno, cura, tipo="base", descrizione="", buff=False):
        self.nome = nome
        self.danno = danno
        self.cura = cura
        self.tipo = tipo
        self.descrizione = descrizione
        self.buff = buff


class Personaggio:
    def __init__(self, real_char, slot_index, fonts, assets):
        self.nome = real_char.name
        self.hp_max = real_char.max_hp
        self.hp = real_char.hp

        # --- IMPORTANTE: STATISTICHE REALI DAL GIOCO ---
        self.atk = real_char.atk
        self.defense = real_char.defense
        self.magic = real_char.magic
        self.res = real_char.res
        self.spd = real_char.spd
        self.crit_rate = real_char.crit_rate
        # -----------------------------------------------

        self.fonts = fonts
        self.assets = assets  # Asset Manager Reference

        # Caricamento Sprite Ritratto
        self.image = self.assets.get_image(
            f"characters/{self.nome}",
            width=100,
            height=100,
            fallback_type="player",
            preserve_aspect=True
        )

        self.abilita_base = []

        # CALCOLO DANNO BASE: 15 + (ATK * 6)
        dmg_base = int(15 + (self.atk * 6))
        self.abilita_base.append(
            Abilita("Colpo Base", dmg_base, 0, "base", f"Danno Fisico (ATK {self.atk})")
        )

        # CALCOLO DIFESA/CURA: 15 + (MAGIC + RES)
        heal_base = int(15 + self.magic + self.res)
        self.abilita_base.append(
            Abilita("Difesa", 0, heal_base, "base", "Piccola cura e buff", buff=True)
        )

        self.assi = []
        self.zone = []
        self.abilita_speciali = []

        check_map = [
            (ItemIds.ACE_DENARI, "Denari"),
            (ItemIds.ACE_SPADE, "Spade"),
            (ItemIds.ACE_BASTONI, "Bastoni"),
            (ItemIds.ACE_COPPE, "Coppe"),
        ]

        for item_id, suit_key in check_map:
            if real_char.inventory.has_item(item_id):
                data = ACE_DATA_MAP[suit_key]
                self.assi.append(suit_key)
                self.zone.append(data["zone"])

                eff = data["effect"]
                is_buff = (
                    eff["heal"] > 0
                    or eff["type"] in ["buff_atk", "tank", "split", "thorns", "miss"]
                )

                dmg_spec = eff["dmg"]
                if dmg_spec > 0:
                    dmg_spec += (self.magic * 3)

                self.abilita_speciali.append(
                    Abilita(
                        data["skill_name"],
                        dmg_spec,
                        eff["heal"],
                        eff["type"],
                        eff["desc"],
                        buff=is_buff,
                    )
                )

        if self.zone:
            self.colore = ZONE_COLORS.get(self.zone[0], WHITE)
        else:
            self.colore = WHITE
            self.abilita_speciali.append(
                Abilita("Nessuna", 0, 0, "none", "Nessun potere.")
            )

        if self.assi:
            assi_str = ", ".join(self.assi)
            self.flavor_text = (
                f"Eroe: {self.nome}\nATK: {self.atk} | DEF: {self.defense}\nPotere: {assi_str}"
            )
        else:
            self.flavor_text = (
                f"{self.nome}\nATK: {self.atk} | DEF: {self.defense}\nNessun Asso."
            )

        self.rect = pygame.Rect(0, 0, 140, 80)
        # --- POSIZIONAMENTO PARTY (colonna sinistra) ---
        left_margin = PARTY_AREA.left + 40
        top_margin = 140
        gap_y = 190

        self.home_x = left_margin
        self.home_y = top_margin + slot_index * gap_y

        self.x = self.home_x
        self.y = self.home_y
        self.target_x = self.home_x
        self.target_y = self.home_y

        self.azione_base_usata = False
        self.azione_speciale_usata = False
        self.cooldown = 0
        self.is_selected = False
        self.is_dead = (self.hp <= 0)
        if self.is_dead:
            self.colore = (80, 80, 80)

    def update_pos(self):
        self.x += (self.target_x - self.x) * 0.1
        self.y += (self.target_y - self.y) * 0.1
        self.rect.topleft = (int(self.x), int(self.y))

    def vai_in_prima_linea(self):
        self.target_x = 220
        self.target_y = 250

    def vai_al_centro_arena(self):
        self.target_x = SCREEN_WIDTH // 2 - 70
        self.target_y = SCREEN_HEIGHT // 2 - 40

    def torna_al_posto(self):
        self.target_x = self.home_x
        self.target_y = self.home_y

    def reset_turno(self):
        self.azione_base_usata = False
        self.azione_speciale_usata = False
        self.torna_al_posto()

    def aggiorna_cooldown(self):
        if self.cooldown > 0:
            self.cooldown -= 1

    def subisci_danno(self, dmg, effetti_list):
        mitigated_dmg = max(1, dmg - (self.defense * 2))
        self.hp -= mitigated_dmg
        effetti_list.append(
            EffettoVisivo(self.rect.centerx, self.rect.y, f"-{int(mitigated_dmg)}", RED)
        )
        if self.hp <= 0:
            self.hp = 0
            self.is_dead = True
            self.colore = (80, 80, 80)

    def guarisci(self, amount, effetti_list, is_buff=False):
        if self.is_dead and amount < 900:
            return
        if self.is_dead and amount >= 900:
            self.is_dead = False
            self.colore = ZONE_COLORS.get(self.zone[0] if self.zone else "Nessuna", WHITE)

        self.hp += amount
        if self.hp > self.hp_max:
            self.hp = self.hp_max

        icon = "↑" if is_buff else "+"
        col = CYAN if is_buff else GREEN
        txt = "BUFF" if is_buff and amount == 0 else str(amount)
        if amount == 0 and not is_buff:
            return

        effetti_list.append(EffettoVisivo(self.rect.centerx, self.rect.y, txt, col, icon))

    def disegna(self, screen):
        # 1. DISEGNO RITRATTO SOPRA IL BOX
        if self.image:
            img_x = self.rect.centerx - 50  # Centrata (100px wide)
            img_y = self.rect.top - 90
            portrait_box = pygame.Rect(img_x, img_y, 100, 100)
            img = scale_to_fit(self.image, portrait_box.width, portrait_box.height)
            img_rect = img.get_rect(center=portrait_box.center)
            screen.blit(img, img_rect)

            if self.is_selected:
                pygame.draw.rect(screen, WHITE, (img_x - 2, img_y - 2, 104, 104), 2)

        # 2. DISEGNO BOX STATUS
        pygame.draw.rect(
            screen,
            (0, 0, 0),
            (self.rect.x + 3, self.rect.y + 3, self.rect.width, self.rect.height),
        )

        bg_col = (30, 30, 40) if not self.is_dead else (20, 10, 10)
        pygame.draw.rect(screen, bg_col, self.rect)

        border_col = self.colore if not self.is_dead else (100, 100, 100)
        thick = 3 if self.is_selected else 2
        if self.is_selected:
            border_col = WHITE
        pygame.draw.rect(screen, border_col, self.rect, thick)

        name_s = self.fonts["main"].render(self.nome, True, border_col)
        screen.blit(name_s, (self.rect.x + 5, self.rect.y + 5))

        if not self.is_dead:
            cd_text = "Special: Pronta"
            cd_col = GOLD
            if self.cooldown > 0:
                cd_text = f"Ricarica: {self.cooldown}"
                cd_col = (150, 150, 150)
            cd_surf = self.fonts["small"].render(cd_text, True, cd_col)
            screen.blit(cd_surf, (self.rect.x + 5, self.rect.y + 23))

        hp_pct = self.hp / max(1, self.hp_max)
        bar_w = 120
        bar_h = 10
        bar_x = self.rect.x + 10
        bar_y = self.rect.y + 45

        pygame.draw.rect(screen, (50, 0, 0), (bar_x, bar_y, bar_w, bar_h))
        if hp_pct > 0:
            col_hp = GREEN if hp_pct > 0.3 else RED
            pygame.draw.rect(screen, col_hp, (bar_x, bar_y, bar_w * hp_pct, bar_h))

        hp_txt = self.fonts["small"].render(f"{int(self.hp)}/{self.hp_max}", True, WHITE)
        screen.blit(hp_txt, (self.rect.x + 10, bar_y + 12))

        if not self.is_dead:
            col_b = CYAN if not self.azione_base_usata else (50, 50, 50)
            pygame.draw.circle(
                screen, col_b, (self.rect.right - 20, self.rect.top + 15), 5
            )

            col_s = GOLD if self.cooldown == 0 else (60, 60, 60)
            pygame.draw.circle(
                screen, col_s, (self.rect.right - 10, self.rect.top + 15), 5
            )


class Boss:
    def __init__(self, fonts, assets):
        self.nome = "L'OSTE ETERNO"
        self.hp_max = 500
        self.hp = self.hp_max
        self.fase = 1
        self.max_fasi = 4
        # --- POSIZIONAMENTO BOSS (colonna destra) ---
        boss_w, boss_h = 220, 320
        boss_x = BOSS_AREA.left + (BOSS_AREA.width - boss_w) // 2
        boss_y = 140
        self.rect = pygame.Rect(boss_x, boss_y, boss_w, boss_h)
        self.colore = GOLD
        self.descrizione = "FASE 1: AVIDITÀ"
        self.shake = 0
        self.immortale = False
        self.fonts = fonts
        self.assets = assets

        # Carica Immagine Boss (Grande)
        self.image = self.assets.get_image(
            "enemy_boss_oste", width=300, height=350, fallback_type="enemy", preserve_aspect=True
        )

        if self.image:
            # flip orizzontale
            self.image = pygame.transform.flip(self.image, True, False)

    def update(self):
        if self.shake > 0:
            self.rect.x += random.randint(-4, 4)
            self.rect.y += random.randint(-4, 4)
            self.shake -= 1
        else:
            boss_x = BOSS_AREA.left + (BOSS_AREA.width - self.rect.width) // 2
            self.rect.topleft = (boss_x, 140)

    def subisci_danno(self, dmg, effetti_list, is_vulnerable=False):
        if self.immortale:
            return False

        final_dmg = dmg * 2 if is_vulnerable else dmg
        self.hp -= final_dmg

        if final_dmg > 0:
            txt = f"{int(final_dmg)} CRIT!" if is_vulnerable else str(int(final_dmg))
            effetti_list.append(EffettoVisivo(self.rect.centerx, self.rect.y + 50, txt, RED))
            self.shake = 15

        if self.hp <= 0:
            self.hp = 0
            return True

        return False

    def cambia_fase(self, effetti_list):
        if self.fase < self.max_fasi:
            self.fase += 1
            self.hp = self.hp_max

            if self.fase == 4:
                effetti_list.append(
                    EffettoVisivo(
                        SCREEN_WIDTH // 2,
                        SCREEN_HEIGHT // 2,
                        "ULTIMA FASE!",
                        RED,
                        duration=120,
                        big=True,
                    )
                )

            effetti_list.append(
                EffettoVisivo(self.rect.centerx, self.rect.y, "NUOVA FASE!", WHITE)
            )

            if self.fase == 2:
                self.colore = GREEN
                self.descrizione = "FASE 2: OSTINAZIONE"
            elif self.fase == 3:
                self.colore = RED
                self.descrizione = "FASE 3: GUERRA"
            elif self.fase == 4:
                self.colore = PURPLE
                self.descrizione = "FASE 4: OBLIO"

            return False
        else:
            return True

    def diventa_immortale(self, effetti_list):
        self.immortale = True
        self.colore = IMMORTAL_WHITE
        self.hp = self.hp_max
        self.descrizione = "FASE FINALE: ETERNITÀ"
        effetti_list.append(EffettoVisivo(self.rect.centerx, self.rect.y, "IMMORTALE!", WHITE))

    def disegna(self, screen, fade_alpha=0):
        # 1. DISEGNA IMMAGINE BOSS
        if self.image:
            img_rect = self.image.get_rect(center=self.rect.center)
            screen.blit(self.image, img_rect)
        else:
            pygame.draw.rect(screen, (20, 20, 20), self.rect, border_radius=10)
            pygame.draw.rect(screen, self.colore, self.rect, 4, border_radius=10)

        # 2. UI e Testi
        def outline(text, font, col, center):
            for ox, oy in [(-2, -2), (-2, 2), (2, -2), (2, 2), (-1, 0), (1, 0), (0, -1), (0, 1)]:
                s = font.render(text, True, BLACK)
                r = s.get_rect(center=(center[0] + ox, center[1] + oy))
                screen.blit(s, r)
            s = font.render(text, True, col)
            r = s.get_rect(center=center)
            screen.blit(s, r)

        outline(
            self.nome,
            self.fonts["main"],
            self.colore if not self.immortale else WHITE,
            (self.rect.centerx, self.rect.top - 30),
        )

        if self.immortale:
            outline("∞", self.fonts["big_msg"], WHITE, self.rect.center)
        else:
            hp_pct = self.hp / max(1, self.hp_max)
            bar_w = 160
            bar_x = self.rect.centerx - 80
            bar_y = self.rect.top - 10
            pygame.draw.rect(screen, (50, 0, 0), (bar_x, bar_y, bar_w, 15))
            pygame.draw.rect(screen, self.colore, (bar_x, bar_y, bar_w * hp_pct, 15))
            hp_val = self.fonts["small"].render(f"{int(self.hp)}/{self.hp_max}", True, WHITE)
            screen.blit(hp_val, (bar_x + 50, bar_y + 20))

        txt_fase = self.fonts["main"].render(self.descrizione, True, self.colore)
        screen.blit(txt_fase, txt_fase.get_rect(center=(self.rect.centerx, self.rect.bottom + 30)))

        if fade_alpha > 0:
            fade_s = pygame.Surface((self.rect.width + 100, self.rect.height + 100), pygame.SRCALPHA)
            fade_s.fill((0, 0, 0, min(255, fade_alpha)))
            screen.blit(fade_s, (self.rect.x - 50, self.rect.y - 50))


class CombatLog:
    def __init__(self, font):
        self.logs = []
        self.font = font

    def aggiungi(self, text):
        self.logs.append(text)
        if len(self.logs) > 3:
            self.logs.pop(0)

    def disegna(self, screen):
        panel = pygame.Rect(BOSS_AREA.left + 20, 10, BOSS_AREA.width - 40, 80)
        s = pygame.Surface((panel.width, panel.height), pygame.SRCALPHA)
        s.fill((0, 0, 0, 100))
        screen.blit(s, (panel.x, panel.y))

        for i, l in enumerate(self.logs):
            c = WHITE
            if "Boss" in l or "Oste" in l:
                c = (255, 100, 100)
            elif "cura" in l:
                c = (100, 255, 100)
            screen.blit(self.font.render(l, True, c), (panel.x + 10, panel.y + i * 20))


# --- CLASS STATE PRINCIPALE ---
class BossOsteState(BaseState):
    def __init__(self, state_machine=None):
        super().__init__(StateID.BOSS_OSTE, state_machine)
        self.fonts = {}
        self.party = []
        self.boss = None
        self.log = None
        self.game_state = "MENU"
        self.effetti_visivi = []

        # Game Vars
        self.turno_giocatore = True
        self.selected = None
        self.speciale_usata_globale = False
        self.boss_attack_phase = "IDLE"
        self.boss_attack_timer = 0
        self.boss_target = None
        self.immortal_msg_timer = 0
        self.immortal_msg_text = ""
        self.warning_msg_timer = 0
        self.warning_msg_text = ""
        self.fake_victory_timer = 0
        self.fake_victory_stage = 0
        self.boss_fade_alpha = 0
        self.turn_counter = 1
        self.btn_passa = pygame.Rect(BOSS_AREA.right - 140, SCREEN_HEIGHT - 90, 120, 50)
        self.battle_status = {
            "boss_stunned": False,
            "boss_half_dmg": False,
            "boss_vuln": False,
            "boss_miss_next": False,
            "party_thorns": False,
            "party_split": False,
            "party_tank": False,
            "party_buff_dmg": False,
        }

        # --- INPUT TASTIERA ---
        self.kb_selected_party_idx = 0      # indice party selezionato
        self.kb_focus = "PARTY"             # "PARTY" oppure "ACTIONS"
        self.kb_action_idx = 0              # indice bottone attualmente selezionato

         
        self.dialogue_lines = []
        self.dialogue_index = 0

    def enter(self, prev_state=None, **kwargs):
        pygame.font.init()
        self.fonts = {
            "title": pygame.font.SysFont("Georgia", 40, bold=True),
            "main": pygame.font.SysFont("Arial", 20, bold=True),
            "small": pygame.font.SysFont("Arial", 16),
            "dmg": pygame.font.SysFont("Arial", 24, bold=True),
            "big_msg": pygame.font.SysFont("Arial", 40, bold=True),
            "btn": pygame.font.SysFont("Arial", 18, bold=True),
        }
        self.assets = self._state_machine.controller.render_controller.asset_manager
        self.party = self.build_party_from_gamestate()
        self.layout_party_positions()
        # --- Selezione iniziale per tastiera ---
        alive = [i for i, p in enumerate(self.party) if not p.is_dead]
        if alive:
            self.kb_selected_party_idx = alive[0]
            self._set_selected_by_index(self.kb_selected_party_idx)
            self.kb_focus = "PARTY"     # parti dalla scelta personaggio
            self.kb_action_idx = 0

        self.boss = Boss(self.fonts, self.assets)
        self.log = CombatLog(self.fonts["small"])
        self.log.aggiungi("Inizia lo scontro!")
        self.game_state = "MENU"
        self.effetti_visivi = []

        game = self._state_machine.controller.game
        game.audio.play_bgm("combat.ogg", fade_ms=1000)

    def exit(self, next_state=None):
        pass

    def layout_party_positions(self):
        if len(self.party) != 2:
            return

        portrait_h = 100
        box_h = self.party[0].rect.height  # 80
        inner_gap = 20                     # spazio tra ritratto e box
        block_h = portrait_h + inner_gap + box_h

        gap_between = 80  # distanza tra i due blocchi (aumenta/diminuisci)

        total_h = (2 * block_h) + gap_between

        # centrato + piccolo offset verso il basso
        start_y = (SCREEN_HEIGHT - total_h) // 2 + 120

        x = 40  # margine sinistro

        for i, p in enumerate(self.party):
            p.home_x = x
            p.home_y = start_y + i * (block_h + gap_between)

            # riposiziona immediatamente
            p.x = p.home_x
            p.y = p.home_y
            p.target_x = p.home_x
            p.target_y = p.home_y

    def build_party_from_gamestate(self):
        real_party = self._state_machine.controller.game.gamestate.party.main_characters
        party_objs = []
        for i, char in enumerate(real_party):
            p = Personaggio(char, i, self.fonts, self.assets)
            party_objs.append(p)
        return party_objs

    def get_best_heal_target(self):
        for p in self.party:
            if p.is_dead:
                return p
        self.party.sort(key=lambda x: x.hp)
        return self.party[0]

    def _alive_party_indices(self):
        return [i for i, p in enumerate(self.party) if not p.is_dead]

    def _set_selected_by_index(self, idx: int):
        if not self.party:
            return
        if idx < 0 or idx >= len(self.party):
            return
        p = self.party[idx]
        if p.is_dead:
            return

        if self.selected and self.selected is not p:
            self.selected.is_selected = False
            self.selected.torna_al_posto()

        self.selected = p
        self.selected.is_selected = True
        self.selected.vai_in_prima_linea()

    def _cycle_party(self, direction: int):
        alive = self._alive_party_indices()
        if not alive:
            return
        if self.kb_selected_party_idx not in alive:
            self.kb_selected_party_idx = alive[0]

        pos = alive.index(self.kb_selected_party_idx)
        pos = (pos + direction) % len(alive)
        self.kb_selected_party_idx = alive[pos]
        self._set_selected_by_index(self.kb_selected_party_idx)

    def _get_action_buttons_count(self):
        """
        Ordine bottoni:
        0..(base-1) = abilità base
        poi special (1 o più)
        infine "PASSA"
        """
        if not self.selected:
            return 1  # solo PASSA
        base_n = len(self.selected.abilita_base)
        spec_n = len(self.selected.abilita_speciali)
        return base_n + spec_n + 1

    def _execute_action_by_index(self, idx: int):
        """
        Esegue l'azione evidenziata (come se cliccassi).
        """
        if self.game_state != "COMBAT":
            return
        if not self.turno_giocatore:
            return
        if self.boss_attack_phase != "IDLE":
            return
        if self.immortal_msg_timer != 0:
            return

        # PASSA è l'ultimo
        total = self._get_action_buttons_count()
        pass_idx = total - 1

        if idx == pass_idx:
            # stessa logica del click su PASSA
            self.turno_giocatore = False
            if self.selected:
                self.selected.torna_al_posto()
            self.selected = None
            self.speciale_usata_globale = False

            if self.boss.immortale:
                self._trigger_ending_sequence()
            else:
                self.log.aggiungi("Passi il turno...")
                self.boss_attack_phase = "SHOW_TEXT"
                self.boss_attack_timer = 60
            return

        if not self.selected:
            return

        base_n = len(self.selected.abilita_base)

        # --- BASE ---
        if idx < base_n:
            skill = self.selected.abilita_base[idx]

            if self.boss.immortale:
                self.immortal_msg_text = "L'OSTE TI BLOCCA!"
                self.immortal_msg_timer = 90
                self.selected.azione_base_usata = True
                return

            if self.selected.azione_base_usata:
                self.warning_msg_text = "GIÀ USATA"
                self.warning_msg_timer = 60
                return

            dmg = skill.danno
            if self.battle_status["party_buff_dmg"] and dmg > 0:
                dmg += 20

            if dmg > 0:
                self.boss.subisci_danno(dmg, self.effetti_visivi, is_vulnerable=self.battle_status["boss_vuln"])
                self.log.aggiungi(f"{skill.nome}: colpo!")

            if skill.cura > 0 or skill.buff:
                target = self.get_best_heal_target()
                target.guarisci(skill.cura, self.effetti_visivi, is_buff=skill.buff)
                self.log.aggiungi(f"{skill.nome}: usato!")

            self.selected.azione_base_usata = True

            if self.boss.hp == 0:
                if self.boss.cambia_fase(self.effetti_visivi):
                    self.game_state = "BOSS_DYING"
            return

        # --- SPECIAL ---
        spec_idx = idx - base_n
        s = self.selected.abilita_speciali[spec_idx]

        if self.boss.immortale:
            self.immortal_msg_text = "L'OSTE TI BLOCCA!"
            self.immortal_msg_timer = 90
            self.selected.cooldown = COOLDOWN_TURNS
            self.speciale_usata_globale = True
            return

        if self.selected.cooldown > 0:
            self.warning_msg_text = "IN RICARICA"
            self.warning_msg_timer = 60
            return

        if self.selected.azione_speciale_usata or self.speciale_usata_globale:
            self.warning_msg_text = "GIÀ USATA"
            self.warning_msg_timer = 60
            return

        dmg = s.danno
        if self.battle_status["party_buff_dmg"] and dmg > 0:
            dmg += 30

        if s.tipo == "stun":
            self.battle_status["boss_stunned"] = True
            self.log.aggiungi("Boss STORDITO!")
        elif s.tipo == "debuff_dmg":
            self.battle_status["boss_half_dmg"] = True
            self.log.aggiungi("Boss: Danni dimezzati!")
        elif s.tipo == "vuln":
            self.battle_status["boss_vuln"] = True
            self.log.aggiungi("Boss VULNERABILE")
        elif s.tipo == "thorns":
            self.battle_status["party_thorns"] = True
            self.log.aggiungi("Spine Attive!")
        elif s.tipo == "revive":
            t = self.get_best_heal_target()
            t.guarisci(s.cura, self.effetti_visivi)
            self.log.aggiungi(f"Revive su {t.nome}")
        elif s.tipo == "miss":
            self.battle_status["boss_miss_next"] = True
            self.log.aggiungi("Boss mancherà!")
        elif s.tipo == "split":
            self.battle_status["party_split"] = True
            self.log.aggiungi("Testuggine!")
        elif s.tipo == "tank":
            self.battle_status["party_tank"] = True
            self.log.aggiungi("Immunità Totale!")
        elif s.tipo == "buff_atk":
            self.battle_status["party_buff_dmg"] = True
            self.log.aggiungi("Buff Attacco!")
        elif s.tipo == "full_heal":
            t = self.get_best_heal_target()
            t.guarisci(999, self.effetti_visivi)
        else:
            if dmg > 0:
                self.boss.subisci_danno(dmg, self.effetti_visivi, self.battle_status["boss_vuln"])
            if s.cura > 0:
                self.get_best_heal_target().guarisci(s.cura, self.effetti_visivi)

        self.log.aggiungi(f"SPECIAL: {s.nome}!")
        self.speciale_usata_globale = True
        self.selected.cooldown = COOLDOWN_TURNS

        if self.boss.hp == 0:
            if self.boss.cambia_fase(self.effetti_visivi):
                self.game_state = "BOSS_DYING"

    def outline(self, screen, text, font, col, center):
        for ox, oy in [(-2, -2), (-2, 2), (2, -2), (2, 2), (-1, 0), (1, 0), (0, -1), (0, 1)]:
            s = font.render(text, True, BLACK)
            r = s.get_rect(center=(center[0] + ox, center[1] + oy))
            screen.blit(s, r)
        s = font.render(text, True, col)
        r = s.get_rect(center=center)
        screen.blit(s, r)

    def draw_text_wrapped(self, screen, text, font, color, rect):
        y = rect.top
        font_height = font.get_height()
        paragraphs = text.split("\n")
        for paragraph in paragraphs:
            words = paragraph.split(" ")
            cur_line = []
            cur_w = 0
            for word in words:
                w_surf = font.render(word, True, color)
                if cur_w + w_surf.get_width() >= rect.width:
                    screen.blit(font.render(" ".join(cur_line), True, color), (rect.left, y))
                    y += font_height
                    cur_line = [word]
                    cur_w = w_surf.get_width()
                else:
                    cur_line.append(word)
                    cur_w += w_surf.get_width() + 5
            if cur_line:
                screen.blit(font.render(" ".join(cur_line), True, color), (rect.left, y))
                y += font_height
            y += 5

    def disegna_recap(self, screen, p):
        screen.fill(DARK_BG)
        screen.blit(self.fonts["title"].render("RECAP PARTITA", True, WHITE), (50, 40))
        pygame.draw.line(screen, p.colore, (380, 100), (380, 500), 2)

        x_txt = 50
        y_txt = 120
        screen.blit(self.fonts["main"].render("Zone esplorate:", True, WHITE), (x_txt, y_txt))
        zone_str = ", ".join(p.zone) if p.zone else "Nessuna"
        screen.blit(self.fonts["small"].render(zone_str, True, WHITE), (x_txt + 150, y_txt + 2))

        y_txt += 40
        screen.blit(self.fonts["main"].render("Assi presi:", True, p.colore), (x_txt, y_txt))
        assi_str = ", ".join(p.assi) if p.assi else "Nessuno"
        screen.blit(self.fonts["small"].render(assi_str, True, p.colore), (x_txt + 150, y_txt + 2))

        y_txt += 60
        self.draw_text_wrapped(screen, p.flavor_text, self.fonts["small"], WHITE, pygame.Rect(x_txt, y_txt, 300, 120))
        y_txt += 130

        screen.blit(self.fonts["main"].render("Poteri Acquisiti:", True, p.colore), (x_txt, y_txt))
        y_txt += 30
        for spec in p.abilita_speciali:
            self.draw_text_wrapped(
                screen,
                f"★ {spec.nome}: {spec.descrizione}",
                self.fonts["small"],
                p.colore,
                pygame.Rect(x_txt, y_txt, 300, 60),
            )
            y_txt += 50

        y_txt += 20
        screen.blit(self.fonts["main"].render("Attacchi Base:", True, WHITE), (x_txt, y_txt))
        y_txt += 30
        for i, ab in enumerate(p.abilita_base):
            self.draw_text_wrapped(
                screen,
                f"Attacco {i+1}: {ab.nome} - {ab.descrizione}",
                self.fonts["small"],
                (180, 180, 180),
                pygame.Rect(x_txt, y_txt, 300, 60),
            )
            y_txt += 40

        big_rect = pygame.Rect(470, 140, 210, 300)

        # --- NUOVA LOGICA IMMAGINE ---
        pygame.draw.rect(screen, p.colore, big_rect.inflate(10, 10))
        if p.image:
            img = scale_to_fit(p.image, big_rect.width, big_rect.height)
            img_rect = img.get_rect(center=big_rect.center)
            screen.blit(img, img_rect)

        pygame.draw.rect(screen, WHITE, big_rect, 5)

        screen.blit(self.fonts["title"].render(p.nome, True, WHITE), (450, 100))
        screen.blit(self.fonts["main"].render(f"HP Max: {p.hp_max}", True, WHITE), (450, 470))
        screen.blit(self.fonts["small"].render("Clicca per continuare...", True, (150, 150, 150)), (SCREEN_WIDTH - 200, SCREEN_HEIGHT - 50))

    def _trigger_ending_sequence(self):
        """Sequenza di vittoria HARDCODED (senza ActionRunner)."""
        self.game_state = "ENDING"
        self.dialogue_index = 0
        self.dialogue_lines = [
            ("L'Oste Eterno", "Fermatevi. Ho visto abbastanza."),
            ("L'Oste Eterno", "La violenza non è la soluzione. Ma la vostra tenacia... quella è reale."),
            ("L'Oste Eterno", "Avete trovato gli Assi e dimostrato il vostro valore. Il premio non è sconfiggermi."),
            ("L'Oste Eterno", "Il premio è tornare a casa. Andate, la Sicilia vi aspetta."),
            ("Turiddu", "Signore... grazie! Non ci speravo più."),
            ("Rosalia", "Grazie infinite. Addio, e grazie per la lezione."),
            ("SISTEMA", "FINE DEL GIOCO - GRAZIE PER AVER GIOCATO!")
        ]

        
        
        # Esegui lo script

        
    def _trigger_defeat_sequence(self):
        """Sequenza di sconfitta HARDCODED (senza ActionRunner)."""
        self.game_state = "DEFEAT"
        self.dialogue_index = 0
        self.dialogue_lines = [
            ("L'Oste Eterno", "Tutto qui? Pensavo foste diversi dagli altri."),
            ("L'Oste Eterno", "La vostra corsa finisce qui. Il conto è chiuso."),
            ("Turiddu", "Rosalia... scusa... non ce l'ho fatta..."),
            ("L'Oste Eterno", "Riposate ora. L'Eternità è un lungo sonno."),
            ("SISTEMA", "GAME OVER")
        ]

        

    def handle_event(self, event) -> bool:
        if event.type == pygame.KEYDOWN:
            # --- DIALOGHI HARDCODED (ENDING / DEFEAT) ---
            if self.game_state in ("ENDING", "DEFEAT"):
                if event.key in (pygame.K_SPACE, pygame.K_RETURN, pygame.K_KP_ENTER):
                    self.dialogue_index += 1
                    if self.dialogue_index >= len(self.dialogue_lines):
                        if self.game_state == "ENDING":
                            self._state_machine.change_state(StateID.MAIN_MENU)
                        else:
                            self._state_machine.change_state(StateID.GAME_OVER)
                    return True
                return False
            # MENU / RECAP: invio come click
            if self.game_state == "MENU":
                if event.key in (pygame.K_RETURN, pygame.K_KP_ENTER, pygame.K_SPACE):
                    self.game_state = "RECAP_0"
                    return True

            elif self.game_state.startswith("RECAP"):
                if event.key in (pygame.K_RETURN, pygame.K_KP_ENTER, pygame.K_SPACE):
                    idx = int(self.game_state.split("_")[1])
                    if idx < len(self.party) - 1:
                        self.game_state = f"RECAP_{idx+1}"
                    else:
                        self.game_state = "COMBAT"
                    return True

            elif self.game_state == "COMBAT":
                if not (self.turno_giocatore and self.boss_attack_phase == "IDLE" and self.immortal_msg_timer == 0):
                    return False

                # ESC: torna a scegliere party
                if event.key == pygame.K_ESCAPE:
                    if self.selected:
                        self.selected.is_selected = False
                        self.selected.torna_al_posto()
                        self.selected = None
                    self.kb_focus = "PARTY"
                    return True

                # Se non ho selezionato nessuno, seleziona quello corrente
                if self.kb_focus == "PARTY":
                    if event.key in (pygame.K_UP, pygame.K_w):
                        self._cycle_party(-1)
                        return True
                    if event.key in (pygame.K_DOWN, pygame.K_s):
                        self._cycle_party(+1)
                        return True
                    if event.key in (pygame.K_RETURN, pygame.K_KP_ENTER, pygame.K_SPACE):
                        # conferma selezione personaggio -> passa a menu azioni
                        if self.selected is None:
                            alive = self._alive_party_indices()
                            if alive:
                                self.kb_selected_party_idx = alive[0]
                                self._set_selected_by_index(self.kb_selected_party_idx)
                        self.kb_focus = "ACTIONS"
                        self.kb_action_idx = 0
                        return True

                    # anche frecce destra per entrare nel menu azioni
                    if event.key == pygame.K_RIGHT and self.selected:
                        self.kb_focus = "ACTIONS"
                        self.kb_action_idx = 0
                        return True

                elif self.kb_focus == "ACTIONS":
                    total = self._get_action_buttons_count()

                    if event.key in (pygame.K_LEFT, pygame.K_a):
                        self.kb_action_idx = (self.kb_action_idx - 1) % total
                        return True
                    if event.key in (pygame.K_RIGHT, pygame.K_d):
                        self.kb_action_idx = (self.kb_action_idx + 1) % total
                        return True
                    if event.key in (pygame.K_UP, pygame.K_w):
                        # su -> torna alla party
                        self.kb_focus = "PARTY"
                        return True
                    if event.key in (pygame.K_RETURN, pygame.K_KP_ENTER, pygame.K_SPACE):
                        self._execute_action_by_index(self.kb_action_idx)
                        return True

            return False
        if event.type == pygame.MOUSEBUTTONDOWN:
            mx, my = pygame.mouse.get_pos()
            # Click per avanzare i dialoghi
            if self.game_state in ("ENDING", "DEFEAT"):
                self.dialogue_index += 1
                if self.dialogue_index >= len(self.dialogue_lines):
                    if self.game_state == "ENDING":
                        self._state_machine.change_state(StateID.MAIN_MENU)
                    else:
                        self._state_machine.change_state(StateID.GAME_OVER)
                return True
            
            if self.game_state == "MENU":
                self.game_state = "RECAP_0"
                return True

            elif self.game_state.startswith("RECAP"):
                idx = int(self.game_state.split("_")[1])
                if idx < len(self.party) - 1:
                    self.game_state = f"RECAP_{idx+1}"
                else:
                    self.game_state = "COMBAT"
                return True

            elif self.game_state == "COMBAT":
                if self.turno_giocatore and self.boss_attack_phase == "IDLE" and self.immortal_msg_timer == 0:
                    if self.btn_passa.collidepoint((mx, my)):
                        self.turno_giocatore = False
                        if self.selected:
                            self.selected.torna_al_posto()
                        self.selected = None
                        self.speciale_usata_globale = False

                        if self.boss.immortale:
                            self._trigger_ending_sequence()
                        else:
                            self.log.aggiungi("Passi il turno...")
                            self.boss_attack_phase = "SHOW_TEXT"
                            self.boss_attack_timer = 60
                        return True

                    for p in self.party:
                        if p.rect.collidepoint((mx, my)) and not p.is_dead:
                            if self.selected:
                                self.selected.is_selected = False
                                self.selected.torna_al_posto()
                            self.selected = p
                            self.selected.is_selected = True
                            self.selected.vai_in_prima_linea()
                            return True

                    if self.selected:
                        menu_x, menu_y = 200, 480

                        for i, skill in enumerate(self.selected.abilita_base):
                            btn = pygame.Rect(menu_x + i * 140, menu_y, 130, 40)
                            if btn.collidepoint((mx, my)):
                                if self.boss.immortale:
                                    self.immortal_msg_text = "L'OSTE TI BLOCCA!"
                                    self.immortal_msg_timer = 90
                                    self.selected.azione_base_usata = True
                                elif self.selected.azione_base_usata:
                                    self.warning_msg_text = "GIÀ USATA"
                                    self.warning_msg_timer = 60
                                else:
                                    dmg = skill.danno
                                    if self.battle_status["party_buff_dmg"] and dmg > 0:
                                        dmg += 20

                                    if dmg > 0:
                                        self.boss.subisci_danno(
                                            dmg,
                                            self.effetti_visivi,
                                            is_vulnerable=self.battle_status["boss_vuln"],
                                        )
                                        self.log.aggiungi(f"{skill.nome}: colpo!")

                                    if skill.cura > 0 or skill.buff:
                                        target = self.get_best_heal_target()
                                        target.guarisci(skill.cura, self.effetti_visivi, is_buff=skill.buff)
                                        self.log.aggiungi(f"{skill.nome}: usato!")

                                    self.selected.azione_base_usata = True
                                    if self.boss.hp == 0:
                                        if self.boss.cambia_fase(self.effetti_visivi):
                                            self.game_state = "BOSS_DYING"
                                return True

                        specials_count = len(self.selected.abilita_speciali)
                        for k, s in enumerate(self.selected.abilita_speciali):
                            if specials_count == 1:
                                btn_spec = pygame.Rect(menu_x, menu_y + 50, 270, 40)
                            else:
                                btn_w = 130
                                gap = 10
                                btn_spec = pygame.Rect(menu_x + k * (btn_w + gap), menu_y + 50, btn_w, 40)

                            if btn_spec.collidepoint((mx, my)):
                                if self.boss.immortale:
                                    self.immortal_msg_text = "L'OSTE TI BLOCCA!"
                                    self.immortal_msg_timer = 90
                                    self.selected.cooldown = COOLDOWN_TURNS
                                    self.speciale_usata_globale = True
                                elif self.selected.cooldown > 0:
                                    self.warning_msg_text = "IN RICARICA"
                                    self.warning_msg_timer = 60
                                elif self.selected.azione_speciale_usata or self.speciale_usata_globale:
                                    self.warning_msg_text = "GIÀ USATA"
                                    self.warning_msg_timer = 60
                                else:
                                    dmg = s.danno
                                    if self.battle_status["party_buff_dmg"] and dmg > 0:
                                        dmg += 30

                                    if s.tipo == "stun":
                                        self.battle_status["boss_stunned"] = True
                                        self.log.aggiungi("Boss STORDITO!")
                                    elif s.tipo == "debuff_dmg":
                                        self.battle_status["boss_half_dmg"] = True
                                        self.log.aggiungi("Boss: Danni dimezzati!")
                                    elif s.tipo == "vuln":
                                        self.battle_status["boss_vuln"] = True
                                        self.log.aggiungi("Boss VULNERABILE")
                                    elif s.tipo == "thorns":
                                        self.battle_status["party_thorns"] = True
                                        self.log.aggiungi("Spine Attive!")
                                    elif s.tipo == "revive":
                                        t = self.get_best_heal_target()
                                        t.guarisci(s.cura, self.effetti_visivi)
                                        self.log.aggiungi(f"Revive su {t.nome}")
                                    elif s.tipo == "miss":
                                        self.battle_status["boss_miss_next"] = True
                                        self.log.aggiungi("Boss mancherà!")
                                    elif s.tipo == "split":
                                        self.battle_status["party_split"] = True
                                        self.log.aggiungi("Testuggine!")
                                    elif s.tipo == "tank":
                                        self.battle_status["party_tank"] = True
                                        self.log.aggiungi("Immunità Totale!")
                                    elif s.tipo == "buff_atk":
                                        self.battle_status["party_buff_dmg"] = True
                                        self.log.aggiungi("Buff Attacco!")
                                    elif s.tipo == "full_heal":
                                        t = self.get_best_heal_target()
                                        t.guarisci(999, self.effetti_visivi)
                                    else:
                                        if dmg > 0:
                                            self.boss.subisci_danno(
                                                dmg,
                                                self.effetti_visivi,
                                                self.battle_status["boss_vuln"],
                                            )
                                        if s.cura > 0:
                                            self.get_best_heal_target().guarisci(s.cura, self.effetti_visivi)

                                    self.log.aggiungi(f"SPECIAL: {s.nome}!")
                                    self.speciale_usata_globale = True
                                    self.selected.cooldown = COOLDOWN_TURNS

                                    if self.boss.hp == 0:
                                        if self.boss.cambia_fase(self.effetti_visivi):
                                            self.game_state = "BOSS_DYING"
                                return True

        return False

    def update(self, dt: float):
        # --- FIX: AGGIORNAMENTO SCRIPT RUNNER ---
        # Questo permette ai dialoghi e alle attese di avanzare
       
        if self.immortal_msg_timer > 0:
            self.immortal_msg_timer -= 1
        if self.warning_msg_timer > 0:
            self.warning_msg_timer -= 1

        if self.game_state == "BOSS_DYING":
            for p in self.party:
                p.update_pos()
            self.boss_fade_alpha += 2
            if self.boss_fade_alpha > 255:
                self.effetti_visivi.clear()
                self.game_state = "FAKE_VICTORY"

        elif self.game_state == "FAKE_VICTORY":
            self.fake_victory_timer += 1
            if self.fake_victory_stage == 0 and self.fake_victory_timer > 240:
                self.fake_victory_stage = 1
                self.fake_victory_timer = 0
            elif self.fake_victory_stage == 1 and self.fake_victory_timer > 180:
                self.fake_victory_stage = 2
                self.fake_victory_timer = 0
            elif self.fake_victory_stage == 2:
                alpha = max(0, 255 - self.fake_victory_timer * 3)
                if alpha == 0:
                    self.boss.diventa_immortale(self.effetti_visivi)
                    for p in self.party:
                        p.torna_al_posto()
                        p.x = p.home_x
                        p.y = p.home_y
                    self.game_state = "COMBAT"

        elif self.game_state == "COMBAT" and not self.turno_giocatore:
            # AI BOSS LOGIC
            if self.battle_status["boss_stunned"]:
                if self.boss_attack_phase == "SHOW_TEXT":
                    if self.boss_attack_timer == 60:
                        self.log.aggiungi("Boss STORDITO!")
                    self.boss_attack_timer -= 1
                    if self.boss_attack_timer <= 0:
                        self.battle_status["boss_stunned"] = False
                        self.battle_status["boss_vuln"] = False

                        for p in self.party:
                            p.reset_turno()
                            p.aggiorna_cooldown()

                        self.boss_attack_phase = "IDLE"
                        self.turn_counter += 1
                        self.speciale_usata_globale = False
                        self.turno_giocatore = True
                        self.log.aggiungi("Tocca a voi!")
            else:
                if self.boss_attack_phase == "SHOW_TEXT":
                    if self.boss_attack_timer > 0:
                        self.boss_attack_timer -= 1
                        if not self.boss_target:
                            vivi = [p for p in self.party if not p.is_dead]
                            if vivi:
                                self.boss_target = random.choice(vivi)
                            else:
                                self.game_state = "GAMEOVER"
                    else:
                        if self.boss_target:
                            self.boss_attack_phase = "MOVE_CENTER"
                        else:
                            self.game_state = "GAMEOVER"

                elif self.boss_attack_phase == "MOVE_CENTER":
                    self.boss.update()
                    self.boss_attack_phase = "HIT"
                    self.boss_attack_timer = 20

                elif self.boss_attack_phase == "HIT":
                    if self.boss_attack_timer == 20:
                        dmg = random.randint(0, 30) + (self.boss.fase * 10)

                        if self.battle_status["boss_half_dmg"]:
                            dmg //= 2
                            self.effetti_visivi.append(
                                EffettoVisivo(self.boss.rect.centerx, self.boss.rect.y, "DMG DIMEZZATO", GOLD)
                            )
                            self.battle_status["boss_half_dmg"] = False

                        if self.battle_status["boss_miss_next"]:
                            dmg = 0
                            self.effetti_visivi.append(
                                EffettoVisivo(self.boss_target.rect.centerx, self.boss_target.rect.y, "MISS!", WHITE)
                            )
                            self.battle_status["boss_miss_next"] = False

                        if self.battle_status["party_tank"]:
                            dmg = 0
                            self.effetti_visivi.append(
                                EffettoVisivo(self.boss_target.rect.centerx, self.boss_target.rect.y, "PARATO!", CYAN)
                            )

                        if self.battle_status["party_thorns"]:
                            self.boss.subisci_danno(20, self.effetti_visivi)
                            self.effetti_visivi.append(
                                EffettoVisivo(self.boss.rect.centerx, self.boss.rect.y, "SPINE!", GREEN)
                            )

                        if self.battle_status["party_split"]:
                            vivi = [p for p in self.party if not p.is_dead]
                            dmg_p = dmg // len(vivi) if len(vivi) > 0 else 0
                            for p in vivi:
                                p.subisci_danno(dmg_p, self.effetti_visivi)
                            self.log.aggiungi("Testuggine!")
                        else:
                            if dmg > 0:
                                self.boss_target.subisci_danno(dmg, self.effetti_visivi)
                                self.log.aggiungi(f"Colpito {self.boss_target.nome}")

                    self.boss_attack_timer -= 1
                    if self.boss_attack_timer <= 0:
                        self.boss_attack_phase = "END"

                elif self.boss_attack_phase == "END":
                    self.battle_status["boss_vuln"] = False
                    self.battle_status["party_tank"] = False
                    self.battle_status["party_split"] = False
                    self.battle_status["party_thorns"] = False
                    self.battle_status["party_buff_dmg"] = False

                    if all(p.is_dead for p in self.party):
                        self._trigger_defeat_sequence()
                    else:
                        for p in self.party:
                            p.reset_turno()
                            p.aggiorna_cooldown()

                        self.boss_attack_phase = "IDLE"
                        self.turn_counter += 1
                        self.boss_target = None
                        self.turno_giocatore = True
                        self.log.aggiungi("Tocca a voi!")

        for p in self.party:
            p.update_pos()
        self.boss.update()

        for ef in self.effetti_visivi:
            ef.update()
        self.effetti_visivi[:] = [e for e in self.effetti_visivi if e.timer > 0]

    def render(self, screen):
        screen.fill(DARK_BG)

        if self.game_state == "MENU":
            screen.blit(self.fonts["title"].render("BOSS FINALE", True, GOLD), (SCREEN_WIDTH // 2 - 130, 200))
            screen.blit(self.fonts["main"].render("L'Ultimo Brindisi", True, WHITE), (SCREEN_WIDTH // 2 - 80, 250))
            screen.blit(self.fonts["main"].render("Clicca sullo schermo per iniziare", True, CYAN), (SCREEN_WIDTH // 2 - 150, 400))

        elif self.game_state.startswith("RECAP"):
            idx = int(self.game_state.split("_")[1])
            self.disegna_recap(screen, self.party[idx])

        elif self.game_state == "BOSS_DYING":
            for p in self.party:
                p.disegna(screen)
            self.log.disegna(screen)
            self.boss.disegna(screen, fade_alpha=self.boss_fade_alpha)

        elif self.game_state == "FAKE_VICTORY":
            if self.fake_victory_stage == 0:
                self.outline(screen, "CONGRATULAZIONI!", self.fonts["big_msg"], GOLD, (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2 - 40))
                self.outline(screen, "AVETE SCONFITTO L'OSTE!", self.fonts["big_msg"], GOLD, (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2 + 20))
            elif self.fake_victory_stage == 1:
                self.outline(screen, "O forse no...", self.fonts["big_msg"], RED, (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2))
            elif self.fake_victory_stage == 2:
                s = pygame.Surface((SCREEN_WIDTH, SCREEN_HEIGHT))
                s.fill(WHITE)
                s.set_alpha(max(0, 255 - self.fake_victory_timer * 3))
                screen.blit(s, (0, 0))

        elif self.game_state in ["COMBAT", "DEFEAT_SEQUENCE"]:

            self.boss.disegna(screen)


            for p in self.party:
                p.disegna(screen)

            self.log.disegna(screen)

            for ef in self.effetti_visivi:
                ef.draw(screen, self.fonts)

            if self.turno_giocatore and self.boss_attack_phase == "IDLE":
                actions_left = sum(
                    1 for p in self.party if not p.is_dead and not p.azione_base_usata
                ) + (1 if not self.speciale_usata_globale else 0)

                screen.blit(
                    self.fonts["main"].render(f"AZIONI: {actions_left}", True, CYAN),
                    (20, 20),
                )

                # ---- PASSA (sempre visibile) ----
                col_pass = RED if self.boss.immortale else (100, 50, 50)
                pygame.draw.rect(screen, col_pass, self.btn_passa, border_radius=8)
                pygame.draw.rect(screen, WHITE, self.btn_passa, 2, border_radius=8)
                screen.blit(
                    self.fonts["main"].render("PASSA", True, WHITE),
                    (self.btn_passa.x + 20, self.btn_passa.y + 10),
                )

                # (C) highlight PASSA se selezionato da tastiera
                pass_idx = self._get_action_buttons_count() - 1
                if self.kb_focus == "ACTIONS" and self.kb_action_idx == pass_idx:
                    pygame.draw.rect(screen, WHITE, self.btn_passa, 4, border_radius=8)

                # ---- MENU AZIONI (solo se c'è un personaggio selezionato) ----
                if self.selected:
                    menu_x, menu_y = 200, 480
                    pygame.draw.rect(screen, GRAY_UI, (190, 470, 320, 100), border_radius=10)
                    pygame.draw.rect(screen, self.selected.colore, (190, 470, 320, 100), 2, border_radius=10)

                    # (B) base_n serve per calcolare l'indice globale dei pulsanti special
                    base_n = len(self.selected.abilita_base)

                    # ---- BOTTONI BASE ----
                    for i, skill in enumerate(self.selected.abilita_base):
                        # (A) highlight tastiera sul bottone base i
                        is_kb = (self.kb_focus == "ACTIONS" and self.kb_action_idx == i)

                        col = (255, 255, 255) if is_kb else (
                            CYAN if not self.selected.azione_base_usata else (100, 100, 100)
                        )

                        rect_base = pygame.Rect(menu_x + i * 140, menu_y, 130, 40)
                        pygame.draw.rect(screen, col, rect_base, border_radius=5)
                        screen.blit(
                            self.fonts["small"].render(skill.nome, True, BLACK),
                            (rect_base.x + 5, rect_base.y + 10),
                        )

                    # ---- BOTTONI SPECIAL ----
                    col_sp = GOLD if self.selected.cooldown == 0 else DISABLED_GRAY

                    specials_count = len(self.selected.abilita_speciali)
                    for k, spec_skill in enumerate(self.selected.abilita_speciali):
                        if specials_count == 1:
                            rect_spec = pygame.Rect(menu_x, menu_y + 50, 270, 40)
                        else:
                            btn_w = 130
                            gap = 10
                            rect_spec = pygame.Rect(menu_x + k * (btn_w + gap), menu_y + 50, btn_w, 40)

                        # (B) indice globale special = base_n + k
                        global_idx = base_n + k
                        is_kb = (self.kb_focus == "ACTIONS" and self.kb_action_idx == global_idx)

                        draw_col = (255, 255, 255) if is_kb else col_sp
                        pygame.draw.rect(screen, draw_col, rect_spec, border_radius=5)

                        display_name = spec_skill.nome
                        if specials_count > 1 and len(display_name) > 10:
                            display_name = display_name[:9] + "."

                        screen.blit(
                            self.fonts["main"].render(
                                f"★ {display_name}",
                                True,
                                BLACK if draw_col in (GOLD, (255, 255, 255)) else WHITE,
                            ),
                            (rect_spec.x + 5, rect_spec.y + 8),
                        )

            if (
                not self.turno_giocatore
                and self.boss_attack_phase == "SHOW_TEXT"
                and self.boss_target
                and not self.battle_status["boss_stunned"]
            ):
                self.outline(
                    screen,
                    f"L'OSTE HA SCELTO {self.boss_target.nome.upper()}!",
                    self.fonts["big_msg"],
                    ORANGE,
                    (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2 - 20),
                )

            if self.battle_status["boss_stunned"]:
                self.outline(
                    screen,
                    "BOSS STORDITO!",
                    self.fonts["big_msg"],
                    CYAN,
                    (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2 - 50),
                )

            if self.immortal_msg_timer > 0:
                self.outline(screen, self.immortal_msg_text, self.fonts["big_msg"], RED, (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2))
            if self.warning_msg_timer > 0:
                self.outline(screen, self.warning_msg_text, self.fonts["big_msg"], RED, (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2))

        # --- RENDER HARDCODED DIALOGUES ---
        elif self.game_state in ("ENDING", "DEFEAT"):
            # --- SFONDO ---
            if self.game_state == "ENDING":
                bg = self.assets.get_image(
                    "outro_scena1",
                    SCREEN_WIDTH,
                    SCREEN_HEIGHT,
                    fallback_type="background"
                )
                if bg:
                    screen.blit(bg, (0, 0))
                else:
                    screen.fill((200, 200, 200))
            else:
                screen.fill(BLACK)

            # --- PRENDO BATTUTA CORRENTE ---
            if self.dialogue_index < len(self.dialogue_lines):
                speaker, text = self.dialogue_lines[self.dialogue_index]
            else:
                speaker, text = ("", "")

            # --- DISEGNA RITRATTI SOPRA IL BOX ---
            # Coordinate base: box dialogo in basso, quindi i ritratti stanno sopra (y circa 120)
            portrait_y = 110

            if "Turiddu" in speaker:
                img = self.assets.get_image("characters/Turiddu", 200, 260, fallback_type="player", preserve_aspect=True)
                if img:
                    screen.blit(img, (80, portrait_y))

            elif "Rosalia" in speaker:
                img = self.assets.get_image("characters/Rosalia", 200, 260, fallback_type="player", preserve_aspect=True)
                if img:
                    # opzionale: flip per guardare verso il centro
                    img = pygame.transform.flip(img, True, False)
                    screen.blit(img, (SCREEN_WIDTH - 280, portrait_y))

            elif "Oste" in speaker:
                img = self.assets.get_image("enemy_boss_oste", 260, 300, fallback_type="enemy", preserve_aspect=True)
                if img:
                    img = pygame.transform.flip(img, True, False)
                    screen.blit(img, (SCREEN_WIDTH // 2 - 130, portrait_y))

            # --- BOX DIALOGO ---
            box_rect = pygame.Rect(50, 400, 700, 150)
            pygame.draw.rect(screen, BLACK, box_rect)
            pygame.draw.rect(screen, WHITE, box_rect, 3)

            col = GOLD if "Oste" in speaker else (CYAN if "SISTEMA" not in speaker else RED)

            screen.blit(self.fonts["main"].render(speaker, True, col), (70, 420))
            # se non hai fonts["dialogue"], usa fonts["btn"] o fonts["main"]
            f_dialogue = self.fonts.get("dialogue", self.fonts["btn"])
            screen.blit(f_dialogue.render(text, True, WHITE), (70, 460))

            screen.blit(self.fonts["small"].render("[PREMI SPAZIO]", True, (150, 150, 150)), (600, 520))

        elif self.game_state == "GAMEOVER":
            self.outline(screen, "GAME OVER", self.fonts["big_msg"], RED, (SCREEN_WIDTH // 2, SCREEN_HEIGHT // 2))