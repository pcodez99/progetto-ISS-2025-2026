import unittest
from unittest.mock import Mock

from src.model.feedback.combat_log import CombatLog
from src.model.feedback.feedback_event import FeedbackEvent
from src.model.feedback.feedback_manager import FeedbackManager
from src.model.vfx.vfx_manager import VFXManager


class TestFeedbackManager(unittest.TestCase):

    def test_feedback_emit_always_writes_log_entry(self):
        log = CombatLog()
        audio = Mock()
        vfx = VFXManager()

        fm = FeedbackManager(combat_log=log, audio_manager=audio, vfx_manager=vfx)

        fm.emit(FeedbackEvent(log_text="X hits Y for 3", sfx_id=None, vfx_id=None))
        self.assertEqual(log.lines[-1], "X hits Y for 3")

    def test_feedback_emit_missing_sfx_does_not_crash_and_still_logs(self):
        log = CombatLog()
        audio = Mock()
        audio.play_sfx.side_effect = Exception("boom")
        vfx = VFXManager()

        fake_logger = Mock()
        fm = FeedbackManager(combat_log=log, audio_manager=audio, vfx_manager=vfx, logger=fake_logger)

        fm.emit(FeedbackEvent(log_text="X hits Y for 3", sfx_id="missing.wav"))
        self.assertEqual(log.lines[-1], "X hits Y for 3")
        fake_logger.warning.assert_called()

    def test_feedback_emit_missing_vfx_does_not_crash_and_still_logs(self):
        log = CombatLog()
        audio = Mock()
        vfx = Mock(spec=VFXManager)
        vfx.spawn.side_effect = Exception("vfx boom")

        fake_logger = Mock()
        fm = FeedbackManager(combat_log=log, audio_manager=audio, vfx_manager=vfx, logger=fake_logger)

        fm.emit(FeedbackEvent(log_text="heal", vfx_id="vfx_heal", target_pos=(10, 20)))
        self.assertEqual(log.lines[-1], "heal")
        fake_logger.warning.assert_called()
