"""
Game Model - Unified Facade (Amelia Architecture + Sicily Features)
Updated: RPG Mechanics (2 Players, Stat Boosts, Ace Abilities)
DEBUG DISABLED: Normal gameplay progression restored.
"""
from src.model.ui.prompts import PromptManager
from src.version import VERSION
from src.model.character import Char_Builder

# Amelia Core Imports
from src.model.save.serializer import GameSerializer

# Sicily Infrastructure Imports
from src.model.utils.logging_setup import setup_logging
from src.model.audio.audio_manager import AudioManager
from src.model.settings.settings_manager import SettingsManager
from src.model.content.registry import ContentRegistry
from src.model.debug.debug_console import DebugConsole

# Sicily Content Imports
from src.model.content.world_builder import WorldBuilder
from src.model.vinalia.vinalia_region import VinaliaRegion
from src.model.aurion.aurion_region import AurionRegion
from src.model.viridor.viridor_region import ViridorRegion
from src.model.etna.etna_region import EtnaRegion
from src.model.ferrum.ferrum_region import FerrumRegion
from src.model.items.item_ids import ItemIds

# Party & Logic
from src.model.party_factory import PartyModel, PartyFactory
from src.controller.exploration_turn_manager import ExplorationTurnManager

# Costanti Semi (US 51)
SUIT_DENARI = "Denari"
SUIT_BASTONI = "Bastoni"
SUIT_SPADE = "Spade"
SUIT_COPPE = "Coppe"
VALID_SUITS = {SUIT_DENARI, SUIT_BASTONI, SUIT_SPADE, SUIT_COPPE}

class Game:
    def __init__(self):
        # --- INFRASTRUCTURE (Sicily) ---
        self.version = VERSION
        self.logger, self.log_path = setup_logging()
        
        # Audio & Settings
        self.audio = AudioManager(logger=self.logger)
        self.settings = SettingsManager(logger=self.logger)
        self.settings.load()

        from unittest.mock import Mock
        self.ui_stack = Mock() 
        self.ui_stack.input_context.return_value = "gameplay"
        self.prompts = PromptManager(state_machine=None)

        try:
            self.settings.apply(game=self)
        except Exception:
            pass

        self.content = ContentRegistry()
        self.debug = DebugConsole(enabled=False)

        # --- CORE GAMEPLAY (Amelia) ---
        self.gamestate = GameState()
        self.gameloop = GameLoop()

        # --- GAMEPLAY CONTENT (Sicily) ---
        # Instantiation order matters for dependencies
        self.vinalia = VinaliaRegion(self)
        self.aurion = AurionRegion(self) 
        self.viridor = ViridorRegion(self)
        self.etna = EtnaRegion(self)
        self.ferrum = FerrumRegion(self)
        
        self.inventory_global = {} 
        self.flags = {}

    def start_new_game(self, num_players: int):
        """Inizializza una nuova partita resettando completamente lo stato (US22/Fix Reset)."""
        
        # 1. WIPE GLOBAL STATE
        self.inventory_global = {}
        self.flags = {}
        self.gamestate = GameState() # Crea un GameState fresco, pulito

        # 2. SETUP BASE STATE
        self.gamestate.is_running = True
        self.gamestate.current_level = 1
        self.gamestate.current_room = 1 
        self.gamestate.current_room_id = "hub"
        
        # Epic 22: Create standard party with mask
        factory = PartyFactory()
        self.gamestate.party = factory.create_main_party()
        # In RPG mode we usually force 2 players active
        self.gamestate.party.set_enabled_count(2) 
        self.gamestate.num_humans = 2
        
        # Legacy compatibility for tests accessing .players
        self.gamestate.players = self.gamestate.party.main_characters
        
       # =========================================================
        # DEBUG: SETUP FINALE (Distribuzione 4 Assi)
        # =========================================================
        # Simuliamo che i giocatori abbiano completato 2 regioni a testa.
        # Player 1 (Turiddu) attivo di default.
        #self.give_ace("Denari")
        #self.give_ace("Bastoni")

        # Passiamo turno al Player 2 (Rosalia)
        #self.gamestate.cycle_active_player()
        
        #self.give_ace("Spade")
        #self.give_ace("Coppe")

        # Torniamo al Player 1 (Turiddu) come leader iniziale
        #self.gamestate.cycle_active_player()
        
        # Feedback in console
        #print("DEBUG MODE: 4 Assi distribuiti. Pronto per l'Etna.")
        # =========================================================

        self.enter_hub() 
        return self.gameloop.mainloop()

    def collect_ace(self, suit: str) -> bool:
        # FIX: Case-insensitive check
        suit_cap = suit.capitalize()
        
        if suit_cap not in VALID_SUITS:
            self.logger.warning(f"Invalid suit collected: {suit} (Expected: {VALID_SUITS})")
            return False
            
        ace_id = f"ace_{suit.lower()}"
        if ace_id not in self.gamestate.aces_collected:
            self.gamestate.aces_collected.append(ace_id)
            self.logger.info(f"Ace Collected: {suit_cap}")
            return True
        return False

    def has_ace(self, suit: str) -> bool:
        ace_id = f"ace_{suit.lower()}"
        return ace_id in self.gamestate.aces_collected

    def get_ace_count(self) -> int:
        return len(self.gamestate.aces_collected)

    def get_player_inventory(self, player_index):
        if player_index < len(self.gamestate.players):
            return self.gamestate.players[player_index].get_inventory_in_view_format()
        return [], 0, 0

    def get_player_abilities(self, player_index):
        if player_index < len(self.gamestate.players):
            return self.gamestate.players[player_index].get_abilities_in_view_format()
        return []

    def enter_hub(self):
        self.gamestate.current_room_id = "hub"
        self.gamestate.current_scene = "hub" 
        self.audio.play_bgm("hub.ogg", fade_ms=500, loop=True, context={"scene": "hub"})

    def enter_combat(self):
        self.audio.play_bgm("combat.ogg", fade_ms=500, loop=True, context={"scene": "combat"})

    def exit_combat(self):
        self.enter_hub()

    def load_content(self):
        try:
            # Carica tutto il mondo (Hub + Regioni)
            WorldBuilder.build_all(self.content)
            self.logger.info("World content loaded successfully.")
        except Exception as e:
            self.logger.warning("Content load issue: %s", e)
            import traceback
            traceback.print_exc() 

    def add_global_item(self, item_id: str, qty: int = 1):
        self.inventory_global[str(item_id)] = self.inventory_global.get(str(item_id), 0) + int(qty)

    def give_ace(self, suit: str):
        # FIX: Passa la stringa capitalizzata a collect_ace per validazione
        # e usa lower() per il mapping item_id
        suit_lower = suit.lower()
        
        success = self.collect_ace(suit)
        
        mapping = {
            "coppe": ItemIds.ACE_COPPE,
            "denari": ItemIds.ACE_DENARI,
            "bastoni": ItemIds.ACE_BASTONI,
            "spade": ItemIds.ACE_SPADE
        }
        
        if suit_lower in mapping:
            self.add_global_item(mapping[suit_lower], 1)
            
            # --- RPG LOGIC: ASSEGNA ABILITÀ E ITEM AL PLAYER ATTIVO ---
            player = self.gamestate.get_active_player()
            
            # 1. Assegna Item Fisico al Giocatore (Per recap finale)
            if player:
                item_name = f"Asso di {suit.capitalize()}"
                if not player.inventory.has_item(item_name):
                    ace_item_id = mapping[suit_lower]
                    player.inventory.add_item(ace_item_id, f"Simbolo di potere di {suit.capitalize()}")

            # 2. Assegna Abilità
            ability_name = f"Potere di {suit.capitalize()}"
            ability_desc = "Abilità speciale sbloccata per la battaglia finale."
            
            if suit_lower == "denari":
                ability_name = "Corruzione Aurea"
                ability_desc = "Usa Denari per confondere i nemici."
            elif suit_lower == "spade":
                ability_name = "Fendente d'Onore"
                ability_desc = "Un colpo critico garantito."
            elif suit_lower == "bastoni":
                ability_name = "Forza della Natura"
                ability_desc = "Recupera HP e aumenta Difesa."
            elif suit_lower == "coppe":
                ability_name = "Ebbrezza Mistica"
                ability_desc = "Schiva il prossimo attacco."

            if player:
                # Evita duplicati se richiamato
                has_ability = any(a['name'] == ability_name for a in player.special_abilities)
                if not has_ability:
                    player.learn_special_ability(ability_name, ability_desc)
                    player.regions_completed += 1 # Segna completamento regione

            # Prompt visivo
            if self.prompts:
                # self.prompts.show_info(f"ASSO DI {suit.upper()} OTTENUTO!", 0, 1000)
                pass

    def set_flag(self, key: str, value: bool):
        self.flags[str(key)] = value
        self.gamestate.flags[str(key)] = value

    def get_flag(self, key: str) -> bool:
        return self.gamestate.flags.get(str(key), False)

    def return_to_hub(self):
        self.enter_hub()

