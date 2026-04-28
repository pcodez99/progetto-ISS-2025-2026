"""
Unit Tests for Narrative Epics (11, 24, 28)
Updated method names.
"""
import unittest
from src.model.game import Game
from src.model.scripting.scripts_registry import ScriptsRegistry
from src.model.script_actions import ActionType

class TestNarrativeEpics(unittest.TestCase):
    def setUp(self):
        self.game = Game()
        self.game.start_new_game(1)

    def test_viridor_choice_logic(self):
        """US 95: Scelta Viridor (Fichi/Acqua/Cesoie)."""
        msg = self.game.viridor.make_choice(0)
        self.assertIn("Fichi", msg) # Testo aggiornato in Italiano

    def test_viridor_victory_grants_ace(self):
        """US 98: Vittoria Viridor da Asso di Bastoni."""
        self.assertFalse(self.game.has_ace("Bastoni"))
        self.game.viridor.on_boss_victory()
        self.assertTrue(self.game.has_ace("Bastoni"))
        self.assertTrue(self.game.get_flag("region_viridor_completed"))

    def test_etna_access_blocked_without_aces(self):
        """US 53: Etna bloccato se < 4 assi."""
        self.assertFalse(self.game.etna.can_access())
        # Script check
        script = ScriptsRegistry.get_script("interact_carretto", self.game)
        # Expecting carretto_look script
        self.assertEqual(script.script_id, "carretto_intro")

if __name__ == "__main__":
    unittest.main()