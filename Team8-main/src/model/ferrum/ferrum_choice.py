"""
Ferrum region choice system (US99)
"""
from src.model.items.item_ids import ItemIds

class FerrumChoice:
    """One-time choice in Ferrum region"""

    def __init__(self):
        self.chosen_id = None
        self.is_used = False

    def make_choice(self, choice: str) -> str:
        """Make the choice (oil, shield, orlando)"""
        if self.is_used:
            raise ValueError("Choice already made")

        if choice == "oil":
            self.chosen_id = ItemIds.OLIO_LUBRIFICANTE
        elif choice == "shield":
            self.chosen_id = ItemIds.SCUDO_TORRE
        elif choice == "orlando":
            self.chosen_id = ItemIds.TESTA_ORLANDO
        else:
            raise ValueError(f"Invalid choice: {choice}")

        self.is_used = True
        return self.chosen_id

    def get_chosen_item(self) -> str:
        return self.chosen_id