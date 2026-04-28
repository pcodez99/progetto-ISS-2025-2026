"""
Unit tests for GameController
Epic 4 (US13) + Epic 5 (US16, US17)
"""

import unittest
import tempfile
import shutil

from src.controller.game_controller import GameController
from src.model.save import SaveManager, SlotStatus


class TestGameControllerNewGame(unittest.TestCase):
    """Tests for US13: Starting new games."""
    
    def setUp(self):
        self.controller = GameController()
    
    def tearDown(self):
        self.controller = None
    
    def test_start_singleplayer_game(self):
        """
        Updated: start_new_game(1) now enforces 2-player party (RPG mode).
        """
        self.controller.start_new_game(1)
        
        # RPG Mode enforces 2 main characters
        self.assertEqual(self.controller.game.gamestate.num_humans, 2)
        # Party size check (depends on implementation, typically 2 or 4 slots)
        self.assertGreaterEqual(len(self.controller.game.gamestate.players), 2)
        
        self.assertTrue(self.controller.game.gamestate.is_running)
    
    def test_start_multiplayer_game(self):
        """
        Updated: start_new_game(2) starts with 2 players.
        """
        self.controller.start_new_game(2)
        
        self.assertEqual(self.controller.game.gamestate.num_humans, 2)
        self.assertTrue(self.controller.game.gamestate.is_running)
    
    def test_players_have_correct_names(self):
        """
        Updated: Expects RPG Names (Turiddu, Rosalia).
        """
        self.controller.start_new_game(2)
        
        p1 = self.controller.game.gamestate.players[0]
        p2 = self.controller.game.gamestate.players[1]
        
        self.assertEqual(p1.name, "Turiddu")
        self.assertEqual(p2.name, "Rosalia")


class TestGameControllerInventory(unittest.TestCase):
    """Tests for inventory and abilities access."""
    
    def setUp(self):
        self.controller = GameController()
        self.controller.start_new_game(1)
    
    def test_get_player_inventory_returns_tuple(self):
        """get_player_inventory returns (items, capacity, count)."""
        items, capacity, count = self.controller.get_player_inventory(0)
        
        self.assertIsInstance(items, list)
        self.assertEqual(capacity, 10)
        self.assertEqual(count, 0)
    
    def test_get_player_abilities_returns_list(self):
        """get_player_abilities returns list of ability dicts."""
        abilities = self.controller.get_player_abilities(0)
        
        self.assertIsInstance(abilities, list)
        # RPG Mode: at least 1 base ability
        self.assertGreaterEqual(len(abilities), 1)


