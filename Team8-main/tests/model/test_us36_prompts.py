import unittest
from unittest.mock import Mock
from src.model.game import Game
from src.model.ui.prompts import PromptChoice
from src.model.input.actions import Action
from src.model.input.input_context import InputContext  # FIXED: Was InputContextType

class TestUS36Prompts(unittest.TestCase):
    def setUp(self):
        self.game = Game()
        # Mockiamo la state machine per intercettare le chiamate
        self.game.prompts.state_machine = Mock()

    def test_confirm_prompt_pushes_state(self):
        """Test che show_confirm chiami push_state sulla state machine."""
        def yes(): pass
        def no(): pass

        self.game.prompts.show_confirm("Exit?", on_yes=yes, on_no=no)
        
        # Verifica che sia stato chiamato push_state con i parametri giusti
        self.game.prompts.state_machine.push_state.assert_called_once()
        args, kwargs = self.game.prompts.state_machine.push_state.call_args
        self.assertEqual(kwargs['prompt_type'], 'confirm')
        self.assertEqual(kwargs['message'], "Exit?")

    def test_choice_prompt_pushes_state(self):
        """Test che show_choice chiami push_state."""
        def on_select(v): pass
        opts = [PromptChoice("A", "a"), PromptChoice("B", "b")]
        
        self.game.prompts.show_choice("Pick", opts, on_select)
        
        self.game.prompts.state_machine.push_state.assert_called_once()
        args, kwargs = self.game.prompts.state_machine.push_state.call_args
        self.assertEqual(kwargs['prompt_type'], 'choice')
        self.assertEqual(kwargs['title'], "Pick")

    def test_info_prompt_auto_dismisses_after_timeout_without_blocking(self):
        """Test logica timer info message (non usa state machine)."""
        self.game.prompts.show_info("Hi", now_ms=1000, timeout_ms=50)
        self.assertEqual(self.game.prompts.info_message, "Hi")
        
        # Prima del timeout
        self.game.prompts.update(now_ms=1049)
        self.assertEqual(self.game.prompts.info_message, "Hi")
        
        # Dopo timeout
        self.game.prompts.update(now_ms=1050)
        self.assertIsNone(self.game.prompts.info_message)