"""
Save System Validation
Epic 5: User Stories 16, 17, 18, 19
"""

from typing import Tuple

from src.model.save.dtos import ValidationResult


class SaveValidator:
    """Validates save data structure."""
    
    @staticmethod
    def validate_save_dict(d: dict) -> ValidationResult:
        """
        Validate a save dictionary structure.
        
        Args:
            d: Dictionary to validate.
            
        Returns:
            ValidationResult with ok=True if valid.
        """
        errors = []
        
        # Check schema_version
        if 'schema_version' not in d:
            errors.append("Missing 'schema_version'")
        elif not isinstance(d['schema_version'], int):
            errors.append("'schema_version' must be an integer")
        
        # Check meta
        if 'meta' not in d:
            errors.append("Missing 'meta'")
        elif not isinstance(d['meta'], dict):
            errors.append("'meta' must be a dictionary")
        elif 'room_id' not in d['meta']:
            errors.append("Missing 'meta.room_id'")
        
        # Check data
        if 'data' not in d:
            errors.append("Missing 'data'")
        elif not isinstance(d['data'], dict):
            errors.append("'data' must be a dictionary")
        else:
            data = d['data']
            
            # Check world
            if 'world' not in data:
                errors.append("Missing 'data.world'")
            elif 'room_id' not in data.get('world', {}):
                errors.append("Missing 'data.world.room_id'")
            
            # Check party
            if 'party' not in data:
                errors.append("Missing 'data.party'")
        
        return ValidationResult(ok=len(errors) == 0, errors=errors)


class SaveStateChecker:
    """Checks if saving is allowed in current game state."""
    
    ALLOWED_STATES = {
        'hub', 'room', 'pause_hub', 'pause_room',
        'hubstate', 'roomstate', 'pausestate'
    }
    
    BLOCKED_STATES = {
        'combat', 'cutscene', 'dialogue_blocking',
        'combatstate', 'cutscenestate', 'dialoguestate'
    }
    
    @staticmethod
    def can_save(current_state: str) -> Tuple[bool, str]:
        """
        Check if saving is allowed in the current state.
        
        Args:
            current_state: Current game state identifier.
            
        Returns:
            Tuple of (can_save, message).
        """
        state_lower = (current_state or '').lower()
        
        # Check blocked states first
        for blocked in SaveStateChecker.BLOCKED_STATES:
            if blocked in state_lower:
                return False, f"Cannot save during {current_state}"
        
        # Check allowed states
        for allowed in SaveStateChecker.ALLOWED_STATES:
            if allowed in state_lower:
                return True, "Save allowed"
        
        # Default: not allowed for safety
        return False, f"Save not allowed in state: {current_state}"