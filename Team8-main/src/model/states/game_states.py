"""
Concrete game state implementations.
Updated: 
- CutsceneState: Fixed aspect ratio (no stretching).
- CutsceneState: Removed dark rectangle overlay, using Alpha transparency for inactive speakers.
"""

import logging
import pygame
import sys
from src.model.states.base_state import BaseState, StateID
from src.model.input_actions import Action

from src.model.combat.turn_manager import TurnManager
from src.model.combat.battle_context import BattleContext
from src.model.ui.combat_menu_state import CombatMenuState
from src.model.combat.combat_types import Encounter
from src.model.combat.enemy import Enemy
from src.model.combat.damage_calculator import DamageCalculator
from src.model.utils.rng import RNG
from src.model.ai.enemy_ai import EnemyBrain, DonTaninoBrain, BossOsteBrain
from src.model.ui.interaction_menu_state import InteractionMenuStateData
from src.model.combat.targeting_system import TargetingSystem
from src.model.combat.action_pipeline import ActionPipeline
from src.model.room_data import EntityDefinition
from src.model.etna.boss_oste import BossOste

# Minigame Imports
from src.model.minigame.scopa_model import ScopaModel
from src.view.scopa_view import ScopaView
from src.model.minigame.briscola_model import BriscolaModel
from src.view.briscola_view import BriscolaView
from src.model.minigame.sette_mezzo_model import SetteMezzoModel
from src.view.sette_mezzo_view import SetteMezzoView
from src.model.minigame.cucu_model import CucuModel
from src.view.cucu_view import CucuView
from src.model.states.boss_oste_state import BossOsteState

logger = logging.getLogger(__name__)

# --- MAIN MENU STATE ---
class MainMenuState(BaseState):
    def __init__(self, state_machine=None):
        super().__init__(StateID.MAIN_MENU, state_machine)
        self.sub_menu = "root"  # root, load_game
        self.cursor_index = 0
        self.save_slots = []
        self.version = "Alpha 0.2 (RPG Mode)"

    def enter(self, prev_state=None, **kwargs):
        self.sub_menu = "root"
        self.cursor_index = 0
        if self._state_machine and self._state_machine.controller:
            self.save_slots = self._state_machine.controller.save_manager.list_slots()
            # Musica del menu
            self._state_machine.controller.game.audio.play_bgm("intro.ogg", fade_ms=1000)

    def exit(self, next_state=None):
        pass

    def handle_event(self, event) -> bool:
        input_manager = self._state_machine.controller.input_manager
        
        if input_manager.was_just_pressed(Action.MENU_DOWN):
            self._move_cursor(1)
            return True
        if input_manager.was_just_pressed(Action.MENU_UP):
            self._move_cursor(-1)
            return True
        if input_manager.was_just_pressed(Action.CONFIRM):
            self._confirm_selection()
            return True
        if input_manager.was_just_pressed(Action.CANCEL):
            self._go_back()
            return True
            
        return False

    def update(self, dt: float):
        pass

    def render(self, surface):
        pass

    def _move_cursor(self, delta):
        limit = 0
        if self.sub_menu == "root": limit = 3
        elif self.sub_menu == "load_game": limit = len(self.save_slots) + 1
        
        if limit > 0:
            self.cursor_index = (self.cursor_index + delta) % limit

    def _confirm_selection(self):
        controller = self._state_machine.controller
        
        if self.sub_menu == "root":
            if self.cursor_index == 0: # New Game
                # FIX: Avvia direttamente con 2 giocatori fissi (Turiddu & Rosalia)
                controller.start_new_game(2)
                
            elif self.cursor_index == 1: # Load Game
                self.sub_menu = "load_game"
                self.cursor_index = 0
                self.save_slots = controller.save_manager.list_slots()
            elif self.cursor_index == 2: # Quit
                sys.exit()

        elif self.sub_menu == "load_game":
            if self.cursor_index == len(self.save_slots): # Back option
                self._go_back()
            else:
                slot = self.save_slots[self.cursor_index]
                if slot.status.name != "EMPTY":
                    res = controller.load_game(slot.slot_index)
                    if not res.ok:
                        print(f"Load Error: {res.message}")
                else:
                    print("Slot empty.")

    def _go_back(self):
        if self.sub_menu == "root":
            pass
        else:
            self.sub_menu = "root"
            self.cursor_index = 0

# --- OTHER STATES ---
class NewGameSetupState(BaseState):
    def __init__(self, state_machine=None): super().__init__(StateID.NEW_GAME_SETUP, state_machine)
    def enter(self, prev_state=None, **kwargs): pass
    def exit(self, next_state=None): pass
    def handle_event(self, event) -> bool: return False
    def update(self, dt: float): pass
    def render(self, surface): pass

