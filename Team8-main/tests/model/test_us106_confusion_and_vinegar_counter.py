import unittest
from src.model.game import Game
from src.model.items.item_ids import ItemIds
from src.model.vinalia.boss_uncle_toto import BossUncleToto

class TestUS106ConfusionAndVinegarCounter(unittest.TestCase):
    def setUp(self):
        self.game = Game()
        self.game.start_new_game(1)
        self.boss = BossUncleToto()

    def test_boss_applies_confusion_and_vinegar_clears_and_stuns(self):
        pass

    def test_using_vinegar_without_having_it_fails(self):
        pass