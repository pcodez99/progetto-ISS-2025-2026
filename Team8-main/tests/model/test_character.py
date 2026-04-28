"""
Unit tests for Character creation
Epic 4: User Story 14
"""

import unittest
from src.model.character import (
    Char_Builder, Character, Ability, Inventory, Item
)


class TestCharacterCreation(unittest.TestCase):
    """Tests for US14: Character building."""

    def setUp(self):
        self.character = Char_Builder().build_character(player_index=1)
    
    def tearDown(self):
        self.character = None
    
    def test_player_character_is_correctly_built(self):
        """
        US14: Verify that characters are correctly built with all attributes.
        """
        self.assertGreaterEqual(len(self.character.name), 1)
        self.assertIsInstance(self.character.hp, int)
        self.assertIsInstance(self.character.max_hp, int)
        self.assertEqual(self.character.hp, self.character.max_hp)
        self.assertIsInstance(self.character.atk, int)
        self.assertIsInstance(self.character.defense, int)
        self.assertIsInstance(self.character.magic, int)
        self.assertIsInstance(self.character.res, int)
        self.assertIsInstance(self.character.spd, int)
        self.assertIsInstance(self.character.inventory, Inventory)
        self.assertEqual(self.character.inventory.number_of_items, 0)
        self.assertEqual(self.character.inventory.max_capacity, 10)
        self.assertEqual(len(self.character.inventory.items), 0)
        self.assertIsInstance(self.character.abilities, list)
        self.assertGreaterEqual(len(self.character.abilities), 1)
        for ability in self.character.abilities:
            self.assertIsInstance(ability, Ability)
    
    def test_character_name_follows_pattern(self):
        """
        US14: Name assigned follows "Player{index}" pattern OR Specific RPG Names.
        Updated: Checks for Turiddu/Rosalia or PlayerX fallback.
        """
        char1 = Char_Builder().build_character(player_index=1)
        char2 = Char_Builder().build_character(player_index=2)
        
        # Accept either legacy names or new RPG names
        self.assertIn(char1.name, ["Player1", "Turiddu"])
        self.assertIn(char2.name, ["Player2", "Rosalia"])
    
    def test_character_has_abilities(self):
        """
        US14: Verify abilities are correctly assigned.
        Updated: Expect at least 1 ability (Basic Attack).
        """
        self.assertGreaterEqual(len(self.character.abilities), 1)
        self.assertEqual(self.character.abilities[0].name, "Attacco Base")
    
    def test_hp_equals_max_hp(self):
        """
        US14: Initial HP = Max HP.
        """
        self.assertEqual(self.character.hp, self.character.max_hp)
        self.assertGreater(self.character.hp, 0)
    
    def test_default_stats_values(self):
        """
        US14: Verify default stat values.
        Updated: HP is now 100 for RPG balancing.
        """
        self.assertEqual(self.character.hp, 100)
        self.assertEqual(self.character.max_hp, 100)
        self.assertEqual(self.character.atk, 10)
        self.assertEqual(self.character.defense, 5)


class TestInventory(unittest.TestCase):
    """Tests for Inventory functionality."""
    
    def setUp(self):
        self.inventory = Inventory()
    
    def test_empty_inventory_initial_state(self):
        """New inventory should be empty with default capacity."""
        self.assertEqual(self.inventory.number_of_items, 0)
        self.assertEqual(self.inventory.max_capacity, 10)
        self.assertEqual(len(self.inventory.items), 0)
    
    def test_add_item_increases_count(self):
        """Adding item should increase count."""
        self.inventory.add_item("Sword", "A sharp blade")
        
        self.assertEqual(self.inventory.number_of_items, 1)
        self.assertEqual(len(self.inventory.items), 1)
        self.assertEqual(self.inventory.items[0].name, "Sword")
    
    def test_add_item_returns_false_when_full(self):
        """Adding to full inventory should return False."""
        for i in range(10):
            result = self.inventory.add_item(f"Item{i}", "Description")
            self.assertTrue(result)
        
        result = self.inventory.add_item("Overflow", "Should fail")
        self.assertFalse(result)
        self.assertEqual(self.inventory.number_of_items, 10)
    
    def test_has_item_finds_existing_item(self):
        """has_item should return True for existing items."""
        self.inventory.add_item("Key", "Opens doors")
        
        self.assertTrue(self.inventory.has_item("Key"))
        self.assertFalse(self.inventory.has_item("NonExistent"))
    
    def test_remove_item_decreases_count(self):
        """Removing item should decrease count."""
        self.inventory.add_item("Potion", "Heals HP")
        self.inventory.add_item("Sword", "A weapon")
        
        result = self.inventory.remove_item("Potion")
        
        self.assertTrue(result)
        self.assertEqual(self.inventory.number_of_items, 1)
        self.assertFalse(self.inventory.has_item("Potion"))
        self.assertTrue(self.inventory.has_item("Sword"))
    
    def test_remove_nonexistent_item_returns_false(self):
        """Removing non-existent item should return False."""
        result = self.inventory.remove_item("Ghost")
        self.assertFalse(result)
    
    def test_to_view_format(self):
        """to_view_format should return list of dicts."""
        self.inventory.add_item("Sword", "Sharp")
        self.inventory.add_item("Shield", "Protective")
        
        view_data = self.inventory.to_view_format()
        
        self.assertEqual(len(view_data), 2)
        self.assertEqual(view_data[0]['name'], "Sword")
        self.assertEqual(view_data[0]['description'], "Sharp")
        self.assertEqual(view_data[1]['name'], "Shield")


class TestCharacterViewFormat(unittest.TestCase):
    """Tests for Character view format methods."""
    
    def setUp(self):
        self.character = Char_Builder().build_character(player_index=1)
    
    def test_get_inventory_in_view_format(self):
        """get_inventory_in_view_format returns correct tuple."""
        self.character.inventory.add_item("TestItem", "TestDesc")
        
        items, capacity, count = self.character.get_inventory_in_view_format()
        
        self.assertEqual(capacity, 10)
        self.assertEqual(count, 1)
        self.assertEqual(len(items), 1)
        self.assertEqual(items[0]['name'], "TestItem")
    
    def test_get_abilities_in_view_format(self):
        """get_abilities_in_view_format returns list of dicts."""
        abilities = self.character.get_abilities_in_view_format()
        
        # Expect at least 1 ability (Attacco Base)
        self.assertGreaterEqual(len(abilities), 1)
        self.assertIn('name', abilities[0])
        self.assertIn('description', abilities[0])


if __name__ == "__main__":
    unittest.main()