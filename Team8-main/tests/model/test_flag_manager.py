"""
Unit tests for FlagManager (User Story 5).
Tests flag storage and condition evaluation.
"""

import unittest
import logging
from src.model.flag_manager import FlagManager

# Suppress logging during tests
logging.disable(logging.WARNING)


class TestFlagStorage(unittest.TestCase):
    """Tests for User Story 5: Flag storage."""
    
    def setUp(self):
        self.flag_manager = FlagManager()
    
    def test_set_flag_and_has_flag(self):
        """Test that set_flag makes has_flag return True."""
        self.assertFalse(self.flag_manager.has_flag('test_flag'))
        
        self.flag_manager.set_flag('test_flag')
        
        self.assertTrue(self.flag_manager.has_flag('test_flag'))
    
    def test_clear_flag_removes_flag(self):
        """Test that clear_flag removes the flag."""
        self.flag_manager.set_flag('temp_flag')
        self.assertTrue(self.flag_manager.has_flag('temp_flag'))
        
        self.flag_manager.clear_flag('temp_flag')
        
        self.assertFalse(self.flag_manager.has_flag('temp_flag'))
    
    def test_save_load_preserves_flags(self):
        """Test that flags are preserved through save/load cycle."""
        self.flag_manager.set_flag('persistent_flag', 'custom_value')
        self.flag_manager.set_flag('bool_flag')
        
        # Serialize
        data = self.flag_manager.to_dict()
        
        # Create new manager and load
        new_manager = FlagManager.from_dict(data)
        
        self.assertTrue(new_manager.has_flag('persistent_flag'))
        self.assertEqual(new_manager.get_flag('persistent_flag'), 'custom_value')
        self.assertTrue(new_manager.has_flag('bool_flag'))


class TestConditionEvaluation(unittest.TestCase):
    """Tests for User Story 5: Condition evaluation."""
    
    def setUp(self):
        self.flag_manager = FlagManager()
        self.flag_manager.set_flag('quest_complete')
        self.flag_manager.set_flag('has_key')
        
        # Set up ace counter for testing
        self.ace_count = 4
        self.flag_manager.set_ace_counter(lambda: self.ace_count)
    
    def test_flag_and_or_not_logic(self):
        """Test flag, and, or, not condition logic."""
        # Simple flag check
        self.assertTrue(self.flag_manager.evaluate_condition(
            {'type': 'flag', 'name': 'quest_complete'}
        ))
        
        # NOT condition
        self.assertFalse(self.flag_manager.evaluate_condition(
            {'type': 'not', 'condition': {'type': 'flag', 'name': 'quest_complete'}}
        ))
        
        # AND condition
        self.assertTrue(self.flag_manager.evaluate_condition({
            'type': 'and',
            'conditions': [
                {'type': 'flag', 'name': 'quest_complete'},
                {'type': 'flag', 'name': 'has_key'}
            ]
        }))
        
        # OR condition
        self.assertTrue(self.flag_manager.evaluate_condition({
            'type': 'or',
            'conditions': [
                {'type': 'flag', 'name': 'quest_complete'},
                {'type': 'flag', 'name': 'nonexistent'}
            ]
        }))
    
    def test_aces_count_comparison(self):
        """Test aces_count >= n evaluates correctly."""
        # aces_count = 4
        self.assertTrue(self.flag_manager.evaluate_condition(
            {'type': 'aces_count', 'operator': '>=', 'value': 4}
        ))
        self.assertTrue(self.flag_manager.evaluate_condition(
            {'type': 'aces_count', 'operator': '>=', 'value': 3}
        ))
        self.assertFalse(self.flag_manager.evaluate_condition(
            {'type': 'aces_count', 'operator': '>=', 'value': 5}
        ))
    
    def test_invalid_condition_returns_false_with_warning(self):
        """Test that invalid conditions return False (fail-safe)."""
        # Unknown type
        self.assertFalse(self.flag_manager.evaluate_condition(
            {'type': 'unknown_type', 'value': 123}
        ))
        
        # Empty condition
        self.assertFalse(self.flag_manager.evaluate_condition({}))
        
        # None condition
        self.assertFalse(self.flag_manager.evaluate_condition(None))


if __name__ == "__main__":
    unittest.main()