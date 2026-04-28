"""
Unit Tests for Save System
Epic 5: User Stories 16, 17, 18, 19
Max 3 tests per user story
"""

import unittest
import tempfile
import shutil
import os
import json

from src.model.save import (
    SaveManager, SaveValidator, SaveStateChecker, GameSerializer,
    SlotStatus, CURRENT_SAVE_SCHEMA_VERSION
)
from src.model.migration import migrate_to_current, is_future_version
from src.model.game import Game


class TestSaveUS16(unittest.TestCase):
    """US16: Save functionality (max 3 tests)."""
    
    def setUp(self):
        self.temp_dir = tempfile.mkdtemp()
        self.save_manager = SaveManager(save_dir=self.temp_dir, max_slots=3)
        self.game = Game()
        self.game.start_new_game(1)
    
    def tearDown(self):
        shutil.rmtree(self.temp_dir)
    
    def test_save_blocked_in_combat_state(self):
        """Save is blocked during combat."""
        can_save, message = SaveStateChecker.can_save("CombatState")
        self.assertFalse(can_save)
        
        can_save, message = SaveStateChecker.can_save("CutsceneState")
        self.assertFalse(can_save)
    
    def test_save_allowed_in_hub_and_room_states(self):
        """Save is allowed in Hub, Room, and Pause states."""
        for state in ["HubState", "RoomState", "PauseState"]:
            can_save, _ = SaveStateChecker.can_save(state)
            self.assertTrue(can_save, f"Should allow save in {state}")
    
    def test_save_creates_valid_file(self):
        """Saving creates a valid JSON file with required structure."""
        result = self.save_manager.save_to_slot(1, self.game)
        
        self.assertTrue(result.ok)
        
        # Verify file exists and is valid
        filepath = os.path.join(self.temp_dir, "slot_01.json")
        self.assertTrue(os.path.exists(filepath))
        
        with open(filepath, 'r') as f:
            data = json.load(f)
        
        self.assertIn('schema_version', data)
        self.assertIn('meta', data)
        self.assertIn('data', data)


class TestLoadUS17(unittest.TestCase):
    """US17: Load functionality (max 3 tests)."""
    
    def setUp(self):
        self.temp_dir = tempfile.mkdtemp()
        self.save_manager = SaveManager(save_dir=self.temp_dir, max_slots=3)
        self.game = Game()
        self.game.start_new_game(1)
    
    def tearDown(self):
        shutil.rmtree(self.temp_dir)
    
    def test_load_empty_slot_returns_error(self):
        """Loading from empty slot returns error without crashing."""
        result = self.save_manager.load_from_slot(1)
        
        self.assertFalse(result.ok)
        self.assertIn("Empty", result.message)
    
    def test_load_corrupt_file_returns_error(self):
        """Loading corrupt JSON returns error without crashing."""
        filepath = os.path.join(self.temp_dir, "slot_01.json")
        os.makedirs(self.temp_dir, exist_ok=True)
        with open(filepath, 'w') as f:
            f.write("{invalid json content")
        
        result = self.save_manager.load_from_slot(1)
        
        self.assertFalse(result.ok)
    
    def test_load_valid_save_returns_data(self):
        """Loading valid save returns SaveFileDTO with correct data."""
        self.game.gamestate.current_room_id = "test_dungeon"
        self.save_manager.save_to_slot(1, self.game)
        
        result = self.save_manager.load_from_slot(1)
        
        self.assertTrue(result.ok)
        self.assertIsNotNone(result.save_data)
        self.assertEqual(result.save_data.data.world.room_id, "test_dungeon")


class TestSerializerUS18(unittest.TestCase):
    """US18: Save data structure / serialization (max 3 tests)."""
    
    def setUp(self):
        self.game = Game()
        self.game.start_new_game(2)
    
    def test_round_trip_preserves_position_and_room(self):
        """Serialization preserves world position and room."""
        gs = self.game.gamestate
        gs.current_room_id = "forest"
        gs.party_position = [150, 300]
        
        save_dict = GameSerializer.to_dict(self.game)
        
        new_game = Game()
        new_game.start_new_game(1)
        success = GameSerializer.from_dict(save_dict, new_game)
        
        self.assertTrue(success)
        self.assertEqual(new_game.gamestate.current_room_id, "forest")
        self.assertEqual(new_game.gamestate.party_position, [150, 300])
    
    def test_round_trip_preserves_characters_and_inventory(self):
        """Serialization preserves character stats and inventory."""
        gs = self.game.gamestate
        gs.players[0].name = "Hero"
        gs.players[0].hp = 15
        gs.players[0].inventory.add_item("Sword", "Sharp blade")
        
        save_dict = GameSerializer.to_dict(self.game)
        
        new_game = Game()
        new_game.start_new_game(1)
        GameSerializer.from_dict(save_dict, new_game)
        
        self.assertEqual(new_game.gamestate.players[0].name, "Hero")
        self.assertEqual(new_game.gamestate.players[0].hp, 15)
        self.assertEqual(len(new_game.gamestate.players[0].inventory.items), 1)
    
    def test_round_trip_preserves_progression(self):
        """Serialization preserves aces and flags."""
        gs = self.game.gamestate
        gs.aces_collected = ["ace_spade", "ace_heart"]
        gs.flags = {"door_unlocked": True, "boss_defeated": False}
        
        save_dict = GameSerializer.to_dict(self.game)
        
        new_game = Game()
        new_game.start_new_game(1)
        GameSerializer.from_dict(save_dict, new_game)
        
        self.assertEqual(new_game.gamestate.aces_collected, ["ace_spade", "ace_heart"])
        self.assertEqual(new_game.gamestate.flags["door_unlocked"], True)


