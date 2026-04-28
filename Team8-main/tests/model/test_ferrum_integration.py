"""
Integration tests for Ferrum region (US99-103)
Updated Item IDs.
"""
import unittest
from unittest.mock import Mock
from src.model.ferrum.ferrum_choice import FerrumChoice
from src.model.ferrum.gatekeeper_scrap_golem import GatekeeperScrapGolem
from src.model.items.item_ids import ItemIds

class TestFerrumIntegration(unittest.TestCase):
    def setUp(self):
        self.game_model = Mock()
        self.game_model.gamestate = Mock()
        self.party_chars = [Mock(), Mock()]
        for char in self.party_chars:
            char.max_hp = 30
            char.hp = 30
        self.game_model.gamestate.party.main_characters = self.party_chars
        
        self.golem = GatekeeperScrapGolem(self.game_model)
        self.choice = FerrumChoice()

    def test_choice_grants_exactly_one_item(self):
        """US99: One-time choice grants exactly one item"""
        item_id = self.choice.make_choice("oil")
        self.assertEqual(item_id, ItemIds.OLIO_LUBRIFICANTE)
        self.assertTrue(self.choice.is_used)
        
        with self.assertRaises(ValueError):
            self.choice.make_choice("shield")

    def test_oil_path_skips_combat_and_applies_penalty(self):
        pass

    def test_orlando_path_recruits_guest(self):
        pass