# --- CUTSCENE STATE (INTRO) ---
# --- CUTSCENE STATE (INTRO) ---
class CutsceneState(BaseState):
    """
    Gestisce la intro narrativa.
    Slideshow di immagini con dialoghi e transizioni.
    """
    
    # Costanti Layout
    SCREEN_W = 800
    SCREEN_H = 600
    TEXT_BOX_H = 150
    
    # Dimensioni fisse "Ritratto" (Verticale) per evitare stretch orizzontale
    PORTRAIT_W = 180
    PORTRAIT_H = 280

    def __init__(self, state_machine=None):
        super().__init__(StateID.CUTSCENE, state_machine)
        self.slides = []
        self.current_slide_idx = 0
        self.current_dialogue_idx = 0
        self.timer = 0.0
        
        # State: FADE_IN, PLAYING, FADE_OUT
        self.state = "FADE_IN"
        self.fade_alpha = 255
        self.fade_speed = 300 # Alpha per second
        
        self.assets = None
        self.font = None
        self.font_name = None

    def enter(self, prev_state=None, **kwargs):
        game = self._state_machine.controller.game
        self.assets = self._state_machine.controller.render_controller.asset_manager
        
        # Init Fonts
        if not pygame.font.get_init(): pygame.font.init()
        self.font = pygame.font.SysFont("Arial", 20)
        self.font_name = pygame.font.SysFont("Arial", 22, bold=True)

        # Configurazione Intro
        self.slides = [
            {
                "bg": "intro_scena1",
                "music": "intro_theme.ogg",
                "layout": "turiddu_left_rosalia_right",
                "dialogues": [
                    ("Turiddu", "Picciò, sono le sette. Se non ci muoviamo ora, restiamo bloccati qui fino a ferragosto. Ho visto macchine parcheggiare in terza fila."),
                    ("Rosalia", "Confermo. Tra mezz'ora qui c’è l'inferno. C'è già puzza di sudore e zucchero filato. Iamuninni."),
                    ("Turiddu", "(Si ferma di colpo, occhi a cuoricino) Aspetta... Quell'odore... sono zeppoline! Quelle col miele!"),
                    ("Rosalia", "Turiddu, santuzza benedetta, hai mangiato per un reggimento. Se ti mangi pure le zeppole ti dobbiamo portare con la carriola."),
                    ("Turiddu", "Ma taliale! Sono dorate! Sono... poesia! Amunì dai, un cartoccio al volo per il viaggio. È per tenere alto il morale!"),
                    ("Rosalia", "(Si passa una mano in faccia) Sei una condanna. Va bene, una porzione. Ma veloce che mi sta salendo l'ansia.")
                ]
            },
            {
                "bg": "intro_scena2",
                "layout": "strammu_left_party_right",
                "dialogues": [
                    ("U Strammu", "Zeppoline? Roba per riempire la pancia. Qui ho qualcosa per riempire l'anima."),
                    ("Rosalia", "(Sospettosa) ...Scusi? E lei da dove viene fuori? Giuro che qui c'era solo un muro scrostato."),
                    ("U Strammu", "Le cose appaiono quando servono, gioia mia. Voi scappate mentre il sole cala. Avete paura della notte? O avete solo sete?"),
                    ("Turiddu", "No guardi, niente alcol, devo guidare. E abbiamo una certa fretta. Buona serata."),
                    ("U Strammu", "Chi ha parlato di alcol? Chistu è 'U Spiritu ru Fikudinnia'. Viola come il livido, dolce come il perdono. Non ubriaca la testa... sveglia il cuore."),
                    ("Turiddu", "Mizzica ma è viola! Pare evidenziatore sciolto... però profuma bello."),
                    ("Rosalia", "Turiddu, smettila di annusare tutto! ...Però, oh, ha un odore incredibile davvero."),
                    ("U Strammu", "Non voglio soldi, picciotti. I soldi sono carta sporca. Io voglio un brindisi. Un brindisi alla nostra terra, che ci fa dannare e ci fa innamorare."),
                    ("Rosalia", "Un brindisi? E basta? Mi puzza di fregatura..."),
                    ("Turiddu", "(Ha già il bicchiere in mano) Finchè è gratis… Alla salute!"),
                    ("Rosalia", "Turiddu! ...Vabbè, ormai. Facciamo 'sto brindisi e scappiamo. Offre la casa?"),
                    ("U Strammu", "(Sorride sotto i baffi) Alzate i calici. Guardate il sole che muore. Si dice: Alla Calata!. Perché tutto quello che scende... poi deve risalire."),
                    ("Tutti Insieme", "ALLA CALATA!"),
                    ("Turiddu", "Mmmh! Buonis... simo... Però... mi sento... strano..."),
                    ("Rosalia", "Perché le luci si stanno sciogliendo? Sembrano... cera..."),
                    ("Turiddu", "La testa... mi pisa... Rosalia? Perché sei sfuocata?"),
                    ("Rosalia", "Il pavimento... è diventato... fango? Non mi reggo..."),
                    ("U Strammu", "Buon viaggio, picciotti.")
                ]
            },
            {
                "bg": "intro_scena3",
                "duration": 2.0, # Secondi di attesa senza dialoghi
                "layout": "none",
                "dialogues": [] 
            }
        ]
        
        self.current_slide_idx = 0
        self.current_dialogue_idx = 0
        self.timer = 0.0
        self.state = "FADE_IN"
        self.fade_alpha = 255
        
        # Start Music if present
        first_slide = self.slides[0]
        if "music" in first_slide:
            game.audio.play_bgm(first_slide["music"], fade_ms=2000, loop=True)

    def exit(self, next_state=None):
        pass

    def handle_event(self, event) -> bool:
        if self.state == "PLAYING":
            slide = self.slides[self.current_slide_idx]
            has_dialogue = self.current_dialogue_idx < len(slide["dialogues"])
            
            if has_dialogue:
                if event.type == pygame.KEYDOWN and event.key in (pygame.K_SPACE, pygame.K_RETURN):
                    self.current_dialogue_idx += 1
                    return True
        return False

    def update(self, dt: float):
        if self.state == "FADE_IN":
            self.fade_alpha -= self.fade_speed * dt
            if self.fade_alpha <= 0:
                self.fade_alpha = 0
                self.state = "PLAYING"
                
        elif self.state == "PLAYING":
            slide = self.slides[self.current_slide_idx]
            
            # Check if dialogues finished
            if self.current_dialogue_idx >= len(slide["dialogues"]):
                # If there is a duration wait, handle it
                if "duration" in slide:
                    self.timer += dt
                    if self.timer >= slide["duration"]:
                        self.state = "FADE_OUT"
                else:
                    # Immediate transition if no duration and dialogues done
                    self.state = "FADE_OUT"
        
        elif self.state == "FADE_OUT":
            self.fade_alpha += self.fade_speed * dt
            if self.fade_alpha >= 255:
                self.fade_alpha = 255
                self._next_slide()

    def _next_slide(self):
        self.current_slide_idx += 1
        if self.current_slide_idx < len(self.slides):
            self.current_dialogue_idx = 0
            self.timer = 0
            self.state = "FADE_IN"
            
            # Check music change
            next_slide = self.slides[self.current_slide_idx]
            if "music" in next_slide:
                self._state_machine.controller.game.audio.play_bgm(next_slide["music"], fade_ms=1000)
        else:
            # End of Cutscene -> Goto Hub
            self._state_machine.controller.game.audio.stop_bgm(fade_ms=2000)
            self._state_machine.change_state(StateID.HUB, spawn_id="default")

    def render(self, surface):
        slide = self.slides[self.current_slide_idx]
        
        # 1. Draw Background
        bg_img = self.assets.get_image(slide["bg"], self.SCREEN_W, self.SCREEN_H, fallback_type="background")
        if bg_img:
            surface.blit(bg_img, (0, 0))
        else:
            surface.fill((20, 20, 20))
        
        # 2. Draw Characters (Portraits)
        layout = slide.get("layout", "none")
        
        def draw_portrait(name, x, y, flip=False):
            # Richiediamo le dimensioni ESPLICITE Portrait per evitare lo stretch quadrato
            img = self.assets.get_image(f"characters/{name}", self.PORTRAIT_W, self.PORTRAIT_H, fallback_type="npc")
            
            if not img: return

            if flip:
                img = pygame.transform.flip(img, True, False)
            
            # Disegna semplice, nessuna trasparenza/dimming come richiesto
            surface.blit(img, (x, y))

        # Calcolo Y allineata in basso sopra il box di testo
        char_y = self.SCREEN_H - self.TEXT_BOX_H - self.PORTRAIT_H + 20

        if layout == "turiddu_left_rosalia_right":
            draw_portrait("Turiddu", 20, char_y, flip=False)
            draw_portrait("Rosalia", self.SCREEN_W - self.PORTRAIT_W - 20, char_y, flip=True)
            
        elif layout == "strammu_left_party_right":
            # U Strammu a sinistra
            draw_portrait("U Strammu", 20, char_y, flip=False)
            
            # Party a destra (Turiddu e Rosalia vicini)
            # Rosalia più esterna
            r_x = self.SCREEN_W - self.PORTRAIT_W - 20
            # Turiddu un po' prima di Rosalia
            t_x = r_x - 180 
            
            draw_portrait("Turiddu", t_x, char_y, flip=True)
            draw_portrait("Rosalia", r_x, char_y, flip=True)

        # 3. Draw Dialogue Box
        if self.current_dialogue_idx < len(slide["dialogues"]):
            speaker, text = slide["dialogues"][self.current_dialogue_idx]
            
            # Box Rect
            box_rect = pygame.Rect(50, self.SCREEN_H - self.TEXT_BOX_H - 20, self.SCREEN_W - 100, self.TEXT_BOX_H)
            
            # Sfondo nero pieno (leggibilità massima)
            pygame.draw.rect(surface, (0, 0, 0), box_rect)
            pygame.draw.rect(surface, (255, 255, 255), box_rect, 2)
            
            # Speaker Name
            name_surf = self.font_name.render(speaker, True, (255, 215, 0)) # Gold
            surface.blit(name_surf, (box_rect.x + 20, box_rect.y + 15))
            
            # Text Wrapping
            words = text.split(' ')
            lines = []
            curr_line = []
            for word in words:
                curr_line.append(word)
                if self.font.size(' '.join(curr_line))[0] > box_rect.width - 40:
                    curr_line.pop()
                    lines.append(' '.join(curr_line))
                    curr_line = [word]
            lines.append(' '.join(curr_line))
            
            for i, line in enumerate(lines):
                line_surf = self.font.render(line, True, (255, 255, 255))
                surface.blit(line_surf, (box_rect.x + 20, box_rect.y + 50 + i * 25))
                
            # Hint
            hint_surf = self.font.render("Premere [SPAZIO]", True, (150, 150, 150))
            surface.blit(hint_surf, (box_rect.right - 180, box_rect.bottom - 30))

        # 4. Fade Overlay
        if self.fade_alpha > 0:
            fade_s = pygame.Surface((self.SCREEN_W, self.SCREEN_H))
            fade_s.fill((0, 0, 0))
            fade_s.set_alpha(int(self.fade_alpha))
            surface.blit(fade_s, (0, 0))