class TestMigrationUS19(unittest.TestCase):
    """US19: Version compatibility & migration (max 3 tests)."""
    
    def setUp(self):
        self.temp_dir = tempfile.mkdtemp()
        self.save_manager = SaveManager(save_dir=self.temp_dir, max_slots=3)
    
    def tearDown(self):
        shutil.rmtree(self.temp_dir)
    
    def test_current_version_loads_without_migration(self):
        """Save at current version loads directly."""
        save_data = {
            'schema_version': CURRENT_SAVE_SCHEMA_VERSION,
            'meta': {'room_id': 'hub', 'timestamp_iso': '', 'playtime_seconds': 0, 'aces_count': 0},
            'data': {
                'world': {'room_id': 'hub'},
                'party': {'characters': [], 'num_humans': 1},
                'progression': {'aces': [], 'flags': {}},
                'world_state': {'removed_entities': []},
                'turn_state': {},
                'checkpoint': {}
            }
        }
        
        migrated = migrate_to_current(save_data.copy())
        
        self.assertEqual(migrated['schema_version'], CURRENT_SAVE_SCHEMA_VERSION)
        validation = SaveValidator.validate_save_dict(migrated)
        self.assertTrue(validation.ok)
    
    def test_old_version_migrates_successfully(self):
        """Old save (v0) migrates to current version and validates."""
        old_save = {
            'schema_version': 0,
            'meta': {'room_id': 'hub'},
            'data': {}
        }
        
        migrated = migrate_to_current(old_save)
        
        self.assertEqual(migrated['schema_version'], CURRENT_SAVE_SCHEMA_VERSION)
        
        validation = SaveValidator.validate_save_dict(migrated)
        self.assertTrue(validation.ok, f"Errors: {validation.errors}")
    
    def test_future_version_rejected(self):
        """Save from future version is rejected on load."""
        future_save = {
            'schema_version': CURRENT_SAVE_SCHEMA_VERSION + 10,
            'meta': {'room_id': 'hub', 'timestamp_iso': '', 'playtime_seconds': 0, 'aces_count': 0},
            'data': {'world': {'room_id': 'hub'}, 'party': {}}
        }
        
        # Write future save to file
        filepath = os.path.join(self.temp_dir, "slot_01.json")
        os.makedirs(self.temp_dir, exist_ok=True)
        with open(filepath, 'w') as f:
            json.dump(future_save, f)
        
        result = self.save_manager.load_from_slot(1)
        
        self.assertFalse(result.ok)
        self.assertIn("newer version", result.message.lower())


class TestSaveControllerIntegration(unittest.TestCase):
    """Integration tests for controller-level save/load."""
    
    def setUp(self):
        self.temp_dir = tempfile.mkdtemp()
        
        from src.controller.game_controller import GameController
        self.controller = GameController()
        self.controller.save_manager = SaveManager(save_dir=self.temp_dir, max_slots=3)
        self.controller.start_new_game(1)
    
    def tearDown(self):
        shutil.rmtree(self.temp_dir)
    
    def test_controller_save_blocked_in_combat(self):
        """Controller correctly blocks save in combat."""
        self.controller.set_current_state("CombatState")
        
        can_save, _ = self.controller.can_save_game()
        
        self.assertFalse(can_save)
    
    def test_controller_overwrite_requires_confirmation(self):
        """Overwriting existing save requires confirmation flag."""
        self.controller.set_current_state("HubState")
        self.controller.save_game(1, confirmed=True)
        
        result = self.controller.save_game(1, confirmed=False)
        
        self.assertFalse(result.ok)
        self.assertEqual(result.message, "CONFIRM_OVERWRITE")
    
    def test_full_save_load_cycle(self):
        """Complete save/load cycle preserves game state."""
        # Setup state
        self.controller.game.gamestate.current_room_id = "castle"
        self.controller.game.gamestate.party_position = [200, 400]
        self.controller.game.gamestate.aces_collected = ["ace_diamond"]
        self.controller.set_current_state("RoomState")
        
        # Save
        save_result = self.controller.save_game(1, confirmed=True)
        self.assertTrue(save_result.ok)
        
        # Load into new controller
        from src.controller.game_controller import GameController
        new_controller = GameController()
        new_controller.save_manager = SaveManager(save_dir=self.temp_dir, max_slots=3)
        
        load_result = new_controller.load_game(1)
        
        self.assertTrue(load_result.ok)
        self.assertEqual(new_controller.game.gamestate.current_room_id, "castle")
        self.assertEqual(new_controller.game.gamestate.party_position, [200, 400])
        self.assertEqual(new_controller.game.gamestate.aces_collected, ["ace_diamond"])


if __name__ == "__main__":
    unittest.main()