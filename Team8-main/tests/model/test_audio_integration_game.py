import unittest
from unittest.mock import Mock
from src.model.game import Game


class TestAudioIntegrationGame(unittest.TestCase):

    def test_enter_combat_requests_combat_bgm_and_overrides_previous(self):
        game = Game()
        game.audio = Mock()

        game.enter_hub()
        game.enter_combat()

        game.audio.play_bgm.assert_any_call("hub.ogg", fade_ms=500, loop=True, context={"scene": "hub"})
        game.audio.play_bgm.assert_any_call("combat.ogg", fade_ms=500, loop=True, context={"scene": "combat"})