# --- EXPLORATION STATE (ROOM) ---
class RoomState(BaseState):
    def __init__(self, state_machine=None):
        super().__init__(StateID.ROOM, state_machine)
        self.room_id = None
        self.spawn_id = None
        self.move_speed = 200.0
        
        # MODIFICA: Aumentato da 90.0 a 140.0 per facilitare l'interazione col Carretto
        self.interaction_range = 140.0 
        
    def enter(self, prev_state: BaseState = None, **kwargs):
        self.room_id = kwargs.get('room_id')
        self.spawn_id = kwargs.get('spawn_id', 'default')
        logger.info(f"Entered RoomState: {self.room_id} at {self.spawn_id}")
        
        try:
            render_ctrl = self._state_machine.controller.render_controller
            game = self._state_machine.controller.game
            
            room_data = game.content.get("rooms", self.room_id)
            if room_data:
                render_ctrl.load_room(room_data, self.spawn_id)
                spawn_pos = room_data.get_spawn_position(self.spawn_id)
                
                game.gamestate.party_position = list(spawn_pos)
                game.gamestate.current_room_id = self.room_id
                
                active = game.gamestate.get_active_player()
                if active:
                    active.x, active.y = spawn_pos
                
                if room_data.is_checkpoint:
                    game.gamestate.set_checkpoint()
                    game.prompts.show_info("Checkpoint Reached", 0, 1500)
                
                # Check immediato dei trigger all'ingresso
                player_rect = pygame.Rect(spawn_pos[0], spawn_pos[1], 32, 32)
                self._check_triggers(game, player_rect, on_enter=True)

        except AttributeError:
            pass

    def exit(self, next_state: BaseState = None):
        pass
        
    def handle_event(self, event) -> bool:
        input_manager = self._state_machine.controller.input_manager
        
        if event.type == pygame.KEYDOWN:
            if event.key == pygame.K_i:
                self._state_machine.push_state(StateID.INVENTORY)
                return True
            if event.key == pygame.K_a:
                self._state_machine.push_state(StateID.ACES_MENU)
                return True
            
            if input_manager.was_just_pressed(Action.PAUSE):
                self._state_machine.push_state(StateID.PAUSE)
                return True
            
            # --- FIX: BLOCCO CAMBIO GIOCATORE FUORI DALL'HUB ---
            if input_manager.was_just_pressed(Action.NEXT_CHARACTER):
                if self.room_id == "hub":
                    self._state_machine.controller.trigger_next_turn()
                else:
                    self._state_machine.controller.game.prompts.show_info("Non puoi cambiare qui!", 0, 1000)
                return True
            # ---------------------------------------------------

            if event.key == pygame.K_F1: 
                self._state_machine.change_state(StateID.COMBAT, encounter_id="debug_encounter")
                return True
            if event.key == pygame.K_F2:
                self._state_machine.change_state(StateID.SCOPA)
                return True

        return False 

    def update(self, dt: float):
        try:
            game = self._state_machine.controller.game
            input_manager = self._state_machine.controller.input_manager
            render_ctrl = self._state_machine.controller.render_controller
            action_runner = self._state_machine.controller.action_runner
        except AttributeError:
            return 
        
        if action_runner.is_running():
            action_runner.update(dt)
            return
        
        self._handle_movement(dt, game, input_manager, render_ctrl)
        self._update_focus(game)

        if input_manager.was_just_pressed(Action.INTERACT):
            self._handle_interaction(game, action_runner)

    def _handle_movement(self, dt, game, input_manager, render_ctrl):
        dx, dy = 0, 0
        if input_manager.is_down(Action.MOVE_UP): dy -= 1
        if input_manager.is_down(Action.MOVE_DOWN): dy += 1
        if input_manager.is_down(Action.MOVE_LEFT): dx -= 1
        if input_manager.is_down(Action.MOVE_RIGHT): dx += 1

        if dx != 0 or dy != 0:
            length = (dx**2 + dy**2)**0.5
            dx, dy = dx/length, dy/length
            current_pos = game.gamestate.party_position
            target_x = current_pos[0] + dx * self.move_speed * dt
            target_y = current_pos[1] + dy * self.move_speed * dt
            
            player_rect = pygame.Rect(target_x, target_y, 32, 32)
            room_data = game.content.get("rooms", game.gamestate.current_room_id)
            
            if room_data and not room_data.check_collision(player_rect):
                # Aggiorna posizione globale (per camera e trigger)
                game.gamestate.party_position = [target_x, target_y]
                
                # --- AGGIORNA POSIZIONE INDIVIDUALE ---
                active_char = game.gamestate.get_active_player()
                if active_char:
                    active_char.x = target_x
                    active_char.y = target_y
                # --------------------------------------

                if render_ctrl:
                    render_ctrl.update_camera(int(target_x), int(target_y), dt)
                self._check_triggers(game, player_rect, on_enter=True)

    def _update_focus(self, game):
        room_data = game.content.get("rooms", game.gamestate.current_room_id)
        if not room_data: return
        px, py = game.gamestate.party_position
        player_rect = pygame.Rect(px, py, 32, 32)

        nearest = room_data.get_closest_interactable(px, py, self.interaction_range)
        
        if not nearest:
            triggers = room_data.check_triggers(player_rect)
            for t in triggers:
                if t.trigger_type == "script" and t.label:
                    nearest = EntityDefinition(
                        entity_id=f"trigger_{t.id}",
                        entity_type="interactable",
                        x=t.rect.x, y=t.rect.y,
                        width=t.rect.width, height=t.rect.height,
                        script_id=t.script_id,
                        interaction_label=t.label
                    )
                    break

        game.gamestate.active_interactable = nearest

    def _handle_interaction(self, game, action_runner):
        current_pos = game.gamestate.party_position
        player_rect = pygame.Rect(current_pos[0], current_pos[1], 32, 32)
        if self._check_triggers(game, player_rect, on_enter=False):
            return
        active_obj = game.gamestate.active_interactable
        if not active_obj: return
        if active_obj.actions:
            self._state_machine.push_state(
                StateID.INTERACTION_MENU,
                title=active_obj.interaction_label or active_obj.entity_id,
                options=active_obj.actions
            )
            return
        if active_obj.script_id:
            action_runner.run_script_by_id(active_obj.script_id)

    def _check_triggers(self, game, player_rect, on_enter: bool):
        room_data = game.content.get("rooms", game.gamestate.current_room_id)
        if not room_data: return False
        triggers = room_data.check_triggers(player_rect)
        action_runner = self._state_machine.controller.action_runner
        
        for trigger in triggers:
            if trigger.trigger_type == "script":
                
                # --- FIX: Se c'è già uno script in esecuzione, ignora nuovi trigger automatici ---
                if action_runner.is_running():
                    return False
                # -------------------------------------------------------------------------------

                # 1. Auto Trigger (No Confirm)
                if on_enter and trigger.auto_trigger:
                    action_runner.run_script_by_id(trigger.script_id)
                    return True
                
                # 2. Trigger on Enter with Confirm (FIX: Use script prompt)
                if on_enter and trigger.requires_confirm:
                    self._trigger_script_prompt(trigger, action_runner, game)
                    return True

                # 3. Manual Interaction
                if not on_enter and not trigger.auto_trigger:
                    action_runner.run_script_by_id(trigger.script_id)
                    return True
            
            # --- LEGACY SUPPORT FOR 'EXIT' TYPE ---
            elif trigger.trigger_type == "exit":
                # (Il resto del codice exit rimane invariato...)
                if trigger.data.get("gate_mode") == "aurion_choice":
                    already = game.get_flag("aurion_starter_received")
                    path_flag = trigger.data.get("path_flag")
                    allowed = (not already) or (path_flag and game.get_flag(path_flag))

                    if not allowed:
                        msg = trigger.data.get("blocked_msg", "Non puoi più entrare in questa porta.")
                        game.prompts.show_info(msg, 0, 2500)
                        return True

                req_flag = trigger.data.get("req_flag")
                if req_flag and not game.get_flag(req_flag):
                    locked_msg = trigger.data.get("locked_msg", "Locked.")
                    game.prompts.show_info(locked_msg, 0, 2000)
                    return True 
                
                if trigger.requires_confirm:
                    if on_enter:
                        self._trigger_exit_prompt(trigger, game)
                        return True
                else:
                    if on_enter or (not on_enter):
                        self._perform_room_change(trigger, game)
                        return True
        return False
    
    def _trigger_script_prompt(self, trigger, action_runner, game):
        """Prompt per trigger di tipo 'script'."""
        def on_yes(): 
            action_runner.run_script_by_id(trigger.script_id)
        def on_no(): 
            pass
            
        game.prompts.show_confirm(message=trigger.prompt_text or "Interagire?", on_yes=on_yes, on_no=on_no)

    def _trigger_exit_prompt(self, trigger, game):
        def on_yes(): self._perform_room_change(trigger, game)
        def on_no(): pass
        game.prompts.show_confirm(message=trigger.prompt_text or "Change area?", on_yes=on_yes, on_no=on_no)

    def _perform_room_change(self, trigger, game):
        target_room = trigger.target_room
        target_spawn = trigger.target_spawn
        game.gamestate.current_room_id = target_room
        self._state_machine.change_state(StateID.ROOM, room_id=target_room, spawn_id=target_spawn)

    def render(self, surface):
        pass

# --- HUB STATE ---
class HubState(RoomState):
    def __init__(self, state_machine=None):
        # FIX: Chiamata al super costruttore corretta (solo state_machine)
        super().__init__(state_machine)
        self._state_id = StateID.HUB 
        self.current_area = None
        
    def enter(self, prev_state: BaseState = None, **kwargs):
        super().enter(prev_state, room_id="hub", spawn_id=kwargs.get('spawn_id', 'default'))
        self.current_area = kwargs.get('area_id', 'hub_main')

