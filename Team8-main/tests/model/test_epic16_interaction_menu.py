import unittest
import pygame
from unittest.mock import Mock

from src.model.game import Game
from src.model.room_data import RoomData, EntityDefinition, SpawnPoint
from src.controller.state_machine import StateMachine
from src.model.states.game_states import RoomState, InteractionMenuState
from src.model.input_actions import Action
from src.model.ui.interaction_menu_state import InteractionMenuStateData

class MockController:
    def __init__(self, game):
        self.game = game
        self.input_manager = Mock()
        self.input_manager.was_just_pressed = Mock(return_value=False)
        self.input_manager.is_down = Mock(return_value=False)
        self.render_controller = Mock()
        self.action_runner = Mock()
        self.action_runner.is_running.return_value = False

class TestEpic16(unittest.TestCase):
    def setUp(self):
        pygame.init()
        self.game = Game()
        self.game.start_new_game(1)
        
        # Setup Room with NPC having actions (US 63)
        self.room = RoomData("hub_npc", width=800, height=600)
        self.room.spawns["default"] = SpawnPoint("default", 100, 100)
        
        self.npc = EntityDefinition(
            "giufa", "npc", x=150, y=100,
            interaction_label="Giufa",
            actions=[
                {"label": "Talk", "script_id": "script_talk"},
                {"label": "Examine", "script_id": "script_examine"}
            ]
        )
        self.room.entities.append(self.npc)
        self.game.content._data["rooms"]["hub_npc"] = self.room
        
        self.controller = MockController(self.game)
        self.sm = StateMachine()
        self.sm.controller = self.controller
        
        # States
        self.room_state = RoomState(self.sm)
        self.menu_state = InteractionMenuState(self.sm)
        self.sm.register_state(self.room_state)
        self.sm.register_state(self.menu_state)
        
        self.sm.change_state(self.room_state.state_id, room_id="hub_npc")

    def test_us63_open_menu(self):
        """Interact with NPC opens menu."""
        # 1. Move close to NPC
        self.game.gamestate.party_position = [140, 100]
        self.room_state.update(0.0) # Update focus
        
        self.assertIsNotNone(self.game.gamestate.active_interactable)
        
        # 2. Interact
        self.controller.input_manager.was_just_pressed.side_effect = lambda a: a == Action.INTERACT
        self.room_state.update(0.0)
        
        # 3. Verify Menu State is pushed
        self.assertEqual(self.sm.peek().state_id, self.menu_state.state_id)
        self.assertTrue(self.menu_state.menu_data.is_open)
        self.assertEqual(len(self.menu_state.menu_data.options), 2)
        self.assertEqual(self.menu_state.menu_data.options[0].label, "Talk")

    def test_us63_menu_navigation(self):
        """Navigate menu and select option."""
        # Open menu manually
        self.sm.push_state(self.menu_state.state_id, title="Giufa", options=self.npc.actions)
        
        # Initial: Index 0 (Talk)
        self.assertEqual(self.menu_state.menu_data.selected_index, 0)
        
        # Nav Down
        self.controller.input_manager.was_just_pressed.side_effect = lambda a: a == Action.MENU_DOWN
        self.menu_state.handle_event(None)
        self.assertEqual(self.menu_state.menu_data.selected_index, 1) # Examine
        
        # Nav Down (Cycle)
        self.menu_state.handle_event(None)
        self.assertEqual(self.menu_state.menu_data.selected_index, 0) # Talk

    def test_us63_menu_selection(self):
        """Confirm selection runs script."""
        self.sm.push_state(self.menu_state.state_id, title="Giufa", options=self.npc.actions)
        
        # Select Index 0 (Talk -> script_talk)
        self.controller.input_manager.was_just_pressed.side_effect = lambda a: a == Action.CONFIRM
        self.menu_state.handle_event(None)
        
        # Verify popped and script run
        self.assertEqual(self.sm.peek().state_id, self.room_state.state_id)
        self.controller.action_runner.run_script_by_id.assert_called_with("script_talk")

if __name__ == "__main__":
    unittest.main()