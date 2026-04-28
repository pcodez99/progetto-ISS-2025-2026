"""
Scrap Golem gatekeeper with 3 resolution paths (US100)
"""
from src.model.ferrum.ferrum_choice import FerrumChoice
from src.model.items.item_ids import ItemIds

class GatekeeperScrapGolem:
    """Scrap Golem gatekeeper encounter"""

    def __init__(self, game_model):
        self.game = game_model

    def resolve(self, choice_id: str) -> dict:
        """Resolve gatekeeper based on player's choice item"""
        outcome = {
            "skip_combat": False,
            "combat_modifier": None,
            "recruit_guest": False,
            "flags": [],
            "consumed_items": []
        }

        if choice_id == ItemIds.ETERNEL_OIL:
            # Skip fight but with trade-off
            outcome["skip_combat"] = True
            outcome["flags"] = ["ferrum_oil_used", "golem_pacified"]
            outcome["consumed_items"] = [choice_id]
            # Party takes HP penalty
            for char in self.game.gamestate.party.main_characters:
                penalty = max(1, char.max_hp // 5)
                char.hp = max(1, char.hp - penalty)
        
        elif choice_id == ItemIds.TOWER_SHIELD:
            # Start combat with advantage
            outcome["skip_combat"] = False
            outcome["combat_modifier"] = "player_defense_up"
            outcome["flags"] = ["ferrum_shield_used"]
        
        elif choice_id == ItemIds.ORLANDO_HEAD:
            # Skip and recruit guest
            outcome["skip_combat"] = True
            outcome["recruit_guest"] = True
            outcome["flags"] = ["ferrum_orlando_used", "puppet_recruited"]
            outcome["consumed_items"] = [choice_id]
        
        return outcome