# --- COMBAT STATE ---
class CombatState(BaseState):
    PHASE_START_TURN = 0
    PHASE_INPUT = 1
    PHASE_TARGETING = 2
    PHASE_EXECUTE_ACTION = 3
    PHASE_CHECK_OUTCOME = 4
    PHASE_END_TURN = 5

    def __init__(self, state_machine=None):
        super().__init__(StateID.COMBAT, state_machine)
        self.turn_manager = TurnManager()
        self.battle_ctx = None
        self.menu_state = CombatMenuState()
        self.encounter = None
        self.phase = self.PHASE_START_TURN
        self.action_timer = 0.0
        self.rng = RNG()
        self.damage_calculator = DamageCalculator(self.rng)
        self.enemy_brains = {}
        self.pipeline = ActionPipeline(self.damage_calculator, self.rng)
        self.boss_oste_model = None 

    def enter(self, prev_state: BaseState = None, **kwargs):
        encounter_id = kwargs.get('encounter_id', 'debug_encounter')
        seed = kwargs.get('seed')
        if seed is not None: self.rng.set_seed(seed)
        
        game = self._state_machine.controller.game
        
        self.encounter = Encounter(encounter_id, self._get_enemies_for_encounter(encounter_id))
        enemies = []
        for i, eid in enumerate(self.encounter.enemy_ids):
            if "Don Tanino" in eid:
                boss_hp = 200
                boss_atk = 12 
                if game.get_flag("aurion_boss_weakened"):
                    game.prompts.show_info("Don Tanino è indebolito dal Dossier!", 0, 3000)
                    boss_hp = 100
                    boss_atk = 8 
                e = Enemy("Don Tanino", boss_hp, boss_hp, boss_atk, 8, spd=6, ai_behavior="don_tanino")
                self.enemy_brains[e] = DonTaninoBrain()
                
            elif "Oste Eterno" in eid: # FINALE
                self.boss_oste_model = BossOste()
                hp = 100 
                e = Enemy("L'Oste Eterno", hp, hp, 15, 10, spd=5, ai_behavior="oste_eterno")
                # === FIX IMPORTANTE: COLLEGARE IL MODELLO LOGICO ALL'ENTITA' NEMICA ===
                e.custom_model = self.boss_oste_model 
                # ======================================================================
                self.enemy_brains[e] = BossOsteBrain(self.boss_oste_model)
                game.prompts.show_info("FASE 1: AVIDITÀ", 0, 3000)
                
            else:
                e = Enemy(f"{eid} {i+1}", 40, 40, 8, 2, spd=2, ai_behavior="aggressive")
                self.enemy_brains[e] = EnemyBrain("aggressive")
            enemies.append(e)
            
        active_party = game.gamestate.party.get_enabled_characters()
        self.battle_ctx = BattleContext(encounter_id, active_party, enemies)

        self.turn_manager.start_battle(self.battle_ctx.get_all_participants())
        self.menu_state.reset()
        game.enter_combat()
        self.phase = self.PHASE_START_TURN

    def _get_enemies_for_encounter(self, enc_id):
        if enc_id == "aurion_guards_fight": return ["Elite Guard A", "Elite Guard B"]
        if enc_id == "ferrum_golem_fight": return ["Scrap Golem"]
        if enc_id == "vinalia_colapesce_fight": return ["Colapesce Avatar"]
        if "viridor_sphinx" in enc_id: return ["Sphinx Guardian"]
        if enc_id == "boss_tanino": return ["Don Tanino"]
        if enc_id == "boss_oste_eterno": return ["L'Oste Eterno"] # Finale
        return ["Goblin", "Orc"]

    def exit(self, next_state: BaseState = None):
        self._state_machine.controller.game.exit_combat()

    def handle_event(self, event) -> bool:
        if self.phase not in (self.PHASE_INPUT, self.PHASE_TARGETING): return False
        input_manager = self._state_machine.controller.input_manager
        
        if self.phase == self.PHASE_INPUT:
            if input_manager.was_just_pressed(Action.MENU_DOWN): self.menu_state.move_cursor(1); return True
            if input_manager.was_just_pressed(Action.MENU_UP): self.menu_state.move_cursor(-1); return True
            if input_manager.was_just_pressed(Action.CONFIRM): self._handle_action_selection(); return True
            
        elif self.phase == self.PHASE_TARGETING:
            if input_manager.was_just_pressed(Action.CANCEL): self.menu_state.reset(); self.phase = self.PHASE_INPUT; return True
            if input_manager.was_just_pressed(Action.MENU_DOWN) or input_manager.was_just_pressed(Action.MENU_RIGHT): self.menu_state.move_cursor(1); return True
            if input_manager.was_just_pressed(Action.MENU_UP) or input_manager.was_just_pressed(Action.MENU_LEFT): self.menu_state.move_cursor(-1); return True
            if input_manager.was_just_pressed(Action.CONFIRM): self._handle_target_confirmation(); return True
        return False

    def _handle_action_selection(self):
        selected = self.menu_state.get_selected_option()
        if selected == "Attack": self.menu_state.pending_action = {"type": "attack", "name": "Basic Attack", "power": 10, "scope": TargetingSystem.SCOPE_SINGLE_ENEMY}
        elif selected == "Defend": self.menu_state.pending_action = {"type": "defend", "name": "Defend", "scope": TargetingSystem.SCOPE_SELF}
        elif selected == "Flee": 
            if self.encounter.encounter_id == "boss_oste_eterno":
                self._state_machine.controller.game.prompts.show_info("Non puoi fuggire dall'Eternità!", 0, 2000)
                return
            self._state_machine.change_state(StateID.HUB); return
        
        if self.menu_state.pending_action:
            active = self.turn_manager.active_actor()
            cands = TargetingSystem.get_candidates(self.menu_state.pending_action["scope"], active, self.battle_ctx)
            if cands: self.menu_state.start_target_selection(cands); self.phase = self.PHASE_TARGETING

    def _handle_target_confirmation(self):
        action = self.menu_state.pending_action
        cursor = self.menu_state.get_current_target()
        final = TargetingSystem.resolve_final_targets(action["scope"], cursor, self.menu_state.valid_targets, self.rng)
        active = self.turn_manager.active_actor()
        
        logs = self.pipeline.execute(active, final, action)
        
        # OSTE ETERNO: CHECK DAMAGE & PHASE
        if self.boss_oste_model and any(t.name == "L'Oste Eterno" for t in final):
            for t in final:
                if t.name == "L'Oste Eterno":
                    self.boss_oste_model.hp = t.hp
                    if self.boss_oste_model.check_phase_transition():
                        desc = self.boss_oste_model.get_phase_description()
                        self._state_machine.controller.game.prompts.show_info(f"{desc}!", 0, 3000)
                        
                        t.hp = self.boss_oste_model.hp
                        t.max_hp = self.boss_oste_model.max_hp
                        
                        if self.boss_oste_model.is_immortal:
                            self._trigger_fake_victory()
                            return

        for l in logs: self._state_machine.controller.game.prompts.show_info(l, 0, 1500)
        self.menu_state.reset(); self.phase = self.PHASE_EXECUTE_ACTION

    def _trigger_fake_victory(self):
        self._state_machine.controller.game.prompts.show_info("L'OSTE DIVENTA IMMORTALE!", 0, 4000)
        self._state_machine.push_state(StateID.DIALOGUE, dialogue_data=[
            {"speaker": "L'Oste Eterno", "text": "Illusi. Credete davvero di aver vinto?"},
            {"speaker": "L'Oste Eterno", "text": "Io sono l'Eternità. Non ho inizio, non ho fine."},
            {"speaker": "Narratore", "text": "L'Oste si rialza avvolto da una luce bianca."},
        ])
        # Al termine del dialogo (o dopo un timer/input successivo), si attiverà la scelta.
        # Per ora lasciamo che il combattimento continui in modalità "invincibile" finché il giocatore non muore o interviene un altro evento.
        # In una versione rifinita, collegheremmo la fine del dialogo direttamente alla scelta.
        # Qui usiamo un workaround rapido: aggiungiamo un evento "Check Final Choice" nel loop.
        self.battle_ctx.enemies[0].is_immortal = True # Flag visivo locale

    def _trigger_final_choice(self):
        def use_bazooka():
            self._state_machine.controller.game.etna.make_final_choice("bad")
            self._state_machine.change_state(StateID.CREDITS, ending="bad")
        def sit_table():
            self._state_machine.controller.game.etna.make_final_choice("true")
            self._state_machine.change_state(StateID.CREDITS, ending="true")

        from src.model.ui.prompts import PromptChoice
        options = [
            PromptChoice(label="USA CANNOLO BAZOOKA", value=use_bazooka),
            PromptChoice(label="SIEDITI AL TAVOLO", value=sit_table)
        ]
        self._state_machine.push_state(StateID.PROMPT, prompt_type="choice", title="SCELTA FINALE", options=options, on_select=lambda cb: cb())

    def update(self, dt: float):
        if not self.battle_ctx: return
        
        # Check speciale per il finale
        if self.boss_oste_model and self.boss_oste_model.is_immortal and self.phase == self.PHASE_INPUT:
             # Se il boss è immortale e tocca al giocatore, forza la scelta finale invece del menu di combattimento
             self._trigger_final_choice()
             return

        if self.phase == self.PHASE_START_TURN:
            active = self.turn_manager.next_turn()
            if not active: return
            if not getattr(active, "is_alive", True): self.phase = self.PHASE_START_TURN; return
            
            if active in self.battle_ctx.party: 
                self.menu_state.reset(); self.phase = self.PHASE_INPUT
            else: 
                brain = self.enemy_brains.get(active)
                if brain:
                    act = brain.decide_action(active, self.battle_ctx.party[0], 0)
                    if act:
                        logs = self.pipeline.execute(active, [act['target']] if act.get('target') else [], act.get('move', {}))
                        for l in logs: self._state_machine.controller.game.prompts.show_info(l, 0, 2000)
                self.phase = self.PHASE_EXECUTE_ACTION
                
        elif self.phase == self.PHASE_EXECUTE_ACTION:
            self.action_timer += dt
            if self.action_timer > 1.0: 
                self.action_timer = 0; self.phase = self.PHASE_CHECK_OUTCOME
                
        elif self.phase == self.PHASE_CHECK_OUTCOME:
            if not self.battle_ctx.get_living_enemies(): self._resolve_victory()
            elif not self.battle_ctx.get_living_party(): self._trigger_game_over()
            else: self.phase = self.PHASE_END_TURN
            
        elif self.phase == self.PHASE_END_TURN: 
            self.phase = self.PHASE_START_TURN

    def _resolve_victory(self):
        game = self._state_machine.controller.game
        game.prompts.show_info("VICTORY!", 0, 2000)
        enc_id = self.encounter.encounter_id
        if enc_id == "aurion_guards_fight":
            self._state_machine.controller.game.gamestate.current_room_id = "aurion_boss_room"
            self._state_machine.change_state(StateID.ROOM, room_id="aurion_boss_room", spawn_id="bottom")
        elif enc_id == "ferrum_golem_fight":
            self._state_machine.controller.game.gamestate.current_room_id = "ferrum_boss_room"
            self._state_machine.change_state(StateID.ROOM, room_id="ferrum_boss_room", spawn_id="bottom")
        elif "viridor_sphinx" in enc_id:
            self._state_machine.controller.game.gamestate.current_room_id = "viridor_boss_room"
            self._state_machine.change_state(StateID.ROOM, room_id="viridor_boss_room", spawn_id="bottom")
        elif enc_id == "vinalia_colapesce_fight":
            self._state_machine.controller.game.gamestate.current_room_id = "vinalia_boss_room"
            self._state_machine.change_state(StateID.ROOM, room_id="vinalia_boss_room", spawn_id="bottom")
        elif "Don Tanino" in self.encounter.enemy_ids:
            game.aurion.on_boss_victory()
        else:
            self._state_machine.controller.game.gamestate.current_room_id = "hub"
            self._state_machine.change_state(StateID.HUB)

    def _trigger_game_over(self):
        self._state_machine.change_state(StateID.GAME_OVER)

    def render(self, surface): pass

