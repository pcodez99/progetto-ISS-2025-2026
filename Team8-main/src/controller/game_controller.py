"""
Game Controller - Main controller coordinating game logic.
Integration of Epic 1-28. 
Updated: FIX FADE/WAIT BLOCKING BUG.
"""
from src.model.game import Game
from src.model.save import (
    SaveManager, SaveStateChecker, GameSerializer, 
    SlotInfo, SaveResult, LoadResult, SlotStatus
)
from src.controller.input_manager import InputManager
from src.controller.render_controller import RenderController
from src.controller.state_machine import StateMachine
from src.model.states.base_state import StateID
from src.controller.action_runner import ActionRunner
from src.model.script_actions import ActionType
from src.model.input_actions import Action
from src.model.ui.handoff_overlay import HandoffModel

class GameController:
    def __init__(self):
        # 1. Model & Persistence
        self.game = Game()
        self.save_manager = SaveManager()
        
        # 2. Sub-Controllers
        self.input_manager = InputManager()
        self.render_controller = RenderController()
        
        # 3. Action Runner
        self.action_runner = ActionRunner()
        self.action_runner.game_ref = self.game 
        
        # 5. UI Models (Multiplayer Handoff)
        self.handoff_model = HandoffModel()

        # 6. State Machine
        self.state_machine = StateMachine(input_manager=self.input_manager)
        self.state_machine.controller = self
        self.state_machine.register_all_states()
        self.current_state = "MainMenu"

        # Link Prompt Manager to State Machine
        self.game.prompts.state_machine = self.state_machine

        # 4. Handlers
        self._register_action_handlers()

    def _register_action_handlers(self):
        """Registers handlers for all script actions."""
        
        # SHOW_DIALOGUE
        self.action_runner.register_handler(
            ActionType.SHOW_DIALOGUE,
            lambda p: self.state_machine.push_state(
                StateID.DIALOGUE, 
                dialogue_data=[{"speaker": p.get('speaker', '???'), "text": p.get('text', '...')}]
            )
        )

        # SHOW_CHOICE
        def handle_choice(params):
            def on_select(idx):
                self.action_runner.complete_blocking_action(idx)
                
            from src.model.ui.prompts import PromptChoice
            choices_list = params.get('choices', [])
            prompt_choices = [PromptChoice(label=c, value=i) for i, c in enumerate(choices_list)]
            
            self.game.prompts.show_choice(
                title=params.get('prompt', 'Scegli:'),
                options=prompt_choices,
                on_select=on_select
            )

        self.action_runner.register_handler(ActionType.SHOW_CHOICE, handle_choice)
        
        # WAIT: Gestito internamente da ActionRunner.update, ma registriamo un dummy per sicurezza
        self.action_runner.register_handler(ActionType.WAIT, lambda p: None)

        # CHANGE_ROOM
        def handle_change_room(params):
            room_id = params['room_id']
            spawn_id = params.get('spawn_id', 'default')
            self.game.gamestate.current_room_id = room_id
            self.state_machine.change_state(StateID.ROOM, room_id=room_id, spawn_id=spawn_id)
        
        self.action_runner.register_handler(ActionType.CHANGE_ROOM, handle_change_room)

        # START_COMBAT
        self.action_runner.register_handler(
            ActionType.START_COMBAT, 
            lambda p: self.state_machine.change_state(StateID.COMBAT, encounter_id=p['encounter_id'])
        )

        # CHANGE_STATE
        def handle_change_state(params):
            str_to_state = {
                "cutscene": StateID.CUTSCENE,
                "credits": StateID.CREDITS,
                "game_over": StateID.GAME_OVER,
                "hub": StateID.HUB,
                "scopa": StateID.SCOPA, 
                "briscola": StateID.BRISCOLA,
                "sette_mezzo": StateID.SETTE_MEZZO,
                "cucu": StateID.CUCU
            }
            
            raw_id = params.get('state_id')
            target_state = StateID.MAIN_MENU

            if isinstance(raw_id, StateID):
                target_state = raw_id
            elif isinstance(raw_id, str):
                target_state = str_to_state.get(raw_id, StateID.MAIN_MENU)
            
            kwargs = {k: v for k, v in params.items() if k != 'state_id'}
            self.state_machine.change_state(target_state, **kwargs)

        self.action_runner.register_handler(ActionType.CHANGE_STATE, handle_change_state)

        # PROGRESSION
        self.action_runner.register_handler(
            ActionType.SET_FLAG,
            lambda p: self.game.set_flag(p['flag_name'], p['value'])
        )
        
        self.action_runner.register_handler(
            ActionType.GIVE_ITEM,
            lambda p: self._handle_give_item(p)
        )
        
        self.action_runner.register_handler(
            ActionType.SET_CHECKPOINT,
            lambda p: self.game.gamestate.set_checkpoint()
        )
        
        self.action_runner.register_handler(
            ActionType.RECRUIT_GUEST,
            lambda p: setattr(self.game.gamestate.party, 'guest_id', p['guest_id'])
        )
        
        # --- FIX: FADE HANDLERS ---
        # Poiché non abbiamo un sistema di Fade reale che dura N secondi,
        # dobbiamo dire all'ActionRunner che l'azione è completata SUBITO.
        # Altrimenti il gioco aspetta in eterno (Softlock).
        self.action_runner.register_handler(ActionType.FADE_IN, lambda p: self.action_runner.complete_blocking_action())
        self.action_runner.register_handler(ActionType.FADE_OUT, lambda p: self.action_runner.complete_blocking_action())

    def _handle_give_item(self, params):
        item_id = params['item_id']
        qty = params.get('quantity', 1)
        
        self.game.add_global_item(item_id, qty)
        
        player = self.game.gamestate.get_active_player()
        if player and player.inventory:
            for _ in range(qty):
                player.inventory.add_item(item_id, "Oggetto recuperato.")
        
        if self.game.prompts:
            self.game.prompts.show_info(f"Ottenuto: {item_id.replace('_', ' ').title()}!", 0, 2000)

    def start_new_game(self, num_players: int):
        self.game.start_new_game(num_players)
        self.current_state = "CutsceneState"
        self.handoff_model.awaiting_confirm = False
        
        # Start with Intro Cutscene
        self.state_machine.change_state(StateID.CUTSCENE, script_id="intro_sequence")

    def process_frame(self, dt: float):
        # 1. Update Timer Prompts (Fondamentale per vedere i messaggi)
        import pygame
        current_time_ms = pygame.time.get_ticks()
        self.game.prompts.update(current_time_ms)

        # 2. Update State Machine
        self.state_machine.update(dt)
        
        # 3. Check Handoff Overlay (Opzionale se usiamo hotseat fluido)
        if self.handoff_model.awaiting_confirm:
            if self.input_manager.was_just_pressed(Action.CONFIRM):
                self.handoff_model.awaiting_confirm = False
                self.game.gamestate.exploration_turn_manager.confirm_handoff()

        # 4. Flush Inputs
        self.input_manager.begin_frame()

    def trigger_next_turn(self):
        """Passa il controllo al prossimo giocatore attivo."""
        tm = self.game.gamestate.exploration_turn_manager
        party = self.game.gamestate.party.main_characters
        mask = self.game.gamestate.party.enabled_mask
        
        # Cambia indice
        tm.next_turn(party, mask)
        
        # Recupera il NUOVO personaggio attivo
        active_char = tm.get_active_character(party)
        
        if active_char:
            # Sposta il "focus" del gioco (party_position) sulle coordinate di questo personaggio
            self.game.gamestate.party_position = [active_char.x, active_char.y]
            
            # Feedback visivo
            self.game.prompts.show_info(f"Controllo: {active_char.name}", 0, 1500)

    def get_save_slots(self) -> list: return self.save_manager.list_slots()
    def can_save_game(self) -> tuple: return SaveStateChecker.can_save(self.current_state)

    def save_game(self, slot_index: int, confirmed: bool = False, custom_name: str = "") -> SaveResult:
        can_save, message = self.can_save_game()
        if not can_save: return SaveResult(ok=False, message=message)
        
        if self.save_manager.is_slot_occupied(slot_index) and not confirmed:
            return SaveResult(ok=False, message="CONFIRM_OVERWRITE")
            
        return self.save_manager.save_to_slot(slot_index, self.game, custom_name=custom_name)

    def load_game(self, slot_index: int) -> LoadResult:
        result = self.save_manager.load_from_slot(slot_index)
        if result.ok and result.save_data:
            save_dict = result.save_data.to_dict()
            success = GameSerializer.from_dict(save_dict, self.game)
            if success:
                room_id = result.save_data.data.world.room_id
                self.game.gamestate.current_room_id = room_id
                self.current_state = 'HubState' if room_id == 'hub' else 'RoomState'
                state_id = StateID.HUB if room_id == 'hub' else StateID.ROOM
                self.state_machine.change_state(state_id, room_id=room_id)
                return LoadResult(ok=True, message="Game loaded", save_data=result.save_data)
        return result

    def is_slot_empty(self, slot_index: int) -> bool: return not self.save_manager.is_slot_occupied(slot_index)
    def get_slot_info(self, slot_index: int) -> SlotInfo:
        slots = self.save_manager.list_slots()
        return slots[slot_index-1] if 0 < slot_index <= len(slots) else SlotInfo(slot_index, SlotStatus.EMPTY)

    def get_player_inventory(self, idx): return self.game.get_player_inventory(idx)
    def get_player_abilities(self, idx): return self.game.get_player_abilities(idx)
    def set_current_state(self, s): self.current_state = s
    def get_current_state(self): return self.current_state