"""
Unit tests for ActionRunner (User Story 4).
Tests script execution, ordering, and transition halting.
"""

import unittest
from src.controller.action_runner import ActionRunner
from src.model.script_actions import GameScript, ScriptAction, ActionType


class TestActionRunnerExecution(unittest.TestCase):
    """Tests for User Story 4: Script execution."""
    
    def setUp(self):
        self.runner = ActionRunner()
        self.executed_actions = []
        
        # Register test handlers that track execution
        def make_handler(action_name):
            def handler(params):
                self.executed_actions.append((action_name, params))
            return handler
        
        self.runner.register_handler(ActionType.SET_FLAG, make_handler('SET_FLAG'))
        self.runner.register_handler(ActionType.CLEAR_FLAG, make_handler('CLEAR_FLAG'))
        self.runner.register_handler(ActionType.GIVE_ITEM, make_handler('GIVE_ITEM'))
        self.runner.register_handler(ActionType.CHANGE_ROOM, make_handler('CHANGE_ROOM'))
        self.runner.register_handler(ActionType.SHOW_DIALOGUE, make_handler('SHOW_DIALOGUE'))
    
    def test_ordered_deterministic_execution(self):
        """
        Test that actions execute in order deterministically.
        Same script = same execution order.
        """
        script = GameScript(
            script_id='test_script',
            actions=[
                ScriptAction.set_flag('flag_1'),
                ScriptAction.set_flag('flag_2'),
                ScriptAction.give_item('item_key', 1),
            ]
        )
        
        self.runner.run_script(script)
        
        self.assertEqual(len(self.executed_actions), 3)
        self.assertEqual(self.executed_actions[0][0], 'SET_FLAG')
        self.assertEqual(self.executed_actions[0][1]['flag_name'], 'flag_1')
        self.assertEqual(self.executed_actions[1][1]['flag_name'], 'flag_2')
        self.assertEqual(self.executed_actions[2][0], 'GIVE_ITEM')
    
    def test_transition_action_halts_remaining(self):
        """
        Test that transition actions (ChangeRoom/ChangeState/StartCombat)
        halt remaining actions unless cross_state=True.
        """
        script = GameScript(
            script_id='transition_test',
            actions=[
                ScriptAction.set_flag('before_transition'),
                ScriptAction.change_room('other_room', 'default'),
                ScriptAction.set_flag('after_transition'),  # Should NOT execute
            ]
        )
        
        self.runner.run_script(script)
        
        # Only first two should execute, third is halted
        self.assertEqual(len(self.executed_actions), 2)
        action_names = [a[0] for a in self.executed_actions]
        self.assertIn('SET_FLAG', action_names)
        self.assertIn('CHANGE_ROOM', action_names)
        
        # Verify 'after_transition' flag was NOT set
        flag_params = [a[1] for a in self.executed_actions if a[0] == 'SET_FLAG']
        flag_names = [p['flag_name'] for p in flag_params]
        self.assertIn('before_transition', flag_names)
        self.assertNotIn('after_transition', flag_names)
    
    def test_blocking_action_waits_for_completion(self):
        """
        Test that blocking actions pause the runner until completed.
        """
        script = GameScript(
            script_id='blocking_test',
            actions=[
                ScriptAction.set_flag('first'),
                ScriptAction.show_dialogue('NPC', 'Hello!'),  # Blocking
                ScriptAction.set_flag('after_dialogue'),
            ]
        )
        
        self.runner.run_script(script)
        
        # Should execute first two, then wait
        self.assertEqual(len(self.executed_actions), 2)
        self.assertTrue(self.runner.is_waiting())
        
        # Complete the blocking action
        self.runner.complete_blocking_action()
        
        # Now third action should execute
        self.assertEqual(len(self.executed_actions), 3)
        self.assertFalse(self.runner.is_running())


if __name__ == "__main__":
    unittest.main()