# --- SCOPA MINIGAME STATE ---
class ScopaState(BaseState):
    def __init__(self, state_machine=None):
        super().__init__(StateID.SCOPA, state_machine)
        self.model = ScopaModel()
        self.view = None
        self.cursor = {
            'area': 'hand', 
            'index': 0,
            'selected_hand_index': None, 
            'capture_options': [], 
            'option_index': 0 
        }
        self.timer = 0.0
        self.game_over_timer = 0.0

    def enter(self, prev_state=None, **kwargs):
        game = self._state_machine.controller.game
        self.view = ScopaView(self._state_machine.controller.render_controller.renderer, game.settings.audio)
        self.view.assets = self._state_machine.controller.render_controller.asset_manager
        
        self.model.start_game()
        self.cursor = {'area': 'hand', 'index': 0, 'selected_hand_index': None}
        game.enter_combat()

    def exit(self, next_state=None):
        self._state_machine.controller.game.exit_combat()

    def handle_event(self, event) -> bool:
        if self.model.is_game_over():
            if event.type == pygame.KEYDOWN:
                winner = self.model.get_winner()
                game = self._state_machine.controller.game
                
                if winner == "player":
                    game.aurion.on_boss_victory()
                    self._state_machine.controller.game.gamestate.current_room_id = "hub"
                    self._state_machine.change_state(StateID.HUB, spawn_id="from_aurion")
                else:
                    self._state_machine.change_state(
                        StateID.GAME_OVER, 
                        retry_room="aurion_boss_room", 
                        retry_spawn="bottom"
                    )
                return True
            return False

        input_manager = self._state_machine.controller.input_manager
        
        if self.model.state == "CPU_TURN":
            return False

        if input_manager.was_just_pressed(Action.MENU_RIGHT):
            self._move_cursor(1)
            return True
        if input_manager.was_just_pressed(Action.MENU_LEFT):
            self._move_cursor(-1)
            return True
        
        if self.cursor['selected_hand_index'] is not None:
             if input_manager.was_just_pressed(Action.MENU_UP):
                 self.cursor['area'] = 'table'
                 self.cursor['index'] = 0
                 return True
             if input_manager.was_just_pressed(Action.MENU_DOWN):
                 self.cursor['area'] = 'hand'
                 self.cursor['index'] = self.cursor['selected_hand_index']
                 return True

        if input_manager.was_just_pressed(Action.CONFIRM):
            self._handle_confirm()
            return True
            
        if input_manager.was_just_pressed(Action.CANCEL):
            if self.cursor['selected_hand_index'] is not None:
                self.cursor['selected_hand_index'] = None
                self.cursor['area'] = 'hand'
            return True

        return False

    def _move_cursor(self, delta):
        if self.cursor['area'] == 'hand':
            n = len(self.model.mano_player)
            if n > 0: self.cursor['index'] = (self.cursor['index'] + delta) % n
        elif self.cursor['area'] == 'table':
            pass 

    def _handle_confirm(self):
        if self.cursor['area'] == 'hand':
            idx = self.cursor['index']
            self.cursor['selected_hand_index'] = idx
            
            card = self.model.mano_player[idx]
            analysis = self.model.analizza_presa(card)
            
            if analysis['tipo'] == 'calata':
                msg = self.model.play_card(idx)
                self.cursor['selected_hand_index'] = None
                self.cursor['index'] = 0
            else:
                msg = self.model.play_card(idx, 0)
                self.cursor['selected_hand_index'] = None
                self.cursor['index'] = 0

    def update(self, dt: float):
        if self.model.state == "CPU_TURN":
            self.timer += dt
            if self.timer > 1.5:
                self.model.cpu_turn()
                self.timer = 0
                if self.model.mano_player:
                    self.cursor = {'area': 'hand', 'index': 0, 'selected_hand_index': None}

    def render(self, surface):
        if not self.view: return
        
        if self.model.is_game_over():
            winner = self.model.get_winner()
            sp, sc = self.model.calculate_stats()
            self.view.render_game_over(surface.get_size(), winner, sp, sc)
        else:
            self.view.render(surface.get_size(), self.model, self.cursor)

# --- INVENTORY STATE ---
class InventoryState(BaseState):
    def __init__(self, state_machine=None):
        super().__init__(StateID.INVENTORY, state_machine)
        self._render_below = True
        self.selected_index = 0
        self.game = None

    def enter(self, prev_state=None, **kwargs):
        self.game = self._state_machine.controller.game
        self.selected_index = 0

    def exit(self, next_state=None):
        pass

    def handle_event(self, event) -> bool:
        input_manager = self._state_machine.controller.input_manager
        
        if input_manager.was_just_pressed(Action.MENU_DOWN):
            self.selected_index += 1
            return True
        if input_manager.was_just_pressed(Action.MENU_UP):
            self.selected_index = max(0, self.selected_index - 1)
            return True
        if input_manager.was_just_pressed(Action.CANCEL) or input_manager.was_just_pressed(Action.PAUSE):
            self._state_machine.pop_state()
            return True
        if input_manager.was_just_pressed(Action.CONFIRM):
            self._state_machine.controller.game.prompts.show_info("Item used!", 0, 1000)
            return True
        return True

    def update(self, dt: float): pass
    def render(self, surface): pass

