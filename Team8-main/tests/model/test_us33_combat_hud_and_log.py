import unittest
from src.model.ui.combat_menu_state import CombatMenuState

class TestCombatMenuNavigation(unittest.TestCase):
    def setUp(self):
        self.menu = CombatMenuState()
        self.menu.options = ["Atk", "Def", "Run"]

    def test_cursor_wraps_correctly(self):
        # Down wrap
        self.menu.move_cursor(1) # Index 1
        self.menu.move_cursor(1) # Index 2
        self.menu.move_cursor(1) # Index 0 (Wrap)
        self.assertEqual(self.menu.cursor_index, 0)
        self.assertEqual(self.menu.get_selected_option(), "Atk")

        # Up wrap
        self.menu.move_cursor(-1) # Index 2 (Last)
        self.assertEqual(self.menu.cursor_index, 2)
        self.assertEqual(self.menu.get_selected_option(), "Run")

    def test_target_selection_mode(self):
        """
        Updated for Epic 19: start_target_selection now expects a list of objects, not indices.
        We use strings as mock objects here since CombatMenuState holds generic 'Any'.
        """
        # Mock objects
        enemies = ["EnemyA", "EnemyB", "EnemyC"] 
        
        self.menu.start_target_selection(enemies)
        
        self.assertEqual(self.menu.mode, "target_selection")
        
        # Verify selection by checking the internal index and the returned object
        self.assertEqual(self.menu.selected_target_index, 0)
        self.assertEqual(self.menu.get_current_target(), "EnemyA")
        
        self.menu.move_cursor(1)
        self.assertEqual(self.menu.selected_target_index, 1)
        self.assertEqual(self.menu.get_current_target(), "EnemyB")

if __name__ == "__main__":
    unittest.main()