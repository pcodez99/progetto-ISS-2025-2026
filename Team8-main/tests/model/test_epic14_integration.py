import unittest
from src.model.game import Game, SUIT_DENARI, SUIT_BASTONI, SUIT_SPADE, SUIT_COPPE
from src.model.content.hub_builder import HubBuilder
from src.model.scripting.scripts_registry import ScriptsRegistry
from src.controller.action_runner import ActionRunner
from src.model.script_actions import ActionType

class TestEpic14Integration(unittest.TestCase):
    def setUp(self):
        self.game = Game()
        self.game.start_new_game(1)
        self.hub = HubBuilder.create_hub()
        
        # Setup ActionRunner mockato/reale
        self.runner = ActionRunner()
        self.runner.game_ref = self.game
        
        # Mock handlers per verificare gli script
        self.log = []
        
        self.runner.register_handler(
            ActionType.SHOW_DIALOGUE, 
            lambda p: self.log.append(f"MSG: {p['text']}")
        )
        self.runner.register_handler(
            ActionType.CHANGE_ROOM, 
            lambda p: self.log.append(f"MOVE: {p['room_id']}")
        )
        self.runner.register_handler(
            ActionType.SET_FLAG,
            lambda p: self.game.set_flag(p['flag_name'], p['value'])
        )

    def test_us51_ace_progression(self):
        """US 51: Accumulo assi."""
        self.assertEqual(self.game.get_ace_count(), 0)
        self.game.collect_ace(SUIT_DENARI)
        self.assertEqual(self.game.get_ace_count(), 1)
        self.assertTrue(self.game.has_ace(SUIT_DENARI))

    def test_us56_giufa_dialogue(self):
        """US 56: Giufà cambia dialogo."""
        # 1. Intro
        script = ScriptsRegistry.get_script("giufa_hub_talk", self.game)
        self.runner.run_script(script)
        
        # Verifica dialogo intro
        found_intro = any("Belli freschi" in line for line in self.log)
        self.assertTrue(found_intro, f"Log expected intro, got: {self.log}")
        
        self.runner.complete_blocking_action()
        # Il flag viene settato nello script, ma nel test mockato dobbiamo assicurarci che accada.
        # Nello script registry, il set_flag viene chiamato direttamente PRIMA di ritornare lo script.
        # Quindi è già true.
        self.assertTrue(self.game.get_flag("met_giufa"))

if __name__ == "__main__":
    unittest.main()