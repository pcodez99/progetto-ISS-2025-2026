import os
import unittest


class TestNoPygameMixerOutsideAudioManager(unittest.TestCase):

    def test_no_pygame_mixer_calls_outside_audio_manager(self):
        """
        Regola: niente 'pygame.mixer' fuori da AudioManager/loader (bootstrap escluso).
        È un test "grep-based" volutamente semplice.
        """
        allowed_files = {
            os.path.normpath("src/model/audio/audio_manager.py"),
            os.path.normpath("src/model/audio/audio_asset_loader.py"),
        }

        offenders = []
        for root, _, files in os.walk("src"):
            for f in files:
                if not f.endswith(".py"):
                    continue
                path = os.path.normpath(os.path.join(root, f))
                if path in allowed_files:
                    continue
                with open(path, "r", encoding="utf-8") as fp:
                    content = fp.read()
                if "pygame.mixer" in content:
                    offenders.append(path)

        self.assertEqual(offenders, [], msg=f"Found forbidden pygame.mixer usage in: {offenders}")