# --- PAUSE STATE ---
class PauseState(BaseState):
    def __init__(self, state_machine=None): 
        super().__init__(StateID.PAUSE, state_machine)
        self._render_below = True
        self.cursor_index = 0
        self.options = ["Resume", "Save", "Quit"]

    def enter(self, prev_state=None, **kwargs): self.cursor_index = 0
    def exit(self, next_state=None): pass

    def handle_event(self, event) -> bool:
        input_manager = self._state_machine.controller.input_manager
        if input_manager.was_just_pressed(Action.MENU_DOWN): self.cursor_index = (self.cursor_index + 1) % len(self.options); return True
        if input_manager.was_just_pressed(Action.MENU_UP): self.cursor_index = (self.cursor_index - 1) % len(self.options); return True
        if input_manager.was_just_pressed(Action.CONFIRM):
            if self.cursor_index == 0: self._state_machine.pop_state()
            elif self.cursor_index == 1: self._state_machine.push_state(StateID.SAVE_LOAD, mode="save")
            elif self.cursor_index == 2: self._state_machine.change_state(StateID.MAIN_MENU)
            return True
        if input_manager.was_just_pressed(Action.CANCEL): self._state_machine.pop_state(); return True
        return True
    def update(self, dt: float): pass
    def render(self, surface): pass

# --- SAVE LOAD STATE ---
class SaveLoadState(BaseState):
    def __init__(self, state_machine=None):
        super().__init__(StateID.SAVE_LOAD, state_machine)
        self._render_below = True
        self.mode = "save"
        self.slots = []
        self.cursor_index = 0
        
        self.is_input_active = False
        self.input_text = ""
        self.target_slot = -1

    def enter(self, prev_state=None, **kwargs):
        self.mode = kwargs.get('mode', 'save')
        self.slots = self._state_machine.controller.save_manager.list_slots()
        self.cursor_index = 0
        self.is_input_active = False
        self.input_text = ""

    def exit(self, next_state=None): pass

    def handle_event(self, event) -> bool:
        if self.is_input_active:
            if event.type == pygame.KEYDOWN:
                if event.key == pygame.K_RETURN:
                    self._perform_save(self.target_slot, self.input_text)
                    self.is_input_active = False
                    return True
                elif event.key == pygame.K_ESCAPE:
                    self.is_input_active = False
                    return True
                elif event.key == pygame.K_BACKSPACE:
                    self.input_text = self.input_text[:-1]
                else:
                    if len(self.input_text) < 15 and event.unicode.isprintable():
                        self.input_text += event.unicode
            return True

        input_manager = self._state_machine.controller.input_manager
        limit = len(self.slots) + 1
        
        if input_manager.was_just_pressed(Action.MENU_DOWN): 
            self.cursor_index = (self.cursor_index + 1) % limit
            return True
        if input_manager.was_just_pressed(Action.MENU_UP): 
            self.cursor_index = (self.cursor_index - 1) % limit
            return True
            
        if input_manager.was_just_pressed(Action.CONFIRM):
            if self.cursor_index == len(self.slots): 
                self._state_machine.pop_state()
            else: 
                self._handle_slot_selection()
            return True
            
        if input_manager.was_just_pressed(Action.CANCEL): 
            self._state_machine.pop_state()
            return True
        return True

    def _handle_slot_selection(self):
        controller = self._state_machine.controller
        if self.cursor_index < len(self.slots):
            slot_idx = self.slots[self.cursor_index].slot_index
        else:
            return

        if self.mode == "save":
            self.is_input_active = True
            self.target_slot = slot_idx
            self.input_text = f"Save {slot_idx}" 
            
        elif self.mode == "load":
            res = controller.load_game(slot_idx)
            if res.ok:
                controller.game.prompts.show_info("Loaded!", 0, 1000)
                self._state_machine.pop_state() 
            else:
                controller.game.prompts.show_info(f"Err: {res.message}", 0, 2000)

    def _perform_save(self, slot_idx, name):
        controller = self._state_machine.controller
        res = controller.save_game(slot_idx, confirmed=True, custom_name=name)
        if res.ok:
            controller.game.prompts.show_info("Saved!", 0, 1500)
            self.slots = controller.save_manager.list_slots()
        else:
            controller.game.prompts.show_info(f"Error: {res.message}", 0, 2000)

    def update(self, dt: float): pass
    def render(self, surface): pass

# --- GAME OVER STATE ---
class GameOverState(BaseState):
    def __init__(self, state_machine=None): 
        super().__init__(StateID.GAME_OVER, state_machine)
        self.cursor_index = 0
        self.options = ["Riprova Battaglia", "Menu Principale"]
        self.retry_room = None
        self.retry_spawn = None

    def enter(self, prev_state=None, **kwargs): 
        self.cursor_index = 0
        self.retry_room = kwargs.get('retry_room')
        self.retry_spawn = kwargs.get('retry_spawn')

    def exit(self, next_state=None): pass

    def handle_event(self, event) -> bool:
        input_manager = self._state_machine.controller.input_manager
        
        if input_manager.was_just_pressed(Action.MENU_DOWN): 
            self.cursor_index = (self.cursor_index + 1) % 2
            return True
        if input_manager.was_just_pressed(Action.MENU_UP): 
            self.cursor_index = (self.cursor_index - 1) % 2
            return True
            
        if input_manager.was_just_pressed(Action.CONFIRM):
            if self.cursor_index == 0: 
                game = self._state_machine.controller.game
                room_to_load = self.retry_room or game.gamestate.current_room_id
                spawn_to_use = self.retry_spawn or "default"
                if not self.retry_spawn and ("boss" in room_to_load or "gate" in room_to_load):
                    spawn_to_use = "bottom"
                
                game.gamestate.current_room_id = room_to_load
                self._state_machine.change_state(StateID.ROOM, room_id=room_to_load, spawn_id=spawn_to_use)
                
            elif self.cursor_index == 1: 
                self._state_machine.change_state(StateID.MAIN_MENU)
            return True
            
        return False

    def update(self, dt: float): pass
    def render(self, surface): pass

# --- DIALOGUE STATE ---
# --- DIALOGUE STATE ---
class DialogueState(BaseState):
    """
    Stato per i dialoghi in-game.
    Updated: Grafica allineata alla Cutscene (No stretch, no dimming).
    """
    def __init__(self, state_machine=None): 
        super().__init__(StateID.DIALOGUE, state_machine)
        self._render_below = True # Lascia vedere il gioco sotto
        self.dialogue_data = []
        self.current_index = 0
        self.font = None
        self.font_name = None
        self.assets = None
        
        # Dimensioni Ritratti (Identiche a CutsceneState FIXED)
        self.PORTRAIT_W = 200
        self.PORTRAIT_H = 300
        self.SCREEN_W = 800
        self.SCREEN_H = 600
        self.TEXT_BOX_H = 150

    def enter(self, prev_state=None, **kwargs): 
        self.dialogue_data = kwargs.get('dialogue_data', [])
        self.current_index = 0
        
        if not pygame.font.get_init(): pygame.font.init()
        self.font = pygame.font.SysFont("Arial", 20)
        self.font_name = pygame.font.SysFont("Arial", 22, bold=True)
        
        if self._state_machine and self._state_machine.controller:
            self.assets = self._state_machine.controller.render_controller.asset_manager

    def exit(self, next_state=None): pass

    def handle_event(self, event) -> bool:
        if event.type == pygame.KEYDOWN and event.key in (pygame.K_SPACE, pygame.K_RETURN, pygame.K_ESCAPE):
            self.current_index += 1
            if self.current_index >= len(self.dialogue_data):
                self._state_machine.pop_state()
                self._state_machine.controller.action_runner.complete_blocking_action()
            return True
        return True
    
    def update(self, dt: float): pass
    
    def render(self, surface):
        if self.current_index >= len(self.dialogue_data): return

        data = self.dialogue_data[self.current_index]
        speaker = data.get('speaker', '???')
        text = data.get('text', '...')
        
        # 1. Draw Portraits (Se speaker è noto)
        if self.assets and speaker != "Sistema" and speaker != "Narratore":
            # Definisci chi va a sinistra e chi a destra
            # Turiddu, U Strammu, Giufà a Sinistra
            # Rosalia a Destra
            is_left = speaker in ["Turiddu", "U Strammu", "Giufà"]
            
            # Carica immagine con dimensioni fisse (Portrait Style)
            img = self.assets.get_image(f"characters/{speaker}", self.PORTRAIT_W, self.PORTRAIT_H, fallback_type="npc")
            
            if img:
                # Flip se a destra
                if not is_left:
                    img = pygame.transform.flip(img, True, False)
                
                # Posizione X
                x_pos = 20 if is_left else (self.SCREEN_W - self.PORTRAIT_W - 20)
                # Posizione Y (sopra il box)
                y_pos = self.SCREEN_H - self.TEXT_BOX_H - self.PORTRAIT_H + 20
                
                surface.blit(img, (x_pos, y_pos))

        # 2. Draw Text Box
        box_rect = pygame.Rect(50, self.SCREEN_H - self.TEXT_BOX_H - 20, self.SCREEN_W - 100, self.TEXT_BOX_H)
        
        # Sfondo box nero pieno (leggibilità massima)
        pygame.draw.rect(surface, (0, 0, 0), box_rect)
        
        # Bordo
        border_col = (255, 255, 255)
        if speaker == "Sistema": border_col = (255, 255, 0) # Giallo per messaggi sistema
        pygame.draw.rect(surface, border_col, box_rect, 2)
        
        # Speaker Name
        name_col = (255, 215, 0) if speaker != "Sistema" else (255, 50, 50)
        name_surf = self.font_name.render(speaker, True, name_col)
        surface.blit(name_surf, (box_rect.x + 20, box_rect.y + 15))
        
        # Text Wrapping
        words = text.split(' ')
        lines = []
        current_line = []
        max_width = box_rect.width - 40
        
        for word in words:
            test_line = ' '.join(current_line + [word])
            if self.font.size(test_line)[0] < max_width:
                current_line.append(word)
            else:
                lines.append(' '.join(current_line))
                current_line = [word]
        lines.append(' '.join(current_line))
        
        for i, line in enumerate(lines):
            txt_surf = self.font.render(line, True, (255, 255, 255))
            surface.blit(txt_surf, (box_rect.x + 20, box_rect.y + 50 + (i * 25)))

        # Hint
        hint = self.font.render("[SPACE]", True, (100, 100, 100))
        surface.blit(hint, (box_rect.right - 80, box_rect.bottom - 30))

