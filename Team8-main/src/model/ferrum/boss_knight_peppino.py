"""
Knight Peppino boss with Puppet guest modifier (US102)
"""
from src.model.status.status_effects import StatusInstance

class KnightPeppino:
    """Boss that changes behavior if Puppet is recruited"""

    def __init__(self, game_model):
        self.game = game_model
        self.base_stats = {
            "hp": 120,
            "atk": 15,
            "defense": 10,
            "accuracy": 0.85
        }

    def get_effective_stats(self) -> dict:
        """
        Get stats with modifiers applied.
        If Puppet is guest, apply distraction debuff.
        """
        stats = self.base_stats.copy()
        
        if self.game.gamestate.party.has_guest_id("puppet"):
            # Puppet distracts the boss
            stats["accuracy"] = 0.6  # Reduced accuracy
        
        return stats