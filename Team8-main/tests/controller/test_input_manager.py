"""
Unit tests for InputManager (User Story 6).
Tests action mapping, edge detection, and basic input handling.
"""

import unittest
import pygame
from src.controller.input_manager import InputManager
from src.model.input_actions import Action
from src.model.input_context import InputContext


class FakeEvent:
    """Fake pygame event for testing without pygame display initialization."""
    def __init__(self, event_type: int, key: int):
        self.type = event_type
        self.key = key


class TestInputManagerActionMapping(unittest.TestCase):
    """Tests for User Story 6: Input Manager with action mapping."""
    
    def setUp(self):
        """Set up InputManager with exploration context for movement testing."""
        pygame.init()
        self.input_manager = InputManager()
        self.input_manager.set_context(InputContext.EXPLORATION)
    
    def tearDown(self):
        pygame.quit()
    
    def test_keydown_mapped_key_sets_action_just_pressed_true_for_one_frame(self):
        """
        Test that KEYDOWN sets was_just_pressed True for one frame,
        then False on subsequent frames while is_down remains True.
        """
        # Press W key (bound to MOVE_UP)
        event = FakeEvent(pygame.KEYDOWN, pygame.K_w)
        self.input_manager.process_event(event)
        
        # First frame: just_pressed should be True
        self.assertTrue(self.input_manager.was_just_pressed(Action.MOVE_UP))
        self.assertTrue(self.input_manager.is_down(Action.MOVE_UP))
        
        # Next frame: just_pressed should be False, is_down still True
        self.input_manager.begin_frame()
        self.assertFalse(self.input_manager.was_just_pressed(Action.MOVE_UP))
        self.assertTrue(self.input_manager.is_down(Action.MOVE_UP))
    
    def test_keyup_sets_action_just_released_true_and_clears_is_down(self):
        """
        Test that KEYUP sets was_just_released True for one frame
        and sets is_down to False.
        """
        # Press then release
        self.input_manager.process_event(FakeEvent(pygame.KEYDOWN, pygame.K_w))
        self.input_manager.begin_frame()
        self.input_manager.process_event(FakeEvent(pygame.KEYUP, pygame.K_w))
        
        self.assertTrue(self.input_manager.was_just_released(Action.MOVE_UP))
        self.assertFalse(self.input_manager.is_down(Action.MOVE_UP))
        
        # Next frame: just_released should be False
        self.input_manager.begin_frame()
        self.assertFalse(self.input_manager.was_just_released(Action.MOVE_UP))
    
    def test_unmapped_key_does_not_change_any_action_state(self):
        """
        Test that pressing a key not present in any binding
        does not affect any action query result.
        """
        # F12 should not be mapped to any action
        self.input_manager.process_event(FakeEvent(pygame.KEYDOWN, pygame.K_F12))
        
        # No action should be affected
        for action in Action:
            self.assertFalse(self.input_manager.was_just_pressed(action))
            self.assertFalse(self.input_manager.is_down(action))
    
    def test_multiple_keys_bound_to_same_action_stays_down_until_all_released(self):
        """
        Test that action stays down until ALL bound keys are released.
        W and UP are both bound to MOVE_UP.
        """
        # Press both W and UP
        self.input_manager.process_event(FakeEvent(pygame.KEYDOWN, pygame.K_w))
        self.input_manager.begin_frame()
        self.input_manager.process_event(FakeEvent(pygame.KEYDOWN, pygame.K_UP))
        
        self.assertTrue(self.input_manager.is_down(Action.MOVE_UP))
        
        # Release W, action should still be down (UP is still held)
        self.input_manager.begin_frame()
        self.input_manager.process_event(FakeEvent(pygame.KEYUP, pygame.K_w))
        self.assertTrue(self.input_manager.is_down(Action.MOVE_UP))
        
        # Release UP, action should now be up
        self.input_manager.process_event(FakeEvent(pygame.KEYUP, pygame.K_UP))
        self.assertFalse(self.input_manager.is_down(Action.MOVE_UP))


if __name__ == "__main__":
    unittest.main()