class PromptState(BaseState):
    def __init__(self, state_machine=None): 
        super().__init__(StateID.PROMPT, state_machine)
        self._render_below = True
        self.prompt_type = "choice"
        self.message = ""
        self.options = []
        self.current_selection = 0
        self.font = None
        self.font_small = None
        self.on_select = None
        self.on_yes = None
        self.on_no = None

    def enter(self, prev_state=None, **kwargs): 
        self.prompt_type = kwargs.get('prompt_type', 'choice')
        self.message = kwargs.get('message') or kwargs.get('title', 'Confirm?')
        self.options = kwargs.get('options', []) 
        self.on_select = kwargs.get('on_select') 
        self.on_yes = kwargs.get('on_yes')        
        self.on_no = kwargs.get('on_no')          
        self.current_selection = 0
        self.font = pygame.font.SysFont("Arial", 32, bold=True)
        self.font_small = pygame.font.SysFont("Arial", 24)

    def exit(self, next_state=None): pass

    def handle_event(self, event) -> bool:
        if event.type == pygame.KEYDOWN:
            if event.key in (pygame.K_RIGHT, pygame.K_d, pygame.K_DOWN, pygame.K_s):
                if self.prompt_type == "choice": self.current_selection = (self.current_selection + 1) % len(self.options)
                else: self.current_selection = 1
                return True
            if event.key in (pygame.K_LEFT, pygame.K_a, pygame.K_UP, pygame.K_w):
                if self.prompt_type == "choice": self.current_selection = (self.current_selection - 1) % len(self.options)
                else: self.current_selection = 0
                return True
            if event.key in (pygame.K_RETURN, pygame.K_SPACE, pygame.K_e):
                self._state_machine.pop_state()
                if self.prompt_type == "choice":
                    if self.on_select: self.on_select(self.options[self.current_selection].value)
                else:
                    if self.current_selection == 0 and self.on_yes: self.on_yes()
                    elif self.current_selection == 1 and self.on_no: self.on_no()
                return True
            if event.key == pygame.K_ESCAPE:
                self._state_machine.pop_state()
                if self.prompt_type == "confirm" and self.on_no: self.on_no()
                return True
        return True
    
    def update(self, dt: float): pass
    
    def render(self, surface):
        w, h = surface.get_size()
        center_x, center_y = w//2, h//2
        rect = pygame.Rect(center_x - 250, center_y - 100, 500, 200)
        pygame.draw.rect(surface, (0, 0, 50), rect)
        pygame.draw.rect(surface, (255, 255, 255), rect, 3)
        title_surf = self.font.render(self.message, True, (255, 255, 0))
        title_rect = title_surf.get_rect(center=(center_x, rect.y + 50))
        surface.blit(title_surf, title_rect)
        if self.prompt_type == "choice":
            for i, opt in enumerate(self.options):
                color = (0, 255, 0) if i == self.current_selection else (200, 200, 200)
                txt = self.font_small.render(f"> {opt.label}" if i == self.current_selection else f"  {opt.label}", True, color)
                surface.blit(txt, (rect.x + 50, rect.y + 80 + i * 30))
        else:
            opts = ["SÌ", "NO"]
            start_x = center_x - 100
            for i, label in enumerate(opts):
                color = (0, 255, 0) if i == self.current_selection else (100, 100, 100)
                fnt = self.font if i == self.current_selection else self.font_small
                txt = fnt.render(label, True, color)
                pos_x = start_x + (i * 200)
                txt_rect = txt.get_rect(center=(pos_x, rect.y + 130))
                if i == self.current_selection:
                    pygame.draw.rect(surface, (0, 255, 0), txt_rect.inflate(20, 10), 1)
                surface.blit(txt, txt_rect)

class InteractionMenuState(BaseState):
    def __init__(self, state_machine=None): super().__init__(StateID.INTERACTION_MENU, state_machine); self._render_below = True; self.menu_data = InteractionMenuStateData()
    def enter(self, prev_state=None, **kwargs): title = kwargs.get('title', 'Interact'); options = kwargs.get('options', []); self.menu_data.open(title, options)
    def exit(self, next_state=None): self.menu_data.close()
    def handle_event(self, event) -> bool:
        input_manager = self._state_machine.controller.input_manager
        if input_manager.was_just_pressed(Action.MENU_DOWN): self.menu_data.move_cursor(1); return True
        if input_manager.was_just_pressed(Action.MENU_UP): self.menu_data.move_cursor(-1); return True
        if input_manager.was_just_pressed(Action.CONFIRM): script_id = self.menu_data.get_selected_script(); self._state_machine.pop_state(); (self._state_machine.controller.action_runner.run_script_by_id(script_id) if script_id else None); return True
        if input_manager.was_just_pressed(Action.CANCEL): self._state_machine.pop_state(); return True
        return False
    def update(self, dt: float): pass
    def render(self, surface): 
        w, h = surface.get_size()
        rect = pygame.Rect(w - 250, 100, 200, 300)
        pygame.draw.rect(surface, (0, 0, 40), rect)
        pygame.draw.rect(surface, (255, 255, 255), rect, 2)
        font = pygame.font.SysFont("Arial", 20)
        title = font.render(self.menu_data.title, True, (255, 255, 0))
        surface.blit(title, (rect.x + 10, rect.y + 10))
        for i, opt in enumerate(self.menu_data.options):
            col = (0, 255, 0) if i == self.menu_data.selected_index else (200, 200, 200)
            txt = font.render(opt.label, True, col)
            surface.blit(txt, (rect.x + 20, rect.y + 50 + i * 30))

class BriscolaState(BaseState):
    def __init__(self, state_machine=None):
        super().__init__(StateID.BRISCOLA, state_machine)
        self.model = BriscolaModel()
        self.view = None
        self.cursor_index = 0
        self.timer = 0.0

    def enter(self, prev_state=None, **kwargs):
        game = self._state_machine.controller.game
        self.view = BriscolaView(self._state_machine.controller.render_controller.renderer, game.settings.audio)
        self.view.assets = self._state_machine.controller.render_controller.asset_manager
        
        self.model.start_game()
        self.cursor_index = 0
        game.enter_combat()

    def exit(self, next_state=None):
        self._state_machine.controller.game.exit_combat()

    def handle_event(self, event) -> bool:
        if self.model.state == "GAME_OVER":
            if event.type == pygame.KEYDOWN and event.key in (pygame.K_RETURN, pygame.K_SPACE):
                if self.model.winner == "player":
                    game = self._state_machine.controller.game
                    game.ferrum.on_boss_victory()
                    self._state_machine.controller.game.gamestate.current_room_id = "hub" 
                    self._state_machine.change_state(StateID.HUB, spawn_id="from_ferrum")
                else:
                    self._state_machine.change_state(
                        StateID.GAME_OVER, 
                        retry_room="ferrum_boss_room", 
                        retry_spawn="bottom"
                    )
                return True
            return False

        if self.model.state != "PLAYER_TURN":
            return False 

        input_manager = self._state_machine.controller.input_manager
        
        if input_manager.was_just_pressed(Action.MENU_RIGHT):
            self.cursor_index = (self.cursor_index + 1) % max(1, len(self.model.mano_player))
            return True
        if input_manager.was_just_pressed(Action.MENU_LEFT):
            self.cursor_index = (self.cursor_index - 1) % max(1, len(self.model.mano_player))
            return True
        
        if input_manager.was_just_pressed(Action.CONFIRM):
            self.model.play_card_player(self.cursor_index)
            self.cursor_index = 0 
            return True
            
        return False

    def update(self, dt: float):
        if self.model.state == "CPU_TURN":
            self.timer += dt
            if self.timer > 1.0:
                self.model.cpu_turn()
                self.timer = 0
        
        elif self.model.state == "RESOLVE_TRICK":
            self.timer += dt
            if self.timer > 2.0: 
                self.model.resolve_trick()
                self.timer = 0

    def render(self, surface):
        if not self.view: return
        
        if self.model.state == "GAME_OVER":
            self.view.render_game_over(surface.get_size(), self.model.winner, self.model.punti_player, self.model.punti_cpu)
        else:
            self.view.render(surface.get_size(), self.model, self.cursor_index)

