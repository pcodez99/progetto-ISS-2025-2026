import unittest
import pygame
from unittest.mock import Mock

from src.model.game import Game
from src.model.room_data import RoomData, Collider, TriggerZone, EntityDefinition, SpawnPoint
from src.controller.state_machine import StateMachine
from src.model.states.game_states import RoomState
from src.model.input_actions import Action

class MockInputManager:
    def __init__(self): self.keys = {}
    def is_down(self, action): return self.keys.get(action, False)
    def was_just_pressed(self, action): return self.keys.get(action, False)
    def set_context(self, ctx): pass
    def begin_frame(self): pass

class MockController:
    def __init__(self, game):
        self.game = game
        self.input_manager = MockInputManager()
        self.render_controller = Mock() 
        self.action_runner = Mock()
        self.action_runner.is_running.return_value = False

class TestEpic15(unittest.TestCase):
    def setUp(self):
        pygame.init()
        self.game = Game()
        self.game.start_new_game(1)
        
        # Mock Prompts
        self.game.prompts = Mock()
        self.game.prompts.show_confirm = Mock()
        
        # Setup Room
        self.room = RoomData("test_room", width=800, height=600)
        self.room.spawns["start"] = SpawnPoint("start", 100, 100)
        self.room.colliders.append(Collider("wall", pygame.Rect(200, 0, 50, 600))) 
        
        self.room.entities.append(EntityDefinition(
            "chest", "interactable", x=150, y=100, 
            interaction_label="Open Chest", script_id="open_chest"
        ))
        
        self.room.triggers.append(TriggerZone(
            "exit_door", pygame.Rect(0, 100, 50, 50), 
            trigger_type="exit", target_room="next_room", requires_confirm=True,
            prompt_text="Leave?"
        ))

        self.game.content._data["rooms"]["test_room"] = self.room
        
        self.controller = MockController(self.game)
        
        self.sm = StateMachine()
        self.sm.controller = self.controller
        
        self.state = RoomState(self.sm)
        self.sm._state_stack.append(self.state) 
        self.state.enter(room_id="test_room", spawn_id="start")

    def test_us58_collision(self):
        """US 58: Movement blocked by wall."""
        self.assertEqual(self.game.gamestate.party_position, [100, 100])
        
        self.controller.input_manager.keys[Action.MOVE_RIGHT] = True
        
        self.state.update(0.3) 
        self.assertEqual(self.game.gamestate.party_position[0], 160)
        
        # Step 2: Try to move further into wall
        self.state.update(0.3)
        self.assertEqual(self.game.gamestate.party_position[0], 160)

    def test_us61_interaction_focus(self):
        """US 61: Detect closest interactable."""
        self.game.gamestate.party_position = [120, 100]
        self.state.update(0.0) 
        
        obj = self.game.gamestate.active_interactable
        self.assertIsNotNone(obj)
        self.assertEqual(obj.entity_id, "chest")
        
        self.game.gamestate.party_position = [0, 0]
        self.state.update(0.0)
        self.assertIsNone(self.game.gamestate.active_interactable)

    def test_us61_trigger_interaction(self):
        """US 61: Pressing interact runs script."""
        self.game.gamestate.party_position = [120, 100]
        self.state.update(0.0) 
        
        self.controller.input_manager.keys[Action.INTERACT] = True
        self.state.update(0.0)
        
        self.controller.action_runner.run_script_by_id.assert_called_with("open_chest")

    def test_us59_exit_prompt(self):
        """US 59: Trigger with requires_confirm shows prompt on ENTER (Movement)."""
        # Start slightly outside trigger to simulate entry
        self.game.gamestate.party_position = [60, 110] 
        self.state.update(0.0)

        # Move LEFT into the trigger (Rect is 0, 100, 50, 50)
        self.controller.input_manager.keys[Action.MOVE_LEFT] = True
        
        # Update simulation (movement -> check triggers on_enter=True)
        self.state.update(0.1)
        
        self.game.prompts.show_confirm.assert_called()
        _, kwargs = self.game.prompts.show_confirm.call_args
        self.assertIn("Leave?", kwargs.get('message', ''))

if __name__ == "__main__":
    unittest.main()