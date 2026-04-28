import os
import tempfile
import unittest
from unittest.mock import Mock

from src.model.settings.audio_settings import AudioSettings
from src.model.settings.settings_manager import SettingsManager


class TestAudioSettingsPersistence(unittest.TestCase):

    def test_audio_settings_save_load_roundtrip(self):
        with tempfile.TemporaryDirectory() as td:
            path = os.path.join(td, "audio_settings.json")
            sm = SettingsManager(path=path)

            settings = AudioSettings(master=0.7, music=0.2, sfx=0.9)
            ok = sm.save_audio_settings(settings)
            self.assertTrue(ok)

            loaded = sm.load_audio_settings()
            self.assertEqual(loaded, settings.clamp())

    def test_audio_settings_values_are_clamped_to_0_1(self):
        s = AudioSettings(master=-10, music=2.5, sfx=1.2).clamp()
        self.assertEqual(s.master, 0.0)
        self.assertEqual(s.music, 1.0)
        self.assertEqual(s.sfx, 1.0)

    def test_load_missing_settings_file_returns_defaults_without_crash(self):
        with tempfile.TemporaryDirectory() as td:
            path = os.path.join(td, "does_not_exist.json")
            sm = SettingsManager(path=path)
            loaded = sm.load_audio_settings()
            self.assertEqual(loaded, AudioSettings().clamp())

    def test_load_corrupt_settings_file_returns_defaults_and_logs_warning(self):
        with tempfile.TemporaryDirectory() as td:
            path = os.path.join(td, "audio_settings.json")
            with open(path, "w", encoding="utf-8") as fp:
                fp.write("{not_json")

            fake_logger = Mock()
            sm = SettingsManager(path=path, logger=fake_logger)
            loaded = sm.load_audio_settings()
            self.assertEqual(loaded, AudioSettings().clamp())
            fake_logger.warning.assert_called()