class TestGameControllerSaveLoad(unittest.TestCase):
    """Tests for US16, US17: Save/Load functionality."""
    
    def setUp(self):
        self.temp_dir = tempfile.mkdtemp()
        self.controller = GameController()
        self.controller.save_manager = SaveManager(save_dir=self.temp_dir, max_slots=3)
        self.controller.start_new_game(1)
    
    def tearDown(self):
        shutil.rmtree(self.temp_dir)
    
    def test_can_save_in_hub_state(self):
        """US16: Save allowed in HubState."""
        self.controller.set_current_state("HubState")
        can_save, _ = self.controller.can_save_game()
        self.assertTrue(can_save)
    
    def test_can_save_in_room_state(self):
        """US16: Save allowed in RoomState."""
        self.controller.set_current_state("RoomState")
        can_save, _ = self.controller.can_save_game()
        self.assertTrue(can_save)
    
    def test_cannot_save_in_combat_state(self):
        """US16: Save blocked in CombatState."""
        self.controller.set_current_state("CombatState")
        can_save, _ = self.controller.can_save_game()
        self.assertFalse(can_save)
    
    def test_cannot_save_in_cutscene_state(self):
        """US16: Save blocked in CutsceneState."""
        self.controller.set_current_state("CutsceneState")
        can_save, _ = self.controller.can_save_game()
        self.assertFalse(can_save)
    
    def test_save_to_empty_slot_succeeds(self):
        """US16: Saving to empty slot succeeds."""
        self.controller.set_current_state("HubState")
        result = self.controller.save_game(1, confirmed=True)
        
        self.assertTrue(result.ok)
    
    def test_save_requires_confirmation_for_overwrite(self):
        """US16: Overwriting occupied slot requires confirmation."""
        self.controller.set_current_state("HubState")
        self.controller.save_game(1, confirmed=True)
        
        result = self.controller.save_game(1, confirmed=False)
        
        self.assertFalse(result.ok)
        self.assertEqual(result.message, "CONFIRM_OVERWRITE")
    
    def test_save_overwrite_with_confirmation_succeeds(self):
        """US16: Overwrite with confirmation succeeds."""
        self.controller.set_current_state("HubState")
        self.controller.save_game(1, confirmed=True)
        
        result = self.controller.save_game(1, confirmed=True)
        
        self.assertTrue(result.ok)
    
    def test_load_from_empty_slot_fails(self):
        """US17: Loading from empty slot returns error."""
        result = self.controller.load_game(1)
        
        self.assertFalse(result.ok)
    
    def test_load_from_occupied_slot_succeeds(self):
        """US17: Loading from valid slot succeeds."""
        self.controller.set_current_state("HubState")
        self.controller.save_game(1, confirmed=True)
        
        # Create new controller and load
        new_controller = GameController()
        new_controller.save_manager = SaveManager(save_dir=self.temp_dir, max_slots=3)
        result = new_controller.load_game(1)
        
        self.assertTrue(result.ok)
    
    def test_full_save_load_cycle_preserves_data(self):
        """Integration: save, load, verify data restored."""
        # Setup game state
        self.controller.game.gamestate.current_room_id = "forest"
        self.controller.game.gamestate.party_position = [250, 400]
        self.controller.game.gamestate.aces_collected = ["ace_spade"]
        self.controller.set_current_state("RoomState")
        
        # Save
        self.controller.save_game(1, confirmed=True)
        
        # Load in new controller
        new_controller = GameController()
        new_controller.save_manager = SaveManager(save_dir=self.temp_dir, max_slots=3)
        new_controller.load_game(1)
        
        # Verify
        self.assertEqual(new_controller.game.gamestate.current_room_id, "forest")
        self.assertEqual(new_controller.game.gamestate.party_position, [250, 400])
        self.assertEqual(new_controller.game.gamestate.aces_collected, ["ace_spade"])
    
    def test_is_slot_empty_returns_correct_status(self):
        """is_slot_empty returns True for empty, False for occupied."""
        self.assertTrue(self.controller.is_slot_empty(1))
        
        self.controller.set_current_state("HubState")
        self.controller.save_game(1, confirmed=True)
        
        self.assertFalse(self.controller.is_slot_empty(1))
    
    def test_get_slot_info_returns_slot_info(self):
        """get_slot_info returns SlotInfo object."""
        info = self.controller.get_slot_info(1)
        
        self.assertEqual(info.slot_index, 1)
        self.assertEqual(info.status, SlotStatus.EMPTY)


class TestGameControllerStateManagement(unittest.TestCase):
    """Tests for state management."""
    
    def setUp(self):
        self.controller = GameController()
    
    def test_initial_state_is_main_menu(self):
        """Initial state should be MainMenu."""
        self.assertEqual(self.controller.current_state, "MainMenu")
    
    def test_set_current_state_updates_state(self):
        """set_current_state updates current_state."""
        self.controller.set_current_state("CombatState")
        self.assertEqual(self.controller.current_state, "CombatState")
    
    def test_get_current_state_returns_state(self):
        """get_current_state returns current state."""
        self.controller.set_current_state("PauseState")
        self.assertEqual(self.controller.get_current_state(), "PauseState")


if __name__ == "__main__":
    unittest.main()