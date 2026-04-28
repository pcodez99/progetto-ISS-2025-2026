"""
Unit tests for StateMachine (User Story 1).
Tests state transitions, stack management, and overlay rules.
"""

import unittest
from src.controller.state_machine import StateMachine
from src.model.states.base_state import StateID
from src.model.input_context import InputContext


class TestStateMachineTransitions(unittest.TestCase):
    """Tests for User Story 1: State Machine transitions."""
    
    def setUp(self):
        """Set up StateMachine with all states registered."""
        self.state_machine = StateMachine()
        self.state_machine.register_all_states()
    
    def tearDown(self):
        self.state_machine = None
    
    def test_change_state_replaces_entire_stack(self):
        """
        Test that change_state replaces the entire stack with the new state.
        Boot → MainMenu should result in only MainMenu on stack.
        """
        self.state_machine.change_state(StateID.MAIN_MENU)
        
        self.assertEqual(self.state_machine.stack_size(), 1)
        self.assertEqual(self.state_machine.peek().state_id, StateID.MAIN_MENU)
        
        # Change to another state
        self.state_machine.change_state(StateID.HUB)
        
        self.assertEqual(self.state_machine.stack_size(), 1)
        self.assertEqual(self.state_machine.peek().state_id, StateID.HUB)
    
    def test_push_state_adds_overlay_to_stack(self):
        """
        Test that push_state adds overlay states to the stack.
        ESC pushing Pause overlay should result in base state + Pause.
        """
        self.state_machine.change_state(StateID.ROOM)
        self.state_machine.push_state(StateID.PAUSE)
        
        self.assertEqual(self.state_machine.stack_size(), 2)
        self.assertEqual(self.state_machine.peek().state_id, StateID.PAUSE)
        self.assertTrue(self.state_machine.has_state(StateID.ROOM))
    
    def test_pop_state_removes_top_and_restores_previous(self):
        """
        Test that pop_state removes the topmost state and restores previous.
        """
        self.state_machine.change_state(StateID.ROOM)
        self.state_machine.push_state(StateID.PAUSE)
        
        popped = self.state_machine.pop_state()
        
        self.assertEqual(popped.state_id, StateID.PAUSE)
        self.assertEqual(self.state_machine.stack_size(), 1)
        self.assertEqual(self.state_machine.peek().state_id, StateID.ROOM)
    
    def test_change_state_clears_stack_for_game_over(self):
        """
        Test that change_state to GameOver/Error clears entire stack first.
        """
        self.state_machine.change_state(StateID.ROOM)
        self.state_machine.push_state(StateID.PAUSE)
        self.state_machine.push_state(StateID.DIALOGUE)
        
        self.assertEqual(self.state_machine.stack_size(), 3)
        
        # Change to GameOver should clear stack
        self.state_machine.change_state(StateID.GAME_OVER)
        
        self.assertEqual(self.state_machine.stack_size(), 1)
        self.assertEqual(self.state_machine.peek().state_id, StateID.GAME_OVER)


class TestStateMachineInputContext(unittest.TestCase):
    """Tests for Epic 2 integration: input context derived from state."""
    
    def setUp(self):
        self.state_machine = StateMachine()
        self.state_machine.register_all_states()
    
    def test_input_context_derived_from_current_state(self):
        """
        Test that input context is correctly derived from peek().input_context.
        """
        self.state_machine.change_state(StateID.EXPLORATION if hasattr(StateID, 'EXPLORATION') else StateID.ROOM)
        self.assertEqual(
            self.state_machine.get_current_input_context(),
            InputContext.EXPLORATION
        )
        
        self.state_machine.push_state(StateID.DIALOGUE)
        self.assertEqual(
            self.state_machine.get_current_input_context(),
            InputContext.DIALOGUE
        )


if __name__ == "__main__":
    unittest.main()