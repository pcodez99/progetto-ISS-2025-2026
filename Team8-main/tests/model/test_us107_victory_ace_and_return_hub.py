import unittest
from src.model.game import Game
from src.model.items.item_ids import ItemIds

class TestUS107VictoryAceAndReturnHub(unittest.TestCase):
    def setUp(self):
        self.game = Game()
        self.game.start_new_game(1)

    def test_victory_grants_ace_sets_flag_returns_hub(self):
        self.game.gamestate.current_scene = "combat"
        self.game.vinalia.on_boss_victory() # Correct method name

        self.assertTrue(self.game.has_ace("Coppe"))
        self.assertTrue(self.game.get_flag("region_vinalia_completed"))
        self.assertEqual(self.game.gamestate.current_room_id, "hub")