class SetteMezzoState(BaseState):
    def __init__(self, state_machine=None):
        super().__init__(StateID.SETTE_MEZZO, state_machine)
        self.model = SetteMezzoModel()
        self.view = None
        self.cursor_index = 0 
        self.timer = 0.0

    def enter(self, prev_state=None, **kwargs):
        game = self._state_machine.controller.game
        self.view = SetteMezzoView(self._state_machine.controller.render_controller.renderer, game.settings.audio)
        self.view.assets = self._state_machine.controller.render_controller.asset_manager
        
        self.model.start_game()
        self.cursor_index = 0
        game.enter_combat()

    def exit(self, next_state=None):
        self._state_machine.controller.game.exit_combat()

    def handle_event(self, event) -> bool:
        if self.model.state == "GAME_OVER":
            if event.type == pygame.KEYDOWN and event.key in (pygame.K_RETURN, pygame.K_SPACE):
                if self.model.winner == "player":
                    game = self._state_machine.controller.game
                    game.vinalia.on_boss_victory() 
                    self._state_machine.controller.game.gamestate.current_room_id = "hub" 
                    self._state_machine.change_state(StateID.HUB, spawn_id="from_vinalia")
                else:
                    self._state_machine.change_state(
                        StateID.GAME_OVER, 
                        retry_room="vinalia_boss_room", 
                        retry_spawn="bottom"
                    )
                return True
            return False

        if self.model.state != "PLAYER_TURN":
            return False

        input_manager = self._state_machine.controller.input_manager
        
        if input_manager.was_just_pressed(Action.MENU_RIGHT):
            self.cursor_index = 1
            return True
        if input_manager.was_just_pressed(Action.MENU_LEFT):
            self.cursor_index = 0
            return True
        
        if input_manager.was_just_pressed(Action.CONFIRM):
            if self.cursor_index == 0:
                self.model.player_hit()
            else:
                self.model.player_stand()
            return True
            
        return False

    def update(self, dt: float):
        if self.model.state == "CPU_TURN":
            self.timer += dt
            if self.timer > 1.5:
                self.model.cpu_turn()
                self.timer = 0

    def render(self, surface):
        if not self.view: return
        
        if self.model.state == "GAME_OVER":
            self.view.render_game_over(surface.get_size(), self.model.winner, self.model.score_player, self.model.score_cpu)
        else:
            self.view.render(surface.get_size(), self.model, self.cursor_index)

class CucuState(BaseState):
    def __init__(self, state_machine=None):
        super().__init__(StateID.CUCU, state_machine)
        self.model = CucuModel()
        self.view = None
        self.cursor_index = 0 
        self.timer = 0.0

    def enter(self, prev_state=None, **kwargs):
        game = self._state_machine.controller.game
        self.view = CucuView(self._state_machine.controller.render_controller.renderer, game.settings.audio)
        self.view.assets = self._state_machine.controller.render_controller.asset_manager
        
        self.model.start_game()
        self.cursor_index = 0
        game.enter_combat()

    def exit(self, next_state=None):
        self._state_machine.controller.game.exit_combat()

    def handle_event(self, event) -> bool:
        input_manager = self._state_machine.controller.input_manager
        
        if self.model.state == "GAME_OVER":
            if event.type == pygame.KEYDOWN and event.key in (pygame.K_RETURN, pygame.K_SPACE):
                if self.model.winner == "player":
                    game = self._state_machine.controller.game
                    game.viridor.on_boss_victory() 
                    self._state_machine.controller.game.gamestate.current_room_id = "hub" 
                    self._state_machine.change_state(StateID.HUB, spawn_id="from_viridor")
                else:
                    self._state_machine.change_state(
                        StateID.GAME_OVER, 
                        retry_room="viridor_boss_room", 
                        retry_spawn="bottom"
                    )
                return True
            return False
            
        if self.model.state == "ROUND_END":
            if event.type == pygame.KEYDOWN and event.key in (pygame.K_RETURN, pygame.K_SPACE):
                self.model.start_round()
                self.cursor_index = 0
                return True
            return False

        if self.model.state != "PLAYER_TURN":
            return False

        if input_manager.was_just_pressed(Action.MENU_RIGHT):
            self.cursor_index = 1
            return True
        if input_manager.was_just_pressed(Action.MENU_LEFT):
            self.cursor_index = 0
            return True
        
        if input_manager.was_just_pressed(Action.CONFIRM):
            action = "keep" if self.cursor_index == 0 else "swap"
            self.model.player_action(action)
            return True
            
        return False

    def update(self, dt: float):
        if self.model.state == "CPU_TURN":
            self.timer += dt
            if self.timer > 1.5:
                self.model.cpu_turn()
                self.timer = 0

    def render(self, surface):
        if not self.view: return
        
        if self.model.state == "GAME_OVER":
            self.view.render_game_over(surface.get_size(), self.model.winner)
        else:
            self.view.render(surface.get_size(), self.model, self.cursor_index)

class SettingsState(BaseState):
    def __init__(self, state_machine=None): super().__init__(StateID.SETTINGS, state_machine)
    def enter(self, prev_state=None, **kwargs): pass
    def exit(self, next_state=None): pass
    def handle_event(self, event) -> bool: return False
    def update(self, dt: float): pass
    def render(self, surface): pass

class CreditsState(BaseState):
    def __init__(self, state_machine=None): super().__init__(StateID.CREDITS, state_machine)
    def enter(self, prev_state=None, **kwargs): pass
    def exit(self, next_state=None): pass
    def handle_event(self, event) -> bool: return False
    def update(self, dt: float): pass
    def render(self, surface): pass

class ErrorState(BaseState):
    def __init__(self, state_machine=None): super().__init__(StateID.ERROR, state_machine)
    def enter(self, prev_state=None, **kwargs): pass
    def exit(self, next_state=None): pass
    def handle_event(self, event) -> bool: return False
    def update(self, dt: float): pass
    def render(self, surface): pass

class AcesMenuState(BaseState):
    def __init__(self, state_machine=None):
        super().__init__(StateID.ACES_MENU, state_machine)
        self._render_below = True
        self.game = None

    def enter(self, prev_state=None, **kwargs):
        self.game = self._state_machine.controller.game

    def exit(self, next_state=None): pass

    def handle_event(self, event) -> bool:
        input_manager = self._state_machine.controller.input_manager
        if input_manager.was_just_pressed(Action.CANCEL) or \
           input_manager.was_just_pressed(Action.PAUSE) or \
           input_manager.was_just_pressed(Action.CONFIRM): 
            self._state_machine.pop_state()
            return True
        if event.type == pygame.KEYDOWN and event.key == pygame.K_a:
            self._state_machine.pop_state()
            return True
        return True

    def update(self, dt: float): pass
    def render(self, surface): pass

STATE_CLASSES = {
    StateID.MAIN_MENU: MainMenuState,
    StateID.NEW_GAME_SETUP: NewGameSetupState,
    StateID.CUTSCENE: CutsceneState,
    StateID.HUB: HubState,
    StateID.ROOM: RoomState,
    StateID.COMBAT: CombatState,
    StateID.PAUSE: PauseState,
    StateID.DIALOGUE: DialogueState,
    StateID.PROMPT: PromptState,
    StateID.INTERACTION_MENU: InteractionMenuState,
    StateID.INVENTORY: InventoryState,
    StateID.SAVE_LOAD: SaveLoadState,
    StateID.SETTINGS: SettingsState,
    StateID.CREDITS: CreditsState,
    StateID.GAME_OVER: GameOverState,
    StateID.ERROR: ErrorState,
    StateID.SCOPA: ScopaState,
    StateID.BRISCOLA: BriscolaState, 
    StateID.SETTE_MEZZO: SetteMezzoState, 
    StateID.CUCU: CucuState,
    StateID.ACES_MENU: AcesMenuState, 
    StateID.BOSS_OSTE: BossOsteState 
}