class GameLoop:
    def mainloop(self):
        return "entered mainloop"

class GameState:
    """Stato del gioco serializzabile"""
    def __init__(self):
        self.is_running = False
        self.current_level = None
        self.current_room = None
        self.current_room_id = "hub"
        
        self.party = PartyModel()
        self.players = [] # Compatibility alias
        
        self.spawn_id = None
        self.party_position = [0, 0]
        self.num_humans = 1
        self.active_interactable = None
        
        self.exploration_turn_manager = ExplorationTurnManager()
        
        self.aces_collected = []
        self.flags = {}
        self.removed_entities = []
        self.awaiting_handoff_confirm = False
        self.checkpoint_room_id = None
        self.checkpoint_spawn_id = None
        self.playtime_seconds = 0
        self.current_scene = "hub"

    @property
    def exploration_active_index(self):
        return self.exploration_turn_manager.get_active_index()

    @exploration_active_index.setter
    def exploration_active_index(self, value):
        self.exploration_turn_manager._active_index = value

    @property
    def guest_id(self):
        return self.party.guest_id

    @guest_id.setter
    def guest_id(self, value):
        self.party.guest_id = value

    def get_active_player(self):
        if not self.party.main_characters: return None
        return self.exploration_turn_manager.get_active_character(self.party.main_characters)

    def cycle_active_player(self):
        if not self.party.main_characters: return
        self.exploration_turn_manager.next_turn(
            self.party.main_characters, 
            self.party.enabled_mask
        )

    def set_guest(self, guest_id: str):
        self.party.guest_id = guest_id

    def get_guest_bonus(self):
        if not self.party.guest_id:
            return None
        if "pupo" in self.party.guest_id.lower():
            return "DEFENSE_UP"
        return "GENERIC_BONUS"

    def set_checkpoint(self):
        self.checkpoint_room_id = self.current_room_id

    def respawn(self):
        if self.checkpoint_room_id:
            return self.checkpoint_room_id
        return "hub"