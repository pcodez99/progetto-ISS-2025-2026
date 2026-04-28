"""
Tests for Exploration Turn Manager (US24)
"""
import unittest
from src.controller.exploration_turn_manager import ExplorationTurnManager
from src.model.character import Character

class TestExplorationTurnManager(unittest.TestCase):
    def setUp(self):
        self.manager = ExplorationTurnManager()
        self.party = [Character() for _ in range(4)]
        for i, char in enumerate(self.party):
            char.owner_id = f"P{i + 1}"
            char.hp = 20

    def test_exploration_next_turn_cycles_in_order(self):
        """Test turn cycling through enabled characters"""
        enabled_mask = [True, True, False, False] # P1, P2 enabled
        
        self.assertEqual(self.manager.get_active_index(), 0)
        
        # Next -> P2
        self.manager.next_turn(self.party, enabled_mask)
        self.assertEqual(self.manager.get_active_index(), 1)
        
        # Next -> P1 (Wraps)
        self.manager.next_turn(self.party, enabled_mask)
        self.assertEqual(self.manager.get_active_index(), 0)

    def test_handoff_state_blocks_actions(self):
        """Test that handoff blocks movement"""
        enabled_mask = [True, True, False, False]
        self.manager.next_turn(self.party, enabled_mask)
        
        self.assertTrue(self.manager.is_awaiting_confirm())
        self.assertFalse(self.manager.can_move())
        
        self.manager.confirm_handoff()
        self.assertFalse(self.manager.is_awaiting_confirm())
        self.assertTrue(self.manager.can_move())

if __name__ == "__main__":
    unittest.main()