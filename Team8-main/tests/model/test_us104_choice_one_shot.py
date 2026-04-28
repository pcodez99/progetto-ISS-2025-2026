import unittest
from src.model.game import Game
from src.model.items.item_ids import ItemIds

class TestUS104ChoiceOneShot(unittest.TestCase):
    def setUp(self):
        self.game = Game()
        self.game.start_new_game(1)

    def test_choice_grants_exactly_one_and_is_one_time(self):
        chosen = self.game.vinalia.make_choice(1) # Vinegar index
        # This returns a message string now
        self.assertIn("Aceto", chosen)
        self.assertTrue(self.game.get_flag("vinalia_starter_received"))



