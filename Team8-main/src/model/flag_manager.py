"""
Flag Manager for centralized flags and condition evaluation.
User Story 5: Centralized flags and condition evaluation.
"""

import logging
from typing import Any, Optional, Callable

logger = logging.getLogger(__name__)


class FlagManager:
    """
    Centralized flag storage and condition evaluation.
    
    Condition types supported:
    - flag(name): True if flag is set
    - not(condition): Negation
    - and(conditions): All must be true
    - or(conditions): At least one must be true
    - has_item(item_id): True if player has item
    - aces_count >= n: True if ace count meets requirement
    - has_ace(ace_id): True if player has specific ace
    - has_guest(guest_id): True if party has guest
    """
    
    def __init__(self):
        """Initialize the flag manager."""
        self._flags: dict[str, Any] = {}
        self._item_checker: Optional[Callable[[str], bool]] = None
        self._ace_checker: Optional[Callable[[str], bool]] = None
        self._ace_counter: Optional[Callable[[], int]] = None
        self._guest_checker: Optional[Callable[[str], bool]] = None
    
    def set_item_checker(self, checker: Callable[[str], bool]):
        """Set callback to check if player has an item."""
        self._item_checker = checker
    
    def set_ace_checker(self, checker: Callable[[str], bool]):
        """Set callback to check if player has a specific ace."""
        self._ace_checker = checker
    
    def set_ace_counter(self, counter: Callable[[], int]):
        """Set callback to get total ace count."""
        self._ace_counter = counter
    
    def set_guest_checker(self, checker: Callable[[str], bool]):
        """Set callback to check if party has a guest."""
        self._guest_checker = checker
    
    # --- Flag Operations ---
    
    def set_flag(self, name: str, value: Any = True):
        """
        Set a flag value.
        
        Args:
            name: The flag name.
            value: The value to set (default True).
        """
        self._flags[name] = value
        logger.debug(f"Flag set: {name} = {value}")
    
    def clear_flag(self, name: str):
        """
        Clear/remove a flag.
        
        Args:
            name: The flag name to clear.
        """
        if name in self._flags:
            del self._flags[name]
            logger.debug(f"Flag cleared: {name}")
    
    def has_flag(self, name: str) -> bool:
        """
        Check if a flag is set (truthy).
        
        Args:
            name: The flag name.
            
        Returns:
            True if the flag exists and is truthy.
        """
        return bool(self._flags.get(name, False))
    
    def get_flag(self, name: str, default: Any = None) -> Any:
        """
        Get a flag's value.
        
        Args:
            name: The flag name.
            default: Default value if flag not set.
            
        Returns:
            The flag value or default.
        """
        return self._flags.get(name, default)
    
    def get_all_flags(self) -> dict[str, Any]:
        """Returns a copy of all flags."""
        return self._flags.copy()
    
    # --- Condition Evaluation ---
    
    def evaluate_condition(self, condition: dict) -> bool:
        """
        Evaluate a condition expression.
        
        Condition format:
        - {"type": "flag", "name": "flag_name"}
        - {"type": "not", "condition": {...}}
        - {"type": "and", "conditions": [...]}
        - {"type": "or", "conditions": [...]}
        - {"type": "has_item", "item_id": "..."}
        - {"type": "aces_count", "operator": ">=", "value": 4}
        - {"type": "has_ace", "ace_id": "..."}
        - {"type": "has_guest", "guest_id": "..."}
        
        Args:
            condition: The condition dictionary.
            
        Returns:
            True if condition is met, False otherwise (fail-safe).
        """
        if not condition or not isinstance(condition, dict):
            logger.warning(f"Invalid condition format: {condition}")
            return False
        
        condition_type = condition.get('type', '')
        
        try:
            if condition_type == 'flag':
                return self._eval_flag(condition)
            elif condition_type == 'not':
                return self._eval_not(condition)
            elif condition_type == 'and':
                return self._eval_and(condition)
            elif condition_type == 'or':
                return self._eval_or(condition)
            elif condition_type == 'has_item':
                return self._eval_has_item(condition)
            elif condition_type == 'aces_count':
                return self._eval_aces_count(condition)
            elif condition_type == 'has_ace':
                return self._eval_has_ace(condition)
            elif condition_type == 'has_guest':
                return self._eval_has_guest(condition)
            else:
                logger.warning(f"Unknown condition type: {condition_type}")
                return False
        except Exception as e:
            logger.warning(f"Error evaluating condition {condition}: {e}")
            return False
    
    def _eval_flag(self, condition: dict) -> bool:
        """Evaluate a flag condition."""
        name = condition.get('name', '')
        return self.has_flag(name)
    
    def _eval_not(self, condition: dict) -> bool:
        """Evaluate a NOT condition."""
        inner = condition.get('condition', {})
        return not self.evaluate_condition(inner)
    
    def _eval_and(self, condition: dict) -> bool:
        """Evaluate an AND condition."""
        conditions = condition.get('conditions', [])
        if not conditions:
            return True
        return all(self.evaluate_condition(c) for c in conditions)
    
    def _eval_or(self, condition: dict) -> bool:
        """Evaluate an OR condition."""
        conditions = condition.get('conditions', [])
        if not conditions:
            return False
        return any(self.evaluate_condition(c) for c in conditions)
    
    def _eval_has_item(self, condition: dict) -> bool:
        """Evaluate a has_item condition."""
        item_id = condition.get('item_id', '')
        if self._item_checker:
            return self._item_checker(item_id)
        logger.warning("has_item condition used but no item checker set")
        return False
    
    def _eval_aces_count(self, condition: dict) -> bool:
        """Evaluate an aces_count condition."""
        operator = condition.get('operator', '>=')
        value = condition.get('value', 0)
        
        if self._ace_counter:
            count = self._ace_counter()
            if operator == '>=':
                return count >= value
            elif operator == '>':
                return count > value
            elif operator == '==':
                return count == value
            elif operator == '<=':
                return count <= value
            elif operator == '<':
                return count < value
        
        logger.warning("aces_count condition used but no ace counter set")
        return False
    
    def _eval_has_ace(self, condition: dict) -> bool:
        """Evaluate a has_ace condition."""
        ace_id = condition.get('ace_id', '')
        if self._ace_checker:
            return self._ace_checker(ace_id)
        logger.warning("has_ace condition used but no ace checker set")
        return False
    
    def _eval_has_guest(self, condition: dict) -> bool:
        """Evaluate a has_guest condition."""
        guest_id = condition.get('guest_id', '')
        if self._guest_checker:
            return self._guest_checker(guest_id)
        logger.warning("has_guest condition used but no guest checker set")
        return False
    
    # --- Serialization ---
    
    def to_dict(self) -> dict:
        """
        Serialize flags for saving.
        
        Returns:
            Dictionary of flags.
        """
        return {'flags': self._flags.copy()}
    
    @classmethod
    def from_dict(cls, data: dict) -> 'FlagManager':
        """
        Deserialize flags from save data.
        
        Args:
            data: Dictionary containing flags.
            
        Returns:
            FlagManager instance.
        """
        manager = cls()
        manager._flags = data.get('flags', {}).copy()
        return manager
    
    def load_from_dict(self, data: dict):
        """
        Load flags from dictionary (in-place).
        
        Args:
            data: Dictionary containing flags.
        """
        self._flags = data.get('flags', {}).copy()