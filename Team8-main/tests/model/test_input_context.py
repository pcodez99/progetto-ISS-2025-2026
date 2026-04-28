"""
Unit tests for input context behavior (User Story 7).
Tests context-based action filtering and state flushing on context change.
"""

import unittest
import pygame
from src.controller.input_manager import InputManager
from src.model.input_actions import Action
from src.model.input_context import InputContext


class FakeEvent:
    """Fake pygame event for testing."""
    def __init__(self, event_type: int, key: int):
        self.type = event_type
        self.key = key


class TestInputContextFiltering(unittest.TestCase):
    """Tests for User Story 7: Input contexts blocking disallowed actions."""
    
    def setUp(self):
        pygame.init()
        self.input_manager = InputManager()
    
    def tearDown(self):
        pygame.quit()
    
    def test_dialogue_context_blocks_movement_actions_even_when_keys_pressed(self):
        """
        In DialogueContext, movement actions return False even if keys are physically held.
        Only CONFIRM and CANCEL should work.
        """
        self.input_manager.set_context(InputContext.DIALOGUE)
        
        # Press movement key
        self.input_manager.process_event(FakeEvent(pygame.KEYDOWN, pygame.K_w))
        
        # Movement should be blocked
        self.assertFalse(self.input_manager.is_down(Action.MOVE_UP))
        self.assertFalse(self.input_manager.was_just_pressed(Action.MOVE_UP))
        
        # CONFIRM should work
        self.input_manager.process_event(FakeEvent(pygame.KEYDOWN, pygame.K_RETURN))
        self.assertTrue(self.input_manager.was_just_pressed(Action.CONFIRM))
    
    def test_exploration_context_allows_movement_actions(self):
        """
        In ExplorationContext, movement actions should be allowed.
        """
        self.input_manager.set_context(InputContext.EXPLORATION)
        self.input_manager.process_event(FakeEvent(pygame.KEYDOWN, pygame.K_w))
        
        self.assertTrue(self.input_manager.is_down(Action.MOVE_UP))
        self.assertTrue(self.input_manager.was_just_pressed(Action.MOVE_UP))
    
    def test_context_change_clears_keys_down_prevent_resume_without_repress(self):
        """
        Hold MoveUp in Exploration → set_context(Dialogue) → set_context(Exploration) 
        without KEYUP/KEYDOWN → is_down(MoveUp) should be False.
        This prevents accidental actions when resuming a state.
        """
        self.input_manager.set_context(InputContext.EXPLORATION)
        
        # Hold movement key
        self.input_manager.process_event(FakeEvent(pygame.KEYDOWN, pygame.K_w))
        self.assertTrue(self.input_manager.is_down(Action.MOVE_UP))
        
        # Change context to Dialogue and back to Exploration
        self.input_manager.set_context(InputContext.DIALOGUE)
        self.input_manager.set_context(InputContext.EXPLORATION)
        
        # Key should no longer be considered down (flushed on context change)
        self.assertFalse(self.input_manager.is_down(Action.MOVE_UP))
        self.assertFalse(self.input_manager.was_just_pressed(Action.MOVE_UP))
    
    def test_context_change_clears_edges_prevent_accidental_confirm(self):
        """
        Test that edges are cleared on context change to prevent
        accidental confirms after transition.
        """
        self.input_manager.set_context(InputContext.MAIN_MENU)
        
        # Press confirm
        self.input_manager.process_event(FakeEvent(pygame.KEYDOWN, pygame.K_RETURN))
        self.assertTrue(self.input_manager.was_just_pressed(Action.CONFIRM))
        
        # Change to exploration (simulating menu selection leading to exploration)
        self.input_manager.set_context(InputContext.EXPLORATION)
        
        # Confirm edge should be cleared - no accidental action
        self.assertFalse(self.input_manager.was_just_pressed(Action.CONFIRM))
        self.assertFalse(self.input_manager.is_down(Action.CONFIRM))


if __name__ == "__main__":
    unittest.main()