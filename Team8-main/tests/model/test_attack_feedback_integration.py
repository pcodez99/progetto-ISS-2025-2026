import unittest
from unittest.mock import Mock

from src.model.feedback.combat_log import CombatLog
from src.model.feedback.feedback_manager import FeedbackManager
from src.model.vfx.vfx_manager import VFXManager
from src.model.combat.combat_controller import CombatController


class TestAttackFeedbackIntegration(unittest.TestCase):

    def test_attack_action_emits_feedback_event_with_log_sfx_vfx(self):
        log = CombatLog()
        audio = Mock()
        vfx = VFXManager()
        fm = FeedbackManager(combat_log=log, audio_manager=audio, vfx_manager=vfx)

        cc = CombatController(feedback_manager=fm)
        event = cc.attack("A", "B", 7, target_pos=(3, 4), now_ms=1000)

        self.assertIn("A hits B for 7", log.lines[-1])
        audio.play_sfx.assert_called()  # sfx requested
        self.assertEqual(event.vfx_id, "vfx_slash")
        self.assertEqual(len(vfx.entities), 1)
        self.assertEqual(vfx.entities[0].pos, (3, 4))
