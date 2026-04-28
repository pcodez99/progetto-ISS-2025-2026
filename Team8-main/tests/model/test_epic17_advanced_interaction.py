import unittest
import pygame
from unittest.mock import Mock

from src.model.game import Game
from src.model.room_data import RoomData, TriggerZone, EntityDefinition, SpawnPoint
from src.controller.state_machine import StateMachine
from src.model.states.game_states import RoomState, InteractionMenuState
from src.model.input_actions import Action

class MockController:
    def __init__(self, game):
        self.game = game
        self.input_manager = Mock()
        self.input_manager.was_just_pressed = Mock(return_value=False)
        self.input_manager.is_down = Mock(return_value=False)
        self.render_controller = Mock()
        self.action_runner = Mock()
        self.action_runner.is_running.return_value = False
        self.action_runner.run_script_by_id = Mock()

class TestEpic17(unittest.TestCase):
    def setUp(self):
        pygame.init()
        self.game = Game()
        self.game.start_new_game(1)
        self.game.prompts = Mock() # Mock PromptManager
        
        # 1. Setup Rooms (US 67 Transitions)
        self.hall = RoomData("hall", width=800, height=600)
        self.hall.spawns["default"] = SpawnPoint("default", 100, 100)
        
        # Exit to Garden
        self.hall.triggers.append(TriggerZone(
            "to_garden", pygame.Rect(200, 100, 50, 50),
            trigger_type="exit", target_room="garden", requires_confirm=True,
            prompt_text="Go to Garden?"
        ))
        
        # 2. Setup NPC with Menu (US 68)
        self.hall.entities.append(EntityDefinition(
            "giufa", "npc", x=300, y=300,
            interaction_label="Giufa",
            actions=[{"label": "Talk", "script_id": "talk_giufa"}]
        ))
        
        # 3. Setup Env Object (US 69) - Chest
        self.hall.entities.append(EntityDefinition(
            "chest", "interactable", x=400, y=100,
            interaction_label="Old Chest",
            actions=[
                {"label": "Examine", "script_id": "examine_chest"},
                {"label": "Open", "script_id": "open_chest"}
            ]
        ))
        
        # 4. Setup Gated Room (US 70)
        self.garden = RoomData("garden", width=800, height=600)
        self.garden.triggers.append(TriggerZone(
            "to_boss", pygame.Rect(750, 300, 50, 50),
            trigger_type="exit", target_room="boss_room", 
            data={"req_flag": "gate_open", "locked_msg": "Sealed."}
        ))
        
        self.game.content._data["rooms"]["hall"] = self.hall
        self.game.content._data["garden"] = self.garden
        
        self.controller = MockController(self.game)
        self.sm = StateMachine()
        self.sm.controller = self.controller
        
        self.room_state = RoomState(self.sm)
        self.menu_state = InteractionMenuState(self.sm)
        self.sm.register_state(self.room_state)
        self.sm.register_state(self.menu_state)
        
        self.sm.change_state(self.room_state.state_id, room_id="hall")

    def test_us67_transition_prompt(self):
        """Camminare su exit trigger con confirm apre prompt."""
        # Start slightly right of trigger (Trigger at 200, 100)
        self.game.gamestate.party_position = [260, 110]
        
        # Move Left into trigger
        self.controller.input_manager.is_down.side_effect = lambda a: a == Action.MOVE_LEFT
        self.room_state.update(0.1)
        
        # Verify prompt shown
        self.game.prompts.show_confirm.assert_called()
        _, kwargs = self.game.prompts.show_confirm.call_args
        self.assertIn("Go to Garden?", kwargs.get('message', ''))

    def test_us69_env_options(self):
        """Interagire con Chest apre menu con opzioni."""
        self.game.gamestate.party_position = [400, 110]
        self.room_state.update(0.0) # Focus
        
        self.controller.input_manager.was_just_pressed.side_effect = lambda a: a == Action.INTERACT
        self.room_state.update(0.0)
        
        self.assertIsInstance(self.sm.peek(), InteractionMenuState)
        menu_data = self.sm.peek().menu_data
        self.assertTrue(menu_data.is_open)
        self.assertEqual(len(menu_data.options), 2)
        self.assertEqual(menu_data.options[1].label, "Open")

    def test_us70_gated_exit(self):
        """Uscita bloccata impedisce transizione se flag mancante."""
        self.game.content._data["rooms"]["garden"] = self.garden 
        self.room_state.enter(room_id="garden")
        
        self.game.gamestate.party_position = [760, 310]
        
        # Move Right into locked exit
        self.controller.input_manager.is_down.side_effect = lambda a: a == Action.MOVE_RIGHT
        self.room_state.update(0.1)
        
        self.game.prompts.show_info.assert_called_with("Sealed.", 0, 2000)
        self.game.prompts.show_confirm.assert_not_called()

if __name__ == "__main__":
    unittest.main()