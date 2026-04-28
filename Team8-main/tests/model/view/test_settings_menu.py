import unittest
import pygame
from unittest.mock import Mock

from src.model.settings.settings_manager import SettingsManager
from src.view.settings_menu import SettingsMenu


class TestSettingsMenuImmediateApply(unittest.TestCase):
    
    def setUp(self):
        # FIX: Initialize pygame font for SettingsMenu
        pygame.init()
        pygame.font.init()

    def tearDown(self):
        pygame.quit()

    def test_settings_slider_change_calls_audio_manager_set_volumes_immediately(self):
        fake_audio = Mock()
        fake_sm = Mock(spec=SettingsManager)
        fake_sm.load_audio_settings.return_value = type("S", (), {"master": 1.0, "music": 1.0, "sfx": 1.0, "clamp": lambda self: self})()
        
        # FIX: Added fake_renderer Mock
        fake_renderer = Mock()

        # Passiamo anche fake_renderer al costruttore
        menu = SettingsMenu(audio_manager=fake_audio, settings_manager=fake_sm, renderer=fake_renderer)

        menu.cursor_index = 1 # Music
        menu.adjust_value(-8.0) # approx 0.2 from 1.0
        
        self.assertTrue(fake_audio.set_volumes.called)