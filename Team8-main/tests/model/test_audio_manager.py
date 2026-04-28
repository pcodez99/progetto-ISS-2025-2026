import unittest
from unittest.mock import Mock
from src.model.audio.audio_manager import AudioManager
from src.model.audio.audio_asset_loader import AudioAssetLoader


class TestAudioManager(unittest.TestCase):

    def test_audio_manager_play_sfx_missing_asset_logs_warning_and_does_not_raise(self):
        fake_logger = Mock()
        fake_mixer = Mock()
        fake_loader = AudioAssetLoader(logger=fake_logger)

        # forza missing: get_sound ritorna None e logga warning
        fake_loader.get_sound = Mock(return_value=None)

        am = AudioManager(loader=fake_loader, mixer_module=fake_mixer, logger=fake_logger)

        # non deve lanciare
        am.play_sfx("sfx_attack.wav", context={"action": "attack"})

        fake_loader.get_sound.assert_called()
        fake_logger.warning.assert_called()  # log warning (dal loader o manager)

    def test_audio_manager_play_bgm_missing_track_logs_warning_and_does_not_raise(self):
        fake_logger = Mock()
        fake_mixer = Mock()
        am = AudioManager(loader=Mock(), mixer_module=fake_mixer, logger=fake_logger)

        # file non esiste => warning + skip
        am.play_bgm("this_file_does_not_exist.ogg", context={"scene": "hub"})
        fake_logger.warning.assert_called()

    def test_play_bgm_same_track_is_idempotent(self):
        fake_logger = Mock()

        # mock mixer.music chain
        fake_mixer = Mock()
        fake_mixer.music = Mock()

        am = AudioManager(loader=Mock(), mixer_module=fake_mixer, logger=fake_logger)

        # Simuliamo esistenza file bypassando check: monkeypatch semplice
        import os
        original_exists = os.path.exists
        os.path.exists = lambda _: True
        try:
            am.play_bgm("hub.ogg")
            am.play_bgm("hub.ogg")  # seconda chiamata: non deve ricaricare
        finally:
            os.path.exists = original_exists

        self.assertEqual(fake_mixer.music.load.call_count, 1)
        self.assertEqual(fake_mixer.music.play.call_count, 1)
