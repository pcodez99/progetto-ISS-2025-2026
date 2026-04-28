import unittest
from unittest.mock import Mock
from src.view.gameplay_menu import GameplayMenu

class TestInventoryView(unittest.TestCase):
    def setUp(self):
        # Mocking Controller fully to test Menu logic isolation
        self.controller = Mock()
        self.controller.get_player_inventory.return_value = ([], 10, 0)
        self.menu = GameplayMenu(self.controller)

    def test_inventory_button_press_calls_controller(self):
        self.menu.inventory_button_press(0)
        self.controller.get_player_inventory.assert_called_with(0)
        self.controller.show_player_inventory.assert_called()

class TestAbilitiesView(unittest.TestCase):
    def setUp(self):
        self.controller = Mock()
        self.controller.get_player_abilities.return_value = []
        self.menu = GameplayMenu(self.controller)

    def test_abilities_button_press_calls_controller(self):
        self.menu.abilities_button_press(0)
        self.controller.get_player_abilities.assert_called_with(0)
        self.controller.show_player_abilities.assert_called()

if __name__ == "__main__":
    